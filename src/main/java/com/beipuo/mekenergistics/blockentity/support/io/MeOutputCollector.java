package com.beipuo.mekenergistics.blockentity.support.io;

import appeng.api.stacks.AEKey;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import mekanism.api.Action;

public final class MeOutputCollector {
    private MeOutputCollector() {
    }

    /** Atomically moves all non-empty outputs into the destination. */
    public static boolean collectAll(List<? extends MeOutputPort> outputs, MeInputPort destination) {
        if (outputs.isEmpty()) {
            return false;
        }
        for (MeOutputPort output : outputs) {
            AEKey key = output.key();
            long amount = output.amount();
            if (amount > 0 && (key == null || !destination.supports(key)
                    || destination.insert(key, amount, Action.SIMULATE) != amount
                    || output.extract(amount, Action.SIMULATE) != amount)) {
                return false;
            }
        }

        Map<MeOutputPort, Object> outputSnapshots = new IdentityHashMap<>();
        outputs.forEach(output -> outputSnapshots.put(output, output.snapshot()));
        Object destinationSnapshot = destination.snapshot();
        boolean changed = false;
        for (MeOutputPort output : outputs) {
            AEKey key = output.key();
            long amount = output.amount();
            if (key == null || amount <= 0) {
                continue;
            }
            if (destination.insert(key, amount, Action.EXECUTE) != amount
                    || output.extract(amount, Action.EXECUTE) != amount) {
                outputSnapshots.forEach(MeOutputPort::restore);
                destination.restore(destinationSnapshot);
                return false;
            }
            changed = true;
        }
        return changed;
    }
}
