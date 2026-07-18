package com.beipuo.mekenergistics.blockentity.support;

import com.beipuo.mekenergistics.blockentity.api.AeOutputMode;

import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.networking.GridFlags;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageService;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageHelper;
import com.beipuo.mekenergistics.blockentity.MeMekanismMachineBlockEntity;
import com.beipuo.mekenergistics.blockentity.api.MeAeMachine;
import com.beipuo.mekenergistics.blockentity.slot.MePatternInventorySlot;
import com.beipuo.mekenergistics.blockentity.support.io.MeMachineIoAdapter;
import com.beipuo.mekenergistics.blockentity.support.io.MePatternInputRouter;
import com.beipuo.mekenergistics.common.machine.MeMekanismMachine;
import com.beipuo.mekenergistics.config.MekEnergisticsConfig;
import com.beipuo.mekenergistics.registry.ModBlocks;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.tile.base.TileEntityMekanism;
import me.ramidzkh.mekae2.ae2.MekanismKey;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

public final class MeRecipeMachineAeSupport<TILE extends TileEntityMekanism & MeAeMachine & ICraftingProvider & IActionHost>
        extends AbstractMeAeSupport<TILE> {
    private final List<OutputInventorySlot> knownOutputSlots = new ArrayList<>();
    private final List<IChemicalTank> knownChemicalOutputTanks = new ArrayList<>();
    private final List<IExtendedFluidTank> knownFluidOutputTanks = new ArrayList<>();

    public MeRecipeMachineAeSupport(TILE owner) {
        super(owner);
    }

    public void addAeTrackers(MekanismContainer container, Supplier<AeOutputMode> outputModeSupplier,
            Consumer<AeOutputMode> outputModeSetter, boolean trackSmartPatternMultiplication) {
        container.track(SyncableInt.create(() -> outputModeSupplier.get().ordinal(), mode -> outputModeSetter.accept(AeOutputMode.byId(mode))));
        if (trackSmartPatternMultiplication) {
            container.track(SyncableBoolean.create(this.owner::isSmartPatternMultiplicationEnabled, this.owner::setSmartPatternMultiplicationEnabled));
        }
    }

    public boolean insertOutputSlotIntoNetwork(OutputInventorySlot outputSlot, AeOutputMode mode) {
        rememberOutputSlot(outputSlot);
        if (outputSlot == null) {
            return false;
        }
        return drainOutputPorts(mode, List.of(MeMachineIoAdapter.itemOutput(outputSlot)));
    }

    public boolean insertOutputSlotsIntoNetwork(AeOutputMode mode, OutputInventorySlot... outputSlots) {
        boolean changed = false;
        for (OutputInventorySlot outputSlot : outputSlots) {
            rememberOutputSlot(outputSlot);
            changed |= drainOutputPorts(mode, List.of(MeMachineIoAdapter.itemOutput(outputSlot)));
        }
        return changed;
    }

    public boolean insertChemicalTankIntoNetwork(IChemicalTank tank, AeOutputMode mode) {
        rememberChemicalTank(tank);
        if (tank == null) {
            return false;
        }
        return drainOutputPorts(mode, List.of(MeMachineIoAdapter.chemicalOutput(tank)));
    }

    public boolean insertFluidTankIntoNetwork(IExtendedFluidTank tank, AeOutputMode mode) {
        rememberFluidTank(tank);
        if (tank == null) {
            return false;
        }
        return drainOutputPorts(mode, List.of(MeMachineIoAdapter.fluidOutput(tank)));
    }

    public boolean drainOutputs(AeOutputMode mode, boolean sendUpdatePacket, OutputInventorySlot... outputSlots) {
        return insertOutputSlotsIntoNetwork(mode, outputSlots) || sendUpdatePacket;
    }

    public boolean drainOutputs(AeOutputMode mode, boolean sendUpdatePacket, Iterable<? extends IInventorySlot> outputSlots) {
        boolean changed = false;
        for (IInventorySlot slot : outputSlots) {
            if (slot instanceof OutputInventorySlot outputSlot) {
                changed |= insertOutputSlotIntoNetwork(outputSlot, mode);
            }
        }
        return changed || sendUpdatePacket;
    }

    public boolean drainChemicalOutputs(AeOutputMode mode, boolean sendUpdatePacket, IChemicalTank... tanks) {
        boolean changed = false;
        for (IChemicalTank tank : tanks) {
            changed |= insertChemicalTankIntoNetwork(tank, mode);
        }
        return changed || sendUpdatePacket;
    }

    public boolean drainFluidOutputs(AeOutputMode mode, boolean sendUpdatePacket, IExtendedFluidTank... tanks) {
        boolean changed = false;
        for (IExtendedFluidTank tank : tanks) {
            changed |= insertFluidTankIntoNetwork(tank, mode);
        }
        return changed || sendUpdatePacket;
    }

    public boolean drainMixedOutputs(AeOutputMode mode, boolean sendUpdatePacket, OutputInventorySlot outputSlot, IChemicalTank chemicalTank) {
        boolean changed = insertOutputSlotIntoNetwork(outputSlot, mode);
        changed |= insertChemicalTankIntoNetwork(chemicalTank, mode);
        return changed || sendUpdatePacket;
    }

    public boolean drainMixedOutputs(AeOutputMode mode, boolean sendUpdatePacket, OutputInventorySlot outputSlot, IExtendedFluidTank fluidTank) {
        boolean changed = insertOutputSlotIntoNetwork(outputSlot, mode);
        changed |= insertFluidTankIntoNetwork(fluidTank, mode);
        return changed || sendUpdatePacket;
    }

    public boolean pushSingleItem(KeyCounter[] inputHolder, IInventorySlot inputSlot) {
        if (inputHolder == null || inputHolder.length != 1 || inputSlot == null) {
            return false;
        }
        boolean changed = MePatternInputRouter.route(inputHolder,
                List.of(MeMachineIoAdapter.itemInput(inputSlot)));
        if (changed) {
            this.owner.setChanged();
        }
        return changed;
    }

    private void rememberOutputSlot(OutputInventorySlot outputSlot) {
        if (outputSlot != null && !this.knownOutputSlots.contains(outputSlot)) {
            this.knownOutputSlots.add(outputSlot);
        }
    }

    private void rememberChemicalTank(IChemicalTank tank) {
        if (tank != null && !this.knownChemicalOutputTanks.contains(tank)) {
            this.knownChemicalOutputTanks.add(tank);
        }
    }

    private void rememberFluidTank(IExtendedFluidTank tank) {
        if (tank != null && !this.knownFluidOutputTanks.contains(tank)) {
            this.knownFluidOutputTanks.add(tank);
        }
    }

    @Override
    protected boolean hasAeOutputWork() {
        AeOutputMode mode = this.owner.getAeOutputMode();
        if (mode.items()) {
            for (OutputInventorySlot slot : this.knownOutputSlots) {
                if (slot != null && !slot.getStack().isEmpty()) {
                    return true;
                }
            }
            for (IExtendedFluidTank tank : this.knownFluidOutputTanks) {
                if (tank != null && !tank.isEmpty()) {
                    return true;
                }
            }
        }
        if (mode.chemicals()) {
            for (IChemicalTank tank : this.knownChemicalOutputTanks) {
                if (tank != null && !tank.isEmpty()) {
                    return true;
                }
            }
        }
        return this.smartPatternMultiplication.hasPendingWork();
    }

    @Override
    protected boolean processAeOutputWork() {
        boolean hadWork = hasAeOutputWork();
        processSmartPatternViaOwner();
        AeOutputMode mode = this.owner.getAeOutputMode();
        for (OutputInventorySlot slot : this.knownOutputSlots) {
            insertOutputSlotIntoNetwork(slot, mode);
        }
        for (IChemicalTank tank : this.knownChemicalOutputTanks) {
            insertChemicalTankIntoNetwork(tank, mode);
        }
        for (IExtendedFluidTank tank : this.knownFluidOutputTanks) {
            insertFluidTankIntoNetwork(tank, mode);
        }
        boolean hasWork = hasAeOutputWork();
        if (hasWork) {
            alertAeTicker();
        }
        return hadWork && !hasWork;
    }

    @Override
    protected String patternOwnerName() {
        return this.owner.getBlockState().getBlock().getDescriptionId();
    }

    public void saveAeState(CompoundTag tag, HolderLookup.Provider registries, AeOutputMode aeOutputMode) {
        tag.putInt("AeOutputMode", aeOutputMode.ordinal());
        saveCommon(tag, registries);
    }

    public AeOutputMode loadAeState(CompoundTag tag, HolderLookup.Provider registries) {
        AeOutputMode aeOutputMode = AeOutputMode.byId(tag.getInt("AeOutputMode"));
        loadCommon(tag, registries);
        return aeOutputMode;
    }

    public static final class AeBackedEnergyContainer<TILE extends TileEntityMekanism>
            extends MeNetworkEnergyHelper.NetworkBackedEnergyContainer<TILE> {
        private final MeAeMachine aeMachine;

        public AeBackedEnergyContainer(TILE owner, MeRecipeMachineAeSupport<?> support, IContentsListener listener) {
            super(owner, listener, () -> ((MeAeMachine) owner).getGrid(), () -> recipeActionSource((MeAeMachine) owner));
            this.aeMachine = (MeAeMachine) owner;
        }

        private IActionSource actionSource() {
            return recipeActionSource(this.aeMachine);
        }
    }

    public static <RECIPE extends MekanismRecipe<?>> CachedRecipe<RECIPE> withAeRecipeEnergy(
            MachineEnergyContainer<?> energyContainer, CachedRecipe<RECIPE> cachedRecipe) {
        return energyContainer instanceof AeBackedEnergyContainer<?> aeBackedEnergyContainer
                ? withAeRecipeEnergy(aeBackedEnergyContainer.aeMachine, aeBackedEnergyContainer.actionSource(), energyContainer, cachedRecipe)
                : cachedRecipe;
    }

    public <RECIPE extends MekanismRecipe<?>> CachedRecipe<RECIPE> wrapRecipeEnergy(
            MachineEnergyContainer<?> energyContainer, CachedRecipe<RECIPE> cachedRecipe) {
        return withAeRecipeEnergy(this.owner, this.actionSource, energyContainer, cachedRecipe);
    }

    public static <RECIPE extends MekanismRecipe<?>> CachedRecipe<RECIPE> withAeRecipeEnergy(
            MeAeMachine aeMachine, IActionSource actionSource, MachineEnergyContainer<?> energyContainer, CachedRecipe<RECIPE> cachedRecipe) {
        return cachedRecipe.setEnergyRequirements(energyContainer::getEnergyPerTick,
                MeNetworkEnergyHelper.recipeEnergyView(energyContainer, aeMachine::getGrid, actionSource));
    }

    private static IActionSource recipeActionSource(MeAeMachine aeMachine) {
        MeRecipeMachineAeSupport<?> support = aeMachine.getRecipeAeSupport();
        return support == null ? null : support.actionSource;
    }

}
