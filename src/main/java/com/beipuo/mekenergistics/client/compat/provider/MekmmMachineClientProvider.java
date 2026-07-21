package com.beipuo.mekenergistics.client.compat.provider;

import com.beipuo.mekenergistics.client.compat.mekmm.MekanismMoreMachineAdvancedClientScreens;
import com.beipuo.mekenergistics.client.compat.mekmm.MekanismMoreMachineClientScreens;
import com.beipuo.mekenergistics.client.compat.mekmm.MekanismMoreMachineLargeClientScreens;
import com.beipuo.mekenergistics.compat.catalog.CompatMachineCatalog;
import com.beipuo.mekenergistics.compat.catalog.CompatRegistrationRoute;
import com.beipuo.mekenergistics.compat.mekmm.MekanismMoreMachineBaseCompat;
import com.beipuo.mekenergistics.client.compat.mekmm.MekanismMoreMachineLargeClientModels;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public final class MekmmMachineClientProvider implements CompatMachineClientProvider {
    @Override
    public void registerClientSetup(FMLClientSetupEvent event) {
        if (MekanismMoreMachineBaseCompat.hasAvailableLargeMachines()) {
            MekanismMoreMachineLargeClientModels.registerModels();
        }
    }

    @Override
    public void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        if (MekanismMoreMachineBaseCompat.hasAvailableLargeMachines()) {
            MekanismMoreMachineLargeClientModels.registerRenderers(event);
        }
    }

    @Override
    public void registerScreens(RegisterMenuScreensEvent event) {
        if (CompatMachineCatalog.hasAvailableRoute(CompatRegistrationRoute.MEKMM_MACHINE)
                || CompatMachineCatalog.hasAvailableRoute(CompatRegistrationRoute.MEKMM_FACTORY)) {
            MekanismMoreMachineClientScreens.register(event);
        }
        if (CompatMachineCatalog.hasAvailableRoute(CompatRegistrationRoute.MEKMM_ADVANCED_FACTORY)) {
            MekanismMoreMachineAdvancedClientScreens.register(event);
        }
        if (MekanismMoreMachineBaseCompat.hasAvailableLargeMachines()) {
            MekanismMoreMachineLargeClientScreens.register(event);
        }
    }
}
