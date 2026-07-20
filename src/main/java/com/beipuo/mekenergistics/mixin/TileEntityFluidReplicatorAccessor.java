package com.beipuo.mekenergistics.mixin;

import com.jerry.mekmm.common.tile.machine.TileEntityFluidReplicator;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.inventory.slot.chemical.ChemicalInventorySlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = TileEntityFluidReplicator.class, remap = false)
public interface TileEntityFluidReplicatorAccessor {
    @Accessor("energyContainer")
    void mekenergistics$setEnergyContainer(MachineEnergyContainer<TileEntityFluidReplicator> energyContainer);

    @Accessor("uuSlot")
    ChemicalInventorySlot mekenergistics$getUuSlot();
}
