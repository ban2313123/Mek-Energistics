package com.beipuo.mekenergistics.mixin;

import com.beipuo.mekenergistics.upgrade.AdvancedFactoryUpgradeAccess;
import io.github.masyumero.emextras.common.integration.mekaf.tile.factory.TileEntityEMExtraDissolvingFactory;
import java.util.List;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.inventory.IInventorySlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = TileEntityEMExtraDissolvingFactory.class, remap = false)
public abstract class EMExtrasAdvancedFactoryDissolvingPortMixin implements AdvancedFactoryUpgradeAccess {
    @Shadow(remap = false) public IChemicalTank injectTank;

    @Override public List<? extends IChemicalTank> meUpgradeChemicalInputs() { return List.of(this.injectTank); }
    @Override public List<? extends IInventorySlot> meUpgradeExtraItemInputs() {
        return List.of(((TileEntityEMExtraDissolvingFactoryAccessor) this).mekenergistics$getChemicalInputSlot());
    }
}
