package com.beipuo.mekenergistics.compat.thunderbolt;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.KeyCounter;
import com.beipuo.mekenergistics.blockentity.support.AbstractMeAeSupport;
import com.moakiee.thunderbolt.ae2.api.crafting.BatchDispatchMode;
import org.jetbrains.annotations.Nullable;

/** Bridges Thunderbolt's counted batch dispatch to ME machine input transactions. */
public final class ThunderboltBatchCompat {
    private ThunderboltBatchCompat() {
    }

    public static long getBatchCapacity(AbstractMeAeSupport<?> support,
            boolean smartMultiplicationEnabled, IPatternDetails details) {
        return getBatchCapacity(target(support), smartMultiplicationEnabled, details);
    }

    public static long pushBatch(AbstractMeAeSupport<?> support,
            boolean smartMultiplicationEnabled, IPatternDetails details,
            KeyCounter[] oneCopyTemplate, long maxCraft) {
        return pushBatch(target(support), smartMultiplicationEnabled,
                details, oneCopyTemplate, maxCraft);
    }

    public static BatchDispatchMode getBatchDispatchMode(boolean smartMultiplicationEnabled) {
        return smartMultiplicationEnabled ? BatchDispatchMode.UNBOUNDED : BatchDispatchMode.NORMAL;
    }

    static long getBatchCapacity(@Nullable BatchTarget target,
            boolean smartMultiplicationEnabled, IPatternDetails details) {
        if (target == null || details == null || target.isBusy()
                || !target.hasRegisteredPattern(details)) {
            return 0L;
        }
        // Thunderbolt reserves capacity=1 for its optimized ordinary dispatch path.
        return smartMultiplicationEnabled ? Long.MAX_VALUE : 1L;
    }

    static long pushBatch(@Nullable BatchTarget target,
            boolean smartMultiplicationEnabled, IPatternDetails details,
            KeyCounter[] oneCopyTemplate, long maxCraft) {
        if (target == null || details == null || maxCraft <= 0
                || !validPrototype(oneCopyTemplate) || target.isBusy()
                || !target.hasRegisteredPattern(details)) {
            return Math.max(0L, maxCraft);
        }

        long requested = smartMultiplicationEnabled ? maxCraft : 1L;
        long accepted = Math.min(requested,
                Math.max(0L, target.maxAcceptedCopies(oneCopyTemplate)));
        if (accepted <= 0) {
            return maxCraft;
        }

        KeyCounter[] scaledInputs = scale(oneCopyTemplate, accepted);
        if (scaledInputs == null || !target.routeInputs(scaledInputs)) {
            return maxCraft;
        }
        return maxCraft - accepted;
    }

    @Nullable
    static KeyCounter[] scale(KeyCounter[] prototype, long copies) {
        if (!validPrototype(prototype) || copies <= 0) {
            return null;
        }
        KeyCounter[] scaled = new KeyCounter[prototype.length];
        try {
            for (int index = 0; index < prototype.length; index++) {
                KeyCounter counter = new KeyCounter();
                for (var entry : prototype[index]) {
                    counter.add(entry.getKey(), Math.multiplyExact(entry.getLongValue(), copies));
                }
                scaled[index] = counter;
            }
        } catch (ArithmeticException exception) {
            return null;
        }
        return scaled;
    }

    private static boolean validPrototype(@Nullable KeyCounter[] prototype) {
        if (prototype == null || prototype.length == 0) {
            return false;
        }
        for (KeyCounter counter : prototype) {
            if (counter == null || counter.size() != 1) {
                return false;
            }
            var entry = counter.getFirstEntry();
            if (entry == null || entry.getKey() == null || entry.getLongValue() <= 0) {
                return false;
            }
        }
        return true;
    }

    private static BatchTarget target(AbstractMeAeSupport<?> support) {
        return new BatchTarget() {
            @Override
            public boolean isBusy() {
                return support.isPatternBusy();
            }

            @Override
            public boolean hasRegisteredPattern(IPatternDetails details) {
                return support.hasRegisteredPattern(details);
            }

            @Override
            public long maxAcceptedCopies(KeyCounter[] oneCopyTemplate) {
                return support.maxAcceptedCopies(oneCopyTemplate);
            }

            @Override
            public boolean routeInputs(KeyCounter[] scaledInputs) {
                return support.routeDataPatternInputs(scaledInputs);
            }
        };
    }

    interface BatchTarget {
        boolean isBusy();

        boolean hasRegisteredPattern(IPatternDetails details);

        long maxAcceptedCopies(KeyCounter[] oneCopyTemplate);

        boolean routeInputs(KeyCounter[] scaledInputs);
    }
}
