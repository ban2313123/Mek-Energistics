package com.beipuo.mekenergistics.upgrade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

class MeUpgradeDataTest {
    @Test
    void emptyDataHasNoInstalledUpgrades() {
        MeUpgradeData data = MeUpgradeData.EMPTY;
        assertTrue(data.isEmpty());
        assertFalse(data.isInstalled(MeUpgradeType.PATTERN_PROVIDER));
        assertEquals(0, data.count(MeUpgradeType.OUTPUT_INTERFACE));
    }

    @Test
    void withClampsToTypeMaximum() {
        MeUpgradeData data = MeUpgradeData.EMPTY.with(MeUpgradeType.PATTERN_PROVIDER, 99);
        assertEquals(1, data.count(MeUpgradeType.PATTERN_PROVIDER));
    }

    @Test
    void withRemovesNonPositiveCounts() {
        MeUpgradeData data = MeUpgradeData.EMPTY.with(MeUpgradeType.PASSIVE_CRAFTING, 1);
        data = data.with(MeUpgradeType.PASSIVE_CRAFTING, 0);
        assertFalse(data.isInstalled(MeUpgradeType.PASSIVE_CRAFTING));
    }

    @Test
    void flatMapRoundTripsCountsAndPreservedNames() {
        MeUpgradeData data = MeUpgradeData.EMPTY
                .with(MeUpgradeType.PATTERN_PROVIDER, 1)
                .withPreserved(Map.of("some_other_mod:upgrade", 3));
        MeUpgradeData decoded = MeUpgradeData.fromFlatMap(data.toFlatMap());
        assertEquals(1, decoded.count(MeUpgradeType.PATTERN_PROVIDER));
        assertEquals(3, decoded.preserved().get("some_other_mod:upgrade"));
    }

    @Test
    void unknownNamesNeverMapToKnownUpgrades() {
        MeUpgradeData data = MeUpgradeData.fromFlatMap(Map.of("me_pattern_provider", 1, "mystery", 2));
        assertTrue(data.isInstalled(MeUpgradeType.PATTERN_PROVIDER));
        assertEquals(2, data.preserved().get("mystery"));
        assertEquals(0, data.count(MeUpgradeType.PASSIVE_CRAFTING));
    }
}
