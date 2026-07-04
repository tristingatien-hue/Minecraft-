package net.createwarworks;

import net.createwarworks.registry.ModBlockEntities;
import net.createwarworks.registry.ModBlocks;
import net.createwarworks.registry.ModCreativeTabs;
import net.createwarworks.registry.ModEntities;
import net.createwarworks.registry.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(CreateWarworks.MODID)
public class CreateWarworks {
    public static final String MODID = "createwarworks";

    public CreateWarworks(IEventBus modEventBus, ModContainer modContainer) {
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModEntities.ENTITIES.register(modEventBus);
        ModCreativeTabs.TABS.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, WarworksConfig.SPEC);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
