package com.beipuo.mekenergistics.blockentity.compat.eme.machine;

import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.security.IActionHost;
import com.beipuo.mekenergistics.blockentity.api.AeOutputMode;
import com.beipuo.mekenergistics.blockentity.api.MeAeMachine;
import com.beipuo.mekenergistics.blockentity.api.MeSmartCableConnection;

import com.beipuo.mekenergistics.blockentity.support.MeRecipeMachineAeSupport;
import com.beipuo.mekenergistics.blockentity.support.io.MeInputPort;
import com.beipuo.mekenergistics.blockentity.support.io.MeMachineIoAdapter;
import com.beipuo.mekenergistics.blockentity.support.io.MeInputLayout;
import com.beipuo.mekenergistics.blockentity.support.io.MeOutputPort;
import com.beipuo.mekenergistics.common.machine.MeMekanismMachine;
import com.beipuo.mekenergistics.mixin.TileEntitySolidifierAccessor;
import fr.iglee42.evolvedmekanism.recipes.SolidificationRecipe;
import fr.iglee42.evolvedmekanism.tiles.machine.TileEntitySolidifier;
import mekanism.api.IContentsListener;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.slot.InputInventorySlot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class MeSolidifierBlockEntity extends TileEntitySolidifier implements ICraftingProvider, MeSmartCableConnection, IActionHost, MeAeMachine {
    private MeRecipeMachineAeSupport<MeSolidifierBlockEntity> aeSupport;
    private AeOutputMode aeOutputMode = AeOutputMode.BOTH;

    public MeSolidifierBlockEntity(MeMekanismMachine machine, BlockPos pos, BlockState state) {
        super(pos, state);
        getRecipeAeSupport();
    }

    @Override
    public MeRecipeMachineAeSupport<MeSolidifierBlockEntity> getRecipeAeSupport() {
        if (this.aeSupport == null) {
            this.aeSupport = new MeRecipeMachineAeSupport<>(this);
        }
        return this.aeSupport;
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
    public CachedRecipe<SolidificationRecipe> createNewCachedRecipe(@NotNull SolidificationRecipe recipe, int cacheIndex) {
        return getRecipeAeSupport().wrapRecipeEnergy(getEnergyContainer(), super.createNewCachedRecipe(recipe, cacheIndex));
    }

    @Override
    public MeInputLayout getPatternInputLayout() {
        TileEntitySolidifierAccessor accessor = (TileEntitySolidifierAccessor) this;
        InputInventorySlot inputSlot = accessor.mekenergistics$getInputSlot();
        BasicFluidTank fluidTank = accessor.mekenergistics$getInputFluidTank();
        BasicFluidTank extraTank = accessor.mekenergistics$getInputFluidExtraTank();
        MeInputPort itemPort = MeMachineIoAdapter.itemInput(inputSlot);
        MeInputPort fluidPort = MeMachineIoAdapter.fluidInput(fluidTank);
        MeInputPort extraPort = MeMachineIoAdapter.fluidInput(extraTank);
        return MeInputLayout.lanes(java.util.List.of(
                java.util.List.of(itemPort), java.util.List.of(fluidPort, extraPort),
                java.util.List.of(fluidPort, extraPort)));
    }

    @Override public java.util.List<? extends MeOutputPort> getPatternOutputPorts() { return java.util.List.of(
            MeMachineIoAdapter.itemOutput(((TileEntitySolidifierAccessor) this).mekenergistics$getOutputSlot())); }
    @Override public MeMekanismMachine getMachine() { return MeMekanismMachine.SOLIDIFICATION_CHAMBER; }
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
