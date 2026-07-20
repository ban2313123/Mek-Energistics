package com.beipuo.mekenergistics.mixin;

import com.jerry.mekmm.common.tile.machine.TileEntityChemicalReplicator;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = TileEntityChemicalReplicator.class, remap = false)
public interface TileEntityChemicalReplicatorAccessor {
    @Accessor("energyContainer")
    void mekenergistics$setEnergyContainer(MachineEnergyContainer<TileEntityChemicalReplicator> energyContainer);
}
