package com.beipuo.mekenergistics.blockentity.support.io;

import appeng.api.stacks.AEKey;
import mekanism.api.Action;

/** A transactional machine input that accepts one or more AE key types. */
public interface MeInputPort {
    boolean supports(AEKey key);

    /** Returns the amount accepted. */
    long insert(AEKey key, long amount, Action action);

    Object snapshot();

    void restore(Object snapshot);
}
