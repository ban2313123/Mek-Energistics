package com.beipuo.mekenergistics.mixin;

import com.beipuo.mekenergistics.blockentity.api.MeAeMachine;
import com.beipuo.mekenergistics.blockentity.api.MeFactoryAeMachine;
import com.beipuo.mekenergistics.blockentity.api.MeUpgradeableMachine;
import com.beipuo.mekenergistics.block.MeMekanismMachineBlock;
import com.beipuo.mekenergistics.upgrade.MePassiveCraftingUpgrade;
import com.beipuo.mekenergistics.upgrade.MePatternProviderUpgrade;
import com.beipuo.mekenergistics.upgrade.StandaloneUpgradePersistence;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IManagedGridNode;
import appeng.api.util.AECableType;
import java.util.HashSet;
import java.util.Set;
import mekanism.api.Upgrade;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeUpgradeSupport;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = TileEntityMekanism.class, remap = false)
public abstract class TileEntityMekanismMeUpgradeLifecycleMixin {
    /**
     * A concrete class method takes precedence over AE2 and optional integration
     * interface defaults when this Mixin is applied to third-party Mekanism tiles.
     */
    public IGridNode getGridNode(Direction side) {
        if ((Object) this instanceof MeAeMachine machine) {
            IManagedGridNode node = machine.getMainNode();
            return node == null ? null : node.getNode();
        }
        return null;
    }

    /**
     * Same rationale as getGridNode: a concrete class method takes precedence over AE2 and optional
     * integration interface defaults. Productive Bees Genesis' IAe2OutputHost supplies its own
     * default getCableConnectionType, so every concrete tile must resolve the method in a class
     * rather than between two interface defaults (IncompatibleClassChangeError).
     */
    public AECableType getCableConnectionType(Direction side) {
        return AECableType.SMART;
    }

    @Inject(method = "tickServer", at = @At(value = "INVOKE",
            target = "Lmekanism/common/tile/base/TileEntityMekanism;onUpdateServer()Z"))
    private static void mekenergistics$refillLocalEnergyFromMe(Level level, BlockPos pos, BlockState state,
            TileEntityMekanism tile, CallbackInfo ci) {
        if (!(tile instanceof MeAeMachine machine)) {
            return;
        }
        if (!(state.getBlock() instanceof MeMekanismMachineBlock)) {
            if (!(tile instanceof MeUpgradeableMachine upgradeable)
                    || !upgradeable.isMeUpgradeTarget() || !upgradeable.isMeUpgradeActive()) {
                return;
            }
        }
        machine.getRecipeAeSupport().refillLocalEnergyBuffers();
    }

    @Inject(method = "supportsUpgrades", at = @At("RETURN"), cancellable = true)
    private void mekenergistics$enableSyntheticUpgradeSupport(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue() && mekenergistics$isSyntheticUpgradeTarget()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getSupportedUpgrade", at = @At("HEAD"), cancellable = true)
    private void mekenergistics$provideSyntheticUpgradeSet(CallbackInfoReturnable<Set<Upgrade>> cir) {
        if (mekenergistics$isSyntheticUpgradeTarget()) {
            cir.setReturnValue((Object) this instanceof MeFactoryAeMachine
                    ? Set.of(MePassiveCraftingUpgrade.get())
                    : Set.of(MePatternProviderUpgrade.get(), MePassiveCraftingUpgrade.get()));
        }
    }

    @Inject(method = "getSupportedUpgrade", at = @At("RETURN"), cancellable = true)
    private void mekenergistics$supportMeUpgrade(CallbackInfoReturnable<Set<Upgrade>> cir) {
        if (mekenergistics$isSyntheticUpgradeTarget()) {
            return;
        }
        if ((Object) this instanceof MeFactoryAeMachine) {
            Set<Upgrade> upgrades = new HashSet<>(cir.getReturnValue());
            upgrades.add(MePassiveCraftingUpgrade.get());
            cir.setReturnValue(upgrades);
        } else if ((Object) this instanceof MeUpgradeableMachine machine && machine.isMeUpgradeTarget()) {
            Set<Upgrade> upgrades = new HashSet<>(cir.getReturnValue());
            upgrades.add(MePatternProviderUpgrade.get());
            upgrades.add(MePassiveCraftingUpgrade.get());
            cir.setReturnValue(upgrades);
        } else if ((Object) this instanceof MeAeMachine) {
            Set<Upgrade> upgrades = new HashSet<>(cir.getReturnValue());
            upgrades.add(MePatternProviderUpgrade.get());
            upgrades.add(MePassiveCraftingUpgrade.get());
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
        mekenergistics$saveStandaloneUpgrade(tag, MePatternProviderUpgrade.get());
        mekenergistics$saveStandaloneUpgrade(tag, MePassiveCraftingUpgrade.get());
    }

    @Inject(method = "loadAdditional", at = @At("RETURN"))
    private void mekenergistics$loadMeState(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        if ((Object) this instanceof MeUpgradeableMachine machine) {
            machine.loadMeState(tag, registries);
        }
        mekenergistics$restoreStandaloneUpgrade(tag, MePatternProviderUpgrade.get());
        mekenergistics$restoreStandaloneUpgrade(tag, MePassiveCraftingUpgrade.get());
    }

    @Unique
    private void mekenergistics$saveStandaloneUpgrade(CompoundTag tag, Upgrade upgrade) {
        TileEntityMekanism tile = (TileEntityMekanism) (Object) this;
        if (tile.getComponent() != null) {
            StandaloneUpgradePersistence.saveCount(tag, upgrade, tile.getComponent().getUpgrades(upgrade));
        }
    }

    @Unique
    private void mekenergistics$restoreStandaloneUpgrade(CompoundTag tag, Upgrade upgrade) {
        TileEntityMekanism tile = (TileEntityMekanism) (Object) this;
        if (tile.getComponent() == null || !tile.getComponent().supports(upgrade)) {
            return;
        }
        int saved = StandaloneUpgradePersistence.loadCount(tag, upgrade);
        int installed = tile.getComponent().getUpgrades(upgrade);
        if (saved > installed) {
            tile.getComponent().addUpgrades(upgrade, saved - installed);
        }
    }
}
