package net.createwarworks.registry;

import net.createwarworks.CreateWarworks;
import net.createwarworks.block.AutocannonTurretBlock;
import net.createwarworks.block.CannonBarrelBlock;
import net.createwarworks.block.CannonBreechBlock;
import net.createwarworks.block.RadarBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(CreateWarworks.MODID);

    private static BlockBehaviour.Properties metal(float strength) {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(strength, 12.0F)
                .sound(SoundType.NETHERITE_BLOCK)
                .requiresCorrectToolForDrops();
    }

    public static final DeferredBlock<Block> CANNON_BARREL = BLOCKS.register("cannon_barrel",
            () -> new CannonBarrelBlock(metal(5.0F)));

    public static final DeferredBlock<Block> CANNON_BREECH = BLOCKS.register("cannon_breech",
            () -> new CannonBreechBlock(metal(5.5F)));

    public static final DeferredBlock<Block> AUTOCANNON_TURRET = BLOCKS.register("autocannon_turret",
            () -> new AutocannonTurretBlock(metal(4.5F)));

    public static final DeferredBlock<Block> RADAR = BLOCKS.register("radar",
            () -> new RadarBlock(metal(3.5F)));

    private ModBlocks() {
    }
}
