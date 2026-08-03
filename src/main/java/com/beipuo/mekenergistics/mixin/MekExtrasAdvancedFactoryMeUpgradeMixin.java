package com.beipuo.mekenergistics.mixin;

import com.beipuo.mekenergistics.blockentity.api.AeOutputMode;
import com.beipuo.mekenergistics.upgrade.AdvancedFactoryUpgradeAccess;
import com.beipuo.mekenergistics.upgrade.MeUpgradeRecipeMachineRuntime;
import com.jerry.mekextras.common.integration.mekaf.tile.factory.base.TileEntityExtraAdvancedFactoryBase;
import mekanism.api.IContentsListener;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.recipe.lookup.monitor.FactoryRecipeCacheLookupMonitor;
import mekanism.common.tile.base.TileEntityMekanism;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TileEntityExtraAdvancedFactoryBase.class, remap = false)
public abstract class MekExtrasAdvancedFactoryMeUpgradeMixin implements AdvancedFactoryUpgradeAccess {
    @Shadow protected FactoryRecipeCacheLookupMonitor<?>[] recipeCacheLookupMonitors;
    @Unique private MeUpgradeRecipeMachineRuntime mekenergistics$runtime;

    @Override public TileEntityMekanism meUpgradeTile() { return (TileEntityMekanism) (Object) this; }
    @Override public mekanism.common.capabilities.energy.MachineEnergyContainer<?> meUpgradeEnergyContainer() {
        return ((TileEntityExtraAdvancedFactoryBase<?>) (Object) this).getEnergyContainer();
    }

    @Override
    public MeUpgradeRecipeMachineRuntime getOrCreateMeUpgradeRuntime() {
        if (this.mekenergistics$runtime == null) {
            this.mekenergistics$runtime = new MeUpgradeRecipeMachineRuntime(meUpgradeTile(), AeOutputMode.BOTH);
        }
        return this.mekenergistics$runtime;
    }

    @Override public MeUpgradeRecipeMachineRuntime getExistingMeUpgradeRuntime() { return this.mekenergistics$runtime; }

    @Override
    public com.beipuo.mekenergistics.blockentity.support.AbstractMeAeSupport<?> getRecipeAeSupport() {
        return (Object) this instanceof com.beipuo.mekenergistics.blockentity.api.MeFactoryIoOwner legacy
                ? legacy.getAeSupport() : AdvancedFactoryUpgradeAccess.super.getRecipeAeSupport();
    }

    @Override
    public AeOutputMode getAeOutputMode() {
        return (Object) this instanceof com.beipuo.mekenergistics.blockentity.api.MeFactoryIoOwner legacy
                ? legacy.getAeSupport().getAeOutputMode() : AdvancedFactoryUpgradeAccess.super.getAeOutputMode();
    }

    @Override
    public void cycleAeOutputMode() {
        if ((Object) this instanceof com.beipuo.mekenergistics.blockentity.api.MeFactoryIoOwner legacy) {
            legacy.getAeSupport().cycleAeOutputMode();
        } else {
            AdvancedFactoryUpgradeAccess.super.cycleAeOutputMode();
        }
    }

    @Override
    public com.beipuo.mekenergistics.blockentity.support.io.MeInputLayout getPatternInputLayout() {
        return (Object) this instanceof com.beipuo.mekenergistics.blockentity.api.MeFactoryIoOwner legacy
                ? com.beipuo.mekenergistics.blockentity.api.MeFactoryIoOwner.factoryPatternInputLayout(legacy)
                : AdvancedFactoryUpgradeAccess.super.getPatternInputLayout();
    }

    @Override
    public java.util.List<? extends com.beipuo.mekenergistics.blockentity.support.io.MeOutputPort> getPatternOutputPorts() {
        return (Object) this instanceof com.beipuo.mekenergistics.blockentity.api.MeFactoryIoOwner legacy
                ? com.beipuo.mekenergistics.blockentity.api.MeFactoryIoOwner.factoryPatternOutputPorts(legacy)
                : AdvancedFactoryUpgradeAccess.super.getPatternOutputPorts();
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
                if (monitor != null) monitor.unpause();
            }
        }
    }

    @Inject(method = "onUpdateServer", at = @At("RETURN"), cancellable = true)
    private void mekenergistics$processPatternIo(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(processMePatternIo(cir.getReturnValue()));
    }
}
