package com.beipuo.mekenergistics.blockentity.compat.meke.factory;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.KeyCounter;
import com.beipuo.mekenergistics.blockentity.support.MeFactoryAeSupport;
import com.beipuo.mekenergistics.blockentity.api.MeFactoryIoOwner;
import com.beipuo.mekenergistics.common.machine.MeMekanismMachine;
import com.beipuo.mekenergistics.registry.ModBlocks;
import com.jerry.mekextras.common.integration.mekmm.tile.factory.TileEntityExtraRecyclingFactory;
import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.inventory.container.MekanismContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class MeExtraRecyclingFactoryBlockEntity extends TileEntityExtraRecyclingFactory implements MeFactoryIoOwner {
    private final MeMekanismMachine machine;
    private MeFactoryAeSupport aeSupport;

    public MeExtraRecyclingFactoryBlockEntity(MeMekanismMachine machine, BlockPos pos, BlockState state) {
        super(ModBlocks.getMachineBlock(machine), pos, state);
        this.machine = machine;
    }

    @NotNull @Override protected IInventorySlotHolder getInitialInventory(IContentsListener listener) { return getAeSupport().withPatternSlots(super.getInitialInventory(listener)); }
    @Override public List<IInventorySlot> meInputSlots() { return this.inputSlots; }
    @Override public List<IInventorySlot> meOutputSlots() { return this.outputSlots; }
    @Override public void unpauseRecipeMonitors() { for (var monitor : this.recipeCacheLookupMonitors) monitor.unpause(); }
    @Override public MeFactoryAeSupport getAeSupport() { if (this.aeSupport == null) this.aeSupport = new MeFactoryAeSupport(this); return this.aeSupport; }
    @Override public MeMekanismMachine getMachine() { return this.machine; }
    @Override public Level getOwnerLevel() { return getLevel(); }
    @Override public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) { return getMainNode().isActive() && getAvailablePatterns().contains(patternDetails) && (isSmartPatternMultiplicationEnabled() ? getAeSupport().enqueueSmartPattern(patternDetails, inputHolder) : getAeSupport().pushSingleItem(inputHolder, this.inputSlots)); }
    @Override public boolean isBusy() { return false; }
    @Override public void addContainerTrackers(MekanismContainer container) { super.addContainerTrackers(container); addAeOutputModeTracker(container); }
    @Override public mekanism.api.recipes.cache.CachedRecipe<com.jerry.mekmm.api.recipes.RecyclerRecipe> createNewCachedRecipe(@NotNull com.jerry.mekmm.api.recipes.RecyclerRecipe recipe, int cacheIndex) { return MeFactoryAeSupport.withAeRecipeEnergy(this, getEnergyContainer(), super.createNewCachedRecipe(recipe, cacheIndex)); }
    @Override protected boolean onUpdateServer() { boolean sendUpdatePacket = super.onUpdateServer(); sendUpdatePacket |= getAeSupport().processSingleItemSmartPatterns(this.outputSlots, this.inputSlots); return sendUpdatePacket; }
    @Override public void clearRemoved() { super.clearRemoved(); getAeSupport().createNodeOnFirstTick(this); }
    @Override public void setRemoved() { getAeSupport().destroy(); super.setRemoved(); }
    @Override public void onChunkUnloaded() { getAeSupport().destroy(); super.onChunkUnloaded(); }
    @Override public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) { super.saveAdditional(tag, registries); getAeSupport().saveAll(tag, registries); }
    @Override public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) { super.loadAdditional(tag, registries); getAeSupport().loadAll(tag, registries); }
}
