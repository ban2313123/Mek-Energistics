package com.beipuo.mekenergistics.compat.magic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.beipuo.mekenergistics.blockentity.support.io.MeInputLayout;
import com.beipuo.mekenergistics.common.machine.MeMekanismMachine;
import com.beipuo.mekenergistics.compat.catalog.CompatMachineCatalog;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Proves the Magic upgrade profile mapping and the wiring that keeps the optional adapter out of
 * worlds that never install Mekanism Magic.
 */
class MekanismMagicUpgradeProfilesTest {
    private static final Path ACCESS = Path.of(
            "src/main/java/com/beipuo/mekenergistics/compat/magic/MekanismMagicAutomationAccess.java");
    private static final Path PROFILES = Path.of(
            "src/main/java/com/beipuo/mekenergistics/compat/magic/MekanismMagicUpgradeProfiles.java");
    private static final Path MIXIN = Path.of(
            "src/main/java/com/beipuo/mekenergistics/mixin/MekanismMagicMachineMeUpgradeMixin.java");
    private static final Path MIXIN_CONFIG = Path.of("src/main/resources/mekenergistics.mixins.json");
    private static final Path MIXIN_PLUGIN = Path.of(
            "src/main/java/com/beipuo/mekenergistics/mixin/MekEnergisticsMixinPlugin.java");
    private static final Path FACTORY_PROFILES = Path.of(
            "src/main/java/com/beipuo/mekenergistics/upgrade/MekanismFactoryUpgradeProfiles.java");
    private static final Path UPGRADE_REGISTRAR = Path.of(
            "src/main/java/com/beipuo/mekenergistics/upgrade/MeUpgradeSupportRegistrar.java");
    private static final Path BLOCK_ENTITIES = Path.of(
            "src/main/java/com/beipuo/mekenergistics/registry/ModBlockEntities.java");
    private static final Path BUILD_GRADLE = Path.of("build.gradle");
    private static final Path MODS_TOML = Path.of("src/main/templates/META-INF/neoforge.mods.toml");

    @Test
    void emptyPatternInputsCollapseToTheEmptyLayout() {
        assertTrue(MekanismMagicUpgradeProfiles.inputLayoutFor(List.of(), false).isEmpty());
        assertTrue(MekanismMagicUpgradeProfiles.inputLayoutFor(null, true).isEmpty());
        assertTrue(MekanismMagicUpgradeProfiles.outputPortsFor(List.of()).isEmpty());
        assertTrue(MekanismMagicUpgradeProfiles.outputPortsFor(null).isEmpty());
    }

    @Test
    void spiritFactoryDetectionMatchesMagicFactoryClassNames() {
        assertTrue(MekanismMagicUpgradeProfiles.isSpiritFactory(new NativeSpiritFactoryBlockEntity()));
        assertTrue(MekanismMagicUpgradeProfiles.isSpiritFactory(new ExtraSpiritFactoryBlockEntity()));
        assertFalse(MekanismMagicUpgradeProfiles.isSpiritFactory(new NativeRitualEngineBlockEntity()));
        assertFalse(MekanismMagicUpgradeProfiles.isSpiritFactory("ritual"));
    }

    @Test
    void multiSlotSpiritFactoriesUseOneGroupedInputPort() {
        // null slots become unavailable ports; the grouping decision is what this asserts.
        MeInputLayout layout = MekanismMagicUpgradeProfiles.inputLayoutFor(
                java.util.Arrays.asList(null, null, null), true);
        assertFalse(layout.isEmpty());
        assertEquals(1, layout.ports().size(),
                "parallel factory process slots must share one grouped input port");
        assertTrue(layout.lanes().isEmpty());
    }

    @Test
    void multiSlotRitualMachinesKeepDistinctInputPorts() {
        MeInputLayout layout = MekanismMagicUpgradeProfiles.inputLayoutFor(
                java.util.Arrays.asList(null, null, null), false);
        assertFalse(layout.isEmpty());
        assertEquals(3, layout.ports().size(),
                "role-specific ritual slots must stay as separate pattern ports");
    }

    @Test
    void magicAutomationIdentityHasNoMeBlockVariant() {
        assertFalse(CompatMachineCatalog.hasMeVariant(MeMekanismMachine.MEKANISM_MAGIC_AUTOMATION));
        assertEquals("magic_automation", MeMekanismMachine.MEKANISM_MAGIC_AUTOMATION.baseName());
    }

    @Test
    void adapterIsWiredThroughOptionalMixinFactoryFallbackAndDependencyMetadata() throws IOException {
        String access = Files.readString(ACCESS);
        String profiles = Files.readString(PROFILES);
        String mixin = Files.readString(MIXIN);
        String config = Files.readString(MIXIN_CONFIG);
        String plugin = Files.readString(MIXIN_PLUGIN);
        String factoryProfiles = Files.readString(FACTORY_PROFILES);
        String registrar = Files.readString(UPGRADE_REGISTRAR);
        String blockEntities = Files.readString(BLOCK_ENTITIES);
        String buildGradle = Files.readString(BUILD_GRADLE);
        String modsToml = Files.readString(MODS_TOML);

        assertTrue(access.contains("com.example.mekanismmagic.api.IMekanismMagicAutomation"));
        assertTrue(access.contains("mekanismMagicPatternInputs"));
        assertTrue(access.contains("mekanismMagicPatternOutputs"));
        assertTrue(access.contains("mekanismMagicSupportsPatternAutomation"));

        assertTrue(profiles.contains("MekanismMagicAutomationAccess.patternInputs"));
        assertTrue(profiles.contains("MekanismMagicAutomationAccess.patternOutputs"));
        assertTrue(profiles.contains("autoSortedFactoryItemInput"));

        assertTrue(mixin.contains("NativeMagicMachineBlockEntity"));
        assertTrue(mixin.contains("MekanismMagicUpgradeProfiles.forTile"));
        assertTrue(mixin.contains("addMePatternSlots"));
        assertTrue(mixin.contains("processMePatternIo"));

        assertTrue(config.contains("\"MekanismMagicMachineMeUpgradeMixin\""));
        assertTrue(plugin.contains(".MekanismMagicMachineMeUpgradeMixin"));
        assertTrue(plugin.contains("Gate.target(\"mekanism_magic\""));
        assertTrue(plugin.contains("NativeMagicMachineBlockEntity"));

        assertTrue(factoryProfiles.contains("MekanismMagicUpgradeProfiles.forTile"));
        assertTrue(registrar.contains("magicPatternUpgradeBlocks()"));
        assertTrue(registrar.contains("hasMekanismMagic()"));
        assertTrue(registrar.contains("mekanism_magic"));
        assertTrue(registrar.contains("dimension_miner"));

        // NeoForge AE2 only finds hosts through this capability; Magic blocks must be included.
        assertTrue(blockEntities.contains("MeUpgradeSupportRegistrar.magicPatternUpgradeBlocks()"));
        assertTrue(blockEntities.contains("AECapabilities.IN_WORLD_GRID_NODE_HOST"));
        assertTrue(blockEntities.contains("isMeUpgradeActive()"));

        assertTrue(buildGradle.contains("mekanism-magic-1660500:8697428"));
        assertTrue(modsToml.contains("modId=\"mekanism_magic\""));
        assertTrue(modsToml.contains("type=\"optional\""));
    }

    /** Class-name stand-ins so {@code isSpiritFactory} is exercised without loading Magic tiles. */
    private static final class NativeSpiritFactoryBlockEntity {
    }

    private static final class ExtraSpiritFactoryBlockEntity {
    }

    private static final class NativeRitualEngineBlockEntity {
    }
}
