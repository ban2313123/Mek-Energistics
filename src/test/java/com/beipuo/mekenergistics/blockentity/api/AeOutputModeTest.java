package com.beipuo.mekenergistics.blockentity.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import mekanism.common.lib.transmitter.TransmissionType;
import org.junit.jupiter.api.Test;

/**
 * Which of a machine's outputs are pushed into the ME network. The mode is stored as an ordinal in
 * NBT and synced as an ordinal to the client, so the constant order is a save-compatibility
 * contract, and every flag combination must map back to a distinct constant — the lookup falls back
 * to {@code BOTH} rather than failing, which would quietly re-enable exports a player turned off.
 */
class AeOutputModeTest {
    @Test
    void theFirstFourConstantsKeepTheOrderOldSavesWereWrittenWith() {
        assertEquals(List.of(AeOutputMode.BOTH, AeOutputMode.ITEMS, AeOutputMode.CHEMICALS, AeOutputMode.NONE),
                List.of(AeOutputMode.byId(0), AeOutputMode.byId(1), AeOutputMode.byId(2), AeOutputMode.byId(3)),
                "reordering these silently changes what every saved machine exports");
    }

    @Test
    void everyModeRoundTripsThroughItsStoredOrdinal() {
        for (AeOutputMode mode : AeOutputMode.values()) {
            assertSame(mode, AeOutputMode.byId(mode.ordinal()), mode::name);
        }
    }

    @Test
    void anOrdinalFromOutsideTheRangeFallsBackInsteadOfThrowing() {
        assertSame(AeOutputMode.BOTH, AeOutputMode.byId(-1), "a corrupt tag must not crash loading");
        assertSame(AeOutputMode.BOTH, AeOutputMode.byId(AeOutputMode.values().length),
                "an ordinal from a newer version must not crash loading");
    }

    @Test
    void everyCombinationOfTheThreeFlagsHasItsOwnConstant() {
        Set<List<Boolean>> combinations = EnumSet.allOf(AeOutputMode.class).stream()
                .map(mode -> List.of(mode.items(), mode.chemicals(), mode.fluids()))
                .collect(java.util.stream.Collectors.toSet());

        // Eight constants for 2^3 combinations. If that stops holding, toggle() starts resolving to
        // the BOTH fallback and turns exports back on behind the player's back.
        assertEquals(8, combinations.size(), "each mode must describe a distinct set of enabled outputs");
    }

    @Test
    void togglingAnOutputTwiceReturnsToTheOriginalMode() {
        for (AeOutputMode mode : AeOutputMode.values()) {
            for (TransmissionType type : List.of(
                    TransmissionType.ITEM, TransmissionType.CHEMICAL, TransmissionType.FLUID)) {
                assertNotEquals(mode, mode.toggle(type), () -> mode + " toggled by " + type);
                assertSame(mode, mode.toggle(type).toggle(type), () -> mode + " toggled twice by " + type);
            }
        }
    }

    @Test
    void togglingOneOutputLeavesTheOtherTwoAlone() {
        for (AeOutputMode mode : AeOutputMode.values()) {
            AeOutputMode items = mode.toggle(TransmissionType.ITEM);
            assertEquals(!mode.items(), items.items(), mode::name);
            assertEquals(mode.chemicals(), items.chemicals(), mode::name);
            assertEquals(mode.fluids(), items.fluids(), mode::name);

            AeOutputMode chemicals = mode.toggle(TransmissionType.CHEMICAL);
            assertEquals(mode.items(), chemicals.items(), mode::name);
            assertEquals(!mode.chemicals(), chemicals.chemicals(), mode::name);
            assertEquals(mode.fluids(), chemicals.fluids(), mode::name);

            AeOutputMode fluids = mode.toggle(TransmissionType.FLUID);
            assertEquals(mode.items(), fluids.items(), mode::name);
            assertEquals(mode.chemicals(), fluids.chemicals(), mode::name);
            assertEquals(!mode.fluids(), fluids.fluids(), mode::name);
        }
    }

    @Test
    void anOutputTypeWeDoNotExportLeavesTheModeUntouched() {
        assertSame(AeOutputMode.ITEMS, AeOutputMode.ITEMS.toggle(TransmissionType.ENERGY));
        assertSame(AeOutputMode.ITEMS, AeOutputMode.ITEMS.toggle(TransmissionType.HEAT));
    }

    @Test
    void cyclingVisitsEveryModeOnceBeforeComingBackAround() {
        AeOutputMode mode = AeOutputMode.BOTH;
        Set<AeOutputMode> visited = EnumSet.noneOf(AeOutputMode.class);

        for (int step = 0; step < AeOutputMode.values().length; step++) {
            visited.add(mode);
            mode = mode.next();
        }

        assertEquals(EnumSet.allOf(AeOutputMode.class), visited, "the button must reach every mode");
        assertSame(AeOutputMode.BOTH, mode, "and wrap back to where it started");
    }
}
