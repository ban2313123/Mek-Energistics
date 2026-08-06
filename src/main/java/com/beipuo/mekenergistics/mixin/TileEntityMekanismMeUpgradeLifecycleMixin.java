package com.beipuo.mekenergistics.mixin;

import com.beipuo.mekenergistics.blockentity.api.MeUpgradeableMachine;
import com.beipuo.mekenergistics.upgrade.MePatternProviderUpgrade;
import java.util.HashSet;
import java.util.Set;
import mekanism.api.Upgrade;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeUpgradeSupport;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = TileEntityMekanism.class, remap = false)
public abstract class TileEntityMekanismMeUpgradeLifecycleMixin {
    @Inject(method = "supportsUpgrades", at = @At("RETURN"), cancellable = true)
    private void mekenergistics$enableSyntheticUpgradeSupport(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue() && mekenergistics$isSyntheticUpgradeTarget()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getSupportedUpgrade", at = @At("HEAD"), cancellable = true)
    private void mekenergistics$provideSyntheticUpgradeSet(CallbackInfoReturnable<Set<Upgrade>> cir) {
        if (mekenergistics$isSyntheticUpgradeTarget()) {
            cir.setReturnValue(Set.of(MePatternProviderUpgrade.get()));
        }
    }

    @Inject(method = "getSupportedUpgrade", at = @At("RETURN"), cancellable = true)
    private void mekenergistics$supportMeUpgrade(CallbackInfoReturnable<Set<Upgrade>> cir) {
        if (mekenergistics$isSyntheticUpgradeTarget()) {
            return;
        }
        if ((Object) this instanceof MeUpgradeableMachine machine && machine.isMeUpgradeTarget()) {
            Set<Upgrade> upgrades = new HashSet<>(cir.getReturnValue());
            upgrades.add(MePatternProviderUpgrade.get());
            cir.setReturnValue(upgrades);
        }
    }

    @Unique
    private boolean mekenergistics$isSyntheticUpgradeTarget() {
        TileEntityMekanism tile = (TileEntityMekanism) (Object) this;
        if (!(tile instanceof MeUpgradeableMachine) || tile.getBlockState() == null) {
            return false;
        }
        return !Attribute.has(tile.getBlockHolder(), AttributeUpgradeSupport.class);
    }

    @Inject(method = "setRemoved", at = @At("HEAD"))
    private void mekenergistics$destroyMeNode(CallbackInfo ci) {
        if ((Object) this instanceof MeUpgradeableMachine machine) {
            machine.destroyMeNode();
        }
    }

    @Inject(method = "addContainerTrackers", at = @At("RETURN"))
    private void mekenergistics$addMeTrackers(MekanismContainer container, CallbackInfo ci) {
        if ((Object) this instanceof MeUpgradeableMachine machine) {
            machine.addMeTrackers(container);
        }
    }

    @Inject(method = "saveAdditional", at = @At("RETURN"))
    private void mekenergistics$saveMeState(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        if ((Object) this instanceof MeUpgradeableMachine machine) {
            machine.saveMeState(tag, registries);
        }
    }

    @Inject(method = "loadAdditional", at = @At("RETURN"))
    private void mekenergistics$loadMeState(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        if ((Object) this instanceof MeUpgradeableMachine machine) {
            machine.loadMeState(tag, registries);
        }
    }
}
