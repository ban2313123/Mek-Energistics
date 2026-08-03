package com.beipuo.mekenergistics.common.machine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.beipuo.mekenergistics.compat.catalog.CompatFactoryTierGraph;
import com.beipuo.mekenergistics.compat.catalog.CompatMod;
import mekanism.common.tier.FactoryTier;
import org.junit.jupiter.api.Test;

class MeMachineEnergyProfileTest {

    /**
     * Pinned against Mekanism's {@code Factory.setMachineData}:
     * {@code max(baseStorage / 2, baseUsage) * tier.processes}. Values are Mekanism's own defaults,
     * so a drift here means ME factories no longer match the factories they mirror.
     */
    @Test
    void factoryBufferMatchesMekanismsRule() {
        // Enrichment chamber: 50 J/t, 20,000 J buffer.
        assertEquals(30_000L, MeMachineEnergyProfile.factoryStorage(20_000L, 50L, 3), "basic enriching");
        assertEquals(50_000L, MeMachineEnergyProfile.factoryStorage(20_000L, 50L, 5), "advanced enriching");
        assertEquals(70_000L, MeMachineEnergyProfile.factoryStorage(20_000L, 50L, 7), "elite enriching");
        assertEquals(90_000L, MeMachineEnergyProfile.factoryStorage(20_000L, 50L, 9), "ultimate enriching");

        // Purification chamber: 200 J/t, 80,000 J buffer.
        assertEquals(120_000L, MeMachineEnergyProfile.factoryStorage(80_000L, 200L, 3), "basic purifying");
        assertEquals(360_000L, MeMachineEnergyProfile.factoryStorage(80_000L, 200L, 9), "ultimate purifying");
    }

    /** The usage floor matters when a machine's buffer is small relative to what it draws. */
    @Test
    void bufferNeverDropsBelowOneOperationPerProcess() {
        assertEquals(300L, MeMachineEnergyProfile.factoryStorage(100L, 100L, 3));
    }

    @Test
    void mekanismFactoryTiersStillCarryTheExpectedProcessCounts() {
        assertEquals(3, FactoryTier.BASIC.processes);
        assertEquals(5, FactoryTier.ADVANCED.processes);
        assertEquals(7, FactoryTier.ELITE.processes);
        assertEquals(9, FactoryTier.ULTIMATE.processes);
    }

    /**
     * The derivation joins a factory to its base machine on {@code machineTypeId}. If that join ever
     * breaks, factories silently fall back to the flat default instead of failing, so assert it.
     */
    @Test
    void mekanismFactoriesResolveToTheMachineTheyParallelise() {
        assertSame(MeMekanismMachine.ENERGIZED_SMELTER, baseOf(MeMekanismMachine.valueOf("BASIC_SMELTING_FACTORY")));
        assertSame(MeMekanismMachine.ENRICHMENT_CHAMBER, baseOf(MeMekanismMachine.valueOf("ULTIMATE_ENRICHING_FACTORY")));
        assertSame(MeMekanismMachine.PURIFICATION_CHAMBER, baseOf(MeMekanismMachine.valueOf("BASIC_PURIFYING_FACTORY")));
        assertSame(MeMekanismMachine.PRECISION_SAWMILL, baseOf(MeMekanismMachine.valueOf("BASIC_SAWING_FACTORY")));
        assertSame(MeMekanismMachine.COMBINER, baseOf(MeMekanismMachine.valueOf("BASIC_COMBINING_FACTORY")));
    }

    /**
     * Evolved Mekanism gives its machines Mekanism's own energy values rather than adding config of
     * its own, so the shared profile covers them without depending on that mod. The lookup itself
     * filters on mod availability and so needs a running game; what is assertable here is the join
     * key it relies on -- a factory and its base machine must agree on {@code machineTypeId}.
     */
    @Test
    void evolvedMekanismFactoriesShareTheBaseMachineJoinKey() {
        assertEquals(MeMekanismMachine.ALLOYER.machineTypeId(),
                MeMekanismMachine.valueOf("BASIC_ALLOYING_FACTORY").machineTypeId());
        assertEquals("chemixing", MeMekanismMachine.CHEMIXER.machineTypeId());
        assertEquals("melting", MeMekanismMachine.THERMALIZER.machineTypeId());
        assertEquals("solidifying", MeMekanismMachine.SOLIDIFICATION_CHAMBER.machineTypeId());
    }

    /** Building a supplier must not read config -- that would need a running game. */
    @Test
    void suppliersAreBuiltLazily() {
        assertNotNull(MeMachineEnergyProfile.usage(MeMekanismMachine.valueOf("BASIC_SMELTING_FACTORY")));
        assertNotNull(MeMachineEnergyProfile.storage(MeMekanismMachine.valueOf("BASIC_SMELTING_FACTORY")));
        assertNotNull(MeMachineEnergyProfile.usage(MeMekanismMachine.ENRICHMENT_CHAMBER));
        assertNotNull(MeMachineEnergyProfile.storage(MeMekanismMachine.ENRICHMENT_CHAMBER));
    }

    private static MeMekanismMachine baseOf(MeMekanismMachine factory) {
        return CompatFactoryTierGraph.findBaseMachine(CompatMod.MEKANISM, factory.machineTypeId());
    }
}
