package com.beipuo.mekenergistics.common.machine;

import com.beipuo.mekenergistics.compat.OptionalCompatClasses;
import com.beipuo.mekenergistics.compat.catalog.CompatFactoryTierGraph;
import java.util.function.LongSupplier;
import mekanism.api.math.MathUtils;
import mekanism.common.config.MekanismConfig;
import mekanism.common.tier.FactoryTier;
import org.jetbrains.annotations.Nullable;

/**
 * Energy characteristics for ME machines.
 *
 * <p>Base machines read Mekanism's own config. Factories are derived from the machine they
 * parallelise, using the same rule Mekanism applies in {@code Factory.setMachineData}: per-operation
 * usage is inherited unchanged, and the buffer is
 * {@code max(baseStorage / 2, baseUsage) * tier.processes}. A factory therefore costs the same per
 * item as its base machine and buffers enough for the operations it runs in parallel.
 *
 * <p>Evolved Mekanism's machines are covered here too, because that mod reuses Mekanism's own values
 * rather than adding config of its own. MoreMachine does have its own config, which lives in an
 * optional mod, so that mapping sits in {@code MekanismMoreMachineBaseCompat} instead. Anything
 * still unrecognised falls back to the defaults below.
 *
 * <p>Config values are read inside the returned suppliers rather than when the supplier is built,
 * because touching {@link MekanismConfig} pulls in Minecraft's registries and cannot happen outside
 * a running game.
 */
final class MeMachineEnergyProfile {
    /** Fallbacks for machines whose owning mod's config we cannot read. */
    private static final long DEFAULT_USAGE = 50L;
    private static final long DEFAULT_STORAGE = 2_000_000L;

    private MeMachineEnergyProfile() {
    }

    static LongSupplier usage(MeMekanismMachine machine) {
        MeMekanismMachine source = configuredSource(machine);
        return source == null ? () -> DEFAULT_USAGE : () -> readUsage(source);
    }

    static LongSupplier storage(MeMekanismMachine machine) {
        MeMekanismMachine source = configuredSource(machine);
        if (source == null) {
            return () -> DEFAULT_STORAGE;
        }
        if (!machine.isFactory()) {
            return () -> readStorage(source);
        }
        int processes = processCount(machine);
        if (processes <= 0) {
            return () -> DEFAULT_STORAGE;
        }
        return () -> factoryStorage(readStorage(source), readUsage(source), processes);
    }

    /**
     * Mekanism's own factory buffer rule, from {@code Factory.setMachineData}. Kept as a pure
     * function so it can be pinned by tests without a running game.
     */
    static long factoryStorage(long baseStorage, long baseUsage, int processes) {
        return MathUtils.clampToLong(Math.max(baseStorage * 0.5D, baseUsage) * processes);
    }

    /**
     * The machine whose Mekanism config applies, or null when there is none to read. For a factory
     * that is the machine it parallelises: both carry the same {@code machineTypeId} -- an enriching
     * factory and the enrichment chamber are both {@code "enriching"} -- which is the key
     * {@link CompatFactoryTierGraph#findBaseMachine} is built on.
     */
    @Nullable
    private static MeMekanismMachine configuredSource(MeMekanismMachine machine) {
        MeMekanismMachine source = machine.isFactory()
                ? CompatFactoryTierGraph.findBaseMachine(machine.provider(), machine.machineTypeId())
                : machine;
        return source != null && hasMekanismConfig(source) ? source : null;
    }

    /**
     * Parallel operations the factory runs, or 0 when the tier is from a mod we cannot read.
     *
     * <p>Evolved Mekanism needs no special case: its {@code EMFactoryTier} holds real
     * {@link FactoryTier} instances. The Extras mods declare their own tier enums, so their counts
     * come back reflectively -- 11/13/15/17 for Mekanism Extras, 12/14/16/18 for Evolved Extras.
     */
    private static int processCount(MeMekanismMachine machine) {
        FactoryTier tier = machine.factoryTier();
        if (tier != null) {
            return tier.processes;
        }
        return switch (machine.provider()) {
            case MEKE -> OptionalCompatClasses.getMekanismExtrasFactoryProcesses(machine.tierId());
            case EMEKE -> OptionalCompatClasses.getEvolvedMekanismExtrasFactoryProcesses(machine.tierId());
            default -> 0;
        };
    }

    private static boolean hasMekanismConfig(MeMekanismMachine machine) {
        return switch (machine.identity()) {
            case ENRICHMENT_CHAMBER, CRUSHER, ENERGIZED_SMELTER, PRECISION_SAWMILL, OSMIUM_COMPRESSOR,
                    COMBINER, METALLURGIC_INFUSER, PURIFICATION_CHAMBER, CHEMICAL_INJECTION_CHAMBER,
                    ALLOYER, CHEMIXER, THERMALIZER, SOLIDIFICATION_CHAMBER -> true;
            default -> false;
        };
    }

    private static long readUsage(MeMekanismMachine machine) {
        return switch (machine.identity()) {
            case ENRICHMENT_CHAMBER -> MekanismConfig.usage.enrichmentChamber.getAsLong();
            case CRUSHER -> MekanismConfig.usage.crusher.getAsLong();
            case ENERGIZED_SMELTER -> MekanismConfig.usage.energizedSmelter.getAsLong();
            case PRECISION_SAWMILL -> MekanismConfig.usage.precisionSawmill.getAsLong();
            case OSMIUM_COMPRESSOR -> MekanismConfig.usage.osmiumCompressor.getAsLong();
            case COMBINER -> MekanismConfig.usage.combiner.getAsLong();
            case METALLURGIC_INFUSER -> MekanismConfig.usage.metallurgicInfuser.getAsLong();
            case PURIFICATION_CHAMBER -> MekanismConfig.usage.purificationChamber.getAsLong();
            case CHEMICAL_INJECTION_CHAMBER -> MekanismConfig.usage.chemicalInjectionChamber.getAsLong();
            // Evolved Mekanism gives its own machines Mekanism's values rather than adding config
            // of its own (see EMBlockTypes), so these need no dependency on that mod.
            case ALLOYER, CHEMIXER -> MekanismConfig.usage.combiner.getAsLong();
            case THERMALIZER -> MekanismConfig.usage.chemicalOxidizer.getAsLong();
            case SOLIDIFICATION_CHAMBER -> MekanismConfig.usage.pressurizedReactionBase.getAsLong();
            default -> DEFAULT_USAGE;
        };
    }

    private static long readStorage(MeMekanismMachine machine) {
        return switch (machine.identity()) {
            case ENRICHMENT_CHAMBER -> MekanismConfig.storage.enrichmentChamber.getAsLong();
            case CRUSHER -> MekanismConfig.storage.crusher.getAsLong();
            case ENERGIZED_SMELTER -> MekanismConfig.storage.energizedSmelter.getAsLong();
            case PRECISION_SAWMILL -> MekanismConfig.storage.precisionSawmill.getAsLong();
            case OSMIUM_COMPRESSOR -> MekanismConfig.storage.osmiumCompressor.getAsLong();
            case COMBINER -> MekanismConfig.storage.combiner.getAsLong();
            case METALLURGIC_INFUSER -> MekanismConfig.storage.metallurgicInfuser.getAsLong();
            case PURIFICATION_CHAMBER -> MekanismConfig.storage.purificationChamber.getAsLong();
            case CHEMICAL_INJECTION_CHAMBER -> MekanismConfig.storage.chemicalInjectionChamber.getAsLong();
            case ALLOYER, CHEMIXER -> MekanismConfig.storage.combiner.getAsLong();
            case THERMALIZER -> MekanismConfig.storage.chemicalOxidizer.getAsLong();
            case SOLIDIFICATION_CHAMBER -> MekanismConfig.storage.pressurizedReactionBase.getAsLong();
            default -> DEFAULT_STORAGE;
        };
    }
}
