package net.createwarworks.blockentity;

import java.util.ArrayList;
import java.util.List;

import net.createwarworks.WarworksConfig;
import net.createwarworks.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Sweeps for contacts on a fixed interval, caches them for linked turrets,
 * optionally applies the glowing outline, and drives a comparator signal that
 * scales with the nearest contact's distance.
 */
public class RadarBlockEntity extends BlockEntity {
    private final List<Integer> contactIds = new ArrayList<>();
    private int comparatorOutput = 0;

    public RadarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RADAR.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RadarBlockEntity radar) {
        int interval = WarworksConfig.RADAR_SCAN_INTERVAL_TICKS.get();
        if (level.getGameTime() % interval != 0 || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (WarworksConfig.RADAR_NEEDS_REDSTONE.get() && !level.hasNeighborSignal(pos)) {
            radar.clearContacts(serverLevel, pos, state);
            return;
        }
        radar.sweep(serverLevel, pos, state, interval);
    }

    private void sweep(ServerLevel level, BlockPos pos, BlockState state, int interval) {
        int range = WarworksConfig.RADAR_RANGE.get();
        Vec3 center = Vec3.atCenterOf(pos);
        AABB box = new AABB(pos).inflate(range);

        List<LivingEntity> contacts = level.getEntitiesOfClass(LivingEntity.class, box,
                RadarBlockEntity::isDetectable);

        contactIds.clear();
        double nearestSq = Double.MAX_VALUE;
        for (LivingEntity contact : contacts) {
            contactIds.add(contact.getId());
            nearestSq = Math.min(nearestSq, contact.distanceToSqr(center));
            if (WarworksConfig.RADAR_HIGHLIGHTS_CONTACTS.get()) {
                contact.addEffect(new MobEffectInstance(MobEffects.GLOWING, interval + 20, 0, false, false));
            }
        }

        int newOutput;
        if (contacts.isEmpty()) {
            newOutput = 0;
        } else {
            double nearest = Math.sqrt(nearestSq);
            newOutput = 15 - (int) Math.floor(nearest / range * 14.0D);
            newOutput = Math.max(1, Math.min(15, newOutput));
        }
        if (newOutput != comparatorOutput) {
            comparatorOutput = newOutput;
            level.updateNeighbourForOutputSignal(pos, state.getBlock());
        }
    }

    private void clearContacts(ServerLevel level, BlockPos pos, BlockState state) {
        contactIds.clear();
        if (comparatorOutput != 0) {
            comparatorOutput = 0;
            level.updateNeighbourForOutputSignal(pos, state.getBlock());
        }
    }

    private static boolean isDetectable(LivingEntity entity) {
        if (!entity.isAlive() || entity.isSpectator()) {
            return false;
        }
        if (entity instanceof Player player) {
            return WarworksConfig.RADAR_DETECTS_PLAYERS.get() && !player.isCreative();
        }
        return WarworksConfig.RADAR_DETECTS_HOSTILES.get() && entity instanceof Enemy;
    }

    /** Live contact list for linked turrets; resolves cached entity ids. */
    public List<LivingEntity> getContacts(ServerLevel level) {
        List<LivingEntity> result = new ArrayList<>(contactIds.size());
        for (int id : contactIds) {
            Entity entity = level.getEntity(id);
            if (entity instanceof LivingEntity living && living.isAlive()) {
                result.add(living);
            }
        }
        return result;
    }

    public int getComparatorOutput() {
        return comparatorOutput;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("ComparatorOutput", comparatorOutput);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        comparatorOutput = tag.getInt("ComparatorOutput");
    }
}
