package com.beipuo.mekenergistics.blockentity.support.io;

import appeng.api.stacks.AEKey;
import mekanism.api.Action;
import org.jetbrains.annotations.Nullable;

/** A transactional machine output containing at most one AE key. */
public interface MeOutputPort {
    @Nullable
    AEKey key();

    long amount();

    /** Returns the amount extracted. */
    long extract(long amount, Action action);

    Object snapshot();

    void restore(Object snapshot);
}
