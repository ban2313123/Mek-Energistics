package com.beipuo.mekenergistics.upgrade;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Static source-scan contracts for the self-owned ME upgrade uninstall flow (plan 3.1). */
class MeUpgradeUninstallContractTest {
    private static final Path MOD_NETWORK = Path.of(
            "src/main/java/com/beipuo/mekenergistics/network/ModNetwork.java");
    private static final Path CLIENT_ENTRY = Path.of(
            "src/main/java/com/beipuo/mekenergistics/client/MekEnergisticsClient.java");
    private static final Path CONTAINER = Path.of(
            "src/main/java/com/beipuo/mekenergistics/upgrade/MeUpgradeContainer.java");
    private static final List<String> LANG_FILES = List.of(
            "src/main/resources/assets/mekenergistics/lang/en_us.json",
            "src/main/resources/assets/mekenergistics/lang/zh_cn.json",
            "src/main/resources/assets/mekenergistics/lang/ru_ru.json");
    private static final List<String> LANG_KEYS = List.of(
            "gui.mekenergistics.me_upgrades.title",
            "gui.mekenergistics.me_upgrades.button",
            "gui.mekenergistics.me_upgrades.uninstall",
            "gui.mekenergistics.me_upgrades.empty",
            "message.mekenergistics.upgrade_uninstall_failed.not_installed",
            "message.mekenergistics.upgrade_uninstall_failed.guards");

    @Test
    void uninstallPacketsAreRegisteredOnTheServerSide() throws IOException {
        String source = Files.readString(MOD_NETWORK);
        assertTrue(source.contains("UninstallMeUpgradePacket.TYPE"),
                "ModNetwork does not register the uninstall packet");
        assertTrue(source.contains("RequestUpgradeStatePacket.TYPE"),
                "ModNetwork does not register the upgrade-state request packet");
        assertTrue(source.contains("UninstallMeUpgradePacket::handle"),
                "ModNetwork does not wire the uninstall handler");
    }

    @Test
    void upgradeStateSyncIsRegisteredOnTheClientSide() throws IOException {
        String source = Files.readString(CLIENT_ENTRY);
        assertTrue(source.contains("UpgradeStateSyncPacket.TYPE"),
                "client entry does not register the upgrade-state sync packet");
        assertTrue(source.contains("MeUpgradeWindowOverlay::handleStateSync"),
                "client entry does not wire the upgrade-state sync handler");
    }

    @Test
    void everyLanguageFileContainsTheUninstallKeys() throws IOException {
        for (String file : LANG_FILES) {
            String source = Files.readString(Path.of(file));
            for (String key : LANG_KEYS) {
                assertTrue(source.contains(key), file + " is missing " + key);
            }
        }
    }

    @Test
    void containerKeepsThePatternInventoryAndPassiveFirstUninstallGuards() throws IOException {
        String source = Files.readString(CONTAINER);
        assertTrue(source.contains("isPatternInventoryEmpty()"),
                "uninstall no longer guards the pattern inventory");
        assertTrue(source.contains("PASSIVE_CRAFTING"),
                "uninstall no longer requires passive crafting to be removed first");
    }
}
