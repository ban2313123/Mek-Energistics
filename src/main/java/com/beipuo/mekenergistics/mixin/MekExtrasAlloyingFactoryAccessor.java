package com.beipuo.mekenergistics.mixin;

import com.jerry.mekextras.common.tile.factory.TileEntityExtraAlloyingFactory;
import mekanism.api.inventory.IInventorySlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = TileEntityExtraAlloyingFactory.class, remap = false)
public interface MekExtrasAlloyingFactoryAccessor {
    @Accessor("secondExtraSlot")
    IInventorySlot mekenergistics$getSecondExtraSlot();
}
