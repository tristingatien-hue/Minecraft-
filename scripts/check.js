#!/usr/bin/env node
// Smoke test: exercises the core (DB, config, secrets, adapters, templates,
// game engine) under plain Node with a throwaway data directory. Run with:
//   npm run check
// It grows with each build stage and must always pass.
'use strict';

const fs = require('fs');
const os = require('os');
const path = require('path');

process.env.SHOP_DATA_DIR = fs.mkdtempSync(path.join(os.tmpdir(), 'shop-check-'));

let passed = 0;
function ok(name, cond) {
  if (!cond) { console.error(`  ✗ ${name}`); process.exitCode = 1; return; }
  passed++;
  console.log(`  ✓ ${name}`);
}

async function main() {
  console.log('Stage 1: skeleton (db, config, secrets, rpc)');
  const db = require('../src/main/db').get();
  ok('database opens and migrates', db.pragma('user_version', { simple: true }) >= 1);
  ok('channels seeded', db.prepare('SELECT COUNT(*) c FROM channels').get().c === 4);

  const config = require('../src/main/config');
  const cfg = config.load();
  ok('config has defaults', cfg.game.points.saleBase > 0 && cfg.ai.baseUrl.length > 0);
  config.save({ shopName: 'Test Shop' });
  config.reset();
  ok('config persists overrides + keeps defaults', config.load().shopName === 'Test Shop' && config.load().game.levels.length > 0);

  const secrets = require('../src/main/secrets');
  secrets.set('test.token', 'hunter2-token-value');
  ok('secret round-trips', secrets.get('test.token') === 'hunter2-token-value');
  const raw = db.prepare(`SELECT value FROM secrets WHERE key = 'test.token'`).get().value;
  ok('secret is not stored in plaintext', !Buffer.from(raw).toString('utf8').includes('hunter2'));
  secrets.remove('test.token');
  ok('secret removal works', secrets.get('test.token') === null);

  const rpc = require('../src/main/rpc');
  require('../src/main/core/app-rpc');
  const badges = await rpc.dispatch('app.badges', {});
  ok('rpc dispatch + badges', badges.result && badges.result.points === 0 && badges.result.level.length > 0);
  const bad = await rpc.dispatch('no.such.method', {});
  ok('unknown rpc returns error not crash', !!bad.error);

  console.log('\nStage 2: channel adapters');
  const adapters = require('../src/main/adapters');
  ok('four adapters registered', adapters.all().length === 4);
  const channels = await rpc.dispatch('channels.list', {});
  ok('channels.list returns health for all', channels.result.length === 4 && channels.result.every(c => c.status));
  const fb = adapters.get('facebook');
  ok('facebook adapter is manual kind', fb.kind === 'manual' && (await fb.getStatus()).status === 'manual');
  const fbPub = await fb.publishListing({}, { title: 'Oak Bowl', price: '$45.00', description: 'Hand-turned oak bowl.', tags: '#handmade' });
  ok('facebook publish exports text (never auto-posts)', fbPub.exported.includes('Oak Bowl') && fbPub.exported.includes('#handmade'));
  const fbReply = await fb.sendReply({}, 'Thanks, it ships Monday!');
  ok('facebook reply exports text', fbReply.exported === 'Thanks, it ships Monday!');
  ok('amazon + walmart are honest stubs', (await adapters.get('amazon').getStatus()).status === 'not_connected'
    && (await adapters.get('walmart').getStatus()).status === 'not_connected');

  await adapters.get('ebay').saveCredentials({ clientId: 'TestApp-123', clientSecret: 'shh-cert-id', ruName: 'Test_RuName', env: 'sandbox' });
  const begin = await adapters.get('ebay').beginConnect();
  ok('ebay beginConnect builds sandbox consent URL', begin.authUrl.startsWith('https://auth.sandbox.ebay.com/oauth2/authorize')
    && begin.authUrl.includes('client_id=TestApp-123') && begin.authUrl.includes('sell.inventory'));
  ok('ebay client secret stored encrypted', secrets.has('ebay.client_secret')
    && !Buffer.from(db.prepare(`SELECT value FROM secrets WHERE key='ebay.client_secret'`).get().value).toString('utf8').includes('shh-cert-id'));
  ok('ebay publish checklist reports missing setup', adapters.get('ebay').publishChecklist().length === 5);

  console.log('\nStage 3: unified inbox + orders + sync');
  const inbox = require('../src/main/core/inbox');
  const ordersMod = require('../src/main/core/orders');
  const eventsBus = require('../src/main/core/events');
  const seen = [];
  for (const evt of ['message.replied', 'thread.resolved', 'order.new', 'order.shipped', 'sale.completed', 'listing.published']) {
    eventsBus.on(evt, (p) => seen.push({ evt, p }));
  }

  const fakeThreads = [{
    externalThreadId: 'buyer1::123', subject: 'Question about oak bowl', counterpart: 'buyer1', listingRef: '123', orderRef: '',
    messages: [{ externalId: 'm1', direction: 'in', sender: 'buyer1', body: 'Is it food safe?', sentAt: '2026-07-07T10:00:00Z' }]
  }];
  let added = inbox.upsertThreads('ebay', fakeThreads);
  ok('sync inserts new incoming messages', added === 1);
  added = inbox.upsertThreads('ebay', fakeThreads);
  ok('sync dedupes already-seen messages', added === 0);
  const threads = (await rpc.dispatch('inbox.threads', {})).result;
  ok('inbox lists normalized threads with unread', threads.length === 1 && threads[0].unread === 1 && threads[0].channel_name === 'eBay');
  ok('inbox channel filter works', (await rpc.dispatch('inbox.threads', { channel: 'facebook' })).result.length === 0);

  const manual = (await rpc.dispatch('inbox.manualThread', { counterpart: 'Jane', body: 'Still available?', listingRef: 'cutting board' })).result;
  const reply = (await rpc.dispatch('inbox.reply', { threadId: manual.threadId, body: 'Yes! Can deliver Saturday.' })).result;
  ok('facebook reply routes to export (copy/paste)', reply.exported === 'Yes! Can deliver Saturday.');
  ok('reply emits message.replied event', seen.some(s => s.evt === 'message.replied' && s.p.channelId === 'facebook'));
  await rpc.dispatch('inbox.resolve', { threadId: manual.threadId });
  ok('resolve clears unread + emits event', seen.some(s => s.evt === 'thread.resolved'));

  const fakeOrders = [{
    externalId: 'ORD-1', buyer: 'buyer1', itemSummary: 'Walnut cutting board', quantity: 1,
    totalCents: 6500, currency: 'USD', orderDate: '2026-07-06T12:00:00Z',
    shipBy: new Date(Date.now() + 3 * 86400_000).toISOString(), meta: {}
  }];
  ok('order sync inserts', ordersMod.upsertOrders('ebay', fakeOrders) === 1);
  ok('order sync dedupes', ordersMod.upsertOrders('ebay', fakeOrders) === 0);
  const orderRow = (await rpc.dispatch('orders.list', {})).result[0];
  await rpc.dispatch('orders.setCost', { orderId: orderRow.id, costCents: 2000 });
  await rpc.dispatch('orders.setStatus', { orderId: orderRow.id, status: 'packed' });
  await rpc.dispatch('orders.setStatus', { orderId: orderRow.id, status: 'shipped' });
  ok('shipping emits on-time event', seen.some(s => s.evt === 'order.shipped' && s.p.onTime === true));
  await rpc.dispatch('orders.setStatus', { orderId: orderRow.id, status: 'done' });
  ok('completion emits sale.completed', seen.some(s => s.evt === 'sale.completed'));
  const manualOrder = (await rpc.dispatch('orders.manualAdd', { itemSummary: 'Pine shelf', totalCents: 4000, costCents: 1000 })).result;
  ok('manual facebook sale recorded', manualOrder.orderId > 0);
  ok('badges reflect open orders', (await rpc.dispatch('app.badges', {})).result.openOrders === 1);

  console.log('\nStage 4: products, templates, listing composer');
  require('../src/main/core/products');
  require('../src/main/core/listings');
  const prod = (await rpc.dispatch('products.save', {
    title: 'Walnut Serving Board', description: 'Hand-finished serving board with juice groove.',
    price_cents: 6500, cost_cents: 1800, quantity: 2,
    dimensions: '18" x 10" x 1"', materials: 'black walnut', tags: 'serving board, walnut, kitchen',
    photos: ['https://example.com/board.jpg']
  })).result;
  ok('product saves', prod.id > 0);
  ok('products list', (await rpc.dispatch('products.list', {})).result.length === 1);

  const ebayR = (await rpc.dispatch('listings.render', { productId: prod.id, channelId: 'ebay' })).result;
  ok('ebay render fills variables', ebayR.fields.title.value.includes('Walnut Serving Board') && ebayR.fields.title.value.includes('black walnut'));
  ok('ebay render enforces 80-char title info', ebayR.fields.title.limit === 80 && typeof ebayR.fields.title.over === 'boolean');
  const fbR = (await rpc.dispatch('listings.render', { productId: prod.id, channelId: 'facebook' })).result;
  ok('facebook render includes hashtags + price', fbR.fields.tags.value.includes('#walnut') && fbR.fields.price.value === '$65.00');

  const tpl = (await rpc.dispatch('templates.get', { channelId: 'facebook' })).result;
  tpl.fields.title.template = 'CUSTOM {{title}}';
  await rpc.dispatch('templates.save', { channelId: 'facebook', template: tpl });
  const fbR2 = (await rpc.dispatch('listings.render', { productId: prod.id, channelId: 'facebook' })).result;
  ok('templates are editable + persist', fbR2.fields.title.value === 'CUSTOM Walnut Serving Board');
  await rpc.dispatch('templates.reset', { channelId: 'facebook' });

  const pub = (await rpc.dispatch('listings.publish', { productId: prod.id, channelIds: ['facebook', 'amazon'] })).result;
  const fbPubRes = pub.find(r => r.channelId === 'facebook');
  const amzPubRes = pub.find(r => r.channelId === 'amazon');
  ok('facebook publish exports formatted listing', fbPubRes.exported.includes('Walnut Serving Board') && fbPubRes.exported.includes('#handmade'));
  ok('unconnected channel fails cleanly, not silently', /doesn't support publishing/.test(amzPubRes.error));
  ok('listing published event fired', seen.some(s => s.evt === 'listing.published'));
  const hist = (await rpc.dispatch('listings.forProduct', { productId: prod.id })).result;
  ok('listing history recorded', hist.some(l => l.status === 'exported') && hist.some(l => l.status === 'error'));

  console.log(`\n${passed} checks passed${process.exitCode ? ' (WITH FAILURES)' : ''}`);
  require('../src/main/db').close();
  fs.rmSync(process.env.SHOP_DATA_DIR, { recursive: true, force: true });
}

main().catch((e) => { console.error(e); process.exit(1); });
