package com.beipuo.mekenergistics.blockentity.api;

import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.security.IActionHost;
import com.beipuo.mekenergistics.common.machine.MeMekanismMachine;
import mekanism.common.tile.base.TileEntityMekanism;

public interface MeAeSupportOwner extends ICraftingProvider, IActionHost {
    TileEntityMekanism getAeOwnerTile();

    MeMekanismMachine getMachine();

    void saveChanges();

    boolean isSmartPatternMultiplicationEnabled();
}
