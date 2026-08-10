package com.atir.molecularmanipulator.integration.ae2;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.stacks.KeyCounter;

/**
 * Compile-time contract for OmniSequence Transfinite's current batch entry point. The released
 * OmniSequence jar keeps this interface in the same package with the same member descriptors; the
 * optionalApi output is never packaged into the Mek-Energistics jar, so at runtime the real mod's
 * class is the one the mixins are bound to.
 */
public interface MolecularBatchCraftingProvider {

    boolean molecularmanipulator$supportsBatching(IPatternDetails patternDetails);

    default long molecularmanipulator$getBatchLimit(IPatternDetails patternDetails) {
        return Long.MAX_VALUE;
    }

    static boolean supports(ICraftingProvider provider, IPatternDetails patternDetails) {
        return provider instanceof MolecularBatchCraftingProvider batchProvider
                && batchProvider.molecularmanipulator$supportsBatching(patternDetails);
    }

    static boolean supportsOmniDispatch(ICraftingProvider provider, IPatternDetails patternDetails) {
        if (supports(provider, patternDetails)) {
            return true;
        }
        return provider != null && patternDetails != null
                && patternDetails.supportsPushInputsToExternalInventory();
    }

    static long getBatchLimit(ICraftingProvider provider, IPatternDetails patternDetails) {
        return getBatchLimit(provider, patternDetails, null);
    }

    static long getBatchLimit(ICraftingProvider provider, IPatternDetails patternDetails,
            KeyCounter[] firstInputs) {
        if (provider instanceof MolecularBatchCraftingProvider batchProvider
                && batchProvider.molecularmanipulator$supportsBatching(patternDetails)) {
            return Math.max(0, batchProvider.molecularmanipulator$getBatchLimit(patternDetails));
        }
        return 0;
    }
}
