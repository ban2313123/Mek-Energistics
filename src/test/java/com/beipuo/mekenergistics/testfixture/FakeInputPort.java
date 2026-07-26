package com.beipuo.mekenergistics.testfixture;

import appeng.api.stacks.AEKey;
import com.beipuo.mekenergistics.blockentity.support.io.MeInputPort;
import mekanism.api.Action;

/**
 * A single-key input port with a fixed capacity, standing in for one machine slot or tank.
 *
 * <p>Counts simulations and restores so tests can assert that a routing pass probed without
 * writing, and can be told to fail on execute so tests can drive the rollback paths.
 */
public final class FakeInputPort implements MeInputPort {
    private final AEKey key;
    private final long capacity;
    private long amount;
    private int simulations;
    private int restores;
    private boolean failExecution;

    public FakeInputPort(AEKey key, long capacity) {
        this.key = key;
        this.capacity = capacity;
    }

    public long amount() {
        return this.amount;
    }

    /** Lets a test move contents around behind the port's back, e.g. to model a factory auto-balancing. */
    public void setAmount(long amount) {
        this.amount = amount;
    }

    public int simulations() {
        return this.simulations;
    }

    public int restores() {
        return this.restores;
    }

    public void failExecution() {
        this.failExecution = true;
    }

    @Override
    public boolean supports(AEKey key) {
        return this.key.equals(key);
    }

    @Override
    public long insert(AEKey key, long amount, Action action) {
        if (!supports(key) || this.failExecution && action.execute()) {
            return 0;
        }
        if (!action.execute()) {
            this.simulations++;
        }
        long accepted = Math.min(amount, this.capacity - this.amount);
        if (action.execute()) {
            this.amount += accepted;
        }
        return Math.max(0, accepted);
    }

    @Override
    public Object snapshot() {
        return this.amount;
    }

    @Override
    public void restore(Object snapshot) {
        this.restores++;
        this.amount = (long) snapshot;
    }
}
