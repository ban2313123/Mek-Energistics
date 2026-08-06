package com.atir.molecularmanipulator.api.crafting;

public interface OmniBatchAdmission extends AutoCloseable {
    long maxCrafts();

    void commit(OmniBatchDelivery delivery);

    @Override
    default void close() {
    }
}
