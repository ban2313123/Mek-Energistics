package com.beipuo.mekenergistics.client.compat.provider;

import com.beipuo.mekenergistics.client.compat.eme.EvolvedMekanismExtrasAdvancedClientScreens;
import com.beipuo.mekenergistics.client.compat.eme.EvolvedMekanismExtrasClientScreens;
import com.beipuo.mekenergistics.client.compat.eme.EvolvedMekanismExtrasMoreMachineClientScreens;
import com.beipuo.mekenergistics.compat.catalog.CompatMachineCatalog;
import com.beipuo.mekenergistics.compat.catalog.CompatMachineFamily;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public final class EmekeMachineClientProvider implements CompatMachineClientProvider {
    @Override
    public void registerScreens(RegisterMenuScreensEvent event) {
        if (CompatMachineCatalog.hasAvailableFamily(CompatMachineFamily.EMEKE_FACTORY)) {
            EvolvedMekanismExtrasClientScreens.register(event);
        }
        if (CompatMachineCatalog.hasAvailableFamily(CompatMachineFamily.EMEKE_MEKAF_ADVANCED_FACTORY)) {
            EvolvedMekanismExtrasAdvancedClientScreens.register(event);
        }
        if (CompatMachineCatalog.hasAvailableFamily(CompatMachineFamily.EMEKE_MEKMM_FACTORY)) {
            EvolvedMekanismExtrasMoreMachineClientScreens.register(event);
        }
    }
}
