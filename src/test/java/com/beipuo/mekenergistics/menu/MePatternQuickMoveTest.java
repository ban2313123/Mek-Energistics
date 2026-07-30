package com.beipuo.mekenergistics.menu;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class MePatternQuickMoveTest {
    private static final Path MIXIN = Path.of(
            "src/main/java/com/beipuo/mekenergistics/mixin/MekanismContainerPatternQuickMoveMixin.java");

    @Test
    void vanillaContainerQuickMoveIsLimitedToActiveUpgradeAndEncodedPatterns() throws IOException {
        String source = Files.readString(MIXIN);

        assertTrue(source.contains("instanceof MeUpgradeableMachine machine"));
        assertTrue(source.contains("!machine.isMeUpgradeActive()"));
        assertTrue(source.contains("currentSlot instanceof InventoryContainerSlot"));
        assertTrue(source.contains("PatternDetailsHelper.isEncodedPattern(slotStack)"));
        assertTrue(source.contains("insertIntoPatternSlots"));
        assertTrue(source.contains("transferSuccess(currentSlot, player, slotStack, remaining)"));
    }
}
