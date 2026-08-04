package com.atir.molecularmanipulator.api.crafting;

public interface OmniBatchDelivery {
    OmniBatchRequest request();

    void accept(Receipt receipt);

    void reject(Rejection rejection);

    record Receipt(Ownership ownership, Backpressure backpressure) {
    }

    record Rejection(RejectReason reason) {
        public static Rejection reject(RejectReason reason) {
            return new Rejection(reason);
        }
    }

    enum Ownership {
        TRANSFERRED_TO_DURABLE_TARGET,
        PERSISTED_PROVIDER_QUEUE
    }

    enum Backpressure {
        MAY_ACCEPT_MORE,
        RECHECK_NEXT_TICK,
        SATURATED
    }

    enum RejectReason {
        CAPACITY_CHANGED,
        PATTERN_UNAVAILABLE,
        UNSUPPORTED_INPUT,
        INTERNAL_ERROR,
        OTHER
    }
}
