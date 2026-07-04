package net.createwarworks.item;

import net.createwarworks.blockentity.AutocannonTurretBlockEntity;
import net.createwarworks.blockentity.RadarBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * Fire-control tool. Right-click a radar to select it, then right-click a
 * turret to slave that turret to the radar (the turret then acquires targets
 * from the radar's contact list at the radar's full range). Sneak-click a
 * turret to unlink it.
 */
public class TargetingLinkerItem extends Item {
    private static final String RADAR_KEY = "SelectedRadar";

    public TargetingLinkerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        if (level.getBlockEntity(pos) instanceof RadarBlockEntity) {
            if (!level.isClientSide) {
                CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
                tag.putLong(RADAR_KEY, pos.asLong());
                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                if (player != null) {
                    player.displayClientMessage(
                            Component.translatable("message.createwarworks.radar_selected",
                                    pos.getX(), pos.getY(), pos.getZ()), true);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (level.getBlockEntity(pos) instanceof AutocannonTurretBlockEntity turret) {
            if (!level.isClientSide) {
                if (player != null && player.isShiftKeyDown()) {
                    turret.setLinkedRadar(null);
                    player.displayClientMessage(
                            Component.translatable("message.createwarworks.turret_unlinked"), true);
                } else {
                    CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
                    if (tag.contains(RADAR_KEY)) {
                        BlockPos radarPos = BlockPos.of(tag.getLong(RADAR_KEY));
                        turret.setLinkedRadar(radarPos);
                        if (player != null) {
                            player.displayClientMessage(
                                    Component.translatable("message.createwarworks.turret_linked",
                                            radarPos.getX(), radarPos.getY(), radarPos.getZ()), true);
                        }
                    } else if (player != null) {
                        player.displayClientMessage(
                                Component.translatable("message.createwarworks.no_radar_selected"), true);
                    }
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.PASS;
    }
}
