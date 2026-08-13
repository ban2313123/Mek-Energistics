package com.beipuo.mekenergistics.upgrade;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Static contracts for the native Mekanism upgrade install and removal flow. */
class MeUpgradeUninstallContractTest {
    private static final Path MOD_NETWORK = Path.of(
            "src/main/java/com/beipuo/mekenergistics/network/ModNetwork.java");
    private static final Path CLIENT_ENTRY = Path.of(
            "src/main/java/com/beipuo/mekenergistics/client/MekEnergisticsClient.java");
    private static final Path CONTAINER = Path.of(
            "src/main/java/com/beipuo/mekenergistics/upgrade/MeUpgradeContainer.java");
    private static final Path NATIVE_UPGRADE_MIXIN = Path.of(
            "src/main/java/com/beipuo/mekenergistics/mixin/TileComponentUpgradeMixin.java");
    private static final Path OWNER_SUPPORT = Path.of(
            "src/main/java/com/beipuo/mekenergistics/upgrade/MeUpgradeStateOwnerSupport.java");
    private static final Path RECIPE_RUNTIME = Path.of(
            "src/main/java/com/beipuo/mekenergistics/upgrade/MeUpgradeRecipeMachineRuntime.java");
    private static final List<Path> LEGACY_UPGRADE_FLOW = List.of(
            Path.of("src/main/java/com/beipuo/mekenergistics/client/overlay/MeUpgradeWindowOverlay.java"),
            Path.of("src/main/java/com/beipuo/mekenergistics/item/MeUpgradeInteractionHandler.java"),
            Path.of("src/main/java/com/beipuo/mekenergistics/network/packet/RequestUpgradeStatePacket.java"),
            Path.of("src/main/java/com/beipuo/mekenergistics/network/packet/UpgradeStateSyncPacket.java"),
            Path.of("src/main/java/com/beipuo/mekenergistics/network/packet/UninstallMeUpgradePacket.java"));
    private static final List<String> LANG_FILES = List.of(
            "src/main/resources/assets/mekenergistics/lang/en_us.json",
            "src/main/resources/assets/mekenergistics/lang/zh_cn.json",
            "src/main/resources/assets/mekenergistics/lang/ru_ru.json");
    @Test
    void legacyUninstallPacketsAreNotRegisteredOnTheServerSide() throws IOException {
        String source = Files.readString(MOD_NETWORK);
        assertFalse(source.contains("UninstallMeUpgradePacket"));
        assertFalse(source.contains("RequestUpgradeStatePacket"));
    }

    @Test
    void legacyUpgradeOverlayIsNotRegisteredOnTheClientSide() throws IOException {
        String source = Files.readString(CLIENT_ENTRY);
        assertFalse(source.contains("UpgradeStateSyncPacket"));
        assertFalse(source.contains("MeUpgradeWindowOverlay"));
    }

    @Test
    void legacyCustomUpgradeFlowIsRemoved() {
        for (Path source : LEGACY_UPGRADE_FLOW) {
            assertFalse(Files.exists(source), () -> source + " must not remain reachable beside Mekanism's native upgrade flow");
        }
    }

    @Test
    void everyLanguageFileContainsNativeUpgradeNamesAndDescriptions() throws IOException {
        for (String file : LANG_FILES) {
            String source = Files.readString(Path.of(file));
            assertTrue(source.contains("upgrade.mekenergistics.me_pattern_provider"));
            assertTrue(source.contains("upgrade.mekenergistics.me_passive_crafting"));
            assertTrue(source.contains("upgrade.mekenergistics.me_output_interface"));
        }
    }

    @Test
    void containerReadsAndRemovesThroughNativeComponent() throws IOException {
        String source = Files.readString(CONTAINER);
        assertTrue(source.contains("component.getUpgrades"));
        assertTrue(source.contains("component.removeUpgrade"));
        assertTrue(source.contains("migrateToNativeComponent"));
    }

    @Test
    void stockedInterfaceItemsBlockBothContainerAndNativeRemovalPaths() throws IOException {
        String container = Files.readString(CONTAINER);
        String mixin = Files.readString(NATIVE_UPGRADE_MIXIN);

        assertTrue(container.contains("!this.owner.isInterfaceInventoryEmpty()"));
        assertTrue(mixin.contains("!owner.isInterfaceInventoryEmpty()"));
        assertTrue(mixin.contains("cancellable = true"));
        assertTrue(mixin.contains("ci.cancel()"));
        assertTrue(mixin.contains("MeUpgradeConflictPolicy.blocksNativeInstall"));
        assertTrue(mixin.contains("cir.setReturnValue(0)"));
    }

    @Test
    void recoveryBufferBlocksInterfaceRemoval() throws IOException {
        String support = Files.readString(Path.of(
                "src/main/java/com/beipuo/mekenergistics/blockentity/support/AbstractMeAeSupport.java"));
        String owner = Files.readString(Path.of(
                "src/main/java/com/beipuo/mekenergistics/upgrade/MeUpgradeStateOwner.java"));
        assertTrue(support.contains("return this.interfaceInventory.isEmpty() && !hasInterfaceRecovery();"));
        assertTrue(owner.contains("machine.getRecipeAeSupport().canRemoveInterfaceUpgrade()"));
    }

    @Test
    void proxyOwnersDelegateTheInterfaceInventoryGuardToTheirSupport() throws IOException {
        String ownerSupport = Files.readString(OWNER_SUPPORT);
        String runtime = Files.readString(RECIPE_RUNTIME);

        assertTrue(ownerSupport.contains("private final Supplier<Boolean> interfaceInventoryEmpty;"));
        assertTrue(ownerSupport.contains("return Boolean.TRUE.equals(this.interfaceInventoryEmpty.get());"));
        assertTrue(runtime.contains("return MeUpgradeRecipeMachineRuntime.this.support.canRemoveInterfaceUpgrade();"));
    }
}
