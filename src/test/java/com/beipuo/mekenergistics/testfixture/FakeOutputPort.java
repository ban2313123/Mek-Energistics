package com.beipuo.mekenergistics.testfixture;

import appeng.api.stacks.AEKey;
import com.beipuo.mekenergistics.blockentity.support.io.MeOutputPort;
import mekanism.api.Action;

/**
 * A machine output holding one key, mirroring the production ports: an extraction yields whatever
 * is present, up to what was asked for. Records the largest amount it was ever asked to give up,
 * because the drain loop credits the network before extracting and so must never ask for more than
 * the port advertised.
 */
public final class FakeOutputPort implements MeOutputPort {
    private final AEKey key;
    private long amount;
    private long largestRequest;

    public FakeOutputPort(AEKey key, long amount) {
        this.key = key;
        this.amount = amount;
    }

    public long largestRequest() {
        return this.largestRequest;
    }

    public long amountLeft() {
        return this.amount;
    }

    @Override
    public AEKey key() {
        return this.key;
    }

    @Override
    public long amount() {
        return this.amount;
    }

    @Override
    public long extract(long amount, Action action) {
        this.largestRequest = Math.max(this.largestRequest, amount);
        long extracted = Math.min(amount, this.amount);
        if (action.execute()) {
            this.amount -= extracted;
        }
        return extracted;
    }

    @Override
    public Object snapshot() {
        return this.amount;
    }

    @Override
    public void restore(Object snapshot) {
        this.amount = (long) snapshot;
    }
}
