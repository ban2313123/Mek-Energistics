package com.beipuo.mekenergistics.mixin;

import io.github.masyumero.emextras.common.integration.mekaf.tile.factory.TileEntityEMExtraDissolvingFactory;
import mekanism.common.inventory.slot.chemical.ChemicalInventorySlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = TileEntityEMExtraDissolvingFactory.class, remap = false)
public interface TileEntityEMExtraDissolvingFactoryAccessor {
    @Accessor(value = "chemicalInputSlot", remap = false)
    ChemicalInventorySlot mekenergistics$getChemicalInputSlot();
}
