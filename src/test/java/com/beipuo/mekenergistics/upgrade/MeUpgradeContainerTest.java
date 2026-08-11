package com.beipuo.mekenergistics.upgrade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class MeUpgradeContainerTest {
    private static final class TestOwner implements MeUpgradeStateOwner {
        private final MeUpgradeContainer container;
        private final AtomicInteger changes = new AtomicInteger();
        private boolean nativePatternProvider;

        TestOwner() {
            this.container = new MeUpgradeContainer(this, this.changes::incrementAndGet);
        }

        @Override
        public MeUpgradeContainer getMeUpgradeContainer() {
            return this.container;
        }

        @Override
        public boolean supportsNativePatternProvider() {
            return this.nativePatternProvider;
        }
    }

    @Test
    void installsPatternProviderAndPassiveCraftingTogether() {
        TestOwner owner = new TestOwner();
        assertTrue(owner.container.install(MeUpgradeType.PATTERN_PROVIDER).successful());
        assertTrue(owner.container.install(MeUpgradeType.PASSIVE_CRAFTING).successful());
        assertTrue(owner.container.isInstalled(MeUpgradeType.PATTERN_PROVIDER));
        assertTrue(owner.container.isInstalled(MeUpgradeType.PASSIVE_CRAFTING));
    }

    @Test
    void passiveCraftingRequiresPatternProviderOnOrdinaryMachines() {
        TestOwner owner = new TestOwner();
        MeUpgradeConflictPolicy.Result result = owner.container.install(MeUpgradeType.PASSIVE_CRAFTING);
        assertFalse(result.successful());
        assertEquals(MeUpgradeConflictPolicy.Reason.MISSING_PREREQUISITE, result.reason());
        assertFalse(owner.container.isInstalled(MeUpgradeType.PASSIVE_CRAFTING));
    }

    @Test
    void nativePatternMachinesInstallPassiveCraftingDirectly() {
        TestOwner owner = new TestOwner();
        owner.nativePatternProvider = true;
        assertTrue(owner.container.install(MeUpgradeType.PASSIVE_CRAFTING).successful());
    }

    @Test
    void interfaceRejectsPatternProviderAndPassiveCrafting() {
        TestOwner owner = new TestOwner();
        assertTrue(owner.container.install(MeUpgradeType.PATTERN_PROVIDER).successful());
        MeUpgradeConflictPolicy.Result conflict = owner.container.install(MeUpgradeType.OUTPUT_INTERFACE);
        assertFalse(conflict.successful());
        assertEquals(MeUpgradeConflictPolicy.Reason.CONFLICT, conflict.reason());
        assertEquals(MeUpgradeType.PATTERN_PROVIDER, conflict.conflicting());

        TestOwner owner2 = new TestOwner();
        assertTrue(owner2.container.install(MeUpgradeType.PATTERN_PROVIDER).successful());
        assertTrue(owner2.container.install(MeUpgradeType.PASSIVE_CRAFTING).successful());
        assertFalse(owner2.container.install(MeUpgradeType.OUTPUT_INTERFACE).successful());
    }

    @Test
    void patternProviderAndPassiveCraftingRejectInterface() {
        TestOwner owner = new TestOwner();
        assertTrue(owner.container.install(MeUpgradeType.OUTPUT_INTERFACE).successful());
        assertFalse(owner.container.install(MeUpgradeType.PATTERN_PROVIDER).successful());
        assertFalse(owner.container.install(MeUpgradeType.PASSIVE_CRAFTING).successful());
        assertTrue(owner.container.isInstalled(MeUpgradeType.OUTPUT_INTERFACE));
    }

    @Test
    void conflictsDoNotChangeStateOrConsumeAnything() {
        TestOwner owner = new TestOwner();
        owner.container.install(MeUpgradeType.PATTERN_PROVIDER);
        int changesBefore = owner.changes.get();
        MeUpgradeConflictPolicy.Result result = owner.container.install(MeUpgradeType.OUTPUT_INTERFACE);
        assertFalse(result.successful());
        assertEquals(changesBefore, owner.changes.get());
        assertTrue(owner.container.isInstalled(MeUpgradeType.PATTERN_PROVIDER));
        assertFalse(owner.container.isInstalled(MeUpgradeType.OUTPUT_INTERFACE));
    }

    @Test
    void limitIsEnforced() {
        TestOwner owner = new TestOwner();
        assertTrue(owner.container.install(MeUpgradeType.PATTERN_PROVIDER).successful());
        MeUpgradeConflictPolicy.Result result = owner.container.install(MeUpgradeType.PATTERN_PROVIDER, 1);
        assertFalse(result.successful());
        assertEquals(MeUpgradeConflictPolicy.Reason.LIMIT_REACHED, result.reason());
    }

    @Test
    void patternProviderUninstallRequiresEmptyPatternsAndNoPassiveCrafting() {
        TestOwner owner = new TestOwner();
        owner.container.install(MeUpgradeType.PATTERN_PROVIDER);
        owner.container.install(MeUpgradeType.PASSIVE_CRAFTING);
        assertFalse(owner.container.uninstall(MeUpgradeType.PATTERN_PROVIDER));

        owner.container.uninstall(MeUpgradeType.PASSIVE_CRAFTING);
        assertTrue(owner.container.uninstall(MeUpgradeType.PATTERN_PROVIDER));
        assertFalse(owner.container.isInstalled(MeUpgradeType.PATTERN_PROVIDER));
    }

    @Test
    void modeDerivesFromInstalledUpgrades() {
        TestOwner owner = new TestOwner();
        assertEquals(MeMachineMode.NONE, owner.container.mode());
        assertTrue(owner.container.install(MeUpgradeType.PATTERN_PROVIDER).successful());
        assertEquals(MeMachineMode.PATTERN_PROVIDER, owner.container.mode());
        assertTrue(owner.container.install(MeUpgradeType.PASSIVE_CRAFTING).successful());
        assertEquals(MeMachineMode.PATTERN_PROVIDER, owner.container.mode());
    }

    @Test
    void interfaceModeOverridesPatternProvider() {
        TestOwner owner = new TestOwner();
        assertTrue(owner.container.install(MeUpgradeType.PATTERN_PROVIDER).successful());
        assertTrue(owner.container.install(MeUpgradeType.PASSIVE_CRAFTING).successful());
        MeUpgradeConflictPolicy.Result blocked = owner.container.install(MeUpgradeType.OUTPUT_INTERFACE);
        assertFalse(blocked.successful());
        assertEquals(MeMachineMode.PATTERN_PROVIDER, owner.container.mode());

        TestOwner interfaceOwner = new TestOwner();
        assertTrue(interfaceOwner.container.install(MeUpgradeType.OUTPUT_INTERFACE).successful());
        assertEquals(MeMachineMode.OUTPUT_INTERFACE, interfaceOwner.container.mode());
        interfaceOwner.container.uninstall(MeUpgradeType.OUTPUT_INTERFACE);
        assertEquals(MeMachineMode.NONE, interfaceOwner.container.mode());
    }

    @Test
    void setDataNotifiesOwnerAndChangeListener() {
        TestOwner owner = new TestOwner();
        owner.container.setData(MeUpgradeData.EMPTY.with(MeUpgradeType.OUTPUT_INTERFACE, 1));
        assertTrue(owner.container.isInstalled(MeUpgradeType.OUTPUT_INTERFACE));
        assertTrue(owner.changes.get() > 0);
    }
}



