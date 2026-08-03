package com.beipuo.mekenergistics.mixin;

import com.beipuo.mekenergistics.upgrade.AdvancedFactoryUpgradeAccess;
import java.util.List;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

@Pseudo
@Mixin(targets = {
        "com.jerry.mekaf.common.tile.factory.TileEntityPressurizedReactingFactory",
        "com.jerry.mekextras.common.integration.mekaf.tile.factory.TileEntityExtraPRCFactory",
        "io.github.masyumero.emextras.common.integration.mekaf.tile.factory.TileEntityEMExtraPRCFactory"
}, remap = false)
public abstract class AdvancedFactoryPressurizedReactingPortMixin implements AdvancedFactoryUpgradeAccess {
    @Shadow(remap = false) protected List<IInventorySlot> inputItemSlots;
    @Shadow(remap = false) protected List<IInventorySlot> outputItemSlots;
    @Shadow(remap = false) public BasicFluidTank inputFluidTank;
    @Shadow(remap = false) public IChemicalTank inputChemicalTank;
    @Shadow(remap = false) public IChemicalTank outputChemicalTank;

    @Override public List<? extends IInventorySlot> meUpgradeItemInputs() { return this.inputItemSlots; }
    @Override public List<? extends IChemicalTank> meUpgradeChemicalInputs() { return List.of(this.inputChemicalTank); }
    @Override public List<? extends IExtendedFluidTank> meUpgradeFluidInputs() { return List.of(this.inputFluidTank); }
    @Override public List<? extends IInventorySlot> meUpgradeItemOutputs() { return this.outputItemSlots; }
    @Override public List<? extends IChemicalTank> meUpgradeChemicalOutputs() { return List.of(this.outputChemicalTank); }
}
