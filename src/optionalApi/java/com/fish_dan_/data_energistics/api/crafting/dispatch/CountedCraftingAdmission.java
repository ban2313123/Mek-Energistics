package com.fish_dan_.data_energistics.api.crafting.dispatch;

import appeng.api.stacks.KeyCounter;

public interface CountedCraftingAdmission {
    long count();

    default boolean hasTransferredInputOwnership() {
        return false;
    }

    boolean commit(KeyCounter[] deliveredInputs);
}
