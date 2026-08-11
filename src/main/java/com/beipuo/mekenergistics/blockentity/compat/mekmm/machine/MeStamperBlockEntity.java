package com.beipuo.mekenergistics.blockentity.compat.mekmm.machine;

import com.beipuo.mekenergistics.blockentity.api.AeOutputMode;

import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.security.IActionHost;
import appeng.api.util.AECableType;
import com.beipuo.mekenergistics.blockentity.api.MeAeMachine;
import com.beipuo.mekenergistics.blockentity.support.MeRecipeMachineAeSupport;
import com.beipuo.mekenergistics.blockentity.support.io.MeInputLayout;
import com.beipuo.mekenergistics.blockentity.support.io.MeMachineIoAdapter;
import com.beipuo.mekenergistics.blockentity.support.io.MeOutputPort;
import com.beipuo.mekenergistics.blockentity.api.MeSmartCableConnection;
import com.beipuo.mekenergistics.common.machine.MeMekanismMachine;
import com.jerry.mekmm.common.tile.machine.TileEntityStamper;
import java.util.ArrayList;
import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class MeStamperBlockEntity extends TileEntityStamper implements ICraftingProvider, MeSmartCableConnection, IActionHost, MeAeMachine {
    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.SMART;
    }

    private MeRecipeMachineAeSupport<MeStamperBlockEntity> aeSupport;

    @Override
    public MeRecipeMachineAeSupport<MeStamperBlockEntity> getRecipeAeSupport() {
        if (this.aeSupport == null) {
            this.aeSupport = new MeRecipeMachineAeSupport<>(this);
        }
        return this.aeSupport;
    }
    private AeOutputMode aeOutputMode = AeOutputMode.BOTH;
    private InputInventorySlot meItemInputSlot;
    private InputInventorySlot meMoldInputSlot;
    private OutputInventorySlot meOutputSlot;

    public MeStamperBlockEntity(MeMekanismMachine machine, BlockPos pos, BlockState state) {
        super(pos, state);
        getRecipeAeSupport();
    }

    @NotNull
    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener, IContentsListener recipeCacheListener, IContentsListener recipeCacheUnpauseListener) {
        IInventorySlotHolder original = super.getInitialInventory(listener, recipeCacheListener, recipeCacheUnpauseListener);
        captureSlots(original);
        return side -> {
            List<IInventorySlot> slots = new ArrayList<>(original.getInventorySlots(side));
            slots.addAll(getRecipeAeSupport().getPatternSlots());
            return slots;
        };
    }

    private void captureSlots(IInventorySlotHolder original) {
        for (IInventorySlot slot : original.getInventorySlots(null)) {
            if (slot instanceof InputInventorySlot input) {
                if (this.meItemInputSlot == null) {
                    this.meItemInputSlot = input;
                } else if (this.meMoldInputSlot == null) {
                    this.meMoldInputSlot = input;
                }
            } else if (slot instanceof OutputInventorySlot output && this.meOutputSlot == null) {
                this.meOutputSlot = output;
            }
        }
    }

    @Override
    protected boolean onUpdateServer() {
        return getRecipeAeSupport().processPatternIo(this.aeOutputMode, super.onUpdateServer());
    }

    @Override
    public mekanism.api.recipes.cache.CachedRecipe<com.jerry.mekmm.api.recipes.StamperRecipe> createNewCachedRecipe(
            @NotNull com.jerry.mekmm.api.recipes.StamperRecipe recipe, int cacheIndex) {
        return getRecipeAeSupport().wrapRecipeEnergy(getEnergyContainer(), super.createNewCachedRecipe(recipe, cacheIndex));
    }

    @Override
    public MeInputLayout getPatternInputLayout() {
        return this.meItemInputSlot == null ? MeInputLayout.empty()
                : MeInputLayout.unordered(java.util.List.of(MeMachineIoAdapter.itemInput(this.meItemInputSlot)));
    }

    @Override public boolean isPatternBusy() { return this.meMoldInputSlot == null || this.meMoldInputSlot.isEmpty(); }
    @Override public java.util.List<? extends MeOutputPort> getPatternOutputPorts() { return this.meOutputSlot == null
            ? java.util.List.of() : java.util.List.of(MeMachineIoAdapter.itemOutput(this.meOutputSlot)); }
    @Override public MeMekanismMachine getMachine() { return MeMekanismMachine.CNC_STAMPER; }
    public appeng.api.networking.IManagedGridNode getMainNode() { return getRecipeAeSupport().getMainNode(); }
    @Override public AeOutputMode getAeOutputMode() { return this.aeOutputMode; }
    @Override public void cycleAeOutputMode() { this.aeOutputMode = this.aeOutputMode.next(); setChanged(); }
    @Override public void clearRemoved() { super.clearRemoved(); getRecipeAeSupport().createOnFirstTick(); }
    @Override public void setRemoved() { getRecipeAeSupport().destroyNode(); super.setRemoved(); }
    @Override public void onChunkUnloaded() { getRecipeAeSupport().destroyNode(); super.onChunkUnloaded(); }
    @Override public void addContainerTrackers(MekanismContainer container) { super.addContainerTrackers(container); getRecipeAeSupport().addAeTrackers(container, this::getAeOutputMode, mode -> this.aeOutputMode = mode, false); }
    @Override public void saveAdditional(CompoundTag tag, HolderLookup.@NotNull Provider registries) { super.saveAdditional(tag, registries); getRecipeAeSupport().saveAeState(tag, registries, this.aeOutputMode); }
    @Override public void loadAdditional(CompoundTag tag, HolderLookup.@NotNull Provider registries) { super.loadAdditional(tag, registries); this.aeOutputMode = getRecipeAeSupport().loadAeState(tag, registries); }
}
