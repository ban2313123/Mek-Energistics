package com.fish_dan_.data_energistics.common.crafting.trinity.dispatch.provider;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.stacks.KeyCounter;
import com.fish_dan_.data_energistics.api.crafting.dispatch.CountedCraftingAdmission;
import com.fish_dan_.data_energistics.api.crafting.dispatch.CountedCraftingProviderAdapter;

public interface CountedCraftingProvider extends ICraftingProvider, CountedCraftingProviderAdapter {
    CountedCraftingAdmission prepareBatch(
            IPatternDetails pattern, KeyCounter[] oneCraftInputs, long requestedMaxCrafts);
}
