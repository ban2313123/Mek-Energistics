package com.beipuo.mekenergistics.upgrade;

import java.util.Optional;
import java.util.function.Predicate;
import org.jetbrains.annotations.Nullable;

/**
 * Bidirectional mutual-exclusion rules for ME upgrades. Install, uninstall and migration never
 * delete an existing upgrade; conflicting installs are rejected before any state changes.
 */
public final class MeUpgradeConflictPolicy {
    private MeUpgradeConflictPolicy() {
    }

    /**
     * Returns the already-installed upgrade that blocks installing {@code toInstall}, or empty.
     *
     * <ul>
     *   <li>ME interface rejects ME pattern provider and ME passive crafting.</li>
     *   <li>ME pattern provider rejects ME interface.</li>
 *   <li>ME passive crafting rejects ME interface.</li>
     *   <li>ME pattern provider + ME passive crafting remain allowed.</li>
     * </ul>
     */
    public static Optional<MeUpgradeType> conflictWith(MeUpgradeType toInstall, MeUpgradeData current) {
        return conflictWith(toInstall, current == null ? type -> false : current::isInstalled);
    }

    public static Optional<MeUpgradeType> conflictWith(MeUpgradeType toInstall,
            Predicate<MeUpgradeType> installed) {
        if (toInstall == null || installed == null) {
            return Optional.empty();
        }
        if (toInstall == MeUpgradeType.OUTPUT_INTERFACE) {
            if (installed.test(MeUpgradeType.PATTERN_PROVIDER)) {
                return Optional.of(MeUpgradeType.PATTERN_PROVIDER);
            }
            if (installed.test(MeUpgradeType.PASSIVE_CRAFTING)) {
                return Optional.of(MeUpgradeType.PASSIVE_CRAFTING);
            }
            return Optional.empty();
        }
        if (installed.test(MeUpgradeType.OUTPUT_INTERFACE)) {
            return Optional.of(MeUpgradeType.OUTPUT_INTERFACE);
        }
        return Optional.empty();
    }

    /**
     * True when Mekanism's native {@code addUpgrades} must return 0 so the item is not consumed.
     */
    public static boolean blocksNativeInstall(MeUpgradeType toInstall, MeUpgradeStateOwner owner) {
        if (toInstall == null || owner == null || owner.getMeUpgradeContainer() == null) {
            return false;
        }
        return conflictWith(toInstall, owner.getMeUpgradeContainer()::isInstalled).isPresent();
    }

    /** True when {@code toInstall} may coexist with every installed upgrade. */
    public static boolean allows(MeUpgradeType toInstall, MeUpgradeData current) {
        return conflictWith(toInstall, current).isEmpty();
    }

    /** Result of an install attempt; failures never modify state or consume items. */
    public record Result(boolean successful, @Nullable MeUpgradeType conflicting, Reason reason) {
        public static Result ok() {
            return new Result(true, null, Reason.NONE);
        }

        public static Result conflict(MeUpgradeType conflicting) {
            return new Result(false, conflicting, Reason.CONFLICT);
        }

        public static Result unsupported() {
            return new Result(false, null, Reason.UNSUPPORTED);
        }

        public static Result missingPrerequisite() {
            return new Result(false, null, Reason.MISSING_PREREQUISITE);
        }

        public static Result limitReached() {
            return new Result(false, null, Reason.LIMIT_REACHED);
        }

        public static Result blocked() {
            return new Result(false, null, Reason.BLOCKED);
        }
    }

    public enum Reason {
        NONE,
        CONFLICT,
        UNSUPPORTED,
        MISSING_PREREQUISITE,
        LIMIT_REACHED,
        BLOCKED
    }
}


