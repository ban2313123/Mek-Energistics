package com.beipuo.mekenergistics.client.compat.eme;

import com.beipuo.mekenergistics.compat.eme.EvolvedMekanismMachineMenuTypes;
import fr.iglee42.evolvedmekanism.client.gui.GuiAlloyer;
import fr.iglee42.evolvedmekanism.client.gui.GuiChemixer;
import fr.iglee42.evolvedmekanism.client.gui.GuiMelter;
import fr.iglee42.evolvedmekanism.client.gui.GuiSolidifier;
import fr.iglee42.evolvedmekanism.tiles.machine.TileEntityAlloyer;
import fr.iglee42.evolvedmekanism.tiles.machine.TileEntityChemixer;
import fr.iglee42.evolvedmekanism.tiles.machine.TileEntityMelter;
import fr.iglee42.evolvedmekanism.tiles.machine.TileEntitySolidifier;
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
        event.register((MenuType) EvolvedMekanismMachineMenuTypes.ME_ALLOYER.get(),
                (MenuScreens.ScreenConstructor) (menu, inv, title) -> new GuiAlloyer(
                        (MekanismTileContainer<TileEntityAlloyer>) (MekanismTileContainer<?>) menu,
                        inv, title));
        event.register((MenuType) EvolvedMekanismMachineMenuTypes.ME_CHEMIXER.get(),
                (MenuScreens.ScreenConstructor) (menu, inv, title) -> new GuiChemixer(
                        (MekanismTileContainer<TileEntityChemixer>) (MekanismTileContainer<?>) menu,
                        inv, title));
        event.register((MenuType) EvolvedMekanismMachineMenuTypes.ME_SOLIDIFIER.get(),
                (MenuScreens.ScreenConstructor) (menu, inv, title) -> new GuiSolidifier(
                        (MekanismTileContainer<TileEntitySolidifier>) (MekanismTileContainer<?>) menu,
                        inv, title));
        event.register((MenuType) EvolvedMekanismMachineMenuTypes.ME_THERMALIZER.get(),
                (MenuScreens.ScreenConstructor) (menu, inv, title) -> new GuiMelter(
                        (MekanismTileContainer<TileEntityMelter>) (MekanismTileContainer<?>) menu,
                        inv, title));
    }
}
