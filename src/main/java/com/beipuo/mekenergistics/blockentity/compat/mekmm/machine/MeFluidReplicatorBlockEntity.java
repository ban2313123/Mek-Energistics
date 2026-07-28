package com.beipuo.mekenergistics.blockentity.compat.mekmm.machine;

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
import com.beipuo.mekenergistics.mixin.TileEntityFluidReplicatorAccessor;
import com.jerry.mekmm.common.tile.machine.TileEntityFluidReplicator;
import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.slot.chemical.ChemicalInventorySlot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

/** AE adapter for the MEKMM fluid + UU chemical replicator. */
public class MeFluidReplicatorBlockEntity extends TileEntityFluidReplicator
        implements ICraftingProvider, MeSmartCableConnection, IActionHost, MeAeMachine {
    private MeRecipeMachineAeSupport<MeFluidReplicatorBlockEntity> aeSupport;
    private IInventorySlotHolder inventoryHolder;
    private ChemicalInventorySlot conversionSlot;
    private AeOutputMode outputMode = AeOutputMode.BOTH;

    public MeFluidReplicatorBlockEntity(MeMekanismMachine machine, BlockPos pos, BlockState state) {
        super(pos, state);
    }

    private MeRecipeMachineAeSupport<MeFluidReplicatorBlockEntity> support() {
        if (aeSupport == null) {
            aeSupport = new MeRecipeMachineAeSupport<>(this);
        }
        return aeSupport;
    }

    @Override
    public MeRecipeMachineAeSupport<MeFluidReplicatorBlockEntity> getRecipeAeSupport() {
        return support();
    }

    @Override
    protected IFluidTankHolder getInitialFluidTanks(IContentsListener listener,
            IContentsListener recipeCacheListener, IContentsListener recipeCacheUnpauseListener) {
        return super.getInitialFluidTanks(listener, recipeCacheListener, recipeCacheUnpauseListener);
    }

    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener,
            IContentsListener recipeCacheListener, IContentsListener recipeCacheUnpauseListener) {
        EnergyContainerHelper builder = EnergyContainerHelper.forSideWithConfig(this);
        var energy = new MeRecipeMachineAeSupport.AeBackedEnergyContainer<TileEntityFluidReplicator>(
                this, support(), recipeCacheUnpauseListener);
        ((TileEntityFluidReplicatorAccessor) this).mekenergistics$setEnergyContainer(energy);
        builder.addContainer(energy);
        return builder.build();
    }

    @Override
    public IChemicalTankHolder getInitialChemicalTanks(IContentsListener listener,
            IContentsListener recipeCacheListener, IContentsListener recipeCacheUnpauseListener) {
        return super.getInitialChemicalTanks(listener, recipeCacheListener, recipeCacheUnpauseListener);
    }

    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener,
            IContentsListener recipeCacheListener, IContentsListener recipeCacheUnpauseListener) {
        IInventorySlotHolder original = super.getInitialInventory(listener, recipeCacheListener, recipeCacheUnpauseListener);
        this.inventoryHolder = original;
        this.conversionSlot = ((TileEntityFluidReplicatorAccessor) this).mekenergistics$getUuSlot();
        return support().withPatternSlots(original);
    }

    @Override
    protected boolean onUpdateServer() {
        return support().processPatternIo(outputMode, super.onUpdateServer());
    }

    @Override
    public mekanism.api.recipes.cache.CachedRecipe<com.jerry.mekmm.api.recipes.basic.BasicFluidChemicalToFluidRecipe>
            createNewCachedRecipe(@NotNull com.jerry.mekmm.api.recipes.basic.BasicFluidChemicalToFluidRecipe recipe, int cacheIndex) {
        return support().wrapRecipeEnergy(getEnergyContainer(), super.createNewCachedRecipe(recipe, cacheIndex));
    }

    @Override
    public MeInputLayout getPatternInputLayout() {
        if (this.inputTank == null || this.uuTank == null || this.conversionSlot == null) return MeInputLayout.empty();
        MeInputPort fluid = MeMachineIoAdapter.fluidInput(this.inputTank);
        List<MeInputPort> chemical = List.of(MeMachineIoAdapter.chemicalInput(this.uuTank),
                MeMachineIoAdapter.itemInput(this.conversionSlot));
        return MeInputLayout.lanes(List.of(List.of(fluid), chemical));
    }

    @Override public List<? extends MeOutputPort> getPatternOutputPorts() { return this.outputTank == null
            ? List.of() : List.of(MeMachineIoAdapter.fluidOutput(this.outputTank)); }
    @Override public MeMekanismMachine getMachine() { return MeMekanismMachine.FLUID_REPLICATOR; }
    @Override public appeng.api.networking.IManagedGridNode getMainNode() { return support().getMainNode(); }
    @Override public AeOutputMode getAeOutputMode() { return outputMode; }
    @Override public void cycleAeOutputMode() { outputMode = outputMode.next(); setChanged(); }
    @Override public void clearRemoved() { super.clearRemoved(); support().createOnFirstTick(); }
    @Override public void setRemoved() { support().destroyNode(); super.setRemoved(); }
    @Override public void onChunkUnloaded() { support().destroyNode(); super.onChunkUnloaded(); }
    @Override public void addContainerTrackers(MekanismContainer container) { super.addContainerTrackers(container); support().addAeTrackers(container, this::getAeOutputMode, mode -> outputMode = mode, false); }
    @Override public void saveAdditional(CompoundTag tag, HolderLookup.@NotNull Provider registries) { super.saveAdditional(tag, registries); support().saveAeState(tag, registries, outputMode); }
    @Override public void loadAdditional(CompoundTag tag, HolderLookup.@NotNull Provider registries) { super.loadAdditional(tag, registries); outputMode = support().loadAeState(tag, registries); }
}
