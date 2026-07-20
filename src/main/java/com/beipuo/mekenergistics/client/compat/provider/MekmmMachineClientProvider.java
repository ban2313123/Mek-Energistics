package com.beipuo.mekenergistics.client.compat.provider;

import com.beipuo.mekenergistics.client.compat.mekmm.MekanismMoreMachineAdvancedClientScreens;
import com.beipuo.mekenergistics.client.compat.mekmm.MekanismMoreMachineClientScreens;
import com.beipuo.mekenergistics.client.compat.mekmm.MekanismMoreMachineLargeClientScreens;
import com.beipuo.mekenergistics.common.machine.MeMekanismMachine;
import com.beipuo.mekenergistics.compat.catalog.CompatMachineCatalog;
import com.beipuo.mekenergistics.compat.catalog.CompatRegistrationRoute;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public final class MekmmMachineClientProvider implements CompatMachineClientProvider {
    @Override
    public void registerScreens(RegisterMenuScreensEvent event) {
        if (CompatMachineCatalog.hasAvailableRoute(CompatRegistrationRoute.MEKMM_MACHINE)
                || CompatMachineCatalog.hasAvailableRoute(CompatRegistrationRoute.MEKMM_FACTORY)) {
            MekanismMoreMachineClientScreens.register(event);
        }
        if (CompatMachineCatalog.hasAvailableRoute(CompatRegistrationRoute.MEKMM_ADVANCED_FACTORY)) {
            MekanismMoreMachineAdvancedClientScreens.register(event);
        }
        if (CompatMachineCatalog.isAvailable(MeMekanismMachine.LARGE_ANTIPROTONIC_NUCLEOSYNTHESIZER)) {
            MekanismMoreMachineLargeClientScreens.register(event);
        }
    }
}
