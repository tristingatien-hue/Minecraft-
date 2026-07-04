package net.createwarworks.client;

import net.createwarworks.CreateWarworks;
import net.createwarworks.registry.ModEntities;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = CreateWarworks.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientEvents {
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.SHELL.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.BULLET.get(), ThrownItemRenderer::new);
    }

    private ClientEvents() {
    }
}
