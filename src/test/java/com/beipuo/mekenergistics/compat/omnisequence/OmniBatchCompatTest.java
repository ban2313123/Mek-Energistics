package com.beipuo.mekenergistics.compat.omnisequence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class OmniBatchCompatTest {

    /**
     * The no-double-batch contract: exactly one engine owns batch dispatch on a CPU. OmniSequence
     * only batches on CPUs its omni computation core owns, so Mek-Energistics' own CPU batching
     * must stay in charge whenever the mod is absent or the core does not own the CPU.
     */
    @Test
    void presentAndAbsentMatricesNeverRunBothBatchingEngines() {
        assertFalse(OmniBatchCompat.isOmniManagedCpu(false, true),
                "Omni absent: Mek-Energistics batching owns the CPU");
        assertFalse(OmniBatchCompat.isOmniManagedCpu(true, false),
                "core does not own the CPU: Mek-Energistics batching owns it");
        assertTrue(OmniBatchCompat.isOmniManagedCpu(true, true),
                "core owns the CPU: Omni owns dispatch and our batching defers");
    }

    @Test
    void absentOmniProducesNoBridgeWarning() {
        assertEquals(Optional.empty(), OmniBatchCompat.bridgeWarning(false, false, false));
    }

    @Test
    void presentAbiProducesNoBridgeWarning() {
        assertEquals(Optional.empty(), OmniBatchCompat.bridgeWarning(true, true, true));
    }

    @Test
    void missingProviderIsNamedInTheWarning() {
        Optional<String> warning = OmniBatchCompat.bridgeWarning(true, false, true);
        assertTrue(warning.isPresent());
        assertTrue(warning.get().contains("MolecularBatchCraftingProvider"), warning.get());
    }

    @Test
    void missingCoreIsNamedInTheWarning() {
        Optional<String> warning = OmniBatchCompat.bridgeWarning(true, true, false);
        assertTrue(warning.isPresent());
        assertTrue(warning.get().contains("OmniComputationCoreBlockEntity"), warning.get());
    }
}