package com.beipuo.mekenergistics.mixin;

import mekanism.common.inventory.slot.chemical.ChemicalInventorySlot;
import mekanism.common.tile.factory.TileEntityItemStackChemicalToItemStackFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = TileEntityItemStackChemicalToItemStackFactory.class, remap = false)
public interface TileEntityItemStackChemicalToItemStackFactoryAccessor {
    @Accessor("extraSlot")
    ChemicalInventorySlot mekenergistics$getExtraSlot();
}
