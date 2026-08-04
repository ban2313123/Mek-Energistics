package com.fish_dan_.data_energistics.common.crafting.trinity.dispatch.provider;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.stacks.KeyCounter;

public interface CountedCraftingProvider extends ICraftingProvider {
    CountedCraftingAdmission prepareBatch(
            IPatternDetails pattern, KeyCounter[] oneCraftInputs, long requestedMaxCrafts);
}
