package net.createwarworks.registry;

import net.createwarworks.CreateWarworks;
import net.createwarworks.item.TargetingLinkerItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CreateWarworks.MODID);

    // Block items
    public static final DeferredItem<BlockItem> CANNON_BARREL =
            ITEMS.registerSimpleBlockItem("cannon_barrel", ModBlocks.CANNON_BARREL);
    public static final DeferredItem<BlockItem> CANNON_BREECH =
            ITEMS.registerSimpleBlockItem("cannon_breech", ModBlocks.CANNON_BREECH);
    public static final DeferredItem<BlockItem> AUTOCANNON_TURRET =
            ITEMS.registerSimpleBlockItem("autocannon_turret", ModBlocks.AUTOCANNON_TURRET);
    public static final DeferredItem<BlockItem> RADAR =
            ITEMS.registerSimpleBlockItem("radar", ModBlocks.RADAR);

    // Munitions
    public static final DeferredItem<Item> SHELL = ITEMS.register("shell",
            () -> new Item(new Item.Properties().stacksTo(16)));
    public static final DeferredItem<Item> AUTOCANNON_ROUND = ITEMS.register("autocannon_round",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // Tools
    public static final DeferredItem<Item> TARGETING_LINKER = ITEMS.register("targeting_linker",
            () -> new TargetingLinkerItem(new Item.Properties().stacksTo(1)));

    private ModItems() {
    }
}
