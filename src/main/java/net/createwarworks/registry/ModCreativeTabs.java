package net.createwarworks.registry;

import net.createwarworks.CreateWarworks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CreateWarworks.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN =
            TABS.register("main", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.createwarworks"))
                    .icon(() -> new ItemStack(ModItems.SHELL.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.CANNON_BREECH.get());
                        output.accept(ModItems.CANNON_BARREL.get());
                        output.accept(ModItems.AUTOCANNON_TURRET.get());
                        output.accept(ModItems.RADAR.get());
                        output.accept(ModItems.SHELL.get());
                        output.accept(ModItems.AUTOCANNON_ROUND.get());
                        output.accept(ModItems.TARGETING_LINKER.get());
                    })
                    .build());

    private ModCreativeTabs() {
    }
}
