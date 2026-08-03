package com.beipuo.mekenergistics.mixin;

import com.beipuo.mekenergistics.upgrade.AdvancedFactoryUpgradeAccess;
import java.util.List;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.inventory.IInventorySlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

@Pseudo
@Mixin(targets = {
        "com.jerry.mekaf.common.tile.factory.base.TileEntityItemToChemicalFactory",
        "com.jerry.mekextras.common.integration.mekaf.tile.factory.base.TileEntityExtraItemToChemicalFactory",
        "io.github.masyumero.emextras.common.integration.mekaf.tile.factory.base.TileEntityEMExtraItemToChemicalFactory"
}, remap = false)
public abstract class AdvancedFactoryItemToChemicalPortMixin implements AdvancedFactoryUpgradeAccess {
    @Shadow(remap = false) protected List<IInventorySlot> inputItemSlots;
    @Shadow(remap = false) public List<IChemicalTank> outputChemicalTanks;

    @Override public List<? extends IInventorySlot> meUpgradeItemInputs() { return this.inputItemSlots; }
    @Override public List<? extends IChemicalTank> meUpgradeChemicalOutputs() { return this.outputChemicalTanks; }
}
