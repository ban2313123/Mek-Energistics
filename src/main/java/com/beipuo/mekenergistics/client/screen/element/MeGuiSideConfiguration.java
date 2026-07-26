package com.beipuo.mekenergistics.client.screen.element;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.window.GuiSideConfiguration;
import mekanism.common.inventory.container.SelectedWindowData;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ISideConfiguration;

public class MeGuiSideConfiguration<TILE extends TileEntityMekanism & ISideConfiguration> extends GuiSideConfiguration<TILE> {

    public MeGuiSideConfiguration(IGuiWrapper gui, int x, int y, TILE tile, SelectedWindowData windowData) {
        super(gui, x, y, tile, windowData);
    }
}
