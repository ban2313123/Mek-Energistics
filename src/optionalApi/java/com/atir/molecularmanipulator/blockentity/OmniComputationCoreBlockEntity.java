package com.atir.molecularmanipulator.blockentity;

import appeng.me.cluster.implementations.CraftingCPUCluster;

/**
 * Compile-time stub for the OmniSequence omni-core block entity. Only the static CPU-ownership
 * probe is referenced by Mek-Energistics, which the real mod exposes with the same descriptor.
 */
public final class OmniComputationCoreBlockEntity {
    private OmniComputationCoreBlockEntity() {
    }

    public static OmniComputationCoreBlockEntity ownerOf(CraftingCPUCluster cpu) {
        return null;
    }
}
