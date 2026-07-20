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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.tile.interfaces.ISideConfiguration;
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

    /** Creates the AE-backed energy container used by external factory owners. */
    public static <TILE extends TileEntityMekanism & ISideConfiguration> IEnergyContainerHolder energyContainers(
            TILE tile, IContentsListener listener, Runnable unpauseRecipeMonitors,
            java.util.function.Consumer<AeBackedFactoryEnergyContainer<TILE>> containerSetter) {
        EnergyContainerHelper builder = EnergyContainerHelper.forSideWithConfig(tile);
        AeBackedFactoryEnergyContainer<TILE> container = new AeBackedFactoryEnergyContainer<>(tile, () -> {
            listener.onContentsChanged();
            unpauseRecipeMonitors.run();
        });
        containerSetter.accept(container);
        builder.addContainer(container);
        return builder.build();
    }
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
        if (this.aeOutputMode.items() && hasItemOutputBacklog(this.knownOutputSlots)) {
            return true;
        }
        if (this.aeOutputMode.chemicals()) {
            for (IChemicalTank tank : this.knownChemicalOutputTanks) {
                if (tank != null && !tank.isEmpty()) {
                    return true;
                }
            }
        }
        if (this.aeOutputMode.fluids()) {
            for (IExtendedFluidTank tank : this.knownFluidOutputTanks) {
                if (tank != null && !tank.isEmpty()) {
                    return true;
                }
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
        boolean changed = MePatternInputRouter.route(inputHolder, factoryItemPorts(inputSlots));
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
        List<MeInputPort> ports = new ArrayList<>(factoryItemPorts(inputSlots));
        ports.add(MeMachineIoAdapter.chemicalInput(chemicalTank));
        boolean changed = MePatternInputRouter.route(inputHolder, ports);
        if (changed) {
            this.owner.saveChanges();
        }
        return changed;
    }

    /** Routes the second lane to either a direct chemical tank or Mekanism's conversion slot. */
    public boolean pushItemChemicalOrConversion(KeyCounter[] inputHolder,
            List<? extends IInventorySlot> inputSlots, IChemicalTank chemicalTank,
            IInventorySlot conversionSlot) {
        if (inputHolder == null || inputHolder.length != 2 || chemicalTank == null || conversionSlot == null) {
            return false;
        }
        MeInputPort main = factoryItemPorts(inputSlots).isEmpty() ? null : factoryItemPorts(inputSlots).get(0);
        if (main == null) {
            return false;
        }
        List<MeInputPort> secondary = List.of(MeMachineIoAdapter.chemicalInput(chemicalTank),
                MeMachineIoAdapter.itemInput(conversionSlot));
        boolean changed = MePatternInputRouter.routeLanes(inputHolder,
                List.of(List.of(main), secondary));
        if (!changed) {
            changed = MePatternInputRouter.routeLanes(inputHolder,
                    List.of(secondary, List.of(main)));
        }
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
        List<MeInputPort> ports = new ArrayList<>(factoryItemPorts(inputSlots));
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
        List<MeInputPort> main = factoryItemPorts(inputSlots);
        MeInputPort extra = MeMachineIoAdapter.itemInput(extraSlot);
        if (MePatternInputRouter.routeLanes(inputHolder, List.of(main, List.of(extra)))) {
            this.owner.saveChanges();
            return true;
        }
        return false;
    }

    public boolean pushThreeItems(KeyCounter[] inputHolder, List<? extends IInventorySlot> inputSlots,
            IInventorySlot secondSlot, IInventorySlot thirdSlot) {
        if (inputHolder == null || inputHolder.length != 3 || secondSlot == null || thirdSlot == null) {
            return false;
        }
        List<MeInputPort> main = factoryItemPorts(inputSlots);
        MeInputPort second = MeMachineIoAdapter.itemInput(secondSlot);
        MeInputPort third = MeMachineIoAdapter.itemInput(thirdSlot);
        if (MePatternInputRouter.routeLanes(inputHolder, List.of(main, List.of(second), List.of(third)))) {
            this.owner.saveChanges();
            return true;
        }
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

    public boolean processSingleItemSmartPatterns(List<IInventorySlot> outputSlots,
            List<IChemicalTank> outputTanks, List<IInventorySlot> inputSlots) {
        markOwnerHandlesSmartPatternProcessing();
        boolean changed = insertOutputSlotsIntoNetwork(outputSlots);
        for (IChemicalTank outputTank : outputTanks) {
            changed |= insertChemicalTankIntoNetwork(outputTank);
        }
        if (hasItemOutputBacklog(outputSlots) || hasChemicalOutputBacklog(outputTanks)) {
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

    public boolean processItemChemicalOrConversionSmartPatterns(IChemicalTank inputTank,
            IInventorySlot conversionSlot, List<IInventorySlot> outputSlots,
            List<IChemicalTank> outputTanks, List<IInventorySlot> inputSlots) {
        markOwnerHandlesSmartPatternProcessing();
        boolean changed = insertOutputSlotsIntoNetwork(outputSlots);
        for (IChemicalTank outputTank : outputTanks) {
            changed |= insertChemicalTankIntoNetwork(outputTank);
        }
        if (hasItemOutputBacklog(outputSlots) || hasChemicalOutputBacklog(outputTanks)) {
            return changed;
        }
        return processSmartPattern(new ItemChemicalOrConversionPortFeeder(inputSlots, inputTank, conversionSlot)) || changed;
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

    private static Iterable<AEKey> activeInputKeys(List<? extends IInventorySlot> itemSlots,
            IExtendedFluidTank fluidTank, List<? extends IChemicalTank> chemicalTanks,
            IInventorySlot... extraItemSlots) {
        Set<AEKey> keys = new LinkedHashSet<>();
        for (IInventorySlot slot : itemSlots) {
            if (slot != null && !slot.isEmpty()) {
                keys.add(AEItemKey.of(slot.getStack()));
            }
        }
        for (IInventorySlot slot : extraItemSlots) {
            if (slot != null && !slot.isEmpty()) {
                keys.add(AEItemKey.of(slot.getStack()));
            }
        }
        if (fluidTank != null && !fluidTank.isEmpty()) {
            keys.add(AEFluidKey.of(fluidTank.getFluid()));
        }
        for (IChemicalTank tank : chemicalTanks) {
            if (tank != null && !tank.isEmpty()) {
                keys.add(MekanismKey.of(tank.getStack()));
            }
        }
        return keys;
    }

    private static List<IChemicalTank> optionalChemicalTank(IChemicalTank tank) {
        return tank == null ? List.of() : List.of(tank);
    }

    private static List<MeInputPort> factoryItemPorts(List<? extends IInventorySlot> inputSlots) {
        return inputSlots.isEmpty()
                ? List.of()
                : List.of(MeMachineIoAdapter.autoSortedFactoryItemInput(inputSlots));
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
        public Iterable<AEKey> activeInputKeys() {
            return MeFactoryAeSupport.activeInputKeys(this.inputSlots, null, List.of());
        }

        @Override
        public long maxAcceptedCopies(KeyCounter[] oneCraftInputs) {
            return MePatternInputRouter.maxAcceptedCopies(oneCraftInputs, factoryItemPorts(this.inputSlots));
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
        public Iterable<AEKey> activeInputKeys() {
            return MeFactoryAeSupport.activeInputKeys(
                    this.inputSlots, null, optionalChemicalTank(this.inputTank));
        }

        @Override
        public long maxAcceptedCopies(KeyCounter[] oneCraftInputs) {
            if (this.inputTank == null) {
                return 0;
            }
            List<MeInputPort> ports = new ArrayList<>(factoryItemPorts(this.inputSlots));
            ports.add(MeMachineIoAdapter.chemicalInput(this.inputTank));
            return MePatternInputRouter.maxAcceptedCopies(oneCraftInputs, ports);
        }
    }

    private final class ItemChemicalOrConversionPortFeeder implements MeSmartPatternMultiplication.CapacityAwareFeeder {
        private final List<IInventorySlot> inputSlots;
        private final IChemicalTank inputTank;
        private final IInventorySlot conversionSlot;

        private ItemChemicalOrConversionPortFeeder(List<IInventorySlot> inputSlots,
                IChemicalTank inputTank, IInventorySlot conversionSlot) {
            this.inputSlots = inputSlots;
            this.inputTank = inputTank;
            this.conversionSlot = conversionSlot;
        }

        @Override
        public boolean feed(KeyCounter[] oneCraftInputs) {
            return pushItemChemicalOrConversion(oneCraftInputs, this.inputSlots, this.inputTank, this.conversionSlot);
        }

        @Override
        public Iterable<AEKey> activeInputKeys() {
            return MeFactoryAeSupport.activeInputKeys(this.inputSlots, null,
                    optionalChemicalTank(this.inputTank), this.conversionSlot);
        }

        @Override
        public long maxAcceptedCopies(KeyCounter[] oneCraftInputs) {
            if (this.inputTank == null || this.conversionSlot == null || this.inputSlots.isEmpty()) {
                return 0;
            }
            MeInputPort main = factoryItemPorts(this.inputSlots).get(0);
            List<MeInputPort> secondary = List.of(MeMachineIoAdapter.chemicalInput(this.inputTank),
                    MeMachineIoAdapter.itemInput(this.conversionSlot));
            long first = MePatternInputRouter.maxAcceptedLaneCopies(oneCraftInputs,
                    List.of(List.of(main), secondary));
            long second = MePatternInputRouter.maxAcceptedLaneCopies(oneCraftInputs,
                    List.of(secondary, List.of(main)));
            return Math.max(first, second);
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
        public Iterable<AEKey> activeInputKeys() {
            return MeFactoryAeSupport.activeInputKeys(List.of(), null, this.inputTanks);
        }

        @Override
        public long maxAcceptedCopies(KeyCounter[] oneCraftInputs) {
            return MePatternInputRouter.maxAcceptedCopies(oneCraftInputs,
                    this.inputTanks.stream().map(MeMachineIoAdapter::chemicalInput).toList());
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
        public Iterable<AEKey> activeInputKeys() {
            return MeFactoryAeSupport.activeInputKeys(List.of(), this.fluidTank, this.inputTanks);
        }

        @Override
        public long maxAcceptedCopies(KeyCounter[] oneCraftInputs) {
            if (this.fluidTank == null) {
                return 0;
            }
            List<MeInputPort> ports = new ArrayList<>();
            ports.add(MeMachineIoAdapter.fluidInput(this.fluidTank));
            ports.addAll(this.inputTanks.stream().map(MeMachineIoAdapter::chemicalInput).toList());
            return MePatternInputRouter.maxAcceptedCopies(oneCraftInputs, ports);
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
        public Iterable<AEKey> activeInputKeys() {
            return MeFactoryAeSupport.activeInputKeys(
                    this.inputSlots, this.fluidTank, optionalChemicalTank(this.chemicalTank));
        }

        @Override
        public long maxAcceptedCopies(KeyCounter[] oneCraftInputs) {
            if (this.fluidTank == null || this.chemicalTank == null) {
                return 0;
            }
            List<MeInputPort> ports = new ArrayList<>(factoryItemPorts(this.inputSlots));
            ports.add(MeMachineIoAdapter.fluidInput(this.fluidTank));
            ports.add(MeMachineIoAdapter.chemicalInput(this.chemicalTank));
            return MePatternInputRouter.maxAcceptedCopies(oneCraftInputs, ports);
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

        @Override public Iterable<AEKey> activeInputKeys() {
            return MeFactoryAeSupport.activeInputKeys(this.inputSlots, null, List.of(), this.extraSlot);
        }

        @Override public long maxAcceptedCopies(KeyCounter[] inputs) {
            if (this.extraSlot == null) return 0;
            return MePatternInputRouter.maxAcceptedLaneCopies(inputs, List.of(
                    factoryItemPorts(this.inputSlots),
                    List.of(MeMachineIoAdapter.itemInput(this.extraSlot))));
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

        @Override public Iterable<AEKey> activeInputKeys() {
            return MeFactoryAeSupport.activeInputKeys(
                    this.inputSlots, null, List.of(), this.secondSlot, this.thirdSlot);
        }

        @Override public long maxAcceptedCopies(KeyCounter[] inputs) {
            if (this.secondSlot == null || this.thirdSlot == null) return 0;
            return MePatternInputRouter.maxAcceptedLaneCopies(inputs, List.of(
                    factoryItemPorts(this.inputSlots),
                    List.of(MeMachineIoAdapter.itemInput(this.secondSlot)),
                    List.of(MeMachineIoAdapter.itemInput(this.thirdSlot))));
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
