package com.beipuo.mekenergistics.blockentity;

import com.beipuo.mekenergistics.blockentity.support.MeNetworkEnergyHelper;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.api.recipes.ItemStackChemicalToItemStackRecipe;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.api.recipes.SawmillRecipe;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.tile.machine.TileEntityMetallurgicInfuser;
import mekanism.common.tile.prefab.TileEntityAdvancedElectricMachine;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class MeMachineRecipeProcessor {
    private final MeMekanismMachineBlockEntity machine;

    public MeMachineRecipeProcessor(MeMekanismMachineBlockEntity machine) {
        this.machine = machine;
    }

    void processRecipe() {
        if (machine.getLevel() == null || !machine.getMachine().hasRecipeLogic()) {
            return;
        }
        switch (machine.getMachine().slotLayout()) {
            case SINGLE_ITEM -> processSingleItemRecipe();
            case DOUBLE_ITEM -> processCombinerRecipe();
            case SAWING -> processSawingRecipe();
            case ITEM_CHEMICAL -> processItemChemicalRecipe();
        }
    }

    boolean canProcessRecipe() {
        if (machine.getLevel() == null || !machine.getMachine().hasRecipeLogic()) {
            return false;
        }
        if (!hasEnergyForRecipe()) {
            return false;
        }
        return switch (machine.getMachine().slotLayout()) {
            case SINGLE_ITEM -> canProcessSingleItemRecipe();
            case DOUBLE_ITEM -> canProcessCombinerRecipe();
            case SAWING -> canProcessSawingRecipe();
            case ITEM_CHEMICAL -> canProcessItemChemicalRecipe();
        };
    }

    private boolean hasEnergyForRecipe() {
        long energyPerTick = machine.getEnergyContainer().getEnergyPerTick();
        return energyPerTick <= 0 || machine.getEnergyContainer().getEnergy() >= energyPerTick
                || machine.extractAeAsFe(energyPerTick - machine.getEnergyContainer().getEnergy(), Action.SIMULATE) >= energyPerTick - machine.getEnergyContainer().getEnergy();
    }

    private boolean canProcessSingleItemRecipe() {
        ItemStack input = machine.getStack(MeMekanismMachineBlockEntity.INPUT_SLOT);
        if (input.isEmpty()) {
            return false;
        }
        ItemStackToItemStackRecipe recipe = getSingleItemRecipe(input);
        if (recipe == null) {
            return false;
        }
        int needed = clampNeeded(recipe.getInput().getNeededAmount(input));
        ItemStack output = recipe.getOutput(input);
        return needed > 0 && !output.isEmpty() && canFitOutput(MeMekanismMachineBlockEntity.OUTPUT_SLOT, output);
    }

    @Nullable
    private ItemStackToItemStackRecipe getSingleItemRecipe(ItemStack input) {
        return switch (machine.getMachine().factoryType()) {
            case ENRICHING -> MekanismRecipeType.ENRICHING.getInputCache().findFirstRecipe(machine.getLevel(), input);
            case CRUSHING -> MekanismRecipeType.CRUSHING.getInputCache().findFirstRecipe(machine.getLevel(), input);
            case SMELTING -> MekanismRecipeType.SMELTING.getInputCache().findFirstRecipe(machine.getLevel(), input);
            case null, default -> null;
        };
    }

    private boolean canProcessCombinerRecipe() {
        ItemStack input = machine.getStack(MeMekanismMachineBlockEntity.INPUT_SLOT);
        ItemStack secondary = machine.getStack(MeMekanismMachineBlockEntity.SECONDARY_INPUT_SLOT);
        if (input.isEmpty() || secondary.isEmpty()) {
            return false;
        }
        CombinerRecipe recipe = MekanismRecipeType.COMBINING.getInputCache().findFirstRecipe(machine.getLevel(), input, secondary);
        if (recipe == null) {
            return false;
        }
        int neededInput = clampNeeded(recipe.getMainInput().getNeededAmount(input));
        int neededSecondary = clampNeeded(recipe.getExtraInput().getNeededAmount(secondary));
        ItemStack output = recipe.getOutput(input, secondary);
        return neededInput > 0 && neededSecondary > 0 && !output.isEmpty() && canFitOutput(MeMekanismMachineBlockEntity.OUTPUT_SLOT, output);
    }

    private boolean canProcessItemChemicalRecipe() {
        ChemicalStack chemicalStack = machine.getChemicalStack();
        ItemStack input = machine.getStack(MeMekanismMachineBlockEntity.INPUT_SLOT);
        if (input.isEmpty() || chemicalStack.isEmpty()) {
            return false;
        }
        ItemStackChemicalToItemStackRecipe recipe = switch (machine.getMachine().factoryType()) {
            case COMPRESSING -> MekanismRecipeType.COMPRESSING.getInputCache().findFirstRecipe(machine.getLevel(), input, chemicalStack);
            case INFUSING -> MekanismRecipeType.METALLURGIC_INFUSING.getInputCache().findFirstRecipe(machine.getLevel(), input, chemicalStack);
            case INJECTING -> MekanismRecipeType.INJECTING.getInputCache().findFirstRecipe(machine.getLevel(), input, chemicalStack);
            case PURIFYING -> MekanismRecipeType.PURIFYING.getInputCache().findFirstRecipe(machine.getLevel(), input, chemicalStack);
            default -> null;
        };
        if (recipe == null) {
            return false;
        }
        int neededInput = clampNeeded(recipe.getItemInput().getNeededAmount(input));
        long neededChemical = recipe.getChemicalInput().getNeededAmount(chemicalStack);
        ItemStack output = recipe.getOutput(input, chemicalStack);
        return neededInput > 0 && neededChemical > 0 && !output.isEmpty() && canFitOutput(MeMekanismMachineBlockEntity.OUTPUT_SLOT, output);
    }

    private boolean canProcessSawingRecipe() {
        ItemStack input = machine.getStack(MeMekanismMachineBlockEntity.INPUT_SLOT);
        if (input.isEmpty()) {
            return false;
        }
        SawmillRecipe recipe = MekanismRecipeType.SAWING.getInputCache().findFirstRecipe(machine.getLevel(), input);
        if (recipe == null) {
            return false;
        }
        int needed = clampNeeded(recipe.getInput().getNeededAmount(input));
        SawmillRecipe.ChanceOutput output = recipe.getOutput(input);
        ItemStack mainOutput = output.getMainOutput();
        ItemStack secondaryOutput = output.getSecondaryOutput();
        return needed > 0
                && (!mainOutput.isEmpty() || !secondaryOutput.isEmpty())
                && (mainOutput.isEmpty() || canFitOutput(MeMekanismMachineBlockEntity.OUTPUT_SLOT, mainOutput))
                && (secondaryOutput.isEmpty() || canFitOutput(MeMekanismMachineBlockEntity.SECONDARY_OUTPUT_SLOT, secondaryOutput));
    }

    private void processSingleItemRecipe() {
        ItemStack input = machine.getStack(MeMekanismMachineBlockEntity.INPUT_SLOT);
        if (input.isEmpty()) {
            return;
        }
        ItemStackToItemStackRecipe recipe = getSingleItemRecipe(input);
        if (recipe == null) {
            return;
        }

        int needed = clampNeeded(recipe.getInput().getNeededAmount(input));
        ItemStack output = recipe.getOutput(input);
        if (needed <= 0 || output.isEmpty() || !canFitOutput(MeMekanismMachineBlockEntity.OUTPUT_SLOT, output)) {
            return;
        }
        input.shrink(needed);
        machine.setStack(MeMekanismMachineBlockEntity.INPUT_SLOT, input.isEmpty() ? ItemStack.EMPTY : input);
        addToOutput(MeMekanismMachineBlockEntity.OUTPUT_SLOT, output);
        machine.setChanged();
    }

    private void processCombinerRecipe() {
        ItemStack input = machine.getStack(MeMekanismMachineBlockEntity.INPUT_SLOT);
        ItemStack secondary = machine.getStack(MeMekanismMachineBlockEntity.SECONDARY_INPUT_SLOT);
        if (input.isEmpty() || secondary.isEmpty()) {
            return;
        }
        CombinerRecipe recipe = MekanismRecipeType.COMBINING.getInputCache().findFirstRecipe(machine.getLevel(), input, secondary);
        if (recipe == null) {
            return;
        }

        int neededInput = clampNeeded(recipe.getMainInput().getNeededAmount(input));
        int neededSecondary = clampNeeded(recipe.getExtraInput().getNeededAmount(secondary));
        ItemStack output = recipe.getOutput(input, secondary);
        if (neededInput <= 0 || neededSecondary <= 0 || output.isEmpty() || !canFitOutput(MeMekanismMachineBlockEntity.OUTPUT_SLOT, output)) {
            return;
        }
        input.shrink(neededInput);
        secondary.shrink(neededSecondary);
        machine.setStack(MeMekanismMachineBlockEntity.INPUT_SLOT, input.isEmpty() ? ItemStack.EMPTY : input);
        machine.setStack(MeMekanismMachineBlockEntity.SECONDARY_INPUT_SLOT, secondary.isEmpty() ? ItemStack.EMPTY : secondary);
        addToOutput(MeMekanismMachineBlockEntity.OUTPUT_SLOT, output);
        machine.setChanged();
    }

    private void processItemChemicalRecipe() {
        ChemicalStack chemicalStack = machine.getChemicalStack();
        ItemStack input = machine.getStack(MeMekanismMachineBlockEntity.INPUT_SLOT);
        if (input.isEmpty() || chemicalStack.isEmpty()) {
            return;
        }

        ItemStackChemicalToItemStackRecipe recipe = switch (machine.getMachine().factoryType()) {
            case COMPRESSING -> MekanismRecipeType.COMPRESSING.getInputCache().findFirstRecipe(machine.getLevel(), input, chemicalStack);
            case INFUSING -> MekanismRecipeType.METALLURGIC_INFUSING.getInputCache().findFirstRecipe(machine.getLevel(), input, chemicalStack);
            case INJECTING -> MekanismRecipeType.INJECTING.getInputCache().findFirstRecipe(machine.getLevel(), input, chemicalStack);
            case PURIFYING -> MekanismRecipeType.PURIFYING.getInputCache().findFirstRecipe(machine.getLevel(), input, chemicalStack);
            default -> null;
        };
        if (recipe == null) {
            return;
        }

        int neededInput = clampNeeded(recipe.getItemInput().getNeededAmount(input));
        long neededChemical = recipe.getChemicalInput().getNeededAmount(chemicalStack);
        ItemStack output = recipe.getOutput(input, chemicalStack);
        if (neededInput <= 0 || neededChemical <= 0 || output.isEmpty() || !canFitOutput(MeMekanismMachineBlockEntity.OUTPUT_SLOT, output)) {
            return;
        }

        input.shrink(neededInput);
        machine.setStack(MeMekanismMachineBlockEntity.INPUT_SLOT, input.isEmpty() ? ItemStack.EMPTY : input);
        machine.getChemicalTank().shrinkStack(neededChemical, Action.EXECUTE);
        addToOutput(MeMekanismMachineBlockEntity.OUTPUT_SLOT, output);
        machine.setChanged();
    }

    void fillChemicalFromConversionSlot() {
        ItemStack conversionInput = machine.getStack(MeMekanismMachineBlockEntity.SECONDARY_INPUT_SLOT);
        if (conversionInput.isEmpty() || machine.getLevel() == null) {
            return;
        }

        ItemStack singleInput = conversionInput.copyWithCount(1);
        var conversion = MekanismRecipeType.CHEMICAL_CONVERSION.getInputCache().findTypeBasedRecipe(machine.getLevel(), singleInput);
        if (conversion == null) {
            return;
        }

        ChemicalStack converted = conversion.getOutput(singleInput);
        if (converted.isEmpty() || !machine.canAddChemical(converted)) {
            return;
        }

        machine.getChemicalTank().insert(converted, Action.EXECUTE, AutomationType.INTERNAL);
        conversionInput.shrink(1);
        machine.setStack(MeMekanismMachineBlockEntity.SECONDARY_INPUT_SLOT, conversionInput.isEmpty() ? ItemStack.EMPTY : conversionInput);
        machine.setChanged();
    }

    long getChemicalCapacity() {
        return getChemicalCapacity(this.machine);
    }

    static long getChemicalCapacity(MeMekanismMachineBlockEntity machine) {
        return machine.getMachineEarly().factoryType() == mekanism.common.content.blocktype.FactoryType.INFUSING
                ? TileEntityMetallurgicInfuser.MAX_INFUSE
                : TileEntityAdvancedElectricMachine.MAX_GAS;
    }

    private void processSawingRecipe() {
        ItemStack input = machine.getStack(MeMekanismMachineBlockEntity.INPUT_SLOT);
        if (input.isEmpty()) {
            return;
        }

        SawmillRecipe recipe = MekanismRecipeType.SAWING.getInputCache().findFirstRecipe(machine.getLevel(), input);
        if (recipe == null) {
            return;
        }

        int needed = clampNeeded(recipe.getInput().getNeededAmount(input));
        SawmillRecipe.ChanceOutput output = recipe.getOutput(input);
        ItemStack mainOutput = output.getMainOutput();
        ItemStack secondaryOutput = output.getSecondaryOutput();
        if (needed <= 0 || mainOutput.isEmpty() && secondaryOutput.isEmpty()) {
            return;
        }
        if (!mainOutput.isEmpty() && !canFitOutput(MeMekanismMachineBlockEntity.OUTPUT_SLOT, mainOutput)) {
            return;
        }
        if (!secondaryOutput.isEmpty() && !canFitOutput(MeMekanismMachineBlockEntity.SECONDARY_OUTPUT_SLOT, secondaryOutput)) {
            return;
        }
        input.shrink(needed);
        machine.setStack(MeMekanismMachineBlockEntity.INPUT_SLOT, input.isEmpty() ? ItemStack.EMPTY : input);
        addToOutput(MeMekanismMachineBlockEntity.OUTPUT_SLOT, mainOutput);
        addToOutput(MeMekanismMachineBlockEntity.SECONDARY_OUTPUT_SLOT, secondaryOutput);
        machine.setChanged();
    }

    static int clampNeeded(long needed) {
        return needed > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) needed;
    }

    private boolean canFitOutput(int slot, ItemStack stack) {
        ItemStack existing = machine.getStack(slot);
        if (existing.isEmpty()) {
            return stack.getCount() <= machine.getSlotLimit(slot, stack);
        }
        return ItemStack.isSameItemSameComponents(existing, stack)
                && existing.getCount() + stack.getCount() <= Math.min(existing.getMaxStackSize(), machine.getSlotLimit(slot, existing));
    }

    private void addToOutput(int slot, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        ItemStack existing = machine.getStack(slot);
        if (existing.isEmpty()) {
            machine.setStack(slot, stack.copy());
            return;
        }
        existing.grow(stack.getCount());
        machine.setStack(slot, existing);
    }
}
