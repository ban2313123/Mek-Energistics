package com.beipuo.mekenergistics.mixin.omnisequence;

import appeng.api.crafting.IPatternDetails;
import com.atir.molecularmanipulator.integration.ae2.MolecularBatchCraftingProvider;
import com.beipuo.mekenergistics.blockentity.api.MeAeMachine;
import com.beipuo.mekenergistics.blockentity.api.MeFactoryAeMachine;
import com.beipuo.mekenergistics.blockentity.support.AbstractMeAeSupport;
import com.beipuo.mekenergistics.blockentity.support.MeFactoryAeSupport;
import com.beipuo.mekenergistics.compat.omnisequence.OmniBatchCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Implements OmniSequence Transfinite's current {@code MolecularBatchCraftingProvider} entry point
 * on ME machines. OmniSequence's CPU mixin is the batch owner on omni-core-managed CPUs; the
 * {@link OmniManagedCraftingCpuMixin} guard makes Mek-Energistics' own CPU batching defer there, so
 * the two engines never batch the same push.
 */
@Mixin(MeAeMachine.class)
public interface OmniBatchCraftingProviderMixin extends MolecularBatchCraftingProvider {
    @Shadow
    AbstractMeAeSupport<?> getRecipeAeSupport();

    @Override
    default boolean molecularmanipulator$supportsBatching(IPatternDetails patternDetails) {
        return OmniBatchCompat.supportsBatching(getRecipeAeSupport(), patternDetails);
    }

    @Override
    default long molecularmanipulator$getBatchLimit(IPatternDetails patternDetails) {
        return OmniBatchCompat.getBatchLimit(getRecipeAeSupport(), patternDetails);
    }
}

@Mixin(MeFactoryAeMachine.class)
interface OmniBatchFactoryCraftingProviderMixin extends MolecularBatchCraftingProvider {
    @Shadow
    MeFactoryAeSupport getAeSupport();

    @Override
    default boolean molecularmanipulator$supportsBatching(IPatternDetails patternDetails) {
        return OmniBatchCompat.supportsBatching(getAeSupport(), patternDetails);
    }

    @Override
    default long molecularmanipulator$getBatchLimit(IPatternDetails patternDetails) {
        return OmniBatchCompat.getBatchLimit(getAeSupport(), patternDetails);
    }
}
