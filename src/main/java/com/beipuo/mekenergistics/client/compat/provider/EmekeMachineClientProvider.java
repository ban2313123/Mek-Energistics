package com.beipuo.mekenergistics.client.compat.provider;

import com.beipuo.mekenergistics.client.compat.eme.EvolvedMekanismExtrasAdvancedClientScreens;
import com.beipuo.mekenergistics.client.compat.eme.EvolvedMekanismExtrasClientScreens;
import com.beipuo.mekenergistics.client.compat.eme.EvolvedMekanismExtrasMoreMachineClientScreens;
import com.beipuo.mekenergistics.compat.catalog.CompatMachineCatalog;
import com.beipuo.mekenergistics.compat.catalog.CompatMod;
import com.beipuo.mekenergistics.compat.catalog.CompatRequirement;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public final class EmekeMachineClientProvider implements CompatMachineClientProvider {
    @Override
    public void registerScreens(RegisterMenuScreensEvent event) {
        EvolvedMekanismExtrasClientScreens.register(event);
        if (hasRequirement(CompatRequirement.EMEKE_ADVANCED_FACTORIES)) {
            EvolvedMekanismExtrasAdvancedClientScreens.register(event);
        }
        if (hasRequirement(CompatRequirement.EMEKE_MEKMM_FACTORIES)) {
            EvolvedMekanismExtrasMoreMachineClientScreens.register(event);
        }
    }

    private static boolean hasRequirement(CompatRequirement requirement) {
        return CompatMachineCatalog.available()
                .filter(spec -> spec.provider() == CompatMod.EMEKE)
                .anyMatch(spec -> spec.requirements().contains(requirement));
    }
}
