package com.beipuo.mekenergistics.blockentity.compat.mekmm.machine;

import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.security.IActionHost;
import com.beipuo.mekenergistics.blockentity.api.AeOutputMode;
import com.beipuo.mekenergistics.blockentity.api.MeAeMachine;
import com.beipuo.mekenergistics.blockentity.api.MeSmartCableConnection;
import com.beipuo.mekenergistics.blockentity.support.MeRecipeMachineAeSupport;
import com.beipuo.mekenergistics.blockentity.support.io.MeMachineIoAdapter;
import com.beipuo.mekenergistics.blockentity.support.io.MeInputLayout;
import com.beipuo.mekenergistics.blockentity.support.io.MeOutputPort;
import com.beipuo.mekenergistics.common.machine.MeMekanismMachine;
import com.beipuo.mekenergistics.mixin.TileEntityLargeAntiprotonicNucleosynthesizerAccessor;
import com.jerry.meklm.common.tile.machine.TileEntityLargeAntiprotonicNucleosynthesizer;
import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.inventory.container.MekanismContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

/** AE adapter for MekLM's bounding large antiprotonic machine. */
public class MeLargeAntiprotonicNucleosynthesizerBlockEntity extends TileEntityLargeAntiprotonicNucleosynthesizer
        implements ICraftingProvider, MeSmartCableConnection, IActionHost, MeAeMachine {
    private MeRecipeMachineAeSupport<MeLargeAntiprotonicNucleosynthesizerBlockEntity> aeSupport;
    private AeOutputMode outputMode = AeOutputMode.BOTH;

    public MeLargeAntiprotonicNucleosynthesizerBlockEntity(MeMekanismMachine machine, BlockPos pos, BlockState state) {
        super(pos, state);
    }

    private MeRecipeMachineAeSupport<MeLargeAntiprotonicNucleosynthesizerBlockEntity> support() {
        if (aeSupport == null) aeSupport = new MeRecipeMachineAeSupport<>(this);
        return aeSupport;
    }

    @Override public MeRecipeMachineAeSupport<MeLargeAntiprotonicNucleosynthesizerBlockEntity> getRecipeAeSupport() { return support(); }

    @Override public IChemicalTankHolder getInitialChemicalTanks(IContentsListener l, IContentsListener c, IContentsListener u) {
        return super.getInitialChemicalTanks(l, c, u);
    }

    @Override protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener l, IContentsListener c, IContentsListener u) {
        EnergyContainerHelper builder = EnergyContainerHelper.forSideWithConfig(this);
        var energy = new MeRecipeMachineAeSupport.AeBackedEnergyContainer<TileEntityLargeAntiprotonicNucleosynthesizer>(this, support(), u);
        ((TileEntityLargeAntiprotonicNucleosynthesizerAccessor) this).mekenergistics$setEnergyContainer(energy);
        builder.addContainer(energy);
        return builder.build();
    }

    @Override protected IInventorySlotHolder getInitialInventory(IContentsListener l, IContentsListener c, IContentsListener u) {
        return support().withPatternSlots(super.getInitialInventory(l, c, u));
    }

    @Override protected boolean onUpdateServer() {
        return support().processPatternIo(outputMode, super.onUpdateServer());
    }

    @Override public mekanism.api.recipes.cache.CachedRecipe<mekanism.api.recipes.NucleosynthesizingRecipe> createNewCachedRecipe(
            @NotNull mekanism.api.recipes.NucleosynthesizingRecipe recipe, int cacheIndex) {
        return support().wrapRecipeEnergy(getEnergyContainer(), super.createNewCachedRecipe(recipe, cacheIndex));
    }

    @Override public MeInputLayout getPatternInputLayout() {
        var accessor = (TileEntityLargeAntiprotonicNucleosynthesizerAccessor) this;
        if (gasTank == null || accessor.mekenergistics$getInputSlot() == null
                || accessor.mekenergistics$getGasInputSlot() == null) return MeInputLayout.empty();
        return MeInputLayout.unordered(List.of(
                MeMachineIoAdapter.itemInput(accessor.mekenergistics$getInputSlot()),
                MeMachineIoAdapter.chemicalInput(gasTank),
                MeMachineIoAdapter.itemInput(accessor.mekenergistics$getGasInputSlot())));
    }

    @Override public List<? extends MeOutputPort> getPatternOutputPorts() { return List.of(MeMachineIoAdapter.itemOutput(
            ((TileEntityLargeAntiprotonicNucleosynthesizerAccessor) this).mekenergistics$getOutputSlot())); }
    @Override public MeMekanismMachine getMachine() { return MeMekanismMachine.LARGE_ANTIPROTONIC_NUCLEOSYNTHESIZER; }
    @Override public appeng.api.networking.IManagedGridNode getMainNode() { return support().getMainNode(); }
    @Override public AeOutputMode getAeOutputMode() { return outputMode; }
    @Override public void cycleAeOutputMode() { outputMode = outputMode.next(); setChanged(); }
    @Override public void clearRemoved() { super.clearRemoved(); support().createOnFirstTick(); }
    @Override public void setRemoved() { support().destroyNode(); super.setRemoved(); }
    @Override public void onChunkUnloaded() { support().destroyNode(); super.onChunkUnloaded(); }
    @Override public void addContainerTrackers(MekanismContainer c) { super.addContainerTrackers(c); support().addAeTrackers(c, this::getAeOutputMode, m -> outputMode = m, false); }
    @Override public void saveAdditional(CompoundTag t, HolderLookup.@NotNull Provider r) { super.saveAdditional(t, r); support().saveAeState(t, r, outputMode); }
    @Override public void loadAdditional(CompoundTag t, HolderLookup.@NotNull Provider r) { super.loadAdditional(t, r); outputMode = support().loadAeState(t, r); }
}
