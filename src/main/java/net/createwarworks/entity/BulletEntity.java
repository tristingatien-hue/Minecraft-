package net.createwarworks.entity;

import net.createwarworks.WarworksConfig;
import net.createwarworks.registry.ModEntities;
import net.createwarworks.registry.ModItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * An autocannon round in flight: fast, flat trajectory, direct damage, no
 * block damage. Despawns quickly if it hits nothing.
 */
public class BulletEntity extends ThrowableItemProjectile {
    private static final int MAX_LIFE_TICKS = 100;

    private int life = 0;

    public BulletEntity(EntityType<? extends BulletEntity> type, Level level) {
        super(type, level);
    }

    public BulletEntity(Level level, double x, double y, double z) {
        this(ModEntities.BULLET.get(), level);
        setPos(x, y, z);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.AUTOCANNON_ROUND.get();
    }

    @Override
    protected double getDefaultGravity() {
        return 0.01D;
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && ++life > MAX_LIFE_TICKS) {
            discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!level().isClientSide) {
            float damage = WarworksConfig.TURRET_BULLET_DAMAGE.get().floatValue();
            result.getEntity().hurt(level().damageSources().thrown(this, getOwner()), damage);
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!level().isClientSide) {
            discard();
        }
    }
}
