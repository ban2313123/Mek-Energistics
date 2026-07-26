package com.beipuo.mekenergistics.common.machine;

import java.util.function.LongSupplier;
import mekanism.common.config.MekanismConfig;

final class MeMachineEnergyProfile {
    private static final LongSupplier DEFAULT_USAGE = () -> 50L;
    private static final LongSupplier DEFAULT_STORAGE = () -> 2_000_000L;

    private MeMachineEnergyProfile() {
    }

    static LongSupplier usage(MeMekanismMachine machine) {
        if (machine.isFactory()) {
            return DEFAULT_USAGE;
        }
        return switch (machine) {
            case ENRICHMENT_CHAMBER -> MekanismConfig.usage.enrichmentChamber;
            case CRUSHER -> MekanismConfig.usage.crusher;
            case ENERGIZED_SMELTER -> MekanismConfig.usage.energizedSmelter;
            case PRECISION_SAWMILL -> MekanismConfig.usage.precisionSawmill;
            case OSMIUM_COMPRESSOR -> MekanismConfig.usage.osmiumCompressor;
            case COMBINER -> MekanismConfig.usage.combiner;
            case METALLURGIC_INFUSER -> MekanismConfig.usage.metallurgicInfuser;
            case PURIFICATION_CHAMBER -> MekanismConfig.usage.purificationChamber;
            case CHEMICAL_INJECTION_CHAMBER -> MekanismConfig.usage.chemicalInjectionChamber;
            default -> DEFAULT_USAGE;
        };
    }

    static LongSupplier storage(MeMekanismMachine machine) {
        if (machine.isFactory()) {
            return DEFAULT_STORAGE;
        }
        return switch (machine) {
            case ENRICHMENT_CHAMBER -> MekanismConfig.storage.enrichmentChamber;
            case CRUSHER -> MekanismConfig.storage.crusher;
            case ENERGIZED_SMELTER -> MekanismConfig.storage.energizedSmelter;
            case PRECISION_SAWMILL -> MekanismConfig.storage.precisionSawmill;
            case OSMIUM_COMPRESSOR -> MekanismConfig.storage.osmiumCompressor;
            case COMBINER -> MekanismConfig.storage.combiner;
            case METALLURGIC_INFUSER -> MekanismConfig.storage.metallurgicInfuser;
            case PURIFICATION_CHAMBER -> MekanismConfig.storage.purificationChamber;
            case CHEMICAL_INJECTION_CHAMBER -> MekanismConfig.storage.chemicalInjectionChamber;
            default -> DEFAULT_STORAGE;
        };
    }
}
