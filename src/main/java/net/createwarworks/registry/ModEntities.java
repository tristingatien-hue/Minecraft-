package net.createwarworks.registry;

import net.createwarworks.CreateWarworks;
import net.createwarworks.entity.BulletEntity;
import net.createwarworks.entity.ShellEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, CreateWarworks.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<ShellEntity>> SHELL =
            ENTITIES.register("shell", () -> EntityType.Builder.<ShellEntity>of(ShellEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(16)
                    .updateInterval(1)
                    .build("createwarworks:shell"));

    public static final DeferredHolder<EntityType<?>, EntityType<BulletEntity>> BULLET =
            ENTITIES.register("bullet", () -> EntityType.Builder.<BulletEntity>of(BulletEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(8)
                    .updateInterval(1)
                    .build("createwarworks:bullet"));

    private ModEntities() {
    }
}
