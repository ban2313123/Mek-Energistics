package com.beipuo.mekenergistics.upgrade;

/**
 * A machine that holds ME upgrade state through a single {@link MeUpgradeContainer}. Install,
 * uninstall, migration and runtime activation all read and write this container; no other
 * subsystem may keep its own copy of ME upgrade state.
 */
public interface MeUpgradeStateOwner {
    MeUpgradeContainer getMeUpgradeContainer();

    /** Whether this machine supports installing the given ME upgrade. Defaults to true for all
     * machines that expose a container. */
    default boolean supportsUpgrade(MeUpgradeType type) {
        return true;
    }

    /** True when the machine natively provides ME pattern-provider behavior without the pattern
     * provider upgrade (native ME machines and factories). */
    default boolean supportsNativePatternProvider() {
        return false;
    }

    /** True when the machine has no pattern inventory contents that would be orphaned by
     * uninstalling the pattern provider upgrade. */
    default boolean isPatternInventoryEmpty() {
        return true;
    }

    /** Called after any container mutation that changed the effective machine mode. */
    default void onMeUpgradeStateChanged() {
    }
}

