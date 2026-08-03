package com.beipuo.mekenergistics.blockentity.api;

import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.security.IActionHost;
import appeng.api.stacks.KeyCounter;
import com.beipuo.mekenergistics.common.machine.MeMekanismMachine;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.BlockPos;

public interface MeAeSupportOwner extends ICraftingProvider, IActionHost {
    TileEntityMekanism getAeOwnerTile();

    MeMekanismMachine getMachine();

    default BlockPos getGridNodePosition() {
        return getAeOwnerTile().getBlockPos();
    }

    void saveChanges();

    boolean isSmartPatternMultiplicationEnabled();

    /** Maximum complete copies of the supplied one-craft input that can be accepted now. */
    long maxAcceptedPatternCopies(KeyCounter[] oneCraftInputs);
}
