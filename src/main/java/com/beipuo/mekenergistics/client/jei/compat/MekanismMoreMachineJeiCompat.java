package com.beipuo.mekenergistics.client.jei.compat;

import com.beipuo.mekenergistics.client.jei.MekEnergisticsJeiPlugin;
import com.beipuo.mekenergistics.common.machine.MeMekanismMachine;
import com.jerry.mekmm.client.recipe_viewer.MoreMachineRecipeViewerRecipeType;
import mekanism.client.recipe_viewer.type.RecipeViewerRecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;

public final class MekanismMoreMachineJeiCompat {
    private MekanismMoreMachineJeiCompat() {
    }

    public static void registerCatalysts(IRecipeCatalystRegistration registration) {
        MekEnergisticsJeiPlugin.registerMachines(registration, RecipeViewerRecipeType.CONDENSENTRATING,
                MeMekanismMachine.LARGE_ROTARY_CONDENSENTRATOR);
        MekEnergisticsJeiPlugin.registerMachines(registration, RecipeViewerRecipeType.DECONDENSENTRATING,
                MeMekanismMachine.LARGE_ROTARY_CONDENSENTRATOR);
        MekEnergisticsJeiPlugin.registerMachines(registration, RecipeViewerRecipeType.ACTIVATING,
                MeMekanismMachine.LARGE_SOLAR_NEUTRON_ACTIVATOR);
        MekEnergisticsJeiPlugin.registerMachines(registration, RecipeViewerRecipeType.SEPARATING,
                MeMekanismMachine.LARGE_ELECTROLYTIC_SEPARATOR);
        MekEnergisticsJeiPlugin.registerMachines(registration, RecipeViewerRecipeType.CHEMICAL_INFUSING,
                MeMekanismMachine.LARGE_CHEMICAL_INFUSER);
        MekEnergisticsJeiPlugin.registerMachines(registration, RecipeViewerRecipeType.NUCLEOSYNTHESIZING,
                MeMekanismMachine.LARGE_ANTIPROTONIC_NUCLEOSYNTHESIZER);
        MekEnergisticsJeiPlugin.registerMoreMachineFactories(
                registration, MoreMachineRecipeViewerRecipeType.RECYCLER, "recycling");
        MekEnergisticsJeiPlugin.registerMoreMachineFactories(
                registration, MoreMachineRecipeViewerRecipeType.PLANTING_STATION, "planting");
        MekEnergisticsJeiPlugin.registerMoreMachineFactories(
                registration, MoreMachineRecipeViewerRecipeType.STAMPING, "stamping");
        MekEnergisticsJeiPlugin.registerMoreMachineFactories(
                registration, MoreMachineRecipeViewerRecipeType.LATHE, "lathing");
        MekEnergisticsJeiPlugin.registerMoreMachineFactories(
                registration, MoreMachineRecipeViewerRecipeType.ROLLING_MILL, "rolling_mill");
        MekEnergisticsJeiPlugin.registerMoreMachineFactories(
                registration, MoreMachineRecipeViewerRecipeType.REPLICATOR, "replicating");
    }
}
