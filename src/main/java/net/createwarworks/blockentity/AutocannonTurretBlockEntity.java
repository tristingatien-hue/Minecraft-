package net.createwarworks.blockentity;

import java.util.Comparator;
import java.util.List;

import net.createwarworks.WarworksConfig;
import net.createwarworks.entity.BulletEntity;
import net.createwarworks.registry.ModBlockEntities;
import net.createwarworks.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jetbrains.annotations.Nullable;

/**
 * Auto-targeting turret. Armed while receiving redstone. Stores up to 256
 * autocannon rounds internally; loses its target the moment line of sight
 * breaks, so walls actually protect you. Linking a radar (targeting linker)
 * extends its acquisition range to the radar's range using the radar's
 * contact list.
 */
public class AutocannonTurretBlockEntity extends BlockEntity {
    public static final int MAX_AMMO = 256;

    private int ammo = 0;
    private int cooldown = 0;
    @Nullable
    private BlockPos linkedRadar = null;

    public AutocannonTurretBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.AUTOCANNON_TURRET.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, AutocannonTurretBlockEntity turret) {
        if (turret.cooldown > 0) {
            turret.cooldown--;
        }
        if (turret.cooldown > 0 || turret.ammo <= 0 || !level.hasNeighborSignal(pos)) {
            return;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        LivingEntity target = turret.findTarget(serverLevel, pos);
        if (target == null) {
            return;
        }
        turret.fireAt(serverLevel, pos, target);
    }

    private void fireAt(ServerLevel level, BlockPos pos, LivingEntity target) {
        Vec3 muzzle = Vec3.atCenterOf(pos).add(0, 0.6, 0);
        Vec3 aim = target.getEyePosition().subtract(muzzle).normalize()
                .scale(WarworksConfig.TURRET_BULLET_VELOCITY.get());

        BulletEntity bullet = new BulletEntity(level, muzzle.x, muzzle.y, muzzle.z);
        bullet.setDeltaMovement(aim);
        level.addFreshEntity(bullet);

        ammo--;
        cooldown = WarworksConfig.TURRET_FIRE_COOLDOWN_TICKS.get();
        setChanged();

        level.playSound(null, pos, SoundEvents.CROSSBOW_SHOOT, SoundSource.BLOCKS, 1.2F, 1.4F);
        level.sendParticles(ParticleTypes.CRIT, muzzle.x, muzzle.y, muzzle.z, 3, 0.05, 0.05, 0.05, 0.1);
    }

    @Nullable
    private LivingEntity findTarget(ServerLevel level, BlockPos pos) {
        Vec3 eye = Vec3.atCenterOf(pos).add(0, 0.6, 0);

        // Radar-directed targeting: use the linked radar's contact list & range.
        RadarBlockEntity radar = resolveRadar(level);
        List<LivingEntity> candidates;
        if (radar != null) {
            candidates = radar.getContacts(level).stream()
                    .filter(this::isValidTarget)
                    .toList();
        } else {
            int range = WarworksConfig.TURRET_RANGE.get();
            AABB box = new AABB(pos).inflate(range);
            candidates = level.getEntitiesOfClass(LivingEntity.class, box, this::isValidTarget);
        }

        return candidates.stream()
                .filter(e -> hasLineOfSight(level, eye, e))
                .min(Comparator.comparingDouble(e -> e.distanceToSqr(eye)))
                .orElse(null);
    }

    @Nullable
    private RadarBlockEntity resolveRadar(ServerLevel level) {
        if (linkedRadar == null || !level.isLoaded(linkedRadar)) {
            return null;
        }
        return level.getBlockEntity(linkedRadar) instanceof RadarBlockEntity radar ? radar : null;
    }

    private boolean isValidTarget(LivingEntity entity) {
        if (!entity.isAlive() || entity.isSpectator()) {
            return false;
        }
        if (entity instanceof Player player) {
            return WarworksConfig.TURRET_TARGETS_PLAYERS.get() && !player.isCreative();
        }
        return WarworksConfig.TURRET_TARGETS_HOSTILES.get() && entity instanceof Enemy;
    }

    private boolean hasLineOfSight(ServerLevel level, Vec3 from, LivingEntity target) {
        HitResult hit = level.clip(new ClipContext(from, target.getEyePosition(),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()));
        return hit.getType() == HitResult.Type.MISS
                || hit.getLocation().distanceToSqr(target.getEyePosition()) < 1.0D;
    }

    /** Right-click handling: load rounds, or (sneaking, empty hand) empty the magazine. */
    public ItemInteractionResult interact(Player player, ItemStack held) {
        Level level = getLevel();
        if (level == null) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (held.is(ModItems.AUTOCANNON_ROUND.get())) {
            if (!level.isClientSide) {
                int space = MAX_AMMO - ammo;
                int inserted = Math.min(space, held.getCount());
                if (inserted > 0) {
                    ammo += inserted;
                    if (!player.getAbilities().instabuild) {
                        held.shrink(inserted);
                    }
                    setChanged();
                    level.playSound(null, getBlockPos(), SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS, 0.6F, 1.2F);
                }
                player.displayClientMessage(
                        Component.translatable("message.createwarworks.turret_ammo", ammo, MAX_AMMO), true);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        if (held.isEmpty() && player.isShiftKeyDown()) {
            if (!level.isClientSide && ammo > 0) {
                int stackSize = Math.min(ammo, 64);
                ammo -= stackSize;
                setChanged();
                ItemStack rounds = new ItemStack(ModItems.AUTOCANNON_ROUND.get(), stackSize);
                if (!player.getInventory().add(rounds)) {
                    player.drop(rounds, false);
                }
                player.displayClientMessage(
                        Component.translatable("message.createwarworks.turret_ammo", ammo, MAX_AMMO), true);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        if (held.isEmpty()) {
            if (!level.isClientSide) {
                player.displayClientMessage(
                        Component.translatable("message.createwarworks.turret_ammo", ammo, MAX_AMMO), true);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    public void setLinkedRadar(@Nullable BlockPos pos) {
        linkedRadar = pos;
        setChanged();
    }

    public void dropContents(Level level, BlockPos pos) {
        while (ammo > 0) {
            int stackSize = Math.min(ammo, 64);
            ammo -= stackSize;
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(),
                    new ItemStack(ModItems.AUTOCANNON_ROUND.get(), stackSize));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Ammo", ammo);
        tag.putInt("Cooldown", cooldown);
        if (linkedRadar != null) {
            tag.putLong("LinkedRadar", linkedRadar.asLong());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ammo = tag.getInt("Ammo");
        cooldown = tag.getInt("Cooldown");
        linkedRadar = tag.contains("LinkedRadar") ? BlockPos.of(tag.getLong("LinkedRadar")) : null;
    }
}
