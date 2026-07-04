package net.createwarworks.entity;

import net.createwarworks.WarworksConfig;
import net.createwarworks.registry.ModEntities;
import net.createwarworks.registry.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * A big cannon shell. Flies ballistically, leaves a smoke trail, explodes on
 * impact with configurable power, and despawns after a configurable lifetime
 * so stray shots can't fly (and chunk-load) forever.
 */
public class ShellEntity extends ThrowableItemProjectile {
    private int life = 0;

    public ShellEntity(EntityType<? extends ShellEntity> type, Level level) {
        super(type, level);
    }

    public ShellEntity(Level level, double x, double y, double z) {
        this(ModEntities.SHELL.get(), level);
        setPos(x, y, z);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.SHELL.get();
    }

    @Override
    protected double getDefaultGravity() {
        return 0.05D;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            Vec3 pos = position();
            level().addParticle(ParticleTypes.SMOKE, pos.x, pos.y, pos.z, 0, 0, 0);
            level().addParticle(ParticleTypes.FLAME, pos.x, pos.y, pos.z, 0, 0, 0);
        } else if (++life > WarworksConfig.SHELL_LIFETIME_TICKS.get()) {
            discard();
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!level().isClientSide) {
            float power = WarworksConfig.SHELL_EXPLOSION_POWER.get().floatValue();
            Level.ExplosionInteraction interaction = WarworksConfig.SHELL_BLOCK_DAMAGE.get()
                    ? Level.ExplosionInteraction.TNT
                    : Level.ExplosionInteraction.NONE;
            level().explode(this, getX(), getY(), getZ(), power, interaction);
            discard();
        }
    }
}
