package com.beipuo.mekenergistics.blockentity.compat.meke.factory;

import com.beipuo.mekenergistics.blockentity.support.MeFactoryAeSupport;
import com.beipuo.mekenergistics.blockentity.api.MeFactoryIoOwner;
import com.beipuo.mekenergistics.blockentity.support.io.MeInputLayout;
import com.beipuo.mekenergistics.blockentity.support.io.MeMachineIoAdapter;
import com.beipuo.mekenergistics.common.machine.MeMekanismMachine;
import com.beipuo.mekenergistics.registry.ModBlocks;
import com.jerry.mekextras.common.integration.mekmm.tile.factory.TileEntityExtraPlantingFactory;
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

public class MeExtraPlantingFactoryBlockEntity extends TileEntityExtraPlantingFactory implements MeFactoryIoOwner {
    private final MeMekanismMachine machine;
    private MeFactoryAeSupport aeSupport;

    public MeExtraPlantingFactoryBlockEntity(MeMekanismMachine machine, BlockPos pos, BlockState state) {
        super(ModBlocks.getMachineBlock(machine), pos, state);
        this.machine = machine;
    }

    @NotNull @Override protected IInventorySlotHolder getInitialInventory(IContentsListener listener) { return getAeSupport().withPatternSlots(super.getInitialInventory(listener)); }
    @Override public List<IInventorySlot> meInputSlots() { return this.inputSlots; }
    @Override public List<IInventorySlot> meOutputSlots() { return this.outputSlots; }
    @Override public List<? extends mekanism.api.chemical.IChemicalTank> meChemicalInputTanks() { return List.of(getChemicalTank()); }
    @Override public MeInputLayout getPatternInputLayout() {
        return MeInputLayout.unordered(List.of(
                MeMachineIoAdapter.autoSortedFactoryItemInput(this.inputSlots),
                MeMachineIoAdapter.chemicalInput(getChemicalTank()),
                MeMachineIoAdapter.itemInput(getExtraSlot())));
    }
    @Override public void unpauseRecipeMonitors() { for (var monitor : this.recipeCacheLookupMonitors) monitor.unpause(); }
    @Override public MeFactoryAeSupport getAeSupport() { if (this.aeSupport == null) this.aeSupport = new MeFactoryAeSupport(this); return this.aeSupport; }
    @Override public MeMekanismMachine getMachine() { return this.machine; }
    @Override public Level getOwnerLevel() { return getLevel(); }
    @Override public void addContainerTrackers(MekanismContainer container) { super.addContainerTrackers(container); addAeOutputModeTracker(container); }
    @Override public mekanism.api.recipes.cache.CachedRecipe<com.jerry.mekmm.api.recipes.PlantingRecipe> createNewCachedRecipe(@NotNull com.jerry.mekmm.api.recipes.PlantingRecipe recipe, int cacheIndex) { return MeFactoryAeSupport.withAeRecipeEnergy(this, getEnergyContainer(), super.createNewCachedRecipe(recipe, cacheIndex)); }
    @Override protected boolean onUpdateServer() { return getAeSupport().processPatternIo(super.onUpdateServer()); }
    @Override public void clearRemoved() { super.clearRemoved(); getAeSupport().createNodeOnFirstTick(this); }
    @Override public void setRemoved() { getAeSupport().destroy(); super.setRemoved(); }
    @Override public void onChunkUnloaded() { getAeSupport().destroy(); super.onChunkUnloaded(); }
    @Override public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) { super.saveAdditional(tag, registries); getAeSupport().saveAll(tag, registries); }
    @Override public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) { super.loadAdditional(tag, registries); getAeSupport().loadAll(tag, registries); }
}
