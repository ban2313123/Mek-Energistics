package com.beipuo.mekenergistics.blockentity.compat.mekmm.machine;

import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.security.IActionHost;
import com.beipuo.mekenergistics.blockentity.api.AeOutputMode;
import com.beipuo.mekenergistics.blockentity.api.MeAeMachine;
import com.beipuo.mekenergistics.blockentity.api.MeSmartCableConnection;
import com.beipuo.mekenergistics.blockentity.support.MeRecipeMachineAeSupport;
import com.beipuo.mekenergistics.blockentity.support.io.MeInputLayout;
import com.beipuo.mekenergistics.blockentity.support.io.MeMachineIoAdapter;
import com.beipuo.mekenergistics.blockentity.support.io.MeOutputPort;
import com.beipuo.mekenergistics.common.machine.MeMekanismMachine;
import com.beipuo.mekenergistics.mixin.TileEntityLargeChemicalInfuserAccessor;
import com.jerry.meklm.common.tile.machine.TileEntityLargeChemicalInfuser;
import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.api.recipes.ChemicalChemicalToChemicalRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.inventory.container.MekanismContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class MeLargeChemicalInfuserBlockEntity extends TileEntityLargeChemicalInfuser
        implements ICraftingProvider, MeSmartCableConnection, IActionHost, MeAeMachine {
    private MeRecipeMachineAeSupport<MeLargeChemicalInfuserBlockEntity> aeSupport;
    private AeOutputMode outputMode = AeOutputMode.BOTH;

    public MeLargeChemicalInfuserBlockEntity(MeMekanismMachine machine, BlockPos pos, BlockState state) {
        super(pos, state);
    }

    private MeRecipeMachineAeSupport<MeLargeChemicalInfuserBlockEntity> support() {
        if (this.aeSupport == null) {
            this.aeSupport = new MeRecipeMachineAeSupport<>(this);
        }
        return this.aeSupport;
    }

    @Override
    public MeRecipeMachineAeSupport<MeLargeChemicalInfuserBlockEntity> getRecipeAeSupport() {
        return support();
    }

    @NotNull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener,
            IContentsListener recipeCacheListener, IContentsListener recipeCacheUnpauseListener) {
        EnergyContainerHelper builder = EnergyContainerHelper.forSide(this.facingSupplier);
        var energy = new MeRecipeMachineAeSupport.AeBackedEnergyContainer<TileEntityLargeChemicalInfuser>(
                this, support(), recipeCacheUnpauseListener);
        ((TileEntityLargeChemicalInfuserAccessor) this).mekenergistics$setEnergyContainer(energy);
        builder.addContainer(energy, RelativeSide.BACK);
        return builder.build();
    }

    @NotNull
    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener,
            IContentsListener recipeCacheListener, IContentsListener recipeCacheUnpauseListener) {
        return support().withPatternSlots(super.getInitialInventory(
                listener, recipeCacheListener, recipeCacheUnpauseListener));
    }

    @Override
    protected boolean onUpdateServer() {
        return support().processPatternIo(this.outputMode, super.onUpdateServer());
    }

    @NotNull
    @Override
    public CachedRecipe<ChemicalChemicalToChemicalRecipe> createNewCachedRecipe(
            @NotNull ChemicalChemicalToChemicalRecipe recipe, int cacheIndex) {
        return support().wrapRecipeEnergy(getEnergyContainer(), super.createNewCachedRecipe(recipe, cacheIndex));
    }

    @Override
    public MeInputLayout getPatternInputLayout() {
        return MeInputLayout.unordered(List.of(
                MeMachineIoAdapter.chemicalInput(this.leftTank),
                MeMachineIoAdapter.chemicalInput(this.rightTank)));
    }

    @Override
    public List<? extends MeOutputPort> getPatternOutputPorts() {
        return List.of(MeMachineIoAdapter.chemicalOutput(this.centerTank));
    }

    @Override public MeMekanismMachine getMachine() { return MeMekanismMachine.LARGE_CHEMICAL_INFUSER; }
    @Override public AeOutputMode getAeOutputMode() { return this.outputMode; }
    @Override public void cycleAeOutputMode() { this.outputMode = this.outputMode.next(); setChanged(); }
    @Override public void clearRemoved() { super.clearRemoved(); support().createOnFirstTick(); }
    @Override public void setRemoved() { support().destroyNode(); super.setRemoved(); }
    @Override public void onChunkUnloaded() { support().destroyNode(); super.onChunkUnloaded(); }
    @Override public void addContainerTrackers(MekanismContainer container) { super.addContainerTrackers(container); support().addAeTrackers(container, this::getAeOutputMode, mode -> this.outputMode = mode, false); }
    @Override public void saveAdditional(CompoundTag tag, HolderLookup.@NotNull Provider registries) { super.saveAdditional(tag, registries); support().saveAeState(tag, registries, this.outputMode); }
    @Override public void loadAdditional(CompoundTag tag, HolderLookup.@NotNull Provider registries) { super.loadAdditional(tag, registries); this.outputMode = support().loadAeState(tag, registries); }
}
