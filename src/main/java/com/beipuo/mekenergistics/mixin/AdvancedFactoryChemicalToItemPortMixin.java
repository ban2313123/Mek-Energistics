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
        "com.jerry.mekaf.common.tile.factory.base.TileEntityChemicalToItemFactory",
        "com.jerry.mekextras.common.integration.mekaf.tile.factory.base.TileEntityExtraChemicalToItemFactory",
        "io.github.masyumero.emextras.common.integration.mekaf.tile.factory.base.TileEntityEMExtraChemicalToItemFactory"
}, remap = false)
public abstract class AdvancedFactoryChemicalToItemPortMixin implements AdvancedFactoryUpgradeAccess {
    @Shadow(remap = false) public List<IChemicalTank> inputChemicalTanks;
    @Shadow(remap = false) public List<IInventorySlot> outputItemSlots;

    @Override public List<? extends IChemicalTank> meUpgradeChemicalInputs() { return this.inputChemicalTanks; }
    @Override public List<? extends IInventorySlot> meUpgradeItemOutputs() { return this.outputItemSlots; }
}
