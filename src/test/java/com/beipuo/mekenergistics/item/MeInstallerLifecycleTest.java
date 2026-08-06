package com.beipuo.mekenergistics.item;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class MeInstallerLifecycleTest {
    private static final Path STANDARD_INSTALLER =
            Path.of("src/main/java/com/beipuo/mekenergistics/item/MeTierInstallerItem.java");
    private static final Path COMPAT_INSTALLER =
            Path.of("src/main/java/com/beipuo/mekenergistics/item/MeInstallerUpgradeHandler.java");
    private static final Path AE_SUPPORT =
            Path.of("src/main/java/com/beipuo/mekenergistics/blockentity/support/AbstractMeAeSupport.java");

    @Test
    void everyInWorldInstallerRefreshesTheAeLifecycleAfterMutation() throws IOException {
        for (Path installer : new Path[] {STANDARD_INSTALLER, COMPAT_INSTALLER}) {
            String source = Files.readString(installer);
            assertTrue(source.contains("refreshMeLifecycle(upgradedTile)"),
                    () -> installer.getFileName() + " must refresh the newly-created ME block entity");
            assertTrue(source.contains("refreshAfterWorldMutation()"),
                    () -> installer.getFileName() + " must schedule the post-node capability refresh");
        }
    }

    @Test
    void worldMutationRefreshRunsAfterTheManagedNodeIsReady() throws IOException {
        String source = Files.readString(AE_SUPPORT);
        int refresh = source.indexOf("public final void refreshAfterWorldMutation()");
        int firstTick = source.indexOf("GridHelper.onFirstTick", refresh);
        int create = source.indexOf("create();", firstTick);
        int invalidate = source.indexOf("level.invalidateCapabilities(pos);", create);
        int patterns = source.indexOf("rebuildPatternCache(false);", create);
        assertTrue(refresh >= 0 && firstTick > refresh && create > firstTick,
                "world mutation refresh must wait for AE2's first-tick lifecycle");
        assertTrue(patterns > create && invalidate > patterns,
                "node creation must precede pattern publication and capability invalidation");
    }
}
