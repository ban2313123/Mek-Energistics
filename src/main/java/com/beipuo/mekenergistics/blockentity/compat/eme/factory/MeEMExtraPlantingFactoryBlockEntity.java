package com.beipuo.mekenergistics.blockentity.compat.eme.factory;

import com.beipuo.mekenergistics.blockentity.api.MeFactoryIoOwner;
import com.beipuo.mekenergistics.blockentity.support.MeFactoryAeSupport;
import com.beipuo.mekenergistics.blockentity.support.io.MeInputLayout;
import com.beipuo.mekenergistics.blockentity.support.io.MeMachineIoAdapter;
import com.beipuo.mekenergistics.common.machine.MeMekanismMachine;
import com.beipuo.mekenergistics.registry.ModBlocks;
import io.github.masyumero.emextras.common.integration.mekmm.tile.factory.TileEntityEMExtraPlantingFactory;
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

/** AE adapter for EMEKE MekMM planting factories. */
public class MeEMExtraPlantingFactoryBlockEntity extends TileEntityEMExtraPlantingFactory implements MeEvolvedMekanismExtrasFactoryAeMachine {
    private final MeMekanismMachine machine;
    private MeFactoryAeSupport aeSupport;
    public MeEMExtraPlantingFactoryBlockEntity(MeMekanismMachine machine, BlockPos pos, BlockState state) { super(ModBlocks.getMachineBlock(machine), pos, state); this.machine = machine; }
    @Override protected IInventorySlotHolder getInitialInventory(IContentsListener listener) { return getAeSupport().withPatternSlots(super.getInitialInventory(listener)); }
    @Override public List<IInventorySlot> meInputSlots() { return this.inputSlots; }
    @Override public List<IInventorySlot> meOutputSlots() { return this.outputSlots; }
    @Override public void unpauseRecipeMonitors() { for (var monitor : this.recipeCacheLookupMonitors) monitor.unpause(); }
    @Override public MeFactoryAeSupport getAeSupport() { if (aeSupport == null) aeSupport = new MeFactoryAeSupport(this); return aeSupport; }
    @Override public MeMekanismMachine getMachine() { return machine; }
    @Override public Level getOwnerLevel() { return getLevel(); }
    @Override public MeInputLayout getPatternInputLayout() {
        return MeInputLayout.unordered(List.of(
                MeMachineIoAdapter.autoSortedFactoryItemInput(this.inputSlots),
                MeMachineIoAdapter.chemicalInput(getChemicalTank()),
                MeMachineIoAdapter.itemInput(getExtraSlot())));
    }
    @Override public void addContainerTrackers(MekanismContainer c) { super.addContainerTrackers(c); addAeOutputModeTracker(c); }
    @Override public mekanism.api.recipes.cache.CachedRecipe<com.jerry.mekmm.api.recipes.PlantingRecipe> createNewCachedRecipe(@NotNull com.jerry.mekmm.api.recipes.PlantingRecipe r, int i) { return MeFactoryAeSupport.withAeRecipeEnergy(this, getEnergyContainer(), super.createNewCachedRecipe(r, i)); }
    @Override protected boolean onUpdateServer() { return getAeSupport().processPatternIo(super.onUpdateServer()); }
    @Override public void clearRemoved() { super.clearRemoved(); getAeSupport().createNodeOnFirstTick(this); }
    @Override public void setRemoved() { getAeSupport().destroy(); super.setRemoved(); }
    @Override public void onChunkUnloaded() { getAeSupport().destroy(); super.onChunkUnloaded(); }
    @Override public void saveAdditional(CompoundTag t, HolderLookup.Provider r) { super.saveAdditional(t, r); getAeSupport().saveAll(t, r); }
    @Override public void loadAdditional(CompoundTag t, HolderLookup.Provider r) { super.loadAdditional(t, r); getAeSupport().loadAll(t, r); }
}
