package com.beipuo.mekenergistics.compat.dataenergistics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class DataAbiGateTest {

    @Test
    void absentDataProducesNoCountedBridgeWarning() {
        assertEquals(Optional.empty(), DataAbiGate.countedBridgeWarning(false, false, false));
    }

    @Test
    void presentProviderAndAdmissionEnableTheBridgeSilently() {
        assertEquals(Optional.empty(), DataAbiGate.countedBridgeWarning(true, true, true));
    }

    @Test
    void missingProviderGatesTheBridgeAndNamesIt() {
        Optional<String> warning = DataAbiGate.countedBridgeWarning(true, false, true);
        assertTrue(warning.isPresent());
        assertTrue(warning.get().contains("CountedCraftingProvider"), warning.get());
    }

    @Test
    void missingAdmissionGatesTheBridgeAndNamesIt() {
        Optional<String> warning = DataAbiGate.countedBridgeWarning(true, true, false);
        assertTrue(warning.isPresent());
        assertTrue(warning.get().contains("CountedCraftingAdmission"), warning.get());
    }

    @Test
    void bothMissingNameEveryAbsentAbiClass() {
        Optional<String> warning = DataAbiGate.countedBridgeWarning(true, false, false);
        assertTrue(warning.isPresent());
        assertTrue(warning.get().contains("CountedCraftingProvider"), warning.get());
        assertTrue(warning.get().contains("CountedCraftingAdmission"), warning.get());
    }
}