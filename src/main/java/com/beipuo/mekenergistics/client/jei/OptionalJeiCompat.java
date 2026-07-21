package com.beipuo.mekenergistics.client.jei;

import com.beipuo.mekenergistics.MekEnergistics;
import com.beipuo.mekenergistics.compat.OptionalCompatClasses;
import com.beipuo.mekenergistics.compat.catalog.CompatMachineCatalog;
import com.beipuo.mekenergistics.compat.catalog.CompatMachineFamily;
import java.util.List;
import java.util.function.BooleanSupplier;
import mezz.jei.api.registration.IRecipeCatalystRegistration;

/** Loads optional JEI integrations only after their catalog family and classes are available. */
final class OptionalJeiCompat {
    private static final List<Entry> ENTRIES = List.of(
            new Entry(
                    CompatMachineFamily.MEKMM_MACHINE,
                    OptionalCompatClasses::hasMekmm,
                    "com.beipuo.mekenergistics.client.jei.compat.MekanismMoreMachineJeiCompat"),
            new Entry(
                    CompatMachineFamily.EMEK_MACHINE,
                    OptionalCompatClasses::hasEvolvedMekanism,
                    "com.beipuo.mekenergistics.client.jei.compat.EvolvedMekanismJeiCompat"));

    private OptionalJeiCompat() {
    }

    static void registerCatalysts(IRecipeCatalystRegistration registration) {
        for (Entry entry : ENTRIES) {
            if (isEnabled(entry.modAvailable().getAsBoolean(),
                    CompatMachineCatalog.hasAvailableFamily(entry.family()))) {
                invoke(entry, registration);
            }
        }
    }

    static boolean isEnabled(boolean modAvailable, boolean familyAvailable) {
        return modAvailable && familyAvailable;
    }

    private static void invoke(Entry entry, IRecipeCatalystRegistration registration) {
        try {
            Class.forName(entry.className(), true, OptionalJeiCompat.class.getClassLoader())
                    .getMethod("registerCatalysts", IRecipeCatalystRegistration.class)
                    .invoke(null, registration);
        } catch (ReflectiveOperationException | LinkageError exception) {
            MekEnergistics.LOGGER.warn("Unable to load optional JEI integration {} for {}",
                    entry.className(), entry.family(), exception);
        }
    }

    private record Entry(
            CompatMachineFamily family, BooleanSupplier modAvailable, String className) {
    }
}
