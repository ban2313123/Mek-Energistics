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

    /** Drains declared outputs before allowing another smart batch into the machine. */
    public boolean processPatternIo(AeOutputMode mode, boolean sendUpdatePacket) {
        boolean changed = drainPatternOutputs(mode) || sendUpdatePacket;
        if (!hasPatternOutputBacklog(mode)) {
            changed |= processSmartPatternViaAdapter();
        }
        return changed;
    }

    @Override
    protected boolean hasAeOutputWork() {
        AeOutputMode mode = this.owner.getAeOutputMode();
        if (hasPatternOutputBacklog(mode)) {
            return true;
        }
        return this.smartPatternMultiplication.hasPendingWork();
    }

    @Override
    protected boolean processAeOutputWork() {
        boolean hadWork = hasAeOutputWork();
        AeOutputMode mode = this.owner.getAeOutputMode();
        drainPatternOutputs(mode);
        if (!hasPatternOutputBacklog(mode)) {
            processSmartPatternViaAdapter();
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
        AbstractMeAeSupport<?> support = aeMachine.getRecipeAeSupport();
        return support.actionSource;
    }

}
