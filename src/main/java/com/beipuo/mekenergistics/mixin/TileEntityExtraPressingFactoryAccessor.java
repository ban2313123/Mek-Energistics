package com.beipuo.mekenergistics.mixin;

import com.jerry.mekextras.common.integration.mekmm.tile.factory.TileEntityExtraPressingFactory;
import com.jerry.mekextras.common.inventory.slot.StackableInputInventorySlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Exposes the two dedicated inputs of Extras' triple-input pressing factory. */
@Mixin(value = TileEntityExtraPressingFactory.class, remap = false)
public interface TileEntityExtraPressingFactoryAccessor {
    @Accessor("secondarySlot")
    StackableInputInventorySlot mekenergistics$getSecondarySlot();

    @Accessor("tertiarySlot")
    StackableInputInventorySlot mekenergistics$getTertiarySlot();
}
