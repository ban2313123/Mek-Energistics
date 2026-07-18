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
