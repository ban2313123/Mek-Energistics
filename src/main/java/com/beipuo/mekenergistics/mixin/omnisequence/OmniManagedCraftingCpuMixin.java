package com.beipuo.mekenergistics.mixin.omnisequence;

import appeng.crafting.execution.CraftingCpuLogic;
import com.atir.molecularmanipulator.api.crafting.OmniBatchCraftingApi;
import com.beipuo.mekenergistics.crafting.OmniManagedCpuGuard;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = CraftingCpuLogic.class, remap = false)
public abstract class OmniManagedCraftingCpuMixin implements OmniManagedCpuGuard {
    @Override
    public boolean mekenergistics$isOmniManagedCpu() {
        return OmniBatchCraftingApi.apiVersion() == 1
                && OmniBatchCraftingApi.isOmniManagedCpu(this);
    }
}
