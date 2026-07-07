// Stage-1 placeholder dashboard. Replaced by the full gamification
// dashboard in build stage 5.
import { call, h, esc } from '../app.js';

export async function render(el) {
  const info = await call('app.info');
  const cfg = await call('config.get');
  el.appendChild(h(`
    <div>
      <h1 class="page-title">🏆 ${esc(cfg.shopName)}</h1>
      <div class="page-sub">Shop Tycoon Console v${esc(info.version)} — dashboard coming online in stage 5.</div>
      <div class="panel">
        <h2>Welcome to your workshop console</h2>
        <p class="muted">The skeleton is running: window, database, and config system are live.
        Data lives at <span class="mono">${esc(info.dataDir)}</span>.</p>
      </div>
    </div>
  `));
}
