package com.beipuo.mekenergistics.common.machine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.beipuo.mekenergistics.compat.catalog.CompatMachineFamily;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.stream.Collectors;
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
                MeMekanismMachine.valueOf("BASIC_SMELTING_FACTORY"),
                CompatMachineFamily.MEKANISM_FACTORY,
                "basic",
                "smelting",
                "me_basic_smelting_factory",
                "ME Basic Smelting Factory");
        assertDefinition(
                MeMekanismMachine.valueOf("ABSOLUTE_OVERCLOCKED_SMELTING_FACTORY"),
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
                MeMekanismMachine.valueOf("ABSOLUTE_OVERCLOCKED_DISSOLVING_FACTORY"),
                CompatMachineFamily.EMEKE_MEKAF_ADVANCED_FACTORY,
                "absolute_overclocked",
                "dissolving",
                "me_absolute_overclocked_dissolving_factory",
                "ME Absolute Overclocked Dissolving Factory");
        assertDefinition(
                MeMekanismMachine.valueOf("BASIC_ALLOYING_FACTORY"),
                CompatMachineFamily.EMEK_FACTORY,
                "basic",
                "alloying",
                "me_basic_alloying_factory",
                "ME Basic Alloying Factory");
        assertDefinition(
                MeMekanismMachine.valueOf("ABSOLUTE_SMELTING_FACTORY"),
                CompatMachineFamily.MEKE_FACTORY,
                "absolute",
                "smelting",
                "me_absolute_smelting_factory",
                "ME Absolute Smelting Factory");
        assertDefinition(
                MeMekanismMachine.valueOf("BASIC_RECYCLING_FACTORY"),
                CompatMachineFamily.MEKMM_FACTORY,
                "basic",
                "recycling",
                "me_basic_recycling_factory",
                "ME Basic Recycling Factory");
    }

    @Test
    void everyDeclarationHasCompleteIntrinsicMetadata() {
        MeMekanismMachine[] machines = MeMekanismMachine.values();
        assertEquals(407, machines.length);
        for (int ordinal = 0; ordinal < machines.length; ordinal++) {
            MeMekanismMachine machine = machines[ordinal];
            assertSame(machine, MeMekanismMachine.valueOf(machine.name()), machine.name());
            assertEquals(ordinal, machine.ordinal(), machine.name());
            assertEquals(machine.name().toLowerCase(java.util.Locale.ROOT),
                    machine.serializedName(), machine.name());
            assertNotNull(machine.family(), machine.name());
            assertNotNull(machine.provider(), machine.name());
            assertNotNull(machine.registrationRoute(), machine.name());
            assertFalse(machine.baseName().isBlank(), machine.name());
            assertFalse(machine.machineTypeId().isBlank(), machine.name());
            assertFalse(machine.englishName().isBlank(), machine.name());
            assertEquals(machine.isFactory(), machine.tierId() != null, machine.name());
        }
    }

    @Test
    void generatedFactoryGroupsPreserveLegacyDeclarationOrder() {
        assertDeclaration(52, "BASIC_SMELTING_FACTORY");
        assertDeclaration(88, "BASIC_ALLOYING_FACTORY");
        assertDeclaration(92, "OVERCLOCKED_SMELTING_FACTORY");
        assertDeclaration(137, "OVERCLOCKED_ALLOYING_FACTORY");
        assertDeclaration(142, "ABSOLUTE_SMELTING_FACTORY");
        assertDeclaration(178, "ABSOLUTE_ALLOYING_FACTORY");
        assertDeclaration(182, "ABSOLUTE_OVERCLOCKED_SMELTING_FACTORY");
        assertDeclaration(218, "ABSOLUTE_OVERCLOCKED_ALLOYING_FACTORY");
        assertDeclaration(226, "ABSOLUTE_OVERCLOCKED_DISSOLVING_FACTORY");
        assertDeclaration(282, "BASIC_RECYCLING_FACTORY");
        assertDeclaration(306, "ABSOLUTE_RECYCLING_FACTORY");
        assertDeclaration(330, "BASIC_OXIDIZING_FACTORY");
        assertDeclaration(366, "ABSOLUTE_OXIDIZING_FACTORY");
        assertDeclaration(401, "INFINITE_PAINTING_FACTORY");
    }

    @Test
    void completeIdentitySequenceMatchesLegacyEnum() throws NoSuchAlgorithmException {
        String names = Arrays.stream(MeMekanismMachine.values())
                .map(MeMekanismMachine::name)
                .collect(Collectors.joining("\n"));
        String digest = HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-256").digest(names.getBytes(StandardCharsets.UTF_8)));

        assertEquals("b9ec9933ea6cca3dc26f7adda658ac915b976b716d9a74b4cb6f86d76297b40e", digest);
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

        assertTrue(MeMekanismMachine.valueOf("BASIC_RECYCLING_FACTORY").hasRecipeLogic());
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
                MeMekanismMachine.valueOf("BASIC_SMELTING_FACTORY"),
                MeMekanismMachine.valueOf("ULTIMATE_PURIFYING_FACTORY"),
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
        assertEquals(expectedFamily, machine.family(), machine::name);
        assertEquals(expectedType, machine.machineTypeId(), machine::name);
        assertEquals(expectedRegistryName, machine.registryName(), machine::name);
        assertEquals(expectedEnglishName, machine.englishName(), machine::name);
        if (expectedTier == null) {
            assertNull(machine.tierId(), machine::name);
        } else {
            assertEquals(expectedTier, machine.tierId(), machine::name);
        }
    }

    private static void assertDeclaration(int ordinal, String name) {
        MeMekanismMachine machine = MeMekanismMachine.values()[ordinal];
        assertEquals(name, machine.name());
        assertSame(machine, MeMekanismMachine.valueOf(name));
    }
}
