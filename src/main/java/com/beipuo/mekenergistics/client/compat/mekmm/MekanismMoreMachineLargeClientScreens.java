package com.beipuo.mekenergistics.client.compat.mekmm;

import com.beipuo.mekenergistics.registry.ModMenuTypes;
import com.jerry.meklm.client.gui.machine.GuiLargeAntiprotonicNucleosynthesizer;
import com.jerry.meklm.client.gui.machine.GuiLargeChemicalInfuser;
import com.jerry.meklm.client.gui.machine.GuiLargeElectrolyticSeparator;
import com.jerry.meklm.client.gui.machine.GuiLargeRotaryCondensentrator;
import com.jerry.meklm.client.gui.machine.GuiLargeSolarNeutronActivator;
import com.jerry.meklm.common.tile.machine.TileEntityLargeAntiprotonicNucleosynthesizer;
import com.jerry.meklm.common.tile.machine.TileEntityLargeChemicalInfuser;
import com.jerry.meklm.common.tile.machine.TileEntityLargeElectrolyticSeparator;
import com.jerry.meklm.common.tile.machine.TileEntityLargeRotaryCondensentrator;
import com.jerry.meklm.common.tile.machine.TileEntityLargeSolarNeutronActivator;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/** Client screen bindings that preserve MekLM's large-machine controls and layout. */
public final class MekanismMoreMachineLargeClientScreens {
    private MekanismMoreMachineLargeClientScreens() {
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void register(RegisterMenuScreensEvent event) {
        event.register((MenuType) ModMenuTypes.ME_LARGE_ROTARY_CONDENSENTRATOR.get(),
                (MenuScreens.ScreenConstructor) (menu, inv, title) ->
                        new GuiLargeRotaryCondensentrator(
                                (MekanismTileContainer<TileEntityLargeRotaryCondensentrator>) (MekanismTileContainer<?>) menu,
                                inv, title));
        event.register((MenuType) ModMenuTypes.ME_LARGE_SOLAR_NEUTRON_ACTIVATOR.get(),
                (MenuScreens.ScreenConstructor) (menu, inv, title) ->
                        new GuiLargeSolarNeutronActivator(
                                (MekanismTileContainer<TileEntityLargeSolarNeutronActivator>) (MekanismTileContainer<?>) menu,
                                inv, title));
        event.register((MenuType) ModMenuTypes.ME_LARGE_ELECTROLYTIC_SEPARATOR.get(),
                (MenuScreens.ScreenConstructor) (menu, inv, title) ->
                        new GuiLargeElectrolyticSeparator(
                                (MekanismTileContainer<TileEntityLargeElectrolyticSeparator>) (MekanismTileContainer<?>) menu,
                                inv, title));
        event.register((MenuType) ModMenuTypes.ME_LARGE_CHEMICAL_INFUSER.get(),
                (MenuScreens.ScreenConstructor) (menu, inv, title) ->
                        new GuiLargeChemicalInfuser(
                                (MekanismTileContainer<TileEntityLargeChemicalInfuser>) (MekanismTileContainer<?>) menu,
                                inv, title));
        event.register((MenuType) ModMenuTypes.ME_LARGE_ANTIPROTONIC_NUCLEOSYNTHESIZER.get(),
                (MenuScreens.ScreenConstructor) (menu, inv, title) ->
                        new GuiLargeAntiprotonicNucleosynthesizer(
                                (MekanismTileContainer<TileEntityLargeAntiprotonicNucleosynthesizer>) (MekanismTileContainer<?>) menu,
                                inv, title));
    }
}
