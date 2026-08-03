package com.beipuo.mekenergistics.mixin;

import io.github.masyumero.emextras.common.tile.factory.TileEntityEMExtraAlloyingFactory;
import fr.iglee42.evolvedmekanism.tiles.LimitedInputInventorySlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = TileEntityEMExtraAlloyingFactory.class, remap = false)
public interface EmExtrasAlloyingFactoryAccessor {
    @Accessor("secondExtraSlot")
    LimitedInputInventorySlot mekenergistics$getSecondExtraSlot();
}
