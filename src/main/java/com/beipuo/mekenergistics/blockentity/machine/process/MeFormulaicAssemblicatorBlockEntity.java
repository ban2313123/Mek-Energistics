package com.beipuo.mekenergistics.blockentity.machine.process;

import com.beipuo.mekenergistics.blockentity.api.AeOutputMode;


import com.beipuo.mekenergistics.blockentity.api.MeAeMachine;
import com.beipuo.mekenergistics.blockentity.api.MeSmartCableConnection;

import com.beipuo.mekenergistics.blockentity.support.MeRecipeMachineAeSupport;
import com.beipuo.mekenergistics.blockentity.support.io.MeMachineIoAdapter;
import com.beipuo.mekenergistics.blockentity.support.io.MeInputLayout;
import com.beipuo.mekenergistics.blockentity.support.io.MeOutputPort;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.security.IActionHost;
import appeng.api.util.AECableType;
import com.beipuo.mekenergistics.common.machine.MeMekanismMachine;
import com.beipuo.mekenergistics.mixin.TileEntityFormulaicAssemblicatorAccessor;
import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.tile.machine.TileEntityFormulaicAssemblicator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class MeFormulaicAssemblicatorBlockEntity extends TileEntityFormulaicAssemblicator implements ICraftingProvider, MeSmartCableConnection, IActionHost, MeAeMachine {
    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.SMART;
    }

    private MeRecipeMachineAeSupport<MeFormulaicAssemblicatorBlockEntity> aeSupport;

    @Override
    public MeRecipeMachineAeSupport<MeFormulaicAssemblicatorBlockEntity> getRecipeAeSupport() {
        if (this.aeSupport == null) {
            this.aeSupport = new MeRecipeMachineAeSupport<>(this);
        }
        return this.aeSupport;
    }
    private AeOutputMode aeOutputMode = AeOutputMode.BOTH;

    public MeFormulaicAssemblicatorBlockEntity(MeMekanismMachine machine, BlockPos pos, BlockState state) {
        super(pos, state);
        getRecipeAeSupport();
    }

    @NotNull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
        EnergyContainerHelper builder = EnergyContainerHelper.forSideWithConfig(this);
        var energy = new MeRecipeMachineAeSupport.AeBackedEnergyContainer<TileEntityFormulaicAssemblicator>(this, getRecipeAeSupport(), listener);
        ((TileEntityFormulaicAssemblicatorAccessor) this).mekenergistics$setEnergyContainer(energy);
        builder.addContainer(energy);
        return builder.build();
    }

    @NotNull
    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        return withPatternSlots(super.getInitialInventory(listener));
    }

    @Override
    protected boolean onUpdateServer() {
        return getRecipeAeSupport().processPatternIo(this.aeOutputMode, super.onUpdateServer());
    }

    @Override
    public MeInputLayout getPatternInputLayout() {
        List<IInventorySlot> inputSlots = ((TileEntityFormulaicAssemblicatorAccessor) this).mekenergistics$getInputSlots();
        return MeInputLayout.unordered(inputSlots.stream().map(MeMachineIoAdapter::itemInput).toList());
    }

    @Override
    public java.util.List<? extends MeOutputPort> getPatternOutputPorts() {
        return ((TileEntityFormulaicAssemblicatorAccessor) this).mekenergistics$getOutputSlots().stream()
                .map(MeMachineIoAdapter::itemOutput).toList();
    }

    @Override public MeMekanismMachine getMachine() { return MeMekanismMachine.FORMULAIC_ASSEMBLICATOR; }
    public appeng.api.networking.IManagedGridNode getMainNode() { return getRecipeAeSupport().getMainNode(); }
    @Override public AeOutputMode getAeOutputMode() { return this.aeOutputMode; }
    @Override public void cycleAeOutputMode() { this.aeOutputMode = this.aeOutputMode.next(); setChanged(); }
    @Override public void clearRemoved() { super.clearRemoved(); getRecipeAeSupport().createOnFirstTick(); }
    @Override public void setRemoved() { getRecipeAeSupport().destroyNode(); super.setRemoved(); }
    @Override public void onChunkUnloaded() { getRecipeAeSupport().destroyNode(); super.onChunkUnloaded(); }
    @Override public void addContainerTrackers(MekanismContainer container) { super.addContainerTrackers(container); getRecipeAeSupport().addAeTrackers(container, this::getAeOutputMode, mode -> this.aeOutputMode = mode, true); }
    @Override public void saveAdditional(CompoundTag tag, HolderLookup.@NotNull Provider registries) { super.saveAdditional(tag, registries); getRecipeAeSupport().saveAeState(tag, registries, this.aeOutputMode); }
    @Override public void loadAdditional(CompoundTag tag, HolderLookup.@NotNull Provider registries) { super.loadAdditional(tag, registries); this.aeOutputMode = getRecipeAeSupport().loadAeState(tag, registries); }
}

