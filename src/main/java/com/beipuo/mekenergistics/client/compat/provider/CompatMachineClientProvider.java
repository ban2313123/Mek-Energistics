package com.beipuo.mekenergistics.client.compat.provider;

import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public interface CompatMachineClientProvider {
    void registerScreens(RegisterMenuScreensEvent event);

    default void registerClientSetup(FMLClientSetupEvent event) {
    }

    default void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
    }
}
