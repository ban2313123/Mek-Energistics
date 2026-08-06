package com.atir.molecularmanipulator.api.crafting;

import appeng.api.networking.crafting.ICraftingProvider;

public interface OmniBatchCraftingProvider extends ICraftingProvider {
    OmniBatchAdmission prepareOmniBatch(OmniBatchProbe probe);
}
