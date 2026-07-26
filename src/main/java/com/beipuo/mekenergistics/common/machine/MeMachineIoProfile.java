package com.beipuo.mekenergistics.common.machine;

import com.beipuo.mekenergistics.compat.catalog.CompatRegistrationRoute;
import mekanism.common.content.blocktype.FactoryType;

final class MeMachineIoProfile {
    private MeMachineIoProfile() {
    }

    static boolean hasSecondaryItemInput(MeMekanismMachine machine) {
        return machine.factoryType() == FactoryType.COMBINING
                || machine == MeMekanismMachine.CNC_STAMPER
                || machine == MeMekanismMachine.CHEMIXER;
    }

    static boolean hasChemicalInput(MeMekanismMachine machine) {
        FactoryType factoryType = machine.factoryType();
        return factoryType == FactoryType.COMPRESSING
                || factoryType == FactoryType.INFUSING
                || factoryType == FactoryType.INJECTING
                || factoryType == FactoryType.PURIFYING
                || machine == MeMekanismMachine.CHEMIXER;
    }

    static boolean hasRecipeLogic(MeMekanismMachine machine) {
        if (machine.factoryType() != null) {
            return true;
        }
        CompatRegistrationRoute route = machine.registrationRoute();
        return switch (route) {
            case MEKMM_MACHINE, MEKMM_FACTORY, MEKMM_ADVANCED_FACTORY,
                    MEKE_MEKMM_FACTORY, MEKE_MEKMM_ADVANCED_FACTORY -> true;
            default -> false;
        };
    }

    static boolean hasAdvancedChemicalInput(MeMekanismMachine machine) {
        FactoryType factoryType = machine.factoryType();
        return factoryType == FactoryType.COMPRESSING
                || factoryType == FactoryType.INJECTING
                || factoryType == FactoryType.PURIFYING;
    }

    static boolean hasSecondaryOutput(MeMekanismMachine machine) {
        return machine.factoryType() == FactoryType.SAWING;
    }

    /**
     * A coarse shape hint, not a full description of a machine's I/O. Every caller only asks whether
     * the result is {@link MeMekanismMachine.SlotLayout#SINGLE_ITEM}, which is what makes the
     * approximation safe.
     *
     * <p>It genuinely is an approximation: no constant expresses item + item + chemical, so
     * {@link MeMekanismMachine#CHEMIXER} -- whose tile has a main slot, an extra slot and a chemical
     * tank, all three matched by its recipe -- reports {@code DOUBLE_ITEM} and loses the chemical.
     * That is not a live defect: pattern routing comes from each block entity's own
     * {@code getPatternInputLayout}, and the ME Chemixer declares all three lanes there. Anyone
     * giving this method more authority has to extend the enum first.
     */
    static MeMekanismMachine.SlotLayout slotLayout(MeMekanismMachine machine) {
        if (hasSecondaryItemInput(machine)) {
            return MeMekanismMachine.SlotLayout.DOUBLE_ITEM;
        }
        if (hasChemicalInput(machine)) {
            return MeMekanismMachine.SlotLayout.ITEM_CHEMICAL;
        }
        if (hasSecondaryOutput(machine)) {
            return MeMekanismMachine.SlotLayout.SAWING;
        }
        return MeMekanismMachine.SlotLayout.SINGLE_ITEM;
    }
}
