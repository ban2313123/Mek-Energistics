package com.fish_dan_.data_energistics.api.crafting.dispatch;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.KeyCounter;

public interface CountedCraftingProviderAdapter {
    CountedCraftingAdmission prepareBatch(
            IPatternDetails pattern, KeyCounter[] oneCraftInputs, long requestedMaxCrafts);
}
