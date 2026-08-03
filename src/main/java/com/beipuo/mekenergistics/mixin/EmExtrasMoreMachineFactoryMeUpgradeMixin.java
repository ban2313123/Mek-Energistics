package com.beipuo.mekenergistics.mixin;

import com.beipuo.mekenergistics.blockentity.api.AeOutputMode;
import com.beipuo.mekenergistics.upgrade.BasicFactoryUpgradeAccess;
import com.beipuo.mekenergistics.upgrade.MeUpgradeRecipeMachineRuntime;
import io.github.masyumero.emextras.common.integration.mekmm.tile.factory.TileEntityEMExtraMoreMachineFactory;
import io.github.masyumero.emextras.common.integration.mekmm.tile.factory.TileEntityEMExtraPlantingFactory;
import io.github.masyumero.emextras.common.integration.mekmm.tile.factory.TileEntityEMExtraReplicatingFactory;
import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.recipe.lookup.monitor.FactoryRecipeCacheLookupMonitor;
import mekanism.common.tile.base.TileEntityMekanism;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TileEntityEMExtraMoreMachineFactory.class, remap = false)
public abstract class EmExtrasMoreMachineFactoryMeUpgradeMixin implements BasicFactoryUpgradeAccess {
    @Shadow protected List<IInventorySlot> inputSlots;
    @Shadow protected List<IInventorySlot> outputSlots;
    @Shadow protected FactoryRecipeCacheLookupMonitor<?>[] recipeCacheLookupMonitors;
    @Shadow protected abstract IInventorySlot getExtraSlot();

    @Unique private MeUpgradeRecipeMachineRuntime mekenergistics$runtime;

    @Override
    public TileEntityMekanism meUpgradeTile() {
        return (TileEntityMekanism) (Object) this;
    }

    @Override
    public MeUpgradeRecipeMachineRuntime getOrCreateMeUpgradeRuntime() {
        if (this.mekenergistics$runtime == null) {
            this.mekenergistics$runtime = new MeUpgradeRecipeMachineRuntime(meUpgradeTile(), AeOutputMode.BOTH);
        }
        return this.mekenergistics$runtime;
    }

    @Override
    public MeUpgradeRecipeMachineRuntime getExistingMeUpgradeRuntime() {
        return this.mekenergistics$runtime;
    }

    @Override
    public com.beipuo.mekenergistics.blockentity.support.AbstractMeAeSupport<?> getRecipeAeSupport() {
        return (Object) this instanceof com.beipuo.mekenergistics.blockentity.api.MeFactoryIoOwner legacy
                ? legacy.getAeSupport() : BasicFactoryUpgradeAccess.super.getRecipeAeSupport();
    }

    @Override
    public AeOutputMode getAeOutputMode() {
        return (Object) this instanceof com.beipuo.mekenergistics.blockentity.api.MeFactoryIoOwner legacy
                ? legacy.getAeSupport().getAeOutputMode() : BasicFactoryUpgradeAccess.super.getAeOutputMode();
    }

    @Override
    public void cycleAeOutputMode() {
        if ((Object) this instanceof com.beipuo.mekenergistics.blockentity.api.MeFactoryIoOwner legacy) {
            legacy.getAeSupport().cycleAeOutputMode();
        } else {
            BasicFactoryUpgradeAccess.super.cycleAeOutputMode();
        }
    }

    @Override public List<IInventorySlot> meUpgradeInputSlots() { return this.inputSlots; }
    @Override public List<IInventorySlot> meUpgradeOutputSlots() { return this.outputSlots; }
    @Override public IInventorySlot meUpgradeExtraSlot() { return getExtraSlot(); }

    @Override
    public IChemicalTank meUpgradeChemicalTank() {
        if ((Object) this instanceof TileEntityEMExtraPlantingFactory planting) {
            return planting.getChemicalTank();
        }
        if ((Object) this instanceof TileEntityEMExtraReplicatingFactory replicating) {
            return replicating.getChemicalTank();
        }
        return null;
    }

    @Override
    public com.beipuo.mekenergistics.blockentity.support.io.MeInputLayout getPatternInputLayout() {
        return (Object) this instanceof com.beipuo.mekenergistics.blockentity.api.MeFactoryIoOwner legacy
                ? com.beipuo.mekenergistics.blockentity.api.MeFactoryIoOwner.factoryPatternInputLayout(legacy)
                : BasicFactoryUpgradeAccess.super.getPatternInputLayout();
    }

    @Override
    public List<? extends com.beipuo.mekenergistics.blockentity.support.io.MeOutputPort> getPatternOutputPorts() {
        return (Object) this instanceof com.beipuo.mekenergistics.blockentity.api.MeFactoryIoOwner legacy
                ? com.beipuo.mekenergistics.blockentity.api.MeFactoryIoOwner.factoryPatternOutputPorts(legacy)
                : BasicFactoryUpgradeAccess.super.getPatternOutputPorts();
    }

    @Inject(method = "getInitialInventory", at = @At("RETURN"), cancellable = true)
    private void mekenergistics$addPatternSlots(IContentsListener listener,
            CallbackInfoReturnable<IInventorySlotHolder> cir) {
        cir.setReturnValue(addMePatternSlots(cir.getReturnValue(), this::mekenergistics$unpauseRecipes));
    }

    @Unique
    private void mekenergistics$unpauseRecipes() {
        if (this.recipeCacheLookupMonitors != null) {
            for (FactoryRecipeCacheLookupMonitor<?> monitor : this.recipeCacheLookupMonitors) {
                if (monitor != null) {
                    monitor.unpause();
                }
            }
        }
    }

    @Inject(method = "onUpdateServer", at = @At("RETURN"), cancellable = true)
    private void mekenergistics$processPatternIo(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(processMePatternIo(cir.getReturnValue()));
    }
}
