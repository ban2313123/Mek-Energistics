package com.beipuo.mekenergistics.blockentity.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MeChemicalInputCapabilityTest {
    @Test
    void classifiesMachineRecipeIdsWithoutRecipeClasses() {
        assertEquals(MeChemicalInputCapability.CHEMICAL_OR_CONVERSION_SLOT,
                MeChemicalInputCapability.forRecipeId("mekanism:metallurgic_infusing"));
        assertEquals(MeChemicalInputCapability.CHEMICAL_OR_CONVERSION_SLOT,
                MeChemicalInputCapability.forMachineType("infusing"));
        assertEquals(MeChemicalInputCapability.CHEMICAL_OR_CONVERSION_SLOT,
                MeChemicalInputCapability.forRecipeId("mekanism:dissolution"));
        assertEquals(MeChemicalInputCapability.ITEM_TO_CHEMICAL_OUTPUT,
                MeChemicalInputCapability.forRecipeId("mekanism:oxidizing"));
        assertEquals(MeChemicalInputCapability.ITEM_TO_CHEMICAL_OUTPUT,
                MeChemicalInputCapability.forRecipeId("pigment_extracting"));
        assertEquals(MeChemicalInputCapability.ITEM_TO_CHEMICAL_OUTPUT,
                MeChemicalInputCapability.forMachineType("oxidizing"));
    }

    @Test
    void chemicalConversionIsAConversionSlotCapability() {
        MeChemicalInputCapability capability = MeChemicalInputCapability.forRecipeId("mekanism:chemical_conversion");
        assertTrue(capability.acceptsConversionCarrier());
        assertFalse(capability.producesChemicalFromItem());
    }

    @Test
    void customFactoryFamiliesUseTheSameConversionCapability() {
        assertTrue(MeChemicalInputCapability.forMachineType("planting").acceptsConversionCarrier());
        assertTrue(MeChemicalInputCapability.forMachineType("replicating").acceptsConversionCarrier());
        assertTrue(MeChemicalInputCapability.forMachineType("dissolving").acceptsConversionCarrier());
        assertTrue(MeChemicalInputCapability.forMachineType("chemical_replicator").acceptsConversionCarrier());
        assertTrue(MeChemicalInputCapability.forMachineType("fluid_replicator").acceptsConversionCarrier());
        assertTrue(MeChemicalInputCapability.forMachineType("large_antiprotonic_nucleosynthesizer")
                .acceptsConversionCarrier());
    }

    @Test
    void unknownAndInvalidIdsAreNotChemicalCapabilities() {
        assertEquals(MeChemicalInputCapability.NONE, MeChemicalInputCapability.forRecipeId(null));
        assertEquals(MeChemicalInputCapability.NONE, MeChemicalInputCapability.forRecipeId(""));
        assertEquals(MeChemicalInputCapability.NONE, MeChemicalInputCapability.forRecipeId("minecraft:smelting"));
        // These machine families expose chemical tanks directly, not a
        // ChemicalInventorySlot.fillOrConvert conversion lane.
        assertEquals(MeChemicalInputCapability.NONE, MeChemicalInputCapability.forRecipeId("mekaf:washing"));
        assertEquals(MeChemicalInputCapability.NONE, MeChemicalInputCapability.forRecipeId("mekaf:centrifuging"));
        assertEquals(MeChemicalInputCapability.NONE, MeChemicalInputCapability.forRecipeId("mekaf:liquifying"));
        assertEquals(MeChemicalInputCapability.NONE, MeChemicalInputCapability.forRecipeId("mekaf:crystallizing"));
    }
}
