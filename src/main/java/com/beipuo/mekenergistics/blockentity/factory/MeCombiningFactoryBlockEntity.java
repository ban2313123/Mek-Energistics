package com.beipuo.mekenergistics.blockentity.factory;

import com.beipuo.mekenergistics.blockentity.api.MeFactoryAeMachine;
import com.beipuo.mekenergistics.blockentity.support.MeFactoryAeSupport;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.KeyCounter;
import com.beipuo.mekenergistics.common.machine.MeMekanismMachine;
import com.beipuo.mekenergistics.mixin.TileEntityCombiningFactoryAccessor;
import com.beipuo.mekenergistics.registry.ModBlocks;
import mekanism.api.IContentsListener;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.tile.factory.TileEntityCombiningFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class MeCombiningFactoryBlockEntity extends TileEntityCombiningFactory implements MeFactoryAeMachine {
    private final MeMekanismMachine machine;
    private MeFactoryAeSupport aeSupport;

    public MeCombiningFactoryBlockEntity(MeMekanismMachine machine, BlockPos pos, BlockState state) {
        super(ModBlocks.getMachineBlock(machine), pos, state);
        this.machine = machine;
        getAeSupport();
    }

    @NotNull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
        EnergyContainerHelper builder = EnergyContainerHelper.forSideWithConfig(this);
        builder.addContainer(this.energyContainer = new MeFactoryAeSupport.AeBackedFactoryEnergyContainer(this, () -> {
            listener.onContentsChanged();
            for (var cacheLookupMonitor : this.recipeCacheLookupMonitors) {
                cacheLookupMonitor.unpause();
            }
        }));
        return builder.build();
    }

    @NotNull
    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        return getAeSupport().withPatternSlots(super.getInitialInventory(listener));
    }

    @Override public MeFactoryAeSupport getAeSupport() { if (this.aeSupport == null) this.aeSupport = new MeFactoryAeSupport(this); return this.aeSupport; }
    @Override public MeMekanismMachine getMachine() { return this.machine; }
    @Override public Level getOwnerLevel() { return getLevel(); }

    @NotNull
    @Override
    public CachedRecipe<CombinerRecipe> createNewCachedRecipe(@NotNull CombinerRecipe recipe, int cacheIndex) {
        return MeFactoryAeSupport.withAeRecipeEnergy(this.energyContainer, super.createNewCachedRecipe(recipe, cacheIndex));
    }

    @Override
    public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
        if (!getMainNode().isActive() || !getAvailablePatterns().contains(patternDetails) || inputHolder == null || inputHolder.length != 2) {
            return false;
        }
        if (this.aeSupport.isSmartPatternMultiplicationEnabled()) {
            return this.aeSupport.enqueueSmartPattern(patternDetails, inputHolder);
        }
        InputInventorySlot extraSlot = ((TileEntityCombiningFactoryAccessor) this).mekenergistics$getExtraSlot();
        return this.aeSupport.pushTwoItems(inputHolder, this.inputSlots, extraSlot);
    }

    @Override public boolean isBusy() { return false; }
    @Override
    protected boolean onUpdateServer() {
        boolean sendUpdatePacket = super.onUpdateServer();
        InputInventorySlot extraSlot = ((TileEntityCombiningFactoryAccessor) this).mekenergistics$getExtraSlot();
        return this.aeSupport.processTwoItemsSmartPatterns(extraSlot, this.outputSlots, this.inputSlots)
                || sendUpdatePacket;
    }
    @Override public void clearRemoved() { super.clearRemoved(); this.aeSupport.createNodeOnFirstTick(this); }
    @Override public void setRemoved() { this.aeSupport.destroy(); super.setRemoved(); }
    @Override public void onChunkUnloaded() { this.aeSupport.destroy(); super.onChunkUnloaded(); }
    @Override public void addContainerTrackers(MekanismContainer container) { super.addContainerTrackers(container); addAeOutputModeTracker(container); }
    @Override public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) { super.saveAdditional(tag, registries); this.aeSupport.saveAll(tag, registries); }
    @Override public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) { super.loadAdditional(tag, registries); this.aeSupport.loadAll(tag, registries); }

}
