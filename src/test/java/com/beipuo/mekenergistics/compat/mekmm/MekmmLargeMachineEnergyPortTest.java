package com.beipuo.mekenergistics.compat.mekmm;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class MekmmLargeMachineEnergyPortTest {
    private static final Path MACHINE_DIR = Path.of(
            "src/main/java/com/beipuo/mekenergistics/blockentity/compat/mekmm/machine");

    @Test
    void aeBackedLargeMachinesKeepMekmmFixedBackEnergyPort() throws IOException {
        for (String name : new String[] {
                "MeLargeChemicalInfuserBlockEntity.java",
                "MeLargeRotaryCondensentratorBlockEntity.java",
                "MeLargeAntiprotonicNucleosynthesizerBlockEntity.java"
        }) {
            String source = Files.readString(MACHINE_DIR.resolve(name));
            assertTrue(source.contains("EnergyContainerHelper.forSide(this.facingSupplier)"),
                    () -> name + " must use MekMM's fixed-side energy holder");
            assertTrue(source.contains("builder.addContainer(energy, RelativeSide.BACK)"),
                    () -> name + " must expose energy through the physical back port");
            assertFalse(source.contains("EnergyContainerHelper.forSideWithConfig(this)"),
                    () -> name + " must not hide the physical energy port behind copied side configuration");
        }
    }
}
