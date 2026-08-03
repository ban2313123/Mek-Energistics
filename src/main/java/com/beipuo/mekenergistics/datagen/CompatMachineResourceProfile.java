package com.beipuo.mekenergistics.datagen;

import com.beipuo.mekenergistics.common.machine.MeMekanismMachine;
import com.beipuo.mekenergistics.compat.catalog.CompatMachineKind;
import com.beipuo.mekenergistics.compat.catalog.CompatMachineSpec;
import com.beipuo.mekenergistics.compat.catalog.CompatMod;
import java.util.Set;

/** Resource-only exceptions that do not belong to machine behavior metadata. */
final class CompatMachineResourceProfile {
    private static final Set<String> HANDWRITTEN_FACTORY_MODEL_TIERS = Set.of(
            "basic", "advanced", "elite", "ultimate", "absolute", "supreme", "cosmic", "infinite");
    private static final Set<MeMekanismMachine> CUSTOM_ITEM_MODELS = Set.of(
            MeMekanismMachine.ELECTROLYTIC_SEPARATOR,
            MeMekanismMachine.ISOTOPIC_CENTRIFUGE,
            MeMekanismMachine.LARGE_ROTARY_CONDENSENTRATOR,
            MeMekanismMachine.LARGE_SOLAR_NEUTRON_ACTIVATOR,
            MeMekanismMachine.LARGE_ELECTROLYTIC_SEPARATOR,
            MeMekanismMachine.LARGE_CHEMICAL_INFUSER,
            MeMekanismMachine.LARGE_ANTIPROTONIC_NUCLEOSYNTHESIZER,
            MeMekanismMachine.PLANTING_STATION);
    private static final Set<MeMekanismMachine> DEDICATED_ACTIVE_MACHINE_MODELS = Set.of(
            MeMekanismMachine.ALLOYER,
            MeMekanismMachine.CHEMIXER,
            MeMekanismMachine.CNC_LATHE,
            MeMekanismMachine.CNC_ROLLING_MILL,
            MeMekanismMachine.CNC_STAMPER,
            MeMekanismMachine.LARGE_ROTARY_CONDENSENTRATOR,
            MeMekanismMachine.LARGE_SOLAR_NEUTRON_ACTIVATOR,
            MeMekanismMachine.LARGE_ELECTROLYTIC_SEPARATOR,
            MeMekanismMachine.LARGE_CHEMICAL_INFUSER,
            MeMekanismMachine.LARGE_ANTIPROTONIC_NUCLEOSYNTHESIZER,
            MeMekanismMachine.PLANTING_STATION,
            MeMekanismMachine.RECYCLER,
            MeMekanismMachine.SOLIDIFICATION_CHAMBER,
            MeMekanismMachine.THERMALIZER);

    private CompatMachineResourceProfile() {
    }

    static FactoryModelStyle factoryModelStyle(CompatMachineSpec spec) {
        return switch (spec.machineTypeId()) {
            case "centrifuging" -> FactoryModelStyle.CENTRIFUGING;
            case "planting" -> FactoryModelStyle.PLANTING;
            default -> FactoryModelStyle.STANDARD;
        };
    }

    static boolean hasHandwrittenFactoryBlockModel(CompatMachineSpec spec) {
        return spec.tierId() != null
                && HANDWRITTEN_FACTORY_MODEL_TIERS.contains(spec.tierId())
                && factoryModelStyle(spec) != FactoryModelStyle.STANDARD;
    }

    static boolean hasCustomItemModel(CompatMachineSpec spec) {
        return hasHandwrittenFactoryBlockModel(spec) || CUSTOM_ITEM_MODELS.contains(spec.machine());
    }

    static boolean hasDedicatedActiveMachineModel(CompatMachineSpec spec) {
        return DEDICATED_ACTIVE_MACHINE_MODELS.contains(spec.machine());
    }

    static boolean survivesExplosion(CompatMachineSpec spec) {
        return !usesLegacyExternalFactoryLoot(spec) && spec.machine() != MeMekanismMachine.ALLOYER;
    }

    static boolean usesLegacyRandomSequence(CompatMachineSpec spec) {
        if (usesLegacyExternalFactoryLoot(spec)) {
            return true;
        }
        return switch (spec.machine().identity()) {
            case CHEMIXER, SOLIDIFICATION_CHAMBER, THERMALIZER -> true;
            default -> false;
        };
    }

    private static boolean usesLegacyExternalFactoryLoot(CompatMachineSpec spec) {
        String sourceNamespace = spec.sourceBlockId().getNamespace();
        return spec.kind() != CompatMachineKind.MACHINE
                && (sourceNamespace.equals(CompatMod.MEKMM.modId())
                || sourceNamespace.equals(CompatMod.MEKE.modId()));
    }

    enum FactoryModelStyle {
        STANDARD,
        CENTRIFUGING,
        PLANTING
    }
}
