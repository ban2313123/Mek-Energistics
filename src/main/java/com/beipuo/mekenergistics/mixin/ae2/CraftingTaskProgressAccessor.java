package com.beipuo.mekenergistics.mixin.ae2;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "appeng.crafting.execution.ExecutingCraftingJob$TaskProgress", remap = false)
public interface CraftingTaskProgressAccessor {
    @Accessor("value")
    long mekenergistics$getValue();

    @Accessor("value")
    void mekenergistics$setValue(long value);
}
