package com.beipuo.mekenergistics.mixin;

import com.jerry.mekmm.common.tile.machine.TileEntityReplicator;
import mekanism.common.inventory.slot.OutputInventorySlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = TileEntityReplicator.class, remap = false)
public interface TileEntityReplicatorAccessor {
    @Accessor("outputSlot")
    OutputInventorySlot mekenergistics$getOutputSlot();
}
