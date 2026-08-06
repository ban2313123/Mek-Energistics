package com.atir.molecularmanipulator.api.crafting;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEKey;
import java.util.List;

public record OmniBatchProbe(
        IPatternDetails pattern, List<Input> oneCraftInputs, long requestedMaxCrafts) {
    public record Input(int slot, AEKey key, long amount) {
    }
}
