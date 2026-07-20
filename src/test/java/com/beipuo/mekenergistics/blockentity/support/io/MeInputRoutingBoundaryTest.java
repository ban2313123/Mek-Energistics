package com.beipuo.mekenergistics.blockentity.support.io;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class MeInputRoutingBoundaryTest {
    private static final Path BLOCK_ENTITY_ROOT = Path.of(
            "src/main/java/com/beipuo/mekenergistics/blockentity");

    @Test
    void concreteMachinesDoNotParseAeKeys() throws IOException {
        try (var files = Files.walk(BLOCK_ENTITY_ROOT)) {
            files.filter(path -> path.toString().endsWith("BlockEntity.java"))
                    .forEach(MeInputRoutingBoundaryTest::assertNoKeyParsing);
        }
    }

    @Test
    void encodedPatternsAreDecodedOnlyBySafeHelper() throws IOException {
        Path sourceRoot = Path.of("src/main/java/com/beipuo/mekenergistics");
        Path safeHelper = Path.of(
                "src/main/java/com/beipuo/mekenergistics/blockentity/support/MePatternDecodeHelper.java");
        try (var files = Files.walk(sourceRoot)) {
            files.filter(path -> path.toString().endsWith(".java"))
                    .filter(path -> !path.equals(safeHelper))
                    .forEach(path -> assertSourceDoesNotContain(path,
                            "PatternDetailsHelper.decodePattern"));
        }
        assertTrue(read(safeHelper).contains("PatternDetailsHelper.decodePattern"));
    }

    @Test
    void removedInputHelpersStayRemoved() {
        for (String className : List.of(
                "MeLegacyMachineAeHelper.java",
                "MeMekanismMachinePatternInput.java",
                "MeFactoryPatternInput.java",
                "MeExternalFactorySupport.java",
                "MeExtraFactoryBridge.java",
                "MeAdvancedFactorySupport.java")) {
            try (var matches = Files.walk(Path.of("src/main/java"))) {
                assertFalse(matches.anyMatch(path -> path.endsWith(className)), className);
            } catch (IOException exception) {
                throw new AssertionError(exception);
            }
        }
    }

    private static void assertNoKeyParsing(Path path) {
        String source = read(path);
        assertFalse(source.contains("MePatternInputRouter.PatternInput"), path.toString());
        assertFalse(source.contains("instanceof AEItemKey"), path.toString());
        assertFalse(source.contains("instanceof AEFluidKey"), path.toString());
        assertFalse(source.contains("instanceof MekanismKey"), path.toString());
    }

    private static void assertSourceDoesNotContain(Path path, String token) {
        assertFalse(read(path).contains(token), path.toString());
    }

    private static String read(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException exception) {
            throw new AssertionError(exception);
        }
    }
}
