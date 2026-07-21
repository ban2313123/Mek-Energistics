package com.beipuo.mekenergistics.mixin;

import com.jerry.meklm.common.tile.machine.TileEntityLargeChemicalInfuser;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = TileEntityLargeChemicalInfuser.class, remap = false)
public interface TileEntityLargeChemicalInfuserAccessor {
    @Accessor("energyContainer")
    void mekenergistics$setEnergyContainer(
            MachineEnergyContainer<TileEntityLargeChemicalInfuser> energyContainer);
}
