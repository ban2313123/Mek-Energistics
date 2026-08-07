package com.beipuo.mekenergistics.client.overlay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class MePatternWindowOverlayLayoutTest {
    private static final Path SOURCE = Path.of(
            "src/main/java/com/beipuo/mekenergistics/client/overlay/MePatternWindowOverlay.java");

    @Test
    void bottomControlsFollowTheFourthPatternRowWithoutAnEmptySlotRow() throws IOException {
        String source = Files.readString(SOURCE);
        assertEquals(1, count(source, "private static final int WINDOW_HEIGHT = 118;"));
        assertEquals(7, count(source, "relativeY + 96"),
                "page fields and navigation/output buttons must share the row immediately below the slots");
        assertTrue(source.contains("relativeY + 18 + row * 18"));
    }

    @Test
    void patternButtonRejectsUnsupportedOrInactiveExternalMixinTargets() throws IOException {
        String source = Files.readString(SOURCE);
        int factoryBranch = source.indexOf("MeFactoryAeMachine machine");
        int upgradeBranch = source.indexOf("MeUpgradeableMachine upgradeable");
        assertTrue(factoryBranch >= 0 && upgradeBranch >= 0 && factoryBranch < upgradeBranch,
                "factory targets should be resolved before the generic ME upgrade gate");
        assertTrue(source.contains("instanceof MeMekanismMachineBlock"));
        assertTrue(source.contains("upgradeTarget && upgradeable.isMeUpgradeActive()"));

        assertTrue(PatternWindowTargetPolicy.shouldShow(true, false, false),
                "native ME machines receive the upgrade mixin too and must remain visible");
        assertFalse(PatternWindowTargetPolicy.shouldShow(false, false, false),
                "unrelated third-party subclasses must not receive a phantom pattern window");
        assertFalse(PatternWindowTargetPolicy.shouldShow(false, true, false),
                "declared upgrade targets stay hidden until their ME upgrade is active");
        assertTrue(PatternWindowTargetPolicy.shouldShow(false, true, true));
    }

    @Test
    void missingContainerSlotIsNotPassedToMekanismVirtualSlot() throws IOException {
        String source = Files.readString(SOURCE);
        int nullGuard = source.indexOf("if (virtualSlot != null)");
        int parentUpdate = source.indexOf("super.updateVirtualSlot(window, virtualSlot)");

        assertTrue(nullGuard >= 0 && parentUpdate > nullGuard,
                "Mekanism requires a non-null IVirtualSlot when updating its GUI slot");
        assertTrue(source.contains("this.virtualSlot != null && super.mouseClicked"),
                "a cleared page slot must not keep forwarding clicks to an old virtual slot");
    }

    private static int count(String source, String literal) {
        Matcher matcher = Pattern.compile(Pattern.quote(literal)).matcher(source);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }
}
