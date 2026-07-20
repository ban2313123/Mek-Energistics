package com.beipuo.mekenergistics.mixin;

import com.jerry.meklm.common.tile.machine.TileEntityLargeAntiprotonicNucleosynthesizer;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.inventory.slot.chemical.ChemicalInventorySlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = TileEntityLargeAntiprotonicNucleosynthesizer.class, remap = false)
public interface TileEntityLargeAntiprotonicNucleosynthesizerAccessor {
    @Accessor("energyContainer")
    void mekenergistics$setEnergyContainer(MachineEnergyContainer<TileEntityLargeAntiprotonicNucleosynthesizer> energyContainer);
    @Accessor("inputSlot") InputInventorySlot mekenergistics$getInputSlot();
    @Accessor("gasInputSlot") ChemicalInventorySlot mekenergistics$getGasInputSlot();
    @Accessor("outputSlot") OutputInventorySlot mekenergistics$getOutputSlot();
}
