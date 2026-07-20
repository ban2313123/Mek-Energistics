package com.beipuo.mekenergistics.client.compat.mekmm;

import com.beipuo.mekenergistics.blockentity.compat.mekmm.machine.MeLargeAntiprotonicNucleosynthesizerBlockEntity;
import com.beipuo.mekenergistics.client.screen.machine.MeGuiLargeAntiprotonicNucleosynthesizer;
import com.beipuo.mekenergistics.registry.ModMenuTypes;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/** Client screen binding for MekLM's optional large antiprotonic machine. */
public final class MekanismMoreMachineLargeClientScreens {
    private MekanismMoreMachineLargeClientScreens() {
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void register(RegisterMenuScreensEvent event) {
        event.register((MenuType) ModMenuTypes.ME_LARGE_ANTIPROTONIC_NUCLEOSYNTHESIZER.get(),
                (MenuScreens.ScreenConstructor) (menu, inv, title) ->
                        new MeGuiLargeAntiprotonicNucleosynthesizer(
                                (MekanismTileContainer<MeLargeAntiprotonicNucleosynthesizerBlockEntity>) (MekanismTileContainer<?>) menu,
                                inv, title));
    }
}
