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

  console.log(`\n${passed} checks passed${process.exitCode ? ' (WITH FAILURES)' : ''}`);
  require('../src/main/db').close();
  fs.rmSync(process.env.SHOP_DATA_DIR, { recursive: true, force: true });
}

main().catch((e) => { console.error(e); process.exit(1); });
