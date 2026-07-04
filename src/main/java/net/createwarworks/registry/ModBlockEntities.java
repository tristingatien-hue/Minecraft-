package net.createwarworks.registry;

import net.createwarworks.CreateWarworks;
import net.createwarworks.blockentity.AutocannonTurretBlockEntity;
import net.createwarworks.blockentity.CannonBreechBlockEntity;
import net.createwarworks.blockentity.RadarBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, CreateWarworks.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CannonBreechBlockEntity>> CANNON_BREECH =
            BLOCK_ENTITIES.register("cannon_breech", () -> BlockEntityType.Builder
                    .of(CannonBreechBlockEntity::new, ModBlocks.CANNON_BREECH.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AutocannonTurretBlockEntity>> AUTOCANNON_TURRET =
            BLOCK_ENTITIES.register("autocannon_turret", () -> BlockEntityType.Builder
                    .of(AutocannonTurretBlockEntity::new, ModBlocks.AUTOCANNON_TURRET.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RadarBlockEntity>> RADAR =
            BLOCK_ENTITIES.register("radar", () -> BlockEntityType.Builder
                    .of(RadarBlockEntity::new, ModBlocks.RADAR.get())
                    .build(null));

    private ModBlockEntities() {
    }
}
