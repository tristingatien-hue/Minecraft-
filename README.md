# Create: Warworks

Industrial warfare for Create-style packs — **Minecraft 1.21.1, NeoForge**.

One mod combining the ideas of [Create Big Cannons](https://www.curseforge.com/minecraft/mc-mods/create-big-cannons)
and [Create: Radars](https://www.curseforge.com/minecraft/mc-mods/create-radars):
big cannons, auto-targeting turrets, radar detection, and radar-linked fire
control. All original code (MIT), written fresh for NeoForge 1.21.1, with
every balance value exposed in one config file.

Create itself is an **optional** dependency: everything is driven by redstone,
which Create contraptions, links, and displays already speak natively — so it
drops straight into a Create pack without version-lock headaches.

## Features

### Big Cannons
- **Cannon Breech** + stackable **Cannon Barrel** blocks. Point the breech in
  any direction (including up/down) and stack barrels in front of it — each
  barrel adds muzzle velocity. No assembly ritual, no broken half-built
  multiblocks: the breech counts its barrels every shot.
- Right-click the breech with a **Cannon Shell** to load, fire with a redstone
  pulse (dispenser-style rising edge). Sneak-click with an empty hand to unload.
- Shells fly ballistically, trail smoke, explode on impact, and despawn after a
  configurable lifetime so stray shots can't chunk-load forever.

### Autocannon Turret
- Armed while receiving a redstone signal. Automatically acquires the nearest
  valid target (hostiles by default; players optional for PvP servers) and
  fires **Autocannon Rounds** from a 256-round internal magazine.
- Real line-of-sight checks — walls actually protect you.
- Right-click with rounds to load; empty-hand click shows the ammo count;
  sneak-click to empty the magazine.

### Radar
- Sweeps a large radius for players/hostiles on an interval, optionally
  outlining contacts with the glowing effect.
- Comparator output scales 1–15 with the distance of the nearest contact —
  wire it to alarms, doors, or Create displays.
- Optionally gated behind a redstone signal (`needsRedstone`).

### Fire Control (Targeting Linker)
- Right-click a **Radar**, then right-click a turret: the turret is now slaved
  to that radar and acquires targets from the radar's contact list at the
  radar's full range (default 64 blocks vs. the turret's local 24).
- Sneak-click a linked turret to unlink it.

## Balancing

Everything lives in `config/createwarworks-common.toml` (generated on first
launch): barrel velocity math, shell explosion power, block damage on/off,
turret damage/fire rate/range, radar range/interval/filters, and more. Tune it
per-pack or per-server without touching code.

## Building

Requires Java 21 and internet access to `maven.neoforged.net`.

```
./gradlew build
```

The jar lands in `build/libs/`. To test in-dev: `./gradlew runClient`.

## Credits

Inspired by Create Big Cannons (Cannoneers of Create) and Create: Radars
(Arsenalists of Create). This mod shares no code with them — it is a
from-scratch reimagining for NeoForge 1.21.1.
