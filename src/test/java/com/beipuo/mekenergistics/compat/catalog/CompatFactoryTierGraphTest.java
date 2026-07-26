package com.beipuo.mekenergistics.compat.catalog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.beipuo.mekenergistics.common.machine.MeMekanismMachine;
import org.junit.jupiter.api.Test;

class CompatFactoryTierGraphTest {
    @Test
    void indexesEveryFactoryByRouteProviderAndExactSourceId() {
        CompatMachineCatalog.all().forEach(spec -> {
            MeMekanismMachine expected = spec.machine();
            assertEquals(expected, CompatMachineCatalog.findBySourceBlockId(spec.sourceBlockId())
                    .map(CompatMachineSpec::machine).orElse(null), spec.machine().name());
            assertEquals(expected, CompatFactoryTierGraph.findDeclaredByRegistryName(
                    spec.machine().registryName()), spec.machine().name());
            if (spec.kind() != CompatMachineKind.MACHINE) {
                assertEquals(expected, CompatFactoryTierGraph.findDeclaredFactory(
                        spec.route(), spec.tierId(), spec.machineTypeId()), spec.machine().name());
                assertEquals(expected, CompatFactoryTierGraph.findDeclaredFactory(
                        spec.provider(), spec.tierId(), spec.machineTypeId()), spec.machine().name());
            }
        });
    }

    @Test
    void followsCoreAndCrossProviderUpgradeEdges() {
        assertEquals(MeMekanismMachine.BASIC_SMELTING_FACTORY,
                MeMekanismMachine.ENERGIZED_SMELTER.getBasicFactory());
        assertEquals(MeMekanismMachine.ADVANCED_SMELTING_FACTORY,
                MeMekanismMachine.BASIC_SMELTING_FACTORY.getNextFactory());
        assertEquals(MeMekanismMachine.ELITE_SMELTING_FACTORY,
                MeMekanismMachine.ADVANCED_SMELTING_FACTORY.getNextFactory());
        assertEquals(MeMekanismMachine.ULTIMATE_SMELTING_FACTORY,
                MeMekanismMachine.ELITE_SMELTING_FACTORY.getNextFactory());

        assertEquals(MeMekanismMachine.OVERCLOCKED_SMELTING_FACTORY,
                CompatFactoryTierGraph.declaredNextFactory(MeMekanismMachine.ULTIMATE_SMELTING_FACTORY));
        assertEquals(MeMekanismMachine.ABSOLUTE_OVERCLOCKED_SMELTING_FACTORY,
                CompatFactoryTierGraph.declaredNextFactory(MeMekanismMachine.CREATIVE_SMELTING_FACTORY));
    }

    @Test
    void preservesMoreMachineAndAdvancedFactoryTracks() {
        assertEquals(MeMekanismMachine.BASIC_RECYCLING_FACTORY,
                CompatFactoryTierGraph.declaredBasicFactory(MeMekanismMachine.RECYCLER));
        assertEquals(MeMekanismMachine.ABSOLUTE_RECYCLING_FACTORY,
                CompatFactoryTierGraph.declaredNextFactory(MeMekanismMachine.ULTIMATE_RECYCLING_FACTORY));
        assertEquals(MeMekanismMachine.BASIC_CENTRIFUGING_FACTORY,
                CompatFactoryTierGraph.declaredBasicFactory(MeMekanismMachine.ISOTOPIC_CENTRIFUGE));
        assertEquals(MeMekanismMachine.ABSOLUTE_CENTRIFUGING_FACTORY,
                CompatFactoryTierGraph.declaredNextFactory(MeMekanismMachine.ULTIMATE_CENTRIFUGING_FACTORY));
    }

    @Test
    void resolvesInstallerSelectedTierWithoutFamilyBranches() {
        assertEquals(MeMekanismMachine.ABSOLUTE_RECYCLING_FACTORY,
                CompatFactoryTierGraph.declaredFactoryAtTier(
                        MeMekanismMachine.ULTIMATE_RECYCLING_FACTORY, CompatMod.MEKE, "absolute"));
        assertEquals(MeMekanismMachine.ABSOLUTE_CENTRIFUGING_FACTORY,
                CompatFactoryTierGraph.declaredFactoryAtTier(
                        MeMekanismMachine.ULTIMATE_CENTRIFUGING_FACTORY, CompatMod.MEKE, "absolute"));
        assertEquals(MeMekanismMachine.ABSOLUTE_OVERCLOCKED_SMELTING_FACTORY,
                CompatFactoryTierGraph.declaredFactoryAtTier(
                        MeMekanismMachine.CREATIVE_SMELTING_FACTORY, CompatMod.EMEKE,
                        "absolute_overclocked"));
    }

    @Test
    void resolvesOnlyForwardMaxTierInstallerTargets() {
        assertEquals(MeMekanismMachine.MULTIVERSAL_SMELTING_FACTORY,
                CompatFactoryTierGraph.declaredForwardFactoryAtTier(
                        MeMekanismMachine.ENERGIZED_SMELTER, CompatMod.EMEK, "multiversal"));
        assertEquals(MeMekanismMachine.CREATIVE_SMELTING_FACTORY,
                CompatFactoryTierGraph.declaredForwardFactoryAtTier(
                        MeMekanismMachine.OVERCLOCKED_SMELTING_FACTORY, CompatMod.EMEK, "creative"));
        assertEquals(MeMekanismMachine.CREATIVE_ALLOYING_FACTORY,
                CompatFactoryTierGraph.declaredForwardFactoryAtTier(
                        MeMekanismMachine.ALLOYER, CompatMod.EMEK, "creative"));

        assertNull(CompatFactoryTierGraph.declaredForwardFactoryAtTier(
                MeMekanismMachine.CREATIVE_SMELTING_FACTORY, CompatMod.EMEK, "creative"));
        assertNull(CompatFactoryTierGraph.declaredForwardFactoryAtTier(
                MeMekanismMachine.CREATIVE_SMELTING_FACTORY, CompatMod.EMEK, "multiversal"));
        assertNull(CompatFactoryTierGraph.declaredForwardFactoryAtTier(
                MeMekanismMachine.ABSOLUTE_SMELTING_FACTORY, CompatMod.EMEK, "creative"));
        assertNull(CompatFactoryTierGraph.declaredForwardFactoryAtTier(
                MeMekanismMachine.THERMALIZER, CompatMod.EMEK, "creative"));
    }
}
