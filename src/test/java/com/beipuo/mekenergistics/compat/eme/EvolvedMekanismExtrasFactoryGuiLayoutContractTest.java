package com.beipuo.mekenergistics.compat.eme;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class EvolvedMekanismExtrasFactoryGuiLayoutContractTest {

    @Test
    void meCentrifugingAndPlantingFactoriesPreserveEmekeInventoryOffsets() throws IOException {
        assertPreservesSourceContainer(
                "EvolvedMekanismExtrasAdvancedMenuTypes.java",
                "MePatternEMExtraAdvancedFactoryContainer",
                "EMExtraAdvancedFactoryContainer");
        assertPreservesSourceContainer(
                "EvolvedMekanismExtrasMoreMachineMenuTypes.java",
                "MePatternEMExtraMoreMachineFactoryContainer",
                "EMExtraMoreMachineFactoryContainer");
    }

    private static void assertPreservesSourceContainer(
            String registrationFile, String meContainer, String sourceContainer) throws IOException {
        Path sourceRoot = Path.of("src/main/java/com/beipuo/mekenergistics");
        Path registration = sourceRoot.resolve("compat/eme").resolve(registrationFile);
        Path container = sourceRoot.resolve("menu/compat/eme").resolve(meContainer + ".java");

        assertTrue(Files.exists(container),
                meContainer + " must exist so the ME menu keeps EMEKE's player-inventory offsets");

        String registrationSource = Files.readString(registration);
        String containerSource = Files.readString(container);
        assertAll(
                () -> assertTrue(registrationSource.contains("new " + meContainer + "("),
                        registrationFile + " must construct " + meContainer),
                () -> assertTrue(containerSource.contains("extends " + sourceContainer),
                        meContainer + " must inherit geometry from " + sourceContainer),
                () -> assertTrue(containerSource.contains("implements MePatternQuickMoveContainer"),
                        meContainer + " must retain ME pattern quick-move behavior"));
    }
}
