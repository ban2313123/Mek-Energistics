package com.beipuo.mekenergistics.mixin;

import com.beipuo.mekenergistics.upgrade.AdvancedFactoryUpgradeAccess;
import com.jerry.mekextras.common.integration.mekaf.tile.factory.TileEntityExtraDissolvingFactory;
import java.util.List;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.inventory.IInventorySlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = TileEntityExtraDissolvingFactory.class, remap = false)
public abstract class MekExtrasAdvancedFactoryDissolvingPortMixin implements AdvancedFactoryUpgradeAccess {
    @Shadow(remap = false) public IChemicalTank chemicalTank;

    @Override public List<? extends IChemicalTank> meUpgradeChemicalInputs() { return List.of(this.chemicalTank); }
    @Override public List<? extends IInventorySlot> meUpgradeExtraItemInputs() {
        return List.of(((TileEntityExtraDissolvingFactoryAccessor) this).mekenergistics$getChemicalInputSlot());
    }
}
