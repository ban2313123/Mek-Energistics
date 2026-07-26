package com.beipuo.mekenergistics.common.machine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.beipuo.mekenergistics.compat.catalog.CompatMachineFamily;
import com.beipuo.mekenergistics.compat.catalog.CompatMod;
import org.junit.jupiter.api.Test;

class MeMekanismMachineDefinitionTest {

    @Test
    void definitionsPreserveRepresentativeMachineAndFactoryNames() {
        assertDefinition(
                MeMekanismMachine.ENRICHMENT_CHAMBER,
                CompatMachineFamily.MEKANISM_MACHINE,
                null,
                "enriching",
                "me_enrichment_chamber",
                "ME Enrichment Chamber");
        assertDefinition(
                MeMekanismMachine.BASIC_SMELTING_FACTORY,
                CompatMachineFamily.MEKANISM_FACTORY,
                "basic",
                "smelting",
                "me_basic_smelting_factory",
                "ME Basic Smelting Factory");
        assertDefinition(
                MeMekanismMachine.ABSOLUTE_OVERCLOCKED_SMELTING_FACTORY,
                CompatMachineFamily.EMEKE_FACTORY,
                "absolute_overclocked",
                "smelting",
                "me_absolute_overclocked_smelting_factory",
                "ME Absolute Overclocked Smelting Factory");
        assertDefinition(
                MeMekanismMachine.LARGE_CHEMICAL_INFUSER,
                CompatMachineFamily.MEKMM_MACHINE,
                null,
                "large_chemical_infuser",
                "me_large_chemical_infuser",
                "ME Large Chemical Infuser");
        assertDefinition(
                MeMekanismMachine.ALLOYER,
                CompatMachineFamily.EMEK_MACHINE,
                null,
                "alloying",
                "me_alloyer",
                "ME Alloyer");
        assertDefinition(
                MeMekanismMachine.ABSOLUTE_OVERCLOCKED_DISSOLVING_FACTORY,
                CompatMachineFamily.EMEKE_MEKAF_ADVANCED_FACTORY,
                "absolute_overclocked",
                "dissolving",
                "me_absolute_overclocked_dissolving_factory",
                "ME Absolute Overclocked Dissolving Factory");
        assertDefinition(
                MeMekanismMachine.BASIC_ALLOYING_FACTORY,
                CompatMachineFamily.EMEK_FACTORY,
                "basic",
                "alloying",
                "me_basic_alloying_factory",
                "ME Basic Alloying Factory");
        assertDefinition(
                MeMekanismMachine.ABSOLUTE_SMELTING_FACTORY,
                CompatMachineFamily.MEKE_FACTORY,
                "absolute",
                "smelting",
                "me_absolute_smelting_factory",
                "ME Absolute Smelting Factory");
        assertDefinition(
                MeMekanismMachine.BASIC_RECYCLING_FACTORY,
                CompatMachineFamily.MEKMM_FACTORY,
                "basic",
                "recycling",
                "me_basic_recycling_factory",
                "ME Basic Recycling Factory");
    }

    @Test
    void everyDeclarationHasCompleteIntrinsicMetadata() {
        for (MeMekanismMachine machine : MeMekanismMachine.values()) {
            assertNotNull(machine.family(), machine.name());
            assertNotNull(machine.provider(), machine.name());
            assertNotNull(machine.registrationRoute(), machine.name());
            assertFalse(machine.baseName().isBlank(), machine.name());
            assertFalse(machine.registryName().isBlank(), machine.name());
            assertFalse(machine.machineTypeId().isBlank(), machine.name());
            assertFalse(machine.englishName().isBlank(), machine.name());
            assertEquals(machine.isFactory(), machine.tierId() != null, machine.name());
        }
    }

    @Test
    void ioProfilesPreserveExceptionalRecipeShapes() {
        assertEquals(MeMekanismMachine.SlotLayout.DOUBLE_ITEM,
                MeMekanismMachine.COMBINER.slotLayout());
        assertEquals(MeMekanismMachine.SlotLayout.DOUBLE_ITEM,
                MeMekanismMachine.CNC_STAMPER.slotLayout());
        assertTrue(MeMekanismMachine.CHEMIXER.hasSecondaryItemInput());
        assertTrue(MeMekanismMachine.CHEMIXER.hasChemicalInput());

        assertEquals(MeMekanismMachine.SlotLayout.ITEM_CHEMICAL,
                MeMekanismMachine.OSMIUM_COMPRESSOR.slotLayout());
        assertTrue(MeMekanismMachine.OSMIUM_COMPRESSOR.hasAdvancedChemicalInput());
        assertEquals(MeMekanismMachine.SlotLayout.SAWING,
                MeMekanismMachine.PRECISION_SAWMILL.slotLayout());
        assertEquals(MeMekanismMachine.SlotLayout.SINGLE_ITEM,
                MeMekanismMachine.ENRICHMENT_CHAMBER.slotLayout());

        assertTrue(MeMekanismMachine.BASIC_RECYCLING_FACTORY.hasRecipeLogic());
        assertFalse(MeMekanismMachine.DIGITAL_MINER.hasRecipeLogic());
    }

    /**
     * Factories inherit their base machine's energy instead of a flat constant, so the suppliers now
     * read Mekanism's config. Building them must stay side-effect free -- touching MekanismConfig
     * loads Minecraft registries, which cannot happen outside a running game -- so this asserts a
     * supplier is produced without evaluating it. The derivation itself is covered by
     * {@link MeMachineEnergyProfileTest}.
     */
    @Test
    void energyProfileIsBuiltWithoutReadingConfig() {
        for (MeMekanismMachine machine : new MeMekanismMachine[] {
                MeMekanismMachine.BASIC_SMELTING_FACTORY,
                MeMekanismMachine.ULTIMATE_PURIFYING_FACTORY,
                MeMekanismMachine.ENRICHMENT_CHAMBER}) {
            assertNotNull(machine.energyUsage(), machine.name());
            assertNotNull(machine.energyStorage(), machine.name());
        }
    }

    private static void assertDefinition(
            MeMekanismMachine machine,
            CompatMachineFamily expectedFamily,
            String expectedTier,
            String expectedType,
            String expectedRegistryName,
            String expectedEnglishName) {
        assertEquals(expectedFamily, machine.family());
        assertEquals(expectedFamily.provider(), machine.provider());
        assertEquals(expectedType, machine.machineTypeId());
        assertEquals(expectedRegistryName, machine.registryName());
        assertEquals(expectedEnglishName, machine.englishName());
        if (expectedTier == null) {
            assertNull(machine.tierId());
        } else {
            assertEquals(expectedTier, machine.tierId());
        }
        assertEquals(CompatMod.MEKANISM == machine.provider() && !machine.isFactory(),
                machine.descriptionKey().startsWith("description.mekanism.")
                        && !"description.mekanism.factory".equals(machine.descriptionKey()));
    }
}
