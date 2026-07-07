// AI assistant — arrives in build stage 6 (post-MVP, per the build order).
//
// The scaffolding is already in place so stage 6 is additive, not invasive:
//   - provider.js        swappable local model backend (config-driven)
//   - approval_queue     DB table: every AI proposal lands here as 'pending';
//                        nothing is sent or posted until the owner approves
//   - events.js          the same domain events the game engine scores
//
// HARD GUARDRAILS (enforced in code when stage 6 lands, not just prompts):
//   1. The assistant NEVER sends messages or posts/edits listings itself.
//      Its only write path is INSERT INTO approval_queue (status 'pending').
//      Sending happens in inbox.reply / listings.publish, which only the
//      owner's UI actions invoke. Adapter guardrails still apply beneath
//      that (manual channels physically cannot auto-post — they only export).
//   2. Drafts may only reference product fields that exist in the products
//      table — no invented specs, materials, discounts, or delivery promises.
//   3. Points are its optimization signal, but points only accrue from real
//      outcomes (see game.js), so the honest strategy is the optimal one.
'use strict';

const rpc = require('../rpc');

rpc.register('ai.status', async () => {
  const cfg = require('../config').load().ai;
  const reachable = cfg.enabled ? await require('./provider').available() : false;
  return {
    enabled: false,
    stage: 6,
    detail: 'The AI assistant ships in build stage 6, after the MVP is confirmed running. Its backend is configurable now (Settings → Local AI assistant).',
    backend: { baseUrl: cfg.baseUrl, model: cfg.model, reachable }
  };
});

module.exports = {};
