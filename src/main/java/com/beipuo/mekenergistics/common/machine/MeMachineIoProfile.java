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
