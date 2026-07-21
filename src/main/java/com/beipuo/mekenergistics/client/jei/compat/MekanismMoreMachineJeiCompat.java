package com.beipuo.mekenergistics.client.jei.compat;

import com.beipuo.mekenergistics.client.jei.MekEnergisticsJeiPlugin;
import com.jerry.mekmm.client.recipe_viewer.MMRecipeViewerRecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;

public final class MekanismMoreMachineJeiCompat {
    private MekanismMoreMachineJeiCompat() {
    }

    public static void registerCatalysts(IRecipeCatalystRegistration registration) {
        MekEnergisticsJeiPlugin.registerMoreMachineFactories(
                registration, MMRecipeViewerRecipeType.RECYCLER, "recycling");
        MekEnergisticsJeiPlugin.registerMoreMachineFactories(
                registration, MMRecipeViewerRecipeType.PLANTING_STATION, "planting");
        MekEnergisticsJeiPlugin.registerMoreMachineFactories(
                registration, MMRecipeViewerRecipeType.STAMPING, "stamping");
        MekEnergisticsJeiPlugin.registerMoreMachineFactories(
                registration, MMRecipeViewerRecipeType.LATHE, "lathing");
        MekEnergisticsJeiPlugin.registerMoreMachineFactories(
                registration, MMRecipeViewerRecipeType.ROLLING_MILL, "rolling_mill");
        MekEnergisticsJeiPlugin.registerMoreMachineFactories(
                registration, MMRecipeViewerRecipeType.REPLICATOR, "replicating");
    }
}
