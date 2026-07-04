package net.createwarworks;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * All balance values for Create: Warworks live here, exposed in
 * config/createwarworks-common.toml so packs and servers can tune the mod
 * without touching code.
 */
public final class WarworksConfig {
    public static final ModConfigSpec SPEC;

    // --- Big cannon ---
    public static final ModConfigSpec.IntValue CANNON_MAX_BARREL_LENGTH;
    public static final ModConfigSpec.DoubleValue CANNON_BASE_VELOCITY;
    public static final ModConfigSpec.DoubleValue CANNON_VELOCITY_PER_BARREL;
    public static final ModConfigSpec.DoubleValue CANNON_MAX_VELOCITY;
    public static final ModConfigSpec.IntValue CANNON_COOLDOWN_TICKS;

    // --- Shells ---
    public static final ModConfigSpec.DoubleValue SHELL_EXPLOSION_POWER;
    public static final ModConfigSpec.BooleanValue SHELL_BLOCK_DAMAGE;
    public static final ModConfigSpec.IntValue SHELL_LIFETIME_TICKS;

    // --- Autocannon turret ---
    public static final ModConfigSpec.IntValue TURRET_RANGE;
    public static final ModConfigSpec.IntValue TURRET_FIRE_COOLDOWN_TICKS;
    public static final ModConfigSpec.DoubleValue TURRET_BULLET_DAMAGE;
    public static final ModConfigSpec.DoubleValue TURRET_BULLET_VELOCITY;
    public static final ModConfigSpec.BooleanValue TURRET_TARGETS_PLAYERS;
    public static final ModConfigSpec.BooleanValue TURRET_TARGETS_HOSTILES;

    // --- Radar ---
    public static final ModConfigSpec.IntValue RADAR_RANGE;
    public static final ModConfigSpec.IntValue RADAR_SCAN_INTERVAL_TICKS;
    public static final ModConfigSpec.BooleanValue RADAR_DETECTS_PLAYERS;
    public static final ModConfigSpec.BooleanValue RADAR_DETECTS_HOSTILES;
    public static final ModConfigSpec.BooleanValue RADAR_HIGHLIGHTS_CONTACTS;
    public static final ModConfigSpec.BooleanValue RADAR_NEEDS_REDSTONE;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("big_cannon");
        CANNON_MAX_BARREL_LENGTH = builder
                .comment("Maximum number of cannon barrel blocks counted in front of a breech.")
                .defineInRange("maxBarrelLength", 15, 1, 64);
        CANNON_BASE_VELOCITY = builder
                .comment("Muzzle velocity (blocks/tick) of a breech with zero barrels attached.")
                .defineInRange("baseVelocity", 1.5D, 0.1D, 20.0D);
        CANNON_VELOCITY_PER_BARREL = builder
                .comment("Extra muzzle velocity (blocks/tick) added per barrel block.")
                .defineInRange("velocityPerBarrel", 0.45D, 0.0D, 5.0D);
        CANNON_MAX_VELOCITY = builder
                .comment("Hard cap on muzzle velocity (blocks/tick).")
                .defineInRange("maxVelocity", 8.0D, 0.5D, 20.0D);
        CANNON_COOLDOWN_TICKS = builder
                .comment("Ticks a breech must wait between shots (20 ticks = 1 second).")
                .defineInRange("cooldownTicks", 60, 0, 1200);
        builder.pop();

        builder.push("shell");
        SHELL_EXPLOSION_POWER = builder
                .comment("Explosion power of a cannon shell (TNT is 4.0).")
                .defineInRange("explosionPower", 4.0D, 0.0D, 16.0D);
        SHELL_BLOCK_DAMAGE = builder
                .comment("Whether shell explosions break blocks. Set false for arena-safe combat.")
                .define("blockDamage", true);
        SHELL_LIFETIME_TICKS = builder
                .comment("Ticks before an airborne shell despawns (prevents runaway chunk loading).")
                .defineInRange("lifetimeTicks", 400, 20, 12000);
        builder.pop();

        builder.push("autocannon_turret");
        TURRET_RANGE = builder
                .comment("Targeting range in blocks when the turret is NOT linked to a radar.")
                .defineInRange("range", 24, 4, 128);
        TURRET_FIRE_COOLDOWN_TICKS = builder
                .comment("Ticks between turret shots.")
                .defineInRange("fireCooldownTicks", 8, 1, 200);
        TURRET_BULLET_DAMAGE = builder
                .comment("Damage per autocannon round (half-hearts).")
                .defineInRange("bulletDamage", 6.0D, 0.5D, 100.0D);
        TURRET_BULLET_VELOCITY = builder
                .comment("Bullet velocity in blocks/tick.")
                .defineInRange("bulletVelocity", 3.5D, 0.5D, 10.0D);
        TURRET_TARGETS_PLAYERS = builder
                .comment("Whether turrets shoot at players (survival/adventure only). Enable for PvP servers.")
                .define("targetPlayers", false);
        TURRET_TARGETS_HOSTILES = builder
                .comment("Whether turrets shoot at hostile mobs.")
                .define("targetHostiles", true);
        builder.pop();

        builder.push("radar");
        RADAR_RANGE = builder
                .comment("Radar detection radius in blocks. Linked turrets also use this as their range.")
                .defineInRange("range", 64, 8, 256);
        RADAR_SCAN_INTERVAL_TICKS = builder
                .comment("Ticks between radar sweeps.")
                .defineInRange("scanIntervalTicks", 20, 5, 200);
        RADAR_DETECTS_PLAYERS = builder
                .comment("Whether the radar tracks players.")
                .define("detectPlayers", true);
        RADAR_DETECTS_HOSTILES = builder
                .comment("Whether the radar tracks hostile mobs.")
                .define("detectHostiles", true);
        RADAR_HIGHLIGHTS_CONTACTS = builder
                .comment("Whether detected entities get the glowing outline while tracked.")
                .define("highlightContacts", true);
        RADAR_NEEDS_REDSTONE = builder
                .comment("If true, the radar only sweeps while receiving a redstone signal.")
                .define("needsRedstone", false);
        builder.pop();

        SPEC = builder.build();
    }

    private WarworksConfig() {
    }
}
