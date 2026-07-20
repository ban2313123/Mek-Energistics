package com.beipuo.mekenergistics.mixin.ae2;

import appeng.api.crafting.IPatternDetails;
import appeng.crafting.execution.ElapsedTimeTracker;
import appeng.crafting.execution.ExecutingCraftingJob;
import appeng.crafting.inv.ListCraftingInventory;
import java.util.Map;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ExecutingCraftingJob.class, remap = false)
public interface ExecutingCraftingJobAccessor {
    @Accessor("tasks")
    Map<IPatternDetails, Object> mekenergistics$getTasks();

    @Accessor("waitingFor")
    ListCraftingInventory mekenergistics$getWaitingFor();

    @Accessor("timeTracker")
    ElapsedTimeTracker mekenergistics$getTimeTracker();
}
