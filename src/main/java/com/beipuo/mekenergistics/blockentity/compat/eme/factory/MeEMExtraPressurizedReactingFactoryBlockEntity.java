package com.beipuo.mekenergistics.blockentity.compat.eme.factory;

import com.beipuo.mekenergistics.blockentity.support.MeFactoryAeSupport;
import com.beipuo.mekenergistics.common.machine.MeMekanismMachine;
import com.beipuo.mekenergistics.registry.ModBlocks;
import io.github.masyumero.emextras.common.integration.mekaf.tile.factory.TileEntityEMExtraPRCFactory;
import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.inventory.container.MekanismContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class MeEMExtraPressurizedReactingFactoryBlockEntity extends TileEntityEMExtraPRCFactory implements MeEvolvedMekanismExtrasFactoryAeMachine {
    private final MeMekanismMachine machine;
    private MeFactoryAeSupport aeSupport;

    public MeEMExtraPressurizedReactingFactoryBlockEntity(MeMekanismMachine machine, BlockPos pos, BlockState state) {
        super(ModBlocks.getMachineBlock(machine), pos, state);
        this.machine = machine;
    }

    @Override protected IInventorySlotHolder getInitialInventory(IContentsListener listener) { return getAeSupport().withPatternSlots(super.getInitialInventory(listener)); }
    @Override public List<IInventorySlot> meInputSlots() { return this.inputItemSlots; }
    @Override public List<IInventorySlot> meOutputSlots() { return this.outputItemSlots; }
    @Override public List<? extends mekanism.api.chemical.IChemicalTank> meChemicalInputTanks() { return List.of(this.inputChemicalTank); }
    @Override public List<? extends mekanism.api.fluid.IExtendedFluidTank> meFluidInputTanks() { return List.of(this.inputFluidTank); }
    @Override public List<? extends mekanism.api.chemical.IChemicalTank> meChemicalOutputTanks() { return List.of(this.outputChemicalTank); }
    @Override public void unpauseRecipeMonitors() { for (var monitor : this.recipeCacheLookupMonitors) monitor.unpause(); }
    @Override public MeFactoryAeSupport getAeSupport() { if (aeSupport == null) aeSupport = new MeFactoryAeSupport(this); return aeSupport; }
    @Override public MeMekanismMachine getMachine() { return machine; }
    @Override public Level getOwnerLevel() { return getLevel(); }
    @Override public void addContainerTrackers(MekanismContainer container) { super.addContainerTrackers(container); addAeOutputModeTracker(container); }
    @Override public CachedRecipe<PressurizedReactionRecipe> createNewCachedRecipe(PressurizedReactionRecipe recipe, int cacheIndex) { return MeFactoryAeSupport.withAeRecipeEnergy(this, this.energyContainer, super.createNewCachedRecipe(recipe, cacheIndex)); }
    @Override protected boolean onUpdateServer() { return getAeSupport().processPatternIo(super.onUpdateServer()); }
    @Override public void clearRemoved() { super.clearRemoved(); getAeSupport().createNodeOnFirstTick(this); }
    @Override public void setRemoved() { getAeSupport().destroy(); super.setRemoved(); }
    @Override public void onChunkUnloaded() { getAeSupport().destroy(); super.onChunkUnloaded(); }
    @Override public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) { super.saveAdditional(tag, registries); getAeSupport().saveAll(tag, registries); }
    @Override public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) { super.loadAdditional(tag, registries); getAeSupport().loadAll(tag, registries); }
}
