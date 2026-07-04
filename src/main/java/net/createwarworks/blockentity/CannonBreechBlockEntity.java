package net.createwarworks.blockentity;

import net.createwarworks.WarworksConfig;
import net.createwarworks.block.CannonBarrelBlock;
import net.createwarworks.block.CannonBreechBlock;
import net.createwarworks.entity.ShellEntity;
import net.createwarworks.registry.ModBlockEntities;
import net.createwarworks.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Holds the loaded shell and the firing cooldown for a cannon breech.
 * Firing counts the barrel blocks in front of the breech at that moment, so
 * cannons never need an "assembly" step and never get stuck half-assembled.
 */
public class CannonBreechBlockEntity extends BlockEntity {
    private ItemStack loadedShell = ItemStack.EMPTY;
    private int cooldown = 0;

    public CannonBreechBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CANNON_BREECH.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CannonBreechBlockEntity breech) {
        if (breech.cooldown > 0) {
            breech.cooldown--;
            if (breech.cooldown % 10 == 0 && level instanceof ServerLevel serverLevel) {
                Direction facing = state.getValue(CannonBreechBlock.FACING);
                Vec3 muzzle = breech.muzzlePosition(facing, breech.countBarrels(level, pos, facing));
                serverLevel.sendParticles(ParticleTypes.SMOKE, muzzle.x, muzzle.y, muzzle.z, 2, 0.1, 0.1, 0.1, 0.01);
            }
        }
    }

    /** Right-click handling: load a shell, or (sneaking, empty hand) unload it. */
    public ItemInteractionResult interact(Player player, ItemStack held) {
        Level level = getLevel();
        if (level == null) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (held.is(ModItems.SHELL.get())) {
            if (!loadedShell.isEmpty()) {
                if (!level.isClientSide) {
                    player.displayClientMessage(Component.translatable("message.createwarworks.breech_full"), true);
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
            if (!level.isClientSide) {
                loadedShell = held.copyWithCount(1);
                if (!player.getAbilities().instabuild) {
                    held.shrink(1);
                }
                setChanged();
                level.playSound(null, getBlockPos(), SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS, 0.8F, 0.7F);
                player.displayClientMessage(Component.translatable("message.createwarworks.shell_loaded"), true);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        if (held.isEmpty() && player.isShiftKeyDown() && !loadedShell.isEmpty()) {
            if (!level.isClientSide) {
                ItemStack removed = loadedShell;
                loadedShell = ItemStack.EMPTY;
                setChanged();
                if (!player.getInventory().add(removed)) {
                    player.drop(removed, false);
                }
                player.displayClientMessage(Component.translatable("message.createwarworks.shell_removed"), true);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    /** Fired on a redstone rising edge from the block. */
    public void tryFire(ServerLevel level) {
        if (cooldown > 0 || loadedShell.isEmpty()) {
            return;
        }
        BlockState state = getBlockState();
        Direction facing = state.getValue(CannonBreechBlock.FACING);
        int barrels = countBarrels(level, getBlockPos(), facing);

        double velocity = Math.min(
                WarworksConfig.CANNON_BASE_VELOCITY.get() + WarworksConfig.CANNON_VELOCITY_PER_BARREL.get() * barrels,
                WarworksConfig.CANNON_MAX_VELOCITY.get());

        Vec3 muzzle = muzzlePosition(facing, barrels);
        ShellEntity shell = new ShellEntity(level, muzzle.x, muzzle.y, muzzle.z);
        Vec3 dir = Vec3.atLowerCornerOf(facing.getNormal());
        shell.setDeltaMovement(dir.scale(velocity));
        level.addFreshEntity(shell);

        loadedShell = ItemStack.EMPTY;
        cooldown = WarworksConfig.CANNON_COOLDOWN_TICKS.get();
        setChanged();

        level.playSound(null, getBlockPos(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 4.0F, 0.6F);
        level.sendParticles(ParticleTypes.EXPLOSION, muzzle.x, muzzle.y, muzzle.z, 1, 0, 0, 0, 0);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, muzzle.x, muzzle.y, muzzle.z, 12, 0.2, 0.2, 0.2, 0.05);
    }

    /** Counts aligned barrel blocks in front of the breech, up to the config cap. */
    private int countBarrels(Level level, BlockPos pos, Direction facing) {
        int max = WarworksConfig.CANNON_MAX_BARREL_LENGTH.get();
        int count = 0;
        BlockPos.MutableBlockPos cursor = pos.mutable();
        while (count < max) {
            cursor.move(facing);
            BlockState found = level.getBlockState(cursor);
            if (!(found.getBlock() instanceof CannonBarrelBlock)
                    || found.getValue(CannonBarrelBlock.AXIS) != facing.getAxis()) {
                break;
            }
            count++;
        }
        return count;
    }

    private Vec3 muzzlePosition(Direction facing, int barrels) {
        BlockPos muzzleBlock = getBlockPos().relative(facing, barrels + 1);
        return Vec3.atCenterOf(muzzleBlock);
    }

    public void dropContents(Level level, BlockPos pos) {
        if (!loadedShell.isEmpty()) {
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), loadedShell);
            loadedShell = ItemStack.EMPTY;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!loadedShell.isEmpty()) {
            tag.put("Shell", loadedShell.save(registries));
        }
        tag.putInt("Cooldown", cooldown);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        loadedShell = tag.contains("Shell")
                ? ItemStack.parseOptional(registries, tag.getCompound("Shell"))
                : ItemStack.EMPTY;
        cooldown = tag.getInt("Cooldown");
    }
}
