package com.beipuo.mekenergistics.upgrade;

import com.beipuo.mekenergistics.blockentity.api.AeOutputMode;
import com.beipuo.mekenergistics.blockentity.api.MeUpgradeableMachine;
import com.beipuo.mekenergistics.blockentity.support.MeRecipeMachineAeSupport;
import com.beipuo.mekenergistics.blockentity.support.io.MeInputLayout;
import com.beipuo.mekenergistics.blockentity.support.io.MeOutputPort;
import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/** Shared lifecycle for recipe-machine Mixins that expose machine-specific profiles and recipes. */
public final class MeUpgradeRecipeMachineRuntime {
    private final TileEntityMekanism tile;
    private final MeUpgradeableMachine machine;
    private final MeRecipeMachineAeSupport<?> support;
    private final MeUpgradeRuntimeState state = new MeUpgradeRuntimeState();
    private final AeOutputMode defaultOutputMode;
    private AeOutputMode outputMode;

    @SuppressWarnings({"rawtypes", "unchecked"})
    public MeUpgradeRecipeMachineRuntime(TileEntityMekanism tile, AeOutputMode defaultOutputMode) {
        this.tile = tile;
        this.machine = (MeUpgradeableMachine) tile;
        this.support = new MeRecipeMachineAeSupport(tile);
        this.defaultOutputMode = defaultOutputMode;
    }

    public MeRecipeMachineAeSupport<?> support() {
        return this.support;
    }

    public boolean matches(MeUpgradeMachineProfile<?> profile) {
        return profile != null && matchesProfile(profile);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private boolean matchesProfile(MeUpgradeMachineProfile profile) {
        return profile.matches(this.tile);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public MeInputLayout inputLayout(MeUpgradeMachineProfile<?> profile) {
        return ((MeUpgradeMachineProfile) profile).inputLayoutFor(this.tile);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public List<? extends MeOutputPort> outputPorts(MeUpgradeMachineProfile<?> profile) {
        return ((MeUpgradeMachineProfile) profile).outputPortsFor(this.tile);
    }

    public boolean active(boolean target) {
        return this.state.activeFor(this.tile.getLevel(), target, installed(target));
    }

    public boolean installed(boolean target) {
        return target && this.tile.getComponent() != null
                && this.tile.getComponent().isUpgradeInstalled(MePatternProviderUpgrade.get());
    }

    public IInventorySlotHolder withPatternSlots(IInventorySlotHolder holder, IContentsListener recipeCacheListener) {
        this.state.setRecipeCacheListener(recipeCacheListener);
        return this.support.withPatternSlots(holder);
    }

    public <RECIPE extends MekanismRecipe<?>> CachedRecipe<RECIPE> wrapEnergy(
            MachineEnergyContainer<?> energy, CachedRecipe<RECIPE> recipe, boolean active) {
        return active ? this.support.wrapRecipeEnergy(energy, recipe) : recipe;
    }

    public boolean tick(boolean active, boolean changed) {
        switch (this.state.transitionTo(active)) {
            case ACTIVATE -> {
                syncOwner();
                this.support.create(this.tile.getLevel(), this.tile.getBlockPos());
                invalidateCapabilities();
                this.state.refreshRecipeCache();
            }
            case DEACTIVATE -> {
                this.support.destroyNode();
                invalidateCapabilities();
                this.state.refreshRecipeCache();
            }
            case NONE -> {
            }
        }
        return active ? this.support.processPatternIo(outputMode(), changed) : changed;
    }

    public void createNodeIfActive(boolean active) {
        if (active) {
            syncOwner();
            this.support.createOnFirstTick();
            // A loaded factory may already have paused recipe monitors before the ME upgrade is restored.
            // Unpause them here so the next server tick rebuilds the cached recipe with AE energy.
            this.state.refreshRecipeCache();
        }
    }

    public void destroyNode() {
        this.support.destroyNode();
        this.state.markInactive();
    }

    public AeOutputMode outputMode() {
        return this.outputMode == null ? this.defaultOutputMode : this.outputMode;
    }

    public void cycleOutputMode() {
        this.outputMode = outputMode().next();
        this.support.invalidatePatternIoCache();
        this.tile.setChanged();
    }

    public void addTrackers(MekanismContainer container, boolean target) {
        if (!target) {
            return;
        }
        container.track(SyncableBoolean.create(() -> installed(true), this.state::acceptClientActive));
        this.support.addAeTrackers(container, this::outputMode, mode -> this.outputMode = mode, true);
    }

    public void save(CompoundTag tag, HolderLookup.Provider registries) {
        this.support.saveAeState(tag, registries, outputMode());
    }

    public void load(CompoundTag tag, HolderLookup.Provider registries) {
        AeOutputMode loaded = this.support.loadAeState(tag, registries);
        this.outputMode = tag.contains("AeOutputMode") ? loaded : this.defaultOutputMode;
    }

    private void syncOwner() {
        if (!(this.tile.getLevel() instanceof ServerLevel level) || this.tile.getOwnerUUID() == null) {
            return;
        }
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(this.tile.getOwnerUUID());
        if (player != null) {
            this.support.setOwningPlayer(player);
        }
    }

    private void invalidateCapabilities() {
        if (this.tile.getLevel() != null) {
            this.tile.getLevel().invalidateCapabilities(this.tile.getBlockPos());
        }
    }
}
