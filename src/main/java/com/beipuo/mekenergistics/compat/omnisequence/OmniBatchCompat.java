package com.beipuo.mekenergistics.compat.omnisequence;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import com.atir.molecularmanipulator.api.crafting.OmniBatchAdmission;
import com.atir.molecularmanipulator.api.crafting.OmniBatchCraftingApi;
import com.atir.molecularmanipulator.api.crafting.OmniBatchDelivery;
import com.atir.molecularmanipulator.api.crafting.OmniBatchProbe;
import com.atir.molecularmanipulator.api.crafting.OmniBatchRequest;
import com.beipuo.mekenergistics.blockentity.support.AbstractMeAeSupport;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.Nullable;

/** Optional OmniSequence v1 provider bridge. Loaded only while its API is present. */
public final class OmniBatchCompat {
    private static final int SUPPORTED_API_VERSION = 1;

    private OmniBatchCompat() {
    }

    @Nullable
    public static OmniBatchAdmission prepare(AbstractMeAeSupport<?> support, OmniBatchProbe probe) {
        if (OmniBatchCraftingApi.apiVersion() != SUPPORTED_API_VERSION
                || support == null || probe == null || probe.pattern() == null
                || probe.requestedMaxCrafts() < 2 || support.isPatternBusy()
                || !support.hasRegisteredPattern(probe.pattern())) {
            return null;
        }

        KeyCounter[] prototype = toCounters(probe.pattern(), probe.oneCraftInputs());
        if (prototype == null || hasRemainingInputs(probe.pattern(), prototype)) {
            return null;
        }
        long capacity = Math.min(probe.requestedMaxCrafts(), support.maxAcceptedCopies(prototype));
        return capacity < 2 ? null : new Admission(support, probe.pattern(), capacity);
    }

    @Nullable
    static KeyCounter[] toCounters(IPatternDetails pattern, List<? extends OmniBatchProbe.Input> inputs) {
        if (pattern == null || inputs == null) {
            return null;
        }
        KeyCounter[] counters = emptyCounters(pattern.getInputs().length);
        for (OmniBatchProbe.Input input : inputs) {
            if (!add(counters, input.slot(), input.key(), input.amount())) {
                return null;
            }
        }
        return allPopulated(counters) ? counters : null;
    }

    @Nullable
    static KeyCounter[] toCounters(IPatternDetails pattern, OmniBatchRequest request) {
        if (pattern == null || request == null || request.inputs() == null) {
            return null;
        }
        KeyCounter[] counters = emptyCounters(pattern.getInputs().length);
        for (OmniBatchRequest.Input input : request.inputs()) {
            if (!add(counters, input.slot(), input.key(), input.amount())) {
                return null;
            }
        }
        return allPopulated(counters) ? counters : null;
    }

    private static KeyCounter[] emptyCounters(int size) {
        KeyCounter[] counters = new KeyCounter[size];
        for (int i = 0; i < size; i++) {
            counters[i] = new KeyCounter();
        }
        return counters;
    }

    private static boolean add(KeyCounter[] counters, int slot, AEKey key, long amount) {
        if (slot < 0 || slot >= counters.length || key == null || amount <= 0) {
            return false;
        }
        long existing = counters[slot].get(key);
        if (existing > Long.MAX_VALUE - amount) {
            return false;
        }
        counters[slot].add(key, amount);
        return true;
    }

    private static boolean allPopulated(KeyCounter[] counters) {
        if (counters.length == 0) {
            return false;
        }
        for (KeyCounter counter : counters) {
            if (counter.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static boolean hasRemainingInputs(IPatternDetails pattern, KeyCounter[] inputs) {
        IPatternDetails.IInput[] declared = pattern.getInputs();
        for (int slot = 0; slot < declared.length; slot++) {
            for (var entry : inputs[slot]) {
                if (declared[slot].getRemainingKey(entry.getKey()) != null) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean samePattern(IPatternDetails expected, IPatternDetails actual) {
        return expected == actual || expected != null && actual != null
                && Objects.equals(expected.getDefinition(), actual.getDefinition());
    }

    private static final class Admission implements OmniBatchAdmission {
        private final AbstractMeAeSupport<?> support;
        private final IPatternDetails pattern;
        private final long maxCrafts;
        private boolean committed;

        private Admission(AbstractMeAeSupport<?> support, IPatternDetails pattern, long maxCrafts) {
            this.support = support;
            this.pattern = pattern;
            this.maxCrafts = maxCrafts;
        }

        @Override
        public long maxCrafts() {
            return this.maxCrafts;
        }

        @Override
        public void commit(OmniBatchDelivery delivery) {
            if (this.committed) {
                delivery.reject(new OmniBatchDelivery.Rejection(
                        OmniBatchDelivery.RejectReason.INTERNAL_ERROR));
                return;
            }
            this.committed = true;
            OmniBatchRequest request = delivery.request();
            if (request == null || request.craftCount() < 2 || request.craftCount() > this.maxCrafts
                    || !samePattern(this.pattern, request.pattern())) {
                delivery.reject(new OmniBatchDelivery.Rejection(
                        OmniBatchDelivery.RejectReason.PATTERN_UNAVAILABLE));
                return;
            }
            KeyCounter[] delivered = toCounters(this.pattern, request);
            if (delivered == null || hasRemainingInputs(this.pattern, delivered)) {
                delivery.reject(new OmniBatchDelivery.Rejection(
                        OmniBatchDelivery.RejectReason.UNSUPPORTED_INPUT));
                return;
            }
            if (!this.support.routeDataPatternInputs(delivered)) {
                delivery.reject(new OmniBatchDelivery.Rejection(
                        OmniBatchDelivery.RejectReason.CAPACITY_CHANGED));
                return;
            }
            delivery.accept(new OmniBatchDelivery.Receipt(
                    OmniBatchDelivery.Ownership.TRANSFERRED_TO_DURABLE_TARGET,
                    OmniBatchDelivery.Backpressure.RECHECK_NEXT_TICK));
        }
    }
}
