package com.beipuo.mekenergistics.client.overlay;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    void meUpgradePatternButtonDoesNotDependOnTheLiveActivationState() throws IOException {
        String source = Files.readString(SOURCE);
        int factoryBranch = source.indexOf("MeFactoryAeMachine machine");
        int upgradeBranch = source.indexOf("MeUpgradeableMachine upgradeable");
        assertTrue(factoryBranch >= 0 && upgradeBranch >= 0 && factoryBranch < upgradeBranch,
                "factory targets should be resolved before the generic ME upgrade gate");
        assertTrue(source.contains("MeUpgradeableMachine upgradeable && !upgradeable.isMeUpgradeActive()"));
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
