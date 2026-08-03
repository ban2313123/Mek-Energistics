package com.beipuo.mekenergistics.compat.meke;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class MekanismExtrasDissolvingFactoryIoTest {
    private static final Path FACTORY = Path.of(
            "src/main/java/com/beipuo/mekenergistics/blockentity/compat/meke/factory/MeExtraAdvancedDissolvingFactoryBlockEntity.java");
    private static final Path ACCESSOR = Path.of(
            "src/main/java/com/beipuo/mekenergistics/mixin/TileEntityExtraDissolvingFactoryAccessor.java");
    private static final Path MIXIN_CONFIG = Path.of("src/main/resources/mekenergistics.mixins.json");
    private static final Path MIXIN_PLUGIN = Path.of(
            "src/main/java/com/beipuo/mekenergistics/mixin/MekEnergisticsMixinPlugin.java");

    @Test
    void dissolutionFactoryUsesItsRealChemicalConversionSlot() throws IOException {
        String factory = Files.readString(FACTORY);
        String accessor = Files.readString(ACCESSOR);

        assertTrue(factory.contains("mekenergistics$getChemicalInputSlot()"));
        assertFalse(factory.contains("itemInput(getExtraSlot())"),
                "Mekanism Extras inherits a nullable default getExtraSlot implementation");
        assertTrue(accessor.contains("@Accessor(value = \"chemicalInputSlot\", remap = false)"));
    }

    @Test
    void optionalAccessorIsConfiguredAndTargetClassGated() throws IOException {
        assertTrue(Files.readString(MIXIN_CONFIG).contains("\"TileEntityExtraDissolvingFactoryAccessor\""));
        String plugin = Files.readString(MIXIN_PLUGIN);
        assertTrue(plugin.contains(".TileEntityExtraDissolvingFactoryAccessor"));
        assertTrue(plugin.contains("Gate.target(\"mekanism_extras\""));
    }
}
