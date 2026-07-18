package com.beipuo.mekenergistics.blockentity.support;

import com.beipuo.mekenergistics.blockentity.api.AeOutputMode;

import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.implementations.blockentities.PatternContainerGroup;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridFlags;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.crafting.ICraftingProvider;
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
import com.beipuo.mekenergistics.blockentity.api.MeFactoryAeMachine;
import com.beipuo.mekenergistics.blockentity.slot.MePatternInventorySlot;
import com.beipuo.mekenergistics.blockentity.slot.PatternSlotInternalInventory;
import com.beipuo.mekenergistics.blockentity.support.io.MeMachineIoAdapter;
import com.beipuo.mekenergistics.blockentity.support.io.MeInputPort;
import com.beipuo.mekenergistics.blockentity.support.io.MePatternInputRouter;
import com.beipuo.mekenergistics.common.machine.MeMekanismMachine;
import com.beipuo.mekenergistics.config.MekEnergisticsConfig;
import com.beipuo.mekenergistics.registry.ModBlocks;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mekanism.api.Action;
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
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.tile.base.TileEntityMekanism;
import me.ramidzkh.mekae2.ae2.MekanismKey;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

public final class MeFactoryAeSupport extends AbstractMeAeSupport<MeFactoryAeMachine> {
    private final InternalInventory terminalPatternInventory = new PatternSlotInternalInventory(new PatternSlotOwner());
    private final List<IInventorySlot> knownOutputSlots = new ArrayList<>();
    private final List<IChemicalTank> knownChemicalOutputTanks = new ArrayList<>();
    private final List<IExtendedFluidTank> knownFluidOutputTanks = new ArrayList<>();
    private boolean ownerHandlesSmartPatternProcessing;
    private boolean suppressFeedSaveChanges;
    private AeOutputMode aeOutputMode = AeOutputMode.BOTH;

    public MeFactoryAeSupport(MeFactoryAeMachine owner) {
        super(owner);
    }

    public boolean processSmartPattern(MeSmartPatternMultiplication.Feeder feeder) {
        markOwnerHandlesSmartPatternProcessing();
        boolean changed;
        this.suppressFeedSaveChanges = true;
        try {
            changed = this.smartPatternMultiplication.processNext(feeder);
        } finally {
            this.suppressFeedSaveChanges = false;
        }
        if (changed) {
            this.owner.saveChanges();
            if (this.smartPatternMultiplication.hasPendingWork()) {
                alertAeTicker();
            }
        }
        return changed;
    }

    public boolean processSmartPatternIfOutputsClear(MeSmartPatternMultiplication.Feeder feeder, List<IInventorySlot> outputSlots) {
        markOwnerHandlesSmartPatternProcessing();
        boolean changed = insertOutputSlotsIntoNetwork(outputSlots);
        return hasItemOutputBacklog(outputSlots) ? changed : processSmartPattern(feeder) || changed;
    }

    public boolean processSmartPatternIfNoItemOutputBacklog(MeSmartPatternMultiplication.Feeder feeder, List<IInventorySlot> outputSlots) {
        markOwnerHandlesSmartPatternProcessing();
        return hasItemOutputBacklog(outputSlots) ? false : processSmartPattern(feeder);
    }

    public boolean processSmartPatternAfterOutputDrain(MeSmartPatternMultiplication.Feeder feeder, List<IInventorySlot> outputSlots, boolean changed) {
        markOwnerHandlesSmartPatternProcessing();
        changed = insertOutputSlotsIntoNetwork(outputSlots) || changed;
        return hasItemOutputBacklog(outputSlots) ? changed : processSmartPattern(feeder) || changed;
    }

    public void markOwnerHandlesSmartPatternProcessing() {
        this.ownerHandlesSmartPatternProcessing = true;
    }

    public boolean suppressesFeedSaveChanges() {
        return this.suppressFeedSaveChanges;
    }

    public AeOutputMode getAeOutputMode() {
        return this.aeOutputMode;
    }

    public void cycleAeOutputMode() {
        this.aeOutputMode = this.aeOutputMode.next();
        this.owner.saveChanges();
    }

    public void cycleAeOutputMode(mekanism.common.lib.transmitter.TransmissionType type) {
        this.aeOutputMode = this.aeOutputMode.toggle(type);
        this.owner.saveChanges();
    }

    public void setAeOutputMode(AeOutputMode aeOutputMode) {
        this.aeOutputMode = aeOutputMode;
    }

    public InternalInventory getTerminalPatternInventory() {
        return this.terminalPatternInventory;
    }

    public PatternContainerGroup getTerminalGroup() {
        ItemStack iconStack = new ItemStack(ModBlocks.getMachineBlock(this.owner.getMachine()).get());
        AEItemKey icon = iconStack.isEmpty() ? null : AEItemKey.of(iconStack);
        String terminalName = getPatternTerminalName();
        Component name = terminalName.isBlank()
                ? Component.translatable(this.owner.getMachine().translationKey())
                : Component.literal(terminalName);
        return new PatternContainerGroup(icon, name, List.of());
    }

    public boolean insertOutputSlotsIntoNetwork(List<IInventorySlot> outputSlots) {
        rememberOutputSlots(outputSlots);
        boolean changed = false;
        for (IInventorySlot outputSlot : outputSlots) {
            if (outputSlot != null) {
                changed |= drainOutputPorts(this.aeOutputMode,
                        List.of(MeMachineIoAdapter.itemOutput(outputSlot)));
            }
        }
        return changed;
    }

    public boolean insertChemicalTanksIntoNetwork(List<IChemicalTank> tanks) {
        rememberChemicalTanks(tanks);
        boolean changed = false;
        for (IChemicalTank tank : tanks) {
            changed |= insertChemicalTankIntoNetwork(tank);
        }
        return changed;
    }

    public boolean insertChemicalTankIntoNetwork(IChemicalTank tank) {
        rememberChemicalTank(tank);
        if (tank == null) {
            return false;
        }
        return drainOutputPorts(this.aeOutputMode, List.of(MeMachineIoAdapter.chemicalOutput(tank)));
    }

    public boolean insertFluidTankIntoNetwork(IExtendedFluidTank tank) {
        rememberFluidTank(tank);
        if (tank == null) {
            return false;
        }
        return drainOutputPorts(this.aeOutputMode, List.of(MeMachineIoAdapter.fluidOutput(tank)));
    }

    private void rememberOutputSlots(List<IInventorySlot> outputSlots) {
        for (IInventorySlot outputSlot : outputSlots) {
            if (outputSlot != null && !this.knownOutputSlots.contains(outputSlot)) {
                this.knownOutputSlots.add(outputSlot);
            }
        }
    }

    private void rememberChemicalTanks(List<IChemicalTank> tanks) {
        for (IChemicalTank tank : tanks) {
            rememberChemicalTank(tank);
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
        if (hasItemOutputBacklog(this.knownOutputSlots)) {
            return true;
        }
        for (IChemicalTank tank : this.knownChemicalOutputTanks) {
            if (tank != null && !tank.isEmpty()) {
                return true;
            }
        }
        for (IExtendedFluidTank tank : this.knownFluidOutputTanks) {
            if (tank != null && !tank.isEmpty()) {
                return true;
            }
        }
        return this.smartPatternMultiplication.hasPendingWork();
    }

    private static boolean hasItemOutputBacklog(List<IInventorySlot> outputSlots) {
        for (IInventorySlot outputSlot : outputSlots) {
            if (outputSlot != null && !outputSlot.getStack().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean processAeOutputWork() {
        boolean hadWork = hasAeOutputWork();
        if (!this.ownerHandlesSmartPatternProcessing) {
            processSmartPatternViaOwner();
        }
        insertOutputSlotsIntoNetwork(this.knownOutputSlots);
        for (IChemicalTank tank : this.knownChemicalOutputTanks) {
            insertChemicalTankIntoNetwork(tank);
        }
        for (IExtendedFluidTank tank : this.knownFluidOutputTanks) {
            insertFluidTankIntoNetwork(tank);
        }
        boolean hasWork = hasAeOutputWork();
        if (hasWork) {
            alertAeTicker();
        }
        return hadWork && !hasWork;
    }

    public boolean pushSingleItem(KeyCounter[] inputHolder, List<? extends IInventorySlot> inputSlots) {
        boolean changed = MePatternInputRouter.route(inputHolder,
                inputSlots.stream().map(MeMachineIoAdapter::itemInput).toList());
        if (changed) {
            this.owner.saveChanges();
        }
        return changed;
    }

    public boolean pushItemChemical(KeyCounter[] inputHolder, List<? extends IInventorySlot> inputSlots,
            IChemicalTank chemicalTank) {
        if (chemicalTank == null) {
            return false;
        }
        boolean changed = MePatternInputRouter.route(inputHolder,
                java.util.stream.Stream.concat(
                        inputSlots.stream().map(MeMachineIoAdapter::itemInput),
                        java.util.stream.Stream.of(MeMachineIoAdapter.chemicalInput(chemicalTank)))
                        .toList());
        if (changed) {
            this.owner.saveChanges();
        }
        return changed;
    }

    public boolean pushSingleItemWithRequiredExtraSlot(KeyCounter[] inputHolder,
            List<? extends IInventorySlot> inputSlots, IInventorySlot extraSlot) {
        return extraSlot != null && !extraSlot.getStack().isEmpty()
                && pushSingleItem(inputHolder, inputSlots);
    }

    public boolean pushChemical(KeyCounter[] inputHolder, List<? extends IChemicalTank> chemicalTanks) {
        boolean changed = MePatternInputRouter.route(inputHolder,
                chemicalTanks.stream().map(MeMachineIoAdapter::chemicalInput).toList());
        if (changed) {
            this.owner.saveChanges();
        }
        return changed;
    }

    public boolean pushFluidChemical(KeyCounter[] inputHolder, IExtendedFluidTank fluidTank,
            List<? extends IChemicalTank> chemicalTanks) {
        List<MeInputPort> ports = new ArrayList<>();
        if (fluidTank != null) {
            ports.add(MeMachineIoAdapter.fluidInput(fluidTank));
        }
        ports.addAll(chemicalTanks.stream().map(MeMachineIoAdapter::chemicalInput).toList());
        boolean changed = MePatternInputRouter.route(inputHolder, ports);
        if (changed) {
            this.owner.saveChanges();
        }
        return changed;
    }

    public boolean pushItemFluidChemical(KeyCounter[] inputHolder,
            List<? extends IInventorySlot> inputSlots, IExtendedFluidTank fluidTank,
            IChemicalTank chemicalTank) {
        List<MeInputPort> ports = new ArrayList<>(inputSlots.stream()
                .map(MeMachineIoAdapter::itemInput).toList());
        if (fluidTank != null) {
            ports.add(MeMachineIoAdapter.fluidInput(fluidTank));
        }
        if (chemicalTank != null) {
            ports.add(MeMachineIoAdapter.chemicalInput(chemicalTank));
        }
        boolean changed = MePatternInputRouter.route(inputHolder, ports);
        if (changed) {
            this.owner.saveChanges();
        }
        return changed;
    }

    public boolean pushTwoItems(KeyCounter[] inputHolder, List<? extends IInventorySlot> inputSlots,
            IInventorySlot extraSlot) {
        if (inputHolder == null || inputHolder.length != 2 || extraSlot == null) {
            return false;
        }
        MeInputPort extra = MeMachineIoAdapter.itemInput(extraSlot);
        java.util.Map<MeInputPort, Object> snapshots = new java.util.IdentityHashMap<>();
        List<MeInputPort> main = inputSlots.stream().map(MeMachineIoAdapter::itemInput).toList();
        main.forEach(port -> snapshots.put(port, port.snapshot()));
        snapshots.put(extra, extra.snapshot());
        if (MePatternInputRouter.route(new KeyCounter[]{inputHolder[0]}, main)
                && MePatternInputRouter.route(new KeyCounter[]{inputHolder[1]}, List.of(extra))) {
            this.owner.saveChanges();
            return true;
        }
        snapshots.forEach(MeInputPort::restore);
        return false;
    }

    public boolean pushThreeItems(KeyCounter[] inputHolder, List<? extends IInventorySlot> inputSlots,
            IInventorySlot secondSlot, IInventorySlot thirdSlot) {
        if (inputHolder == null || inputHolder.length != 3 || secondSlot == null || thirdSlot == null) {
            return false;
        }
        MeInputPort second = MeMachineIoAdapter.itemInput(secondSlot);
        MeInputPort third = MeMachineIoAdapter.itemInput(thirdSlot);
        java.util.Map<MeInputPort, Object> snapshots = new java.util.IdentityHashMap<>();
        List<MeInputPort> main = inputSlots.stream().map(MeMachineIoAdapter::itemInput).toList();
        main.forEach(port -> snapshots.put(port, port.snapshot()));
        snapshots.put(second, second.snapshot());
        snapshots.put(third, third.snapshot());
        if (MePatternInputRouter.route(new KeyCounter[]{inputHolder[0]}, main)
                && MePatternInputRouter.route(new KeyCounter[]{inputHolder[1]}, List.of(second))
                && MePatternInputRouter.route(new KeyCounter[]{inputHolder[2]}, List.of(third))) {
            this.owner.saveChanges();
            return true;
        }
        snapshots.forEach(MeInputPort::restore);
        return false;
    }

    public boolean processSingleItemSmartPatterns(List<IInventorySlot> outputSlots,
            List<IInventorySlot> inputSlots) {
        markOwnerHandlesSmartPatternProcessing();
        boolean changed = insertOutputSlotsIntoNetwork(outputSlots);
        if (hasItemOutputBacklog(outputSlots)) {
            return changed;
        }
        return processSmartPattern(new SingleItemPortFeeder(inputSlots)) || changed;
    }

    public boolean finishSingleItemSmartPatterns(List<IInventorySlot> inputSlots) {
        markOwnerHandlesSmartPatternProcessing();
        return processSmartPattern(new SingleItemPortFeeder(inputSlots));
    }

    public boolean processSingleItemSmartPatterns(List<IInventorySlot> outputSlots,
            IExtendedFluidTank outputTank, List<IInventorySlot> inputSlots) {
        markOwnerHandlesSmartPatternProcessing();
        boolean changed = insertOutputSlotsIntoNetwork(outputSlots);
        changed |= insertFluidTankIntoNetwork(outputTank);
        if (hasItemOutputBacklog(outputSlots) || outputTank != null && !outputTank.isEmpty()) {
            return changed;
        }
        return processSmartPattern(new SingleItemPortFeeder(inputSlots)) || changed;
    }

    public boolean finishSingleItemSmartPatterns(List<IInventorySlot> inputSlots, IExtendedFluidTank outputTank) {
        markOwnerHandlesSmartPatternProcessing();
        return processSmartPattern(new SingleItemPortFeeder(inputSlots));
    }

    public boolean processSingleItemWithRequiredExtraSlotSmartPatterns(IInventorySlot extraSlot,
            List<IInventorySlot> outputSlots, List<IInventorySlot> inputSlots) {
        if (extraSlot == null || extraSlot.getStack().isEmpty()) {
            return false;
        }
        return processSingleItemSmartPatterns(outputSlots, inputSlots);
    }

    public boolean finishSingleItemWithRequiredExtraSlotSmartPatterns(IInventorySlot extraSlot,
            List<IInventorySlot> inputSlots) {
        if (extraSlot == null || extraSlot.getStack().isEmpty()) {
            return false;
        }
        return finishSingleItemSmartPatterns(inputSlots);
    }

    public boolean processItemChemicalSmartPatterns(IChemicalTank inputTank,
            List<IInventorySlot> outputSlots, List<IChemicalTank> outputTanks,
            List<IInventorySlot> inputSlots) {
        markOwnerHandlesSmartPatternProcessing();
        boolean changed = insertOutputSlotsIntoNetwork(outputSlots);
        for (IChemicalTank outputTank : outputTanks) {
            changed |= insertChemicalTankIntoNetwork(outputTank);
        }
        if (hasItemOutputBacklog(outputSlots) || hasChemicalOutputBacklog(outputTanks)) {
            return changed;
        }
        return processSmartPattern(new ItemChemicalPortFeeder(inputSlots, inputTank)) || changed;
    }

    public boolean finishItemChemicalSmartPatterns(List<IInventorySlot> inputSlots,
            IChemicalTank inputTank) {
        markOwnerHandlesSmartPatternProcessing();
        return processSmartPattern(new ItemChemicalPortFeeder(inputSlots, inputTank));
    }

    public boolean processChemicalSmartPatterns(List<IChemicalTank> inputTanks,
            List<IInventorySlot> outputSlots, List<IChemicalTank> outputTanks) {
        markOwnerHandlesSmartPatternProcessing();
        boolean changed = insertOutputSlotsIntoNetwork(outputSlots);
        for (IChemicalTank outputTank : outputTanks) {
            changed |= insertChemicalTankIntoNetwork(outputTank);
        }
        if (hasItemOutputBacklog(outputSlots) || hasChemicalOutputBacklog(outputTanks)) {
            return changed;
        }
        return processSmartPattern(new ChemicalPortFeeder(inputTanks)) || changed;
    }

    public boolean finishChemicalSmartPatterns(List<IChemicalTank> inputTanks) {
        markOwnerHandlesSmartPatternProcessing();
        return processSmartPattern(new ChemicalPortFeeder(inputTanks));
    }

    public boolean processFluidChemicalSmartPatterns(IExtendedFluidTank fluidTank,
            List<IChemicalTank> inputTanks, List<IInventorySlot> outputSlots,
            List<IChemicalTank> outputTanks) {
        markOwnerHandlesSmartPatternProcessing();
        boolean changed = insertOutputSlotsIntoNetwork(outputSlots);
        for (IChemicalTank outputTank : outputTanks) {
            changed |= insertChemicalTankIntoNetwork(outputTank);
        }
        if (hasItemOutputBacklog(outputSlots) || hasChemicalOutputBacklog(outputTanks)) {
            return changed;
        }
        return processSmartPattern(new FluidChemicalPortFeeder(fluidTank, inputTanks)) || changed;
    }

    public boolean finishFluidChemicalSmartPatterns(IExtendedFluidTank fluidTank,
            List<IChemicalTank> inputTanks) {
        markOwnerHandlesSmartPatternProcessing();
        return processSmartPattern(new FluidChemicalPortFeeder(fluidTank, inputTanks));
    }

    public boolean processItemFluidChemicalSmartPatterns(List<IInventorySlot> inputSlots,
            IExtendedFluidTank fluidTank, IChemicalTank chemicalTank, List<IInventorySlot> outputSlots,
            List<IChemicalTank> outputTanks) {
        markOwnerHandlesSmartPatternProcessing();
        boolean changed = insertOutputSlotsIntoNetwork(outputSlots);
        for (IChemicalTank outputTank : outputTanks) {
            changed |= insertChemicalTankIntoNetwork(outputTank);
        }
        if (hasItemOutputBacklog(outputSlots) || hasChemicalOutputBacklog(outputTanks)) {
            return changed;
        }
        return processSmartPattern(new ItemFluidChemicalPortFeeder(inputSlots, fluidTank, chemicalTank)) || changed;
    }

    public boolean finishItemFluidChemicalSmartPatterns(List<IInventorySlot> inputSlots,
            IExtendedFluidTank fluidTank, IChemicalTank chemicalTank) {
        markOwnerHandlesSmartPatternProcessing();
        return processSmartPattern(new ItemFluidChemicalPortFeeder(inputSlots, fluidTank, chemicalTank));
    }

    public boolean processTwoItemsSmartPatterns(IInventorySlot extraSlot,
            List<IInventorySlot> outputSlots, List<IInventorySlot> inputSlots) {
        markOwnerHandlesSmartPatternProcessing();
        boolean changed = insertOutputSlotsIntoNetwork(outputSlots);
        if (hasItemOutputBacklog(outputSlots)) {
            return changed;
        }
        return processSmartPattern(new TwoItemsPortFeeder(inputSlots, extraSlot)) || changed;
    }

    public boolean finishTwoItemsSmartPatterns(List<IInventorySlot> inputSlots, IInventorySlot extraSlot) {
        markOwnerHandlesSmartPatternProcessing();
        return processSmartPattern(new TwoItemsPortFeeder(inputSlots, extraSlot));
    }

    public boolean processThreeItemsSmartPatterns(IInventorySlot secondSlot, IInventorySlot thirdSlot,
            List<IInventorySlot> outputSlots, List<IInventorySlot> inputSlots) {
        markOwnerHandlesSmartPatternProcessing();
        boolean changed = insertOutputSlotsIntoNetwork(outputSlots);
        if (hasItemOutputBacklog(outputSlots)) {
            return changed;
        }
        return processSmartPattern(new ThreeItemsPortFeeder(inputSlots, secondSlot, thirdSlot)) || changed;
    }

    public boolean finishThreeItemsSmartPatterns(List<IInventorySlot> inputSlots,
            IInventorySlot secondSlot, IInventorySlot thirdSlot) {
        markOwnerHandlesSmartPatternProcessing();
        return processSmartPattern(new ThreeItemsPortFeeder(inputSlots, secondSlot, thirdSlot));
    }

    private boolean hasChemicalOutputBacklog(List<IChemicalTank> outputTanks) {
        for (IChemicalTank outputTank : outputTanks) {
            if (outputTank != null && !outputTank.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private final class SingleItemPortFeeder implements MeSmartPatternMultiplication.CapacityAwareFeeder {
        private final List<IInventorySlot> inputSlots;

        private SingleItemPortFeeder(List<IInventorySlot> inputSlots) {
            this.inputSlots = inputSlots;
        }

        @Override
        public boolean feed(KeyCounter[] oneCraftInputs) {
            return pushSingleItem(oneCraftInputs, this.inputSlots);
        }

        @Override
        public long maxAcceptedCopies(KeyCounter[] oneCraftInputs) {
            MeFactoryPatternInput input = MeFactoryPatternInput.single(oneCraftInputs == null || oneCraftInputs.length == 0
                    ? null : oneCraftInputs[0]);
            return input == null || !input.isItem()
                    ? 0 : MeFactoryInventoryInsert.acceptedCopiesAcrossSlots(this.inputSlots, input.item());
        }
    }

    private final class ItemChemicalPortFeeder implements MeSmartPatternMultiplication.CapacityAwareFeeder {
        private final List<IInventorySlot> inputSlots;
        private final IChemicalTank inputTank;

        private ItemChemicalPortFeeder(List<IInventorySlot> inputSlots, IChemicalTank inputTank) {
            this.inputSlots = inputSlots;
            this.inputTank = inputTank;
        }

        @Override
        public boolean feed(KeyCounter[] oneCraftInputs) {
            return pushItemChemical(oneCraftInputs, this.inputSlots, this.inputTank);
        }

        @Override
        public long maxAcceptedCopies(KeyCounter[] oneCraftInputs) {
            MeFactoryPatternInput input = MeFactoryPatternInput.separate(oneCraftInputs);
            if (input == null || input.item().isEmpty() || input.chemical().isEmpty() || !input.fluid().isEmpty()) {
                return 0;
            }
            return Math.min(MeFactoryInventoryInsert.acceptedCopiesAcrossSlots(this.inputSlots, input.item()),
                    MeFactoryInventoryInsert.acceptedCopiesIntoChemicalTank(this.inputTank, input.chemical()));
        }
    }

    private final class ChemicalPortFeeder implements MeSmartPatternMultiplication.CapacityAwareFeeder {
        private final List<IChemicalTank> inputTanks;

        private ChemicalPortFeeder(List<IChemicalTank> inputTanks) {
            this.inputTanks = inputTanks;
        }

        @Override
        public boolean feed(KeyCounter[] oneCraftInputs) {
            return pushChemical(oneCraftInputs, this.inputTanks);
        }

        @Override
        public long maxAcceptedCopies(KeyCounter[] oneCraftInputs) {
            MeFactoryPatternInput input = MeFactoryPatternInput.single(oneCraftInputs == null || oneCraftInputs.length == 0
                    ? null : oneCraftInputs[0]);
            if (input == null || !input.isChemical()) {
                return 0;
            }
            long accepted = 0;
            for (IChemicalTank tank : this.inputTanks) {
                accepted = Math.max(accepted, MeFactoryInventoryInsert.acceptedCopiesIntoChemicalTank(tank, input.chemical()));
            }
            return accepted;
        }
    }

    private final class FluidChemicalPortFeeder implements MeSmartPatternMultiplication.CapacityAwareFeeder {
        private final IExtendedFluidTank fluidTank;
        private final List<IChemicalTank> inputTanks;

        private FluidChemicalPortFeeder(IExtendedFluidTank fluidTank, List<IChemicalTank> inputTanks) {
            this.fluidTank = fluidTank;
            this.inputTanks = inputTanks;
        }

        @Override
        public boolean feed(KeyCounter[] oneCraftInputs) {
            return pushFluidChemical(oneCraftInputs, this.fluidTank, this.inputTanks);
        }

        @Override
        public long maxAcceptedCopies(KeyCounter[] oneCraftInputs) {
            MeFactoryPatternInput input = MeFactoryPatternInput.separate(oneCraftInputs);
            if (input == null || input.fluid().isEmpty() || input.chemical().isEmpty() || !input.item().isEmpty()) {
                return 0;
            }
            long chemical = 0;
            for (IChemicalTank tank : this.inputTanks) {
                chemical = Math.max(chemical, MeFactoryInventoryInsert.acceptedCopiesIntoChemicalTank(tank, input.chemical()));
            }
            return Math.min(chemical, MeFactoryInventoryInsert.acceptedCopiesIntoFluidTank(this.fluidTank, input.fluid()));
        }
    }

    private final class ItemFluidChemicalPortFeeder implements MeSmartPatternMultiplication.CapacityAwareFeeder {
        private final List<IInventorySlot> inputSlots;
        private final IExtendedFluidTank fluidTank;
        private final IChemicalTank chemicalTank;

        private ItemFluidChemicalPortFeeder(List<IInventorySlot> inputSlots, IExtendedFluidTank fluidTank,
                IChemicalTank chemicalTank) {
            this.inputSlots = inputSlots;
            this.fluidTank = fluidTank;
            this.chemicalTank = chemicalTank;
        }

        @Override
        public boolean feed(KeyCounter[] oneCraftInputs) {
            return pushItemFluidChemical(oneCraftInputs, this.inputSlots, this.fluidTank, this.chemicalTank);
        }

        @Override
        public long maxAcceptedCopies(KeyCounter[] oneCraftInputs) {
            MeFactoryPatternInput input = MeFactoryPatternInput.separate(oneCraftInputs);
            if (input == null || input.item().isEmpty() || input.fluid().isEmpty() || input.chemical().isEmpty()) {
                return 0;
            }
            long items = MeFactoryInventoryInsert.acceptedCopiesAcrossSlots(this.inputSlots, input.item());
            long fluid = MeFactoryInventoryInsert.acceptedCopiesIntoFluidTank(this.fluidTank, input.fluid());
            long chemical = MeFactoryInventoryInsert.acceptedCopiesIntoChemicalTank(this.chemicalTank, input.chemical());
            return Math.min(items, Math.min(fluid, chemical));
        }
    }

    private final class TwoItemsPortFeeder implements MeSmartPatternMultiplication.CapacityAwareFeeder {
        private final List<IInventorySlot> inputSlots;
        private final IInventorySlot extraSlot;

        private TwoItemsPortFeeder(List<IInventorySlot> inputSlots, IInventorySlot extraSlot) {
            this.inputSlots = inputSlots;
            this.extraSlot = extraSlot;
        }

        @Override public boolean feed(KeyCounter[] inputs) { return pushTwoItems(inputs, this.inputSlots, this.extraSlot); }

        @Override public long maxAcceptedCopies(KeyCounter[] inputs) {
            if (inputs == null || inputs.length != 2) return 0;
            MeFactoryPatternInput first = MeFactoryPatternInput.single(inputs[0]);
            MeFactoryPatternInput second = MeFactoryPatternInput.single(inputs[1]);
            if (first == null || second == null || !first.isItem() || !second.isItem()) return 0;
            return Math.min(MeFactoryInventoryInsert.acceptedCopiesAcrossSlots(this.inputSlots, first.item()),
                    MeFactoryInventoryInsert.acceptedCopiesIntoSlot(this.extraSlot, second.item()));
        }
    }

    private final class ThreeItemsPortFeeder implements MeSmartPatternMultiplication.CapacityAwareFeeder {
        private final List<IInventorySlot> inputSlots;
        private final IInventorySlot secondSlot;
        private final IInventorySlot thirdSlot;

        private ThreeItemsPortFeeder(List<IInventorySlot> inputSlots, IInventorySlot secondSlot, IInventorySlot thirdSlot) {
            this.inputSlots = inputSlots;
            this.secondSlot = secondSlot;
            this.thirdSlot = thirdSlot;
        }

        @Override public boolean feed(KeyCounter[] inputs) { return pushThreeItems(inputs, this.inputSlots, this.secondSlot, this.thirdSlot); }

        @Override public long maxAcceptedCopies(KeyCounter[] inputs) {
            if (inputs == null || inputs.length != 3) return 0;
            MeFactoryPatternInput first = MeFactoryPatternInput.single(inputs[0]);
            MeFactoryPatternInput second = MeFactoryPatternInput.single(inputs[1]);
            MeFactoryPatternInput third = MeFactoryPatternInput.single(inputs[2]);
            if (first == null || second == null || third == null || !first.isItem() || !second.isItem() || !third.isItem()) return 0;
            return Math.min(MeFactoryInventoryInsert.acceptedCopiesAcrossSlots(this.inputSlots, first.item()),
                    Math.min(MeFactoryInventoryInsert.acceptedCopiesIntoSlot(this.secondSlot, second.item()),
                            MeFactoryInventoryInsert.acceptedCopiesIntoSlot(this.thirdSlot, third.item())));
        }
    }

    @Override
    protected String patternOwnerName() {
        return this.owner.getMachine().name();
    }

    public static IEnergyContainer recipeEnergyView(MachineEnergyContainer<?> energyContainer) {
        return energyContainer instanceof AeBackedFactoryEnergyContainer<?> aeBackedEnergyContainer
                ? recipeEnergyView(aeBackedEnergyContainer.owner instanceof MeFactoryAeMachine aeMachine ? aeMachine : null, aeBackedEnergyContainer)
                : energyContainer;
    }

    public static IEnergyContainer recipeEnergyView(MeFactoryAeMachine owner, MachineEnergyContainer<?> energyContainer) {
        return owner == null ? energyContainer
                : MeNetworkEnergyHelper.recipeEnergyView(energyContainer, () -> owner.getAeSupport().getGrid(), owner.getAeSupport().actionSource);
    }

    public static <RECIPE extends MekanismRecipe<?>> CachedRecipe<RECIPE> withAeRecipeEnergy(
            MachineEnergyContainer<?> energyContainer, CachedRecipe<RECIPE> cachedRecipe) {
        return cachedRecipe.setEnergyRequirements(energyContainer::getEnergyPerTick, recipeEnergyView(energyContainer));
    }

    public static <RECIPE extends MekanismRecipe<?>> CachedRecipe<RECIPE> withAeRecipeEnergy(
            MeFactoryAeMachine owner, MachineEnergyContainer<?> energyContainer, CachedRecipe<RECIPE> cachedRecipe) {
        return cachedRecipe.setEnergyRequirements(energyContainer::getEnergyPerTick, recipeEnergyView(owner, energyContainer));
    }

    public void createNodeOnFirstTick(TileEntityMekanism tile) {
        createOnFirstTick();
    }

    public void destroy() {
        destroyNode();
    }

    public void saveAll(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("AeOutputMode", this.aeOutputMode.ordinal());
        saveCommon(tag, registries);
    }

    public void loadAll(CompoundTag tag, HolderLookup.Provider registries) {
        this.aeOutputMode = AeOutputMode.byId(tag.getInt("AeOutputMode"));
        loadCommon(tag, registries);
    }

    private final class PatternSlotOwner implements MeAeMachine {
        @Override
        public TileEntityMekanism getAeOwnerTile() {
            return ownerTile;
        }
        @Override
        public AeOutputMode getAeOutputMode() {
            return AeOutputMode.BOTH;
        }

        @Override
        public void cycleAeOutputMode() {
        }

        @Override
        public void setOwner(net.minecraft.server.level.ServerPlayer player) {
            mainNode.setOwningPlayer(player);
        }

        @Override
        public List<BasicInventorySlot> getPatternSlots() {
            return patternSlots;
        }

        @Override
        public MeMekanismMachine getMachine() {
            return owner.getMachine();
        }

        @Override
        public ItemStack getTerminalIconStack() {
            return new ItemStack(ModBlocks.getMachineBlock(owner.getMachine()).get());
        }

        @Override
        public IGrid getGrid() {
            return MeFactoryAeSupport.this.getGrid();
        }

        @Override
        public String getCustomPatternTerminalName() {
            return MeFactoryAeSupport.this.getPatternTerminalName();
        }

        @Override
        public void setCustomPatternTerminalName(String name) {
            MeFactoryAeSupport.this.setPatternTerminalName(name);
        }
    }

    public static final class AeBackedFactoryEnergyContainer<TILE extends TileEntityMekanism>
            extends MeNetworkEnergyHelper.NetworkBackedEnergyContainer<TILE> {
        private final TILE owner;

        public AeBackedFactoryEnergyContainer(TILE owner, IContentsListener listener) {
            super(owner, listener, () -> factoryGrid(owner), () -> factoryActionSource(owner));
            this.owner = owner;
        }
    }

    private static IGrid factoryGrid(TileEntityMekanism owner) {
        MeFactoryAeSupport support = owner instanceof MeFactoryAeMachine aeMachine ? aeMachine.getAeSupport() : null;
        return support == null ? null : support.getGrid();
    }

    private static IActionSource factoryActionSource(TileEntityMekanism owner) {
        MeFactoryAeSupport support = owner instanceof MeFactoryAeMachine aeMachine ? aeMachine.getAeSupport() : null;
        return support == null ? null : support.actionSource;
    }
}
