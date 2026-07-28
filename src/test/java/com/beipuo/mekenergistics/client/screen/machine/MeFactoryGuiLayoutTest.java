package com.beipuo.mekenergistics.client.screen.machine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class MeFactoryGuiLayoutTest {

    private static final Path SCREEN_DIR =
            Path.of("src/main/java/com/beipuo/mekenergistics/client/screen/machine");

    /**
     * Pins the geometry so a change to any one factory screen cannot quietly alter the shared
     * formulas. The values are the pre-refactor results, tier index 0 through 2.
     */
    @Test
    void formulasMatchTheLayoutTheyReplaced() {
        for (int index = 0; index <= 2; index++) {
            assertEquals((36 * (index + 2)) + (2 * index),
                    MeFactoryGuiLayout.imageWidthDelta(index), "imageWidthDelta(" + index + ")");
            assertEquals((22 * (index + 2)) - (3 * index),
                    MeFactoryGuiLayout.inventoryLabelX(index), "inventoryLabelX(" + index + ")");
            assertEquals(210 + 38 * index, MeFactoryGuiLayout.barWidth(index), "barWidth(" + index + ")");
            assertEquals(220 + 38 * index, MeFactoryGuiLayout.buttonX(index), "buttonX(" + index + ")");
        }
        assertEquals(4, MeFactoryGuiLayout.MEKANISM_TIER_OFFSET);
    }

    /**
     * These formulas previously sat inline at six call sites across two files, and had already
     * drifted -- one copy carried a typo in the caveat comment. Keep them in one place.
     */
    @Test
    void noFactoryScreenReinlinesTheFormulas() throws IOException {
        List<Pattern> inlined = List.of(
                Pattern.compile("36 \\* \\(\\w+ \\+ 2\\)"),
                Pattern.compile("22 \\* \\(\\w+ \\+ 2\\)"),
                Pattern.compile("210 \\+ 38 \\*"),
                Pattern.compile("220 \\+ 38 \\*"));

        List<String> offenders = new ArrayList<>();
        try (Stream<Path> files = Files.list(SCREEN_DIR)) {
            for (Path screen : files.filter(p -> p.toString().endsWith(".java")).toList()) {
                if (screen.getFileName().toString().equals("MeFactoryGuiLayout.java")) {
                    continue; // the one place the formulas are allowed to live
                }
                String source = Files.readString(screen);
                if (inlined.stream().anyMatch(p -> p.matcher(source).find())) {
                    offenders.add(screen.getFileName().toString());
                }
            }
        }
        assertEquals(List.of(), offenders,
                "Factory layout maths belongs in MeFactoryGuiLayout, not inlined per screen");
    }

    @Test
    void theDriftedCaveatCommentIsGone() throws IOException {
        List<String> offenders = new ArrayList<>();
        try (Stream<Path> files = Files.list(SCREEN_DIR)) {
            for (Path screen : files.filter(p -> p.toString().endsWith(".java")).toList()) {
                if (Files.readString(screen).contains("mekE的布局公式")) {
                    offenders.add(screen.getFileName().toString());
                }
            }
        }
        assertFalse(offenders.size() > 0, "Duplicated layout caveat comment reappeared in " + offenders);
    }
}
