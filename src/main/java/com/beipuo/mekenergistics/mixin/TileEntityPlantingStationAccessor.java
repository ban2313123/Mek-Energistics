package com.beipuo.mekenergistics.mixin;

import com.jerry.mekmm.common.tile.machine.TileEntityPlantingStation;
import mekanism.common.inventory.slot.OutputInventorySlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = TileEntityPlantingStation.class, remap = false)
public interface TileEntityPlantingStationAccessor {
    @Accessor("mainOutputSlot")
    OutputInventorySlot mekenergistics$getMainOutputSlot();

    @Accessor("secondaryOutputSlot")
    OutputInventorySlot mekenergistics$getSecondaryOutputSlot();
}
