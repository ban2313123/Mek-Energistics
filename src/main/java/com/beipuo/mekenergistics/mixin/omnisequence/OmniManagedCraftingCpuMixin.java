package com.beipuo.mekenergistics.mixin.omnisequence;

import appeng.crafting.execution.CraftingCpuLogic;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import com.atir.molecularmanipulator.blockentity.OmniComputationCoreBlockEntity;
import com.beipuo.mekenergistics.compat.omnisequence.OmniBatchCompat;
import com.beipuo.mekenergistics.crafting.OmniManagedCpuGuard;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Reports whether OmniSequence owns batch dispatch on this CPU. OmniSequence links CPUs to its omni
 * computation core through the same {@code ownerOf} probe its own CPU mixin consults, so matching
 * it exactly makes the deferral agree with the mod's dispatch decision on every present/absent
 * matrix.
 */
@Mixin(value = CraftingCpuLogic.class, remap = false)
public abstract class OmniManagedCraftingCpuMixin implements OmniManagedCpuGuard {
    @Shadow
    @Final
    private CraftingCPUCluster cluster;

    @Override
    public boolean mekenergistics$isOmniManagedCpu() {
        return OmniBatchCompat.isOmniManagedCpu(
                true, OmniComputationCoreBlockEntity.ownerOf(this.cluster) != null);
    }
}
