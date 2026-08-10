package com.beipuo.mekenergistics.blockentity.compat.eme.machine;

import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.security.IActionHost;
import appeng.api.util.AECableType;
import com.beipuo.mekenergistics.blockentity.api.AeOutputMode;
import com.beipuo.mekenergistics.blockentity.api.MeAeMachine;
import com.beipuo.mekenergistics.blockentity.api.MeSmartCableConnection;

import com.beipuo.mekenergistics.blockentity.support.MeRecipeMachineAeSupport;
import com.beipuo.mekenergistics.blockentity.support.io.MeMachineIoAdapter;
import com.beipuo.mekenergistics.blockentity.support.io.MeInputLayout;
import com.beipuo.mekenergistics.blockentity.support.io.MeOutputPort;
import com.beipuo.mekenergistics.common.machine.MeMekanismMachine;
import com.beipuo.mekenergistics.mixin.TileEntityAlloyerAccessor;
import fr.iglee42.evolvedmekanism.recipes.AlloyerRecipe;
import fr.iglee42.evolvedmekanism.tiles.machine.TileEntityAlloyer;
import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.slot.InputInventorySlot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class MeAlloyerBlockEntity extends TileEntityAlloyer implements ICraftingProvider, MeSmartCableConnection, IActionHost, MeAeMachine {
    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.SMART;
    }

    private MeRecipeMachineAeSupport<MeAlloyerBlockEntity> aeSupport;

    @Override
    public MeRecipeMachineAeSupport<MeAlloyerBlockEntity> getRecipeAeSupport() {
        if (this.aeSupport == null) {
            this.aeSupport = new MeRecipeMachineAeSupport<>(this);
        }
        return this.aeSupport;
    }
    private AeOutputMode aeOutputMode = AeOutputMode.BOTH;

    public MeAlloyerBlockEntity(MeMekanismMachine machine, BlockPos pos, BlockState state) {
        super(pos, state);
        getRecipeAeSupport();
    }

    @NotNull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener, IContentsListener recipeCacheListener, IContentsListener recipeCacheUnpauseListener) {
        EnergyContainerHelper builder = EnergyContainerHelper.forSideWithConfig(this);
        var energy = new MeRecipeMachineAeSupport.AeBackedEnergyContainer<TileEntityAlloyer>(this, getRecipeAeSupport(), recipeCacheUnpauseListener);
        ((TileEntityAlloyerAccessor) this).mekenergistics$setEnergyContainer(energy);
        builder.addContainer(energy);
        return builder.build();
    }

    @NotNull
    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener, IContentsListener recipeCacheListener, IContentsListener recipeCacheUnpauseListener) {
        return withPatternSlots(super.getInitialInventory(listener, recipeCacheListener, recipeCacheUnpauseListener));
    }

    @Override
    protected boolean onUpdateServer() {
        return getRecipeAeSupport().processPatternIo(this.aeOutputMode, super.onUpdateServer());
    }

    @NotNull
    @Override
    public CachedRecipe<AlloyerRecipe> createNewCachedRecipe(@NotNull AlloyerRecipe recipe, int cacheIndex) {
        return getRecipeAeSupport().wrapRecipeEnergy(getEnergyContainer(), super.createNewCachedRecipe(recipe, cacheIndex));
    }

    @Override
    public MeInputLayout getPatternInputLayout() {
        TileEntityAlloyerAccessor accessor = (TileEntityAlloyerAccessor) this;
        InputInventorySlot mainSlot = accessor.mekenergistics$getMainInputSlot();
        IInventorySlot extraSlot = accessor.mekenergistics$getExtraInputSlot();
        IInventorySlot secondExtraSlot = accessor.mekenergistics$getSecondExtraInputSlot();
        return MeInputLayout.lanes(java.util.List.of(
                java.util.List.of(MeMachineIoAdapter.itemInput(mainSlot)),
                java.util.List.of(MeMachineIoAdapter.itemInput(extraSlot)),
                java.util.List.of(MeMachineIoAdapter.itemInput(secondExtraSlot))));
    }

    @Override public java.util.List<? extends MeOutputPort> getPatternOutputPorts() { return java.util.List.of(
            MeMachineIoAdapter.itemOutput(((TileEntityAlloyerAccessor) this).mekenergistics$getOutputSlot())); }
    @Override public MeMekanismMachine getMachine() { return MeMekanismMachine.ALLOYER; }
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
