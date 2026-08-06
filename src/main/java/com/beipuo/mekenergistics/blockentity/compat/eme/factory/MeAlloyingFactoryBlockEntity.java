package com.beipuo.mekenergistics.blockentity.compat.eme.factory;

import com.beipuo.mekenergistics.blockentity.api.MeFactoryIoOwner;
import com.beipuo.mekenergistics.blockentity.support.MeFactoryAeSupport;
import com.beipuo.mekenergistics.blockentity.support.io.MeInputLayout;
import com.beipuo.mekenergistics.blockentity.support.io.MeMachineIoAdapter;
import com.beipuo.mekenergistics.common.machine.MeMekanismMachine;
import com.beipuo.mekenergistics.compat.eme.EvolvedMekanismRecipeViewerTypes;
import com.beipuo.mekenergistics.registry.ModBlocks;
import fr.iglee42.evolvedmekanism.recipes.AlloyerRecipe;
import fr.iglee42.evolvedmekanism.tiles.factory.TileEntityAlloyingFactory;
import java.util.List;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.api.IContentsListener;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.inventory.container.MekanismContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MeAlloyingFactoryBlockEntity extends TileEntityAlloyingFactory implements MeFactoryIoOwner {
    private final MeMekanismMachine machine;
    private MeFactoryAeSupport aeSupport;

    public MeAlloyingFactoryBlockEntity(MeMekanismMachine machine, BlockPos pos, BlockState state) {
        super(ModBlocks.getMachineBlock(machine), pos, state);
        this.machine = machine;
    }

    @NotNull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
        EnergyContainerHelper builder = EnergyContainerHelper.forSideWithConfig(this);
        builder.addContainer(this.energyContainer = new MeFactoryAeSupport.AeBackedFactoryEnergyContainer<>(this, () -> {
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
        return withPatternSlots(super.getInitialInventory(listener));
    }

    @Override public MeFactoryAeSupport getAeSupport() {
        if (this.aeSupport == null) {
            this.aeSupport = new MeFactoryAeSupport(this);
        }
        return this.aeSupport;
    }
    @Override public MeMekanismMachine getMachine() { return this.machine; }
    @Override public Level getOwnerLevel() { return getLevel(); }
    @Override public List<IInventorySlot> meInputSlots() { return this.inputSlots; }
    @Override public List<IInventorySlot> meOutputSlots() { return this.outputSlots; }
    @Override public void unpauseRecipeMonitors() { for (var monitor : this.recipeCacheLookupMonitors) monitor.unpause(); }
    @Override public MeInputLayout getPatternInputLayout() {
        return MeInputLayout.lanes(List.of(
                List.of(MeMachineIoAdapter.autoSortedFactoryItemInput(this.inputSlots)),
                List.of(MeMachineIoAdapter.itemInput(getExtraSlot())),
                List.of(MeMachineIoAdapter.itemInput(getSecondExtraSlot()))));
    }
    @Nullable @Override public IRecipeViewerRecipeType<AlloyerRecipe> recipeViewerType() { return EvolvedMekanismRecipeViewerTypes.ALLOYING; }
    @Override public void addContainerTrackers(MekanismContainer container) { super.addContainerTrackers(container); addAeOutputModeTracker(container); }
    @Override public CachedRecipe<AlloyerRecipe> createNewCachedRecipe(@NotNull AlloyerRecipe recipe, int cacheIndex) { return MeFactoryAeSupport.withAeRecipeEnergy(this, this.energyContainer, super.createNewCachedRecipe(recipe, cacheIndex)); }
    @Override protected boolean onUpdateServer() { return getAeSupport().processPatternIo(super.onUpdateServer()); }
    @Override public void clearRemoved() { super.clearRemoved(); getAeSupport().createNodeOnFirstTick(this); }
    @Override public void setRemoved() { getAeSupport().destroy(); super.setRemoved(); }
    @Override public void onChunkUnloaded() { getAeSupport().destroy(); super.onChunkUnloaded(); }
    @Override public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) { super.saveAdditional(tag, registries); getAeSupport().saveAll(tag, registries); }
    @Override public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) { super.loadAdditional(tag, registries); getAeSupport().loadAll(tag, registries); }
}
