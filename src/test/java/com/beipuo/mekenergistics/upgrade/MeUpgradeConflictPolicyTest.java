package com.beipuo.mekenergistics.upgrade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class MeUpgradeConflictPolicyTest {
    @Test
    void interfaceConflictsWithPatternProviderAndPassiveCrafting() {
        assertEquals(MeUpgradeType.PATTERN_PROVIDER, MeUpgradeConflictPolicy.conflictWith(
                MeUpgradeType.OUTPUT_INTERFACE, MeUpgradeData.EMPTY.with(MeUpgradeType.PATTERN_PROVIDER, 1))
                .orElseThrow());
        assertEquals(MeUpgradeType.PASSIVE_CRAFTING, MeUpgradeConflictPolicy.conflictWith(
                MeUpgradeType.OUTPUT_INTERFACE, MeUpgradeData.EMPTY.with(MeUpgradeType.PASSIVE_CRAFTING, 1))
                .orElseThrow());
    }

    @Test
    void patternProviderAndPassiveCraftingConflictWithInterface() {
        MeUpgradeData interfaceInstalled = MeUpgradeData.EMPTY.with(MeUpgradeType.OUTPUT_INTERFACE, 1);
        assertEquals(MeUpgradeType.OUTPUT_INTERFACE,
                MeUpgradeConflictPolicy.conflictWith(MeUpgradeType.PATTERN_PROVIDER, interfaceInstalled).orElseThrow());
        assertEquals(MeUpgradeType.OUTPUT_INTERFACE,
                MeUpgradeConflictPolicy.conflictWith(MeUpgradeType.PASSIVE_CRAFTING, interfaceInstalled).orElseThrow());
    }

    @Test
    void patternProviderAndPassiveCraftingMayCoexist() {
        assertTrue(MeUpgradeConflictPolicy.allows(
                MeUpgradeType.PASSIVE_CRAFTING, MeUpgradeData.EMPTY.with(MeUpgradeType.PATTERN_PROVIDER, 1)));
        assertTrue(MeUpgradeConflictPolicy.allows(
                MeUpgradeType.PATTERN_PROVIDER, MeUpgradeData.EMPTY.with(MeUpgradeType.PASSIVE_CRAFTING, 1)));
    }

    @Test
    void nativeInstallUsesLiveInstalledCountsNotLegacyData() {
        Set<MeUpgradeType> installed = EnumSet.of(MeUpgradeType.OUTPUT_INTERFACE);
        assertTrue(MeUpgradeConflictPolicy.conflictWith(MeUpgradeType.PATTERN_PROVIDER, installed::contains).isPresent());
        assertTrue(MeUpgradeConflictPolicy.conflictWith(MeUpgradeType.PASSIVE_CRAFTING, installed::contains).isPresent());
        assertTrue(MeUpgradeConflictPolicy.conflictWith(MeUpgradeType.OUTPUT_INTERFACE, installed::contains).isEmpty());
    }

    @Test
    void blocksNativeInstallReadsTheOwnersContainer() {
        TestOwner owner = new TestOwner();
        assertFalse(MeUpgradeConflictPolicy.blocksNativeInstall(MeUpgradeType.OUTPUT_INTERFACE, owner));

        assertTrue(owner.container.install(MeUpgradeType.OUTPUT_INTERFACE).successful());
        assertTrue(MeUpgradeConflictPolicy.blocksNativeInstall(MeUpgradeType.PATTERN_PROVIDER, owner));
        assertTrue(MeUpgradeConflictPolicy.blocksNativeInstall(MeUpgradeType.PASSIVE_CRAFTING, owner));
        assertFalse(MeUpgradeConflictPolicy.blocksNativeInstall(MeUpgradeType.OUTPUT_INTERFACE, owner));
        assertFalse(MeUpgradeConflictPolicy.blocksNativeInstall(null, owner));
        assertFalse(MeUpgradeConflictPolicy.blocksNativeInstall(MeUpgradeType.PATTERN_PROVIDER, null));
    }

    private static final class TestOwner implements MeUpgradeStateOwner {
        private final MeUpgradeContainer container = new MeUpgradeContainer(this, () -> {
        });

        @Override
        public MeUpgradeContainer getMeUpgradeContainer() {
            return this.container;
        }
    }
}
