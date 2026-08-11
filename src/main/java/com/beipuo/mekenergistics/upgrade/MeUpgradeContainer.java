package com.beipuo.mekenergistics.upgrade;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * Single source of truth for one machine's ME upgrade state. Handles queries, install, uninstall,
 * count limits, prerequisites, conflicts and change notifications.
 *
 * <p>All mutations go through this container; the owning machine reacts through
 * {@link MeUpgradeStateOwner#onMeUpgradeStateChanged()}.</p>
 */
public final class MeUpgradeContainer {
    private MeUpgradeData data;
    private final MeUpgradeStateOwner owner;
    private final Runnable changeNotifier;

    public MeUpgradeContainer(@NotNull MeUpgradeStateOwner owner, @NotNull Runnable changeNotifier) {
        this.owner = Objects.requireNonNull(owner, "owner");
        this.changeNotifier = Objects.requireNonNull(changeNotifier, "changeNotifier");
        this.data = MeUpgradeData.EMPTY;
    }

    public MeUpgradeData data() {
        return this.data;
    }

    public int count(MeUpgradeType type) {
        return this.data.count(type);
    }

    public boolean isInstalled(MeUpgradeType type) {
        return this.data.isInstalled(type);
    }

    public boolean isEmpty() {
        return this.data.isEmpty();
    }

    /** Installs one unit of {@code type} after validating support, prerequisites, limits and
     * conflicts. Never partially modifies state. */
    public MeUpgradeConflictPolicy.Result install(MeUpgradeType type) {
        return install(type, 1);
    }

    public MeUpgradeConflictPolicy.Result install(MeUpgradeType type, int amount) {
        if (type == null || amount <= 0) {
            return MeUpgradeConflictPolicy.Result.blocked();
        }
        MeUpgradeConflictPolicy.Result precheck = canInstall(type, amount);
        if (!precheck.successful()) {
            return precheck;
        }
        this.data = this.data.with(type, this.data.count(type) + amount);
        notifyChanged();
        return MeUpgradeConflictPolicy.Result.ok();
    }

    /** Validates an install without modifying state. */
    public MeUpgradeConflictPolicy.Result canInstall(MeUpgradeType type, int amount) {
        if (type == null || amount <= 0) {
            return MeUpgradeConflictPolicy.Result.blocked();
        }
        if (!this.owner.supportsUpgrade(type)) {
            return MeUpgradeConflictPolicy.Result.unsupported();
        }
        int target = this.data.count(type) + amount;
        if (target > type.getMaxCount()) {
            return MeUpgradeConflictPolicy.Result.limitReached();
        }
        MeUpgradeType conflict = MeUpgradeConflictPolicy.conflictWith(type, this.data).orElse(null);
        if (conflict != null) {
            return MeUpgradeConflictPolicy.Result.conflict(conflict);
        }
        if (type == MeUpgradeType.PASSIVE_CRAFTING && !this.owner.supportsNativePatternProvider()
                && !this.data.isInstalled(MeUpgradeType.PATTERN_PROVIDER)) {
            return MeUpgradeConflictPolicy.Result.missingPrerequisite();
        }
        return MeUpgradeConflictPolicy.Result.ok();
    }

    /** Removes one unit of {@code type} after machine-specific uninstall guards. */
    public boolean uninstall(MeUpgradeType type) {
        return uninstall(type, 1);
    }

    public boolean uninstall(MeUpgradeType type, int amount) {
        if (type == null || amount <= 0 || !this.data.isInstalled(type)) {
            return false;
        }
        if (type == MeUpgradeType.PATTERN_PROVIDER) {
            if (!this.owner.isPatternInventoryEmpty()) {
                return false;
            }
            if (this.data.isInstalled(MeUpgradeType.PASSIVE_CRAFTING)) {
                return false;
            }
        }
        this.data = this.data.with(type, this.data.count(type) - amount);
        notifyChanged();
        return true;
    }

    /** Replaces state wholesale (load, migration, reset). Normalizes via {@link MeUpgradeData}. */
    public void setData(MeUpgradeData newData) {
        MeUpgradeData next = newData == null ? MeUpgradeData.EMPTY : newData;
        if (next.equals(this.data)) {
            return;
        }
        this.data = next;
        notifyChanged();
    }

    public void markChanged() {
        notifyChanged();
    }

    public void refreshOnWorldLoad() {
        this.owner.onMeUpgradeStateChanged();
    }

    private void notifyChanged() {
        this.changeNotifier.run();
        this.owner.onMeUpgradeStateChanged();
    }
}




