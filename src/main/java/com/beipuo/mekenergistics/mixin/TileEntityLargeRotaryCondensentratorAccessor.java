package com.beipuo.mekenergistics.mixin;

import com.jerry.meklm.common.tile.machine.TileEntityLargeRotaryCondensentrator;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = TileEntityLargeRotaryCondensentrator.class, remap = false)
public interface TileEntityLargeRotaryCondensentratorAccessor {
    @Accessor("energyContainer")
    void mekenergistics$setEnergyContainer(
            MachineEnergyContainer<TileEntityLargeRotaryCondensentrator> energyContainer);
}
