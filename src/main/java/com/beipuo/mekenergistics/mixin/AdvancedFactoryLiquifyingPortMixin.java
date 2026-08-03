package com.beipuo.mekenergistics.mixin;

import com.beipuo.mekenergistics.upgrade.AdvancedFactoryUpgradeAccess;
import java.util.List;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.inventory.IInventorySlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

@Pseudo
@Mixin(targets = {
        "com.jerry.mekaf.common.tile.factory.TileEntityLiquifyingFactory",
        "com.jerry.mekextras.common.integration.mekaf.tile.factory.TileEntityExtraLiquifyingFactory",
        "io.github.masyumero.emextras.common.integration.mekaf.tile.factory.TileEntityEMExtraLiquifyingFactory"
}, remap = false)
public abstract class AdvancedFactoryLiquifyingPortMixin implements AdvancedFactoryUpgradeAccess {
    @Shadow(remap = false) protected List<IInventorySlot> inputItemSlots;
    @Shadow(remap = false) protected List<IInventorySlot> outputItemSlots;
    @Shadow(remap = false) public IExtendedFluidTank fluidTank;

    @Override public List<? extends IInventorySlot> meUpgradeItemInputs() { return this.inputItemSlots; }
    @Override public List<? extends IInventorySlot> meUpgradeItemOutputs() { return this.outputItemSlots; }
    @Override public List<? extends IExtendedFluidTank> meUpgradeFluidOutputs() { return List.of(this.fluidTank); }
}
