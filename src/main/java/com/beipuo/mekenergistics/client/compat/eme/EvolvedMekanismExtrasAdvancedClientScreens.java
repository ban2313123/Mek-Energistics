package com.beipuo.mekenergistics.client.compat.eme;

import com.beipuo.mekenergistics.registry.ModMenuTypes;
import io.github.masyumero.emextras.common.integration.mekaf.tile.factory.base.TileEntityEMExtraAdvancedFactoryBase;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/** Client screen binding for EMEKE's MEKAF-backed factory integration. */
public final class EvolvedMekanismExtrasAdvancedClientScreens {
    private EvolvedMekanismExtrasAdvancedClientScreens() {
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void register(RegisterMenuScreensEvent event) {
        event.register((MenuType) ModMenuTypes.ME_EM_EXTRA_ADVANCED_FACTORY.get(),
                (MenuScreens.ScreenConstructor) (menu, inv, title) ->
                        new io.github.masyumero.emextras.client.gui.machine.GuiEMExtraAdvancedFactory(
                                (MekanismTileContainer<TileEntityEMExtraAdvancedFactoryBase<?>>) (MekanismTileContainer<?>) menu,
                                inv, title));
    }
}
