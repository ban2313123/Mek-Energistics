package com.beipuo.mekenergistics.upgrade;

import com.beipuo.mekenergistics.blockentity.api.AeOutputMode;
import com.beipuo.mekenergistics.blockentity.support.MePatternSlotInventoryHolder;
import java.util.Map;
import java.util.WeakHashMap;
import mekanism.api.IContentsListener;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.tile.base.TileEntityMekanism;
import org.jetbrains.annotations.Nullable;

/**
 * Per-tile ME-upgrade runtime for SPI hosts. Kept off mixin unique fields so
 * {@link TileEntityMekanism} itself is not an ME machine.
 */
public final class MePatternAutomationRuntimes {
    private static final Map<TileEntityMekanism, MeUpgradeRecipeMachineRuntime> RUNTIMES =
            new WeakHashMap<>();

    private MePatternAutomationRuntimes() {
    }

    public static MeUpgradeRecipeMachineRuntime getOrCreate(TileEntityMekanism tile) {
        synchronized (RUNTIMES) {
            return RUNTIMES.computeIfAbsent(tile,
                    key -> new MeUpgradeRecipeMachineRuntime(key, AeOutputMode.BOTH));
        }
    }

    public static @Nullable MeUpgradeRecipeMachineRuntime getExisting(TileEntityMekanism tile) {
        synchronized (RUNTIMES) {
            return RUNTIMES.get(tile);
        }
    }

    /**
     * Wraps the holder returned by a virtual {@code getInitialInventory} call. Must run at the
     * constructor call site so subclass overrides still receive pattern slots.
     */
    public static IInventorySlotHolder wrapInventory(Object tile, IInventorySlotHolder original,
            IContentsListener listener) {
        if (original == null || original instanceof MePatternSlotInventoryHolder
                || !MePatternAutomationProfiles.isUpgradeTarget(tile)) {
            return original;
        }
        if (tile instanceof MeUpgradeRecipeMachineAdapter adapter) {
            return adapter.addMePatternSlots(original, listener);
        }
        return original;
    }

    public static boolean processTick(Object tile, boolean changed) {
        if (!MePatternAutomationProfiles.isUpgradeTarget(tile)) {
            return changed;
        }
        if (tile instanceof MeUpgradeRecipeMachineAdapter adapter) {
            return adapter.processMePatternIo(changed);
        }
        return changed;
    }
}
