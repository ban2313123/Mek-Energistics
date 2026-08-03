package com.beipuo.mekenergistics.mixin;

import com.jerry.mekextras.common.integration.mekaf.tile.factory.TileEntityExtraDissolvingFactory;
import mekanism.common.inventory.slot.chemical.ChemicalInventorySlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = TileEntityExtraDissolvingFactory.class, remap = false)
public interface TileEntityExtraDissolvingFactoryAccessor {
    @Accessor(value = "chemicalInputSlot", remap = false)
    ChemicalInventorySlot mekenergistics$getChemicalInputSlot();
}
