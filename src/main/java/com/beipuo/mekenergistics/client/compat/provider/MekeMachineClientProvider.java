package com.beipuo.mekenergistics.client.compat.provider;

import com.beipuo.mekenergistics.client.compat.meke.MekanismExtrasAdvancedClientScreens;
import com.beipuo.mekenergistics.client.compat.meke.MekanismExtrasClientScreens;
import com.beipuo.mekenergistics.client.compat.meke.MekanismExtrasMoreMachineClientScreens;
import com.beipuo.mekenergistics.compat.catalog.CompatMachineCatalog;
import com.beipuo.mekenergistics.compat.catalog.CompatRegistrationRoute;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public final class MekeMachineClientProvider implements CompatMachineClientProvider {
    @Override
    public void registerScreens(RegisterMenuScreensEvent event) {
        if (CompatMachineCatalog.hasAvailableRoute(CompatRegistrationRoute.MEKE_FACTORY)) {
            MekanismExtrasClientScreens.register(event);
        }
        if (CompatMachineCatalog.hasAvailableRoute(CompatRegistrationRoute.MEKE_MEKMM_FACTORY)) {
            MekanismExtrasMoreMachineClientScreens.register(event);
        }
        if (CompatMachineCatalog.hasAvailableRoute(CompatRegistrationRoute.MEKE_MEKMM_ADVANCED_FACTORY)) {
            MekanismExtrasAdvancedClientScreens.register(event);
        }
    }
}
