package com.beipuo.mekenergistics.client.compat.eme;

import com.beipuo.mekenergistics.compat.eme.EvolvedMekanismMachineMenuTypes;
import fr.iglee42.evolvedmekanism.client.gui.GuiMelter;
import fr.iglee42.evolvedmekanism.tiles.machine.TileEntityMelter;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.inventory.MenuType;

/** Client screen bindings for Evolved Mekanism machine variants. */
public final class EvolvedMekanismMachineClientScreens {
    private EvolvedMekanismMachineClientScreens() {
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void register(RegisterMenuScreensEvent event) {
        event.register((MenuType) EvolvedMekanismMachineMenuTypes.ME_THERMALIZER.get(),
                (MenuScreens.ScreenConstructor) (menu, inv, title) -> new GuiMelter(
                        (MekanismTileContainer<TileEntityMelter>) (MekanismTileContainer<?>) menu,
                        inv, title));
    }
}
