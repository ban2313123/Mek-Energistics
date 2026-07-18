package com.beipuo.mekenergistics.blockentity.compat.mekmm.factory;

import com.beipuo.mekenergistics.blockentity.compat.shared.MeExternalFactorySupport;
import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.KeyCounter;
import com.beipuo.mekenergistics.blockentity.support.MeFactoryAeSupport;
import java.util.Collections;
import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

final class MeAdvancedFactorySupport {
    private MeAdvancedFactorySupport() {
    }

    interface Owner extends MeMoreMachineFactoryAeMachine, MeExternalFactorySupport.Owner {
    }

    static IInventorySlotHolder withPatternSlots(IInventorySlotHolder original, Owner owner) {
        return owner.getAeSupport().withPatternSlots(original);
    }

    static boolean pushSingleItem(Owner owner, KeyCounter[] inputHolder) {
        return owner.getAeSupport().pushSingleItem(inputHolder, owner.meInputSlots());
    }

    static boolean pushSingleItem(Owner owner, IPatternDetails patternDetails, KeyCounter[] inputHolder) {
        return owner.getAeSupport().isSmartPatternMultiplicationEnabled()
                ? owner.getAeSupport().enqueueSmartPattern(patternDetails, inputHolder)
                : owner.getAeSupport().pushSingleItem(inputHolder, owner.meInputSlots());
    }

    static boolean processSingleItemSmartPatterns(Owner owner) {
        return owner.getAeSupport().processSingleItemSmartPatterns(owner.meOutputSlots(), List.of(), owner.meInputSlots());
    }

    static boolean finishSingleItemSmartPatterns(Owner owner) {
        return owner.getAeSupport().finishSingleItemSmartPatterns(owner.meInputSlots());
    }

    static boolean finishSingleItemSmartPatterns(Owner owner, java.util.List<mekanism.api.chemical.IChemicalTank> outputTanks) {
        return owner.getAeSupport().finishSingleItemSmartPatterns(owner.meInputSlots());
    }

    static boolean finishSingleItemSmartPatterns(Owner owner, mekanism.api.fluid.IExtendedFluidTank outputTank) {
        return owner.getAeSupport().finishSingleItemSmartPatterns(owner.meInputSlots(), outputTank);
    }

    static boolean processSingleItemSmartPatterns(Owner owner, java.util.List<mekanism.api.chemical.IChemicalTank> outputTanks) {
        return owner.getAeSupport().processSingleItemSmartPatterns(owner.meOutputSlots(), outputTanks, owner.meInputSlots());
    }

    static boolean processSingleItemSmartPatterns(Owner owner, mekanism.api.fluid.IExtendedFluidTank outputTank) {
        return owner.getAeSupport().processSingleItemSmartPatterns(owner.meOutputSlots(), outputTank, owner.meInputSlots());
    }

    static boolean pushItemChemical(Owner owner, KeyCounter[] inputHolder, mekanism.api.chemical.IChemicalTank chemicalTank) {
        return owner.getAeSupport().pushItemChemical(inputHolder, owner.meInputSlots(), chemicalTank);
    }

    static boolean pushItemChemical(Owner owner, IPatternDetails patternDetails, KeyCounter[] inputHolder, mekanism.api.chemical.IChemicalTank chemicalTank) {
        return owner.getAeSupport().isSmartPatternMultiplicationEnabled()
                ? owner.getAeSupport().enqueueSmartPattern(patternDetails, inputHolder)
                : owner.getAeSupport().pushItemChemical(inputHolder, owner.meInputSlots(), chemicalTank);
    }

    static boolean processItemChemicalSmartPatterns(Owner owner, mekanism.api.chemical.IChemicalTank chemicalTank) {
        return owner.getAeSupport().processItemChemicalSmartPatterns(chemicalTank, owner.meOutputSlots(), List.of(), owner.meInputSlots());
    }

    static boolean finishItemChemicalSmartPatterns(Owner owner, mekanism.api.chemical.IChemicalTank chemicalTank) {
        return owner.getAeSupport().finishItemChemicalSmartPatterns(owner.meInputSlots(), chemicalTank);
    }

    static boolean finishItemChemicalSmartPatterns(Owner owner, mekanism.api.chemical.IChemicalTank chemicalTank,
            java.util.List<mekanism.api.chemical.IChemicalTank> outputTanks) {
        return owner.getAeSupport().finishItemChemicalSmartPatterns(owner.meInputSlots(), chemicalTank);
    }

    static boolean processItemChemicalSmartPatterns(Owner owner, mekanism.api.chemical.IChemicalTank chemicalTank, java.util.List<mekanism.api.chemical.IChemicalTank> outputTanks) {
        return owner.getAeSupport().processItemChemicalSmartPatterns(chemicalTank, owner.meOutputSlots(), outputTanks, owner.meInputSlots());
    }

    static boolean pushChemical(Owner owner, KeyCounter[] inputHolder, mekanism.api.chemical.IChemicalTank chemicalTank) {
        return owner.getAeSupport().pushChemical(inputHolder, java.util.List.of(chemicalTank));
    }

    static boolean pushChemical(Owner owner, IPatternDetails patternDetails, KeyCounter[] inputHolder, mekanism.api.chemical.IChemicalTank chemicalTank) {
        return owner.getAeSupport().isSmartPatternMultiplicationEnabled()
                ? owner.getAeSupport().enqueueSmartPattern(patternDetails, inputHolder)
                : owner.getAeSupport().pushChemical(inputHolder, java.util.List.of(chemicalTank));
    }

    static boolean processChemicalSmartPatterns(Owner owner, mekanism.api.chemical.IChemicalTank chemicalTank) {
        return owner.getAeSupport().processChemicalSmartPatterns(List.of(chemicalTank), owner.meOutputSlots(), List.of());
    }

    static boolean finishChemicalSmartPatterns(Owner owner, mekanism.api.chemical.IChemicalTank chemicalTank) {
        return owner.getAeSupport().finishChemicalSmartPatterns(List.of(chemicalTank));
    }

    static boolean pushChemical(Owner owner, KeyCounter[] inputHolder, java.util.List<mekanism.api.chemical.IChemicalTank> chemicalTanks) {
        return owner.getAeSupport().pushChemical(inputHolder, chemicalTanks);
    }

    static boolean pushChemical(Owner owner, IPatternDetails patternDetails, KeyCounter[] inputHolder, java.util.List<mekanism.api.chemical.IChemicalTank> chemicalTanks) {
        return owner.getAeSupport().isSmartPatternMultiplicationEnabled()
                ? owner.getAeSupport().enqueueSmartPattern(patternDetails, inputHolder)
                : owner.getAeSupport().pushChemical(inputHolder, chemicalTanks);
    }

    static boolean processChemicalSmartPatterns(Owner owner, java.util.List<mekanism.api.chemical.IChemicalTank> chemicalTanks) {
        return owner.getAeSupport().processChemicalSmartPatterns(chemicalTanks, owner.meOutputSlots(), List.of());
    }

    static boolean finishChemicalSmartPatterns(Owner owner, java.util.List<mekanism.api.chemical.IChemicalTank> chemicalTanks) {
        return owner.getAeSupport().finishChemicalSmartPatterns(chemicalTanks);
    }

    static boolean finishChemicalSmartPatterns(Owner owner, java.util.List<mekanism.api.chemical.IChemicalTank> chemicalTanks,
            java.util.List<mekanism.api.chemical.IChemicalTank> outputTanks) {
        return owner.getAeSupport().finishChemicalSmartPatterns(chemicalTanks);
    }

    static boolean processChemicalSmartPatterns(Owner owner, java.util.List<mekanism.api.chemical.IChemicalTank> chemicalTanks, java.util.List<mekanism.api.chemical.IChemicalTank> outputTanks) {
        return owner.getAeSupport().processChemicalSmartPatterns(chemicalTanks, owner.meOutputSlots(), outputTanks);
    }

    static boolean pushFluidChemical(Owner owner, KeyCounter[] inputHolder, mekanism.api.fluid.IExtendedFluidTank fluidTank, mekanism.api.chemical.IChemicalTank chemicalTank) {
        return owner.getAeSupport().pushFluidChemical(inputHolder, fluidTank, java.util.List.of(chemicalTank));
    }

    static boolean pushFluidChemical(Owner owner, IPatternDetails patternDetails, KeyCounter[] inputHolder, mekanism.api.fluid.IExtendedFluidTank fluidTank, mekanism.api.chemical.IChemicalTank chemicalTank) {
        return owner.getAeSupport().isSmartPatternMultiplicationEnabled()
                ? owner.getAeSupport().enqueueSmartPattern(patternDetails, inputHolder)
                : owner.getAeSupport().pushFluidChemical(inputHolder, fluidTank, java.util.List.of(chemicalTank));
    }

    static boolean processFluidChemicalSmartPatterns(Owner owner, mekanism.api.fluid.IExtendedFluidTank fluidTank, mekanism.api.chemical.IChemicalTank chemicalTank) {
        return owner.getAeSupport().processFluidChemicalSmartPatterns(fluidTank, List.of(chemicalTank), owner.meOutputSlots(), List.of());
    }

    static boolean finishFluidChemicalSmartPatterns(Owner owner, mekanism.api.fluid.IExtendedFluidTank fluidTank, mekanism.api.chemical.IChemicalTank chemicalTank) {
        return owner.getAeSupport().finishFluidChemicalSmartPatterns(fluidTank, List.of(chemicalTank));
    }

    static boolean pushFluidChemical(Owner owner, KeyCounter[] inputHolder, mekanism.api.fluid.IExtendedFluidTank fluidTank, java.util.List<mekanism.api.chemical.IChemicalTank> chemicalTanks) {
        return owner.getAeSupport().pushFluidChemical(inputHolder, fluidTank, chemicalTanks);
    }

    static boolean pushFluidChemical(Owner owner, IPatternDetails patternDetails, KeyCounter[] inputHolder, mekanism.api.fluid.IExtendedFluidTank fluidTank, java.util.List<mekanism.api.chemical.IChemicalTank> chemicalTanks) {
        return owner.getAeSupport().isSmartPatternMultiplicationEnabled()
                ? owner.getAeSupport().enqueueSmartPattern(patternDetails, inputHolder)
                : owner.getAeSupport().pushFluidChemical(inputHolder, fluidTank, chemicalTanks);
    }

    static boolean processFluidChemicalSmartPatterns(Owner owner, mekanism.api.fluid.IExtendedFluidTank fluidTank, java.util.List<mekanism.api.chemical.IChemicalTank> chemicalTanks) {
        return owner.getAeSupport().processFluidChemicalSmartPatterns(fluidTank, chemicalTanks, owner.meOutputSlots(), List.of());
    }

    static boolean finishFluidChemicalSmartPatterns(Owner owner, mekanism.api.fluid.IExtendedFluidTank fluidTank, java.util.List<mekanism.api.chemical.IChemicalTank> chemicalTanks) {
        return owner.getAeSupport().finishFluidChemicalSmartPatterns(fluidTank, chemicalTanks);
    }

    static boolean finishFluidChemicalSmartPatterns(Owner owner, mekanism.api.fluid.IExtendedFluidTank fluidTank,
            java.util.List<mekanism.api.chemical.IChemicalTank> chemicalTanks, java.util.List<mekanism.api.chemical.IChemicalTank> outputTanks) {
        return owner.getAeSupport().finishFluidChemicalSmartPatterns(fluidTank, chemicalTanks);
    }

    static boolean processFluidChemicalSmartPatterns(Owner owner, mekanism.api.fluid.IExtendedFluidTank fluidTank, java.util.List<mekanism.api.chemical.IChemicalTank> chemicalTanks, java.util.List<mekanism.api.chemical.IChemicalTank> outputTanks) {
        return owner.getAeSupport().processFluidChemicalSmartPatterns(fluidTank, chemicalTanks, owner.meOutputSlots(), outputTanks);
    }

    static boolean pushItemFluidChemical(Owner owner, KeyCounter[] inputHolder, mekanism.api.fluid.IExtendedFluidTank fluidTank, mekanism.api.chemical.IChemicalTank chemicalTank) {
        return owner.getAeSupport().pushItemFluidChemical(inputHolder, owner.meInputSlots(), fluidTank, chemicalTank);
    }

    static boolean pushItemFluidChemical(Owner owner, IPatternDetails patternDetails, KeyCounter[] inputHolder, mekanism.api.fluid.IExtendedFluidTank fluidTank, mekanism.api.chemical.IChemicalTank chemicalTank) {
        return owner.getAeSupport().isSmartPatternMultiplicationEnabled()
                ? owner.getAeSupport().enqueueSmartPattern(patternDetails, inputHolder)
                : owner.getAeSupport().pushItemFluidChemical(inputHolder, owner.meInputSlots(), fluidTank, chemicalTank);
    }

    static boolean processItemFluidChemicalSmartPatterns(Owner owner, mekanism.api.fluid.IExtendedFluidTank fluidTank, mekanism.api.chemical.IChemicalTank chemicalTank) {
        return owner.getAeSupport().processItemFluidChemicalSmartPatterns(owner.meInputSlots(), fluidTank, chemicalTank, owner.meOutputSlots(), List.of());
    }

    static boolean finishItemFluidChemicalSmartPatterns(Owner owner, mekanism.api.fluid.IExtendedFluidTank fluidTank, mekanism.api.chemical.IChemicalTank chemicalTank) {
        return owner.getAeSupport().finishItemFluidChemicalSmartPatterns(owner.meInputSlots(), fluidTank, chemicalTank);
    }

    static boolean finishItemFluidChemicalSmartPatterns(Owner owner, mekanism.api.fluid.IExtendedFluidTank fluidTank,
            mekanism.api.chemical.IChemicalTank chemicalTank, mekanism.api.chemical.IChemicalTank outputTank) {
        return owner.getAeSupport().finishItemFluidChemicalSmartPatterns(owner.meInputSlots(), fluidTank, chemicalTank);
    }

    static boolean processItemFluidChemicalSmartPatterns(Owner owner, mekanism.api.fluid.IExtendedFluidTank fluidTank, mekanism.api.chemical.IChemicalTank chemicalTank, mekanism.api.chemical.IChemicalTank outputTank) {
        return owner.getAeSupport().processItemFluidChemicalSmartPatterns(owner.meInputSlots(), fluidTank, chemicalTank, owner.meOutputSlots(), List.of(outputTank));
    }

    static boolean updateServer(Owner owner, boolean sendUpdatePacket) {
        return MeExternalFactorySupport.updateServer(owner, sendUpdatePacket);
    }

    static boolean updateServer(Owner owner, boolean sendUpdatePacket, java.util.function.BooleanSupplier processor) {
        return MeExternalFactorySupport.updateServer(owner, sendUpdatePacket, processor);
    }

    static boolean drainOutputs(Owner owner) {
        return MeExternalFactorySupport.drainOutputs(owner);
    }

    static boolean updateServer(Owner owner, boolean sendUpdatePacket, java.util.List<mekanism.api.chemical.IChemicalTank> outputTanks) {
        return MeExternalFactorySupport.updateServer(owner, sendUpdatePacket, outputTanks);
    }

    static boolean updateServer(Owner owner, boolean sendUpdatePacket, mekanism.api.chemical.IChemicalTank outputTank) {
        return MeExternalFactorySupport.updateServer(owner, sendUpdatePacket, outputTank);
    }

    static boolean updateServer(Owner owner, boolean sendUpdatePacket, mekanism.api.fluid.IExtendedFluidTank outputTank) {
        return MeExternalFactorySupport.updateServer(owner, sendUpdatePacket, outputTank);
    }

    static boolean updateServer(Owner owner, boolean sendUpdatePacket, java.util.List<mekanism.api.chemical.IChemicalTank> outputTanks,
            java.util.function.BooleanSupplier processor) {
        return MeExternalFactorySupport.updateServer(owner, sendUpdatePacket, outputTanks, processor);
    }

    static boolean updateServer(Owner owner, boolean sendUpdatePacket, mekanism.api.chemical.IChemicalTank outputTank,
            java.util.function.BooleanSupplier processor) {
        return MeExternalFactorySupport.updateServer(owner, sendUpdatePacket, outputTank, processor);
    }

    static boolean updateServer(Owner owner, boolean sendUpdatePacket, mekanism.api.fluid.IExtendedFluidTank outputTank,
            java.util.function.BooleanSupplier processor) {
        return MeExternalFactorySupport.updateServer(owner, sendUpdatePacket, outputTank, processor);
    }

    static void createNodeOnFirstTick(TileEntityMekanism tile, MeFactoryAeSupport support, Level level, BlockPos pos) {
        support.createNodeOnFirstTick(tile);
    }

    static void save(MeFactoryAeSupport support, CompoundTag tag, HolderLookup.Provider registries) {
        support.saveAll(tag, registries);
    }

    static void load(MeFactoryAeSupport support, CompoundTag tag, HolderLookup.Provider registries) {
        support.loadAll(tag, registries);
    }

    static List<IInventorySlot> noItemOutput() {
        return Collections.emptyList();
    }

    static <RECIPE extends MekanismRecipe<?>> CachedRecipe<RECIPE> wrapRecipeEnergy(
            Owner owner, MachineEnergyContainer<?> energyContainer, CachedRecipe<RECIPE> cachedRecipe) {
        return MeFactoryAeSupport.withAeRecipeEnergy(owner, energyContainer, cachedRecipe);
    }
}

