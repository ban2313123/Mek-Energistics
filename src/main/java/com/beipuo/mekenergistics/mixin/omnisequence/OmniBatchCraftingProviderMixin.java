package com.beipuo.mekenergistics.mixin.omnisequence;

import com.atir.molecularmanipulator.api.crafting.OmniBatchAdmission;
import com.atir.molecularmanipulator.api.crafting.OmniBatchCraftingProvider;
import com.atir.molecularmanipulator.api.crafting.OmniBatchProbe;
import com.beipuo.mekenergistics.blockentity.api.MeAeMachine;
import com.beipuo.mekenergistics.blockentity.api.MeFactoryAeMachine;
import com.beipuo.mekenergistics.blockentity.support.AbstractMeAeSupport;
import com.beipuo.mekenergistics.blockentity.support.MeFactoryAeSupport;
import com.beipuo.mekenergistics.compat.omnisequence.OmniBatchCompat;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MeAeMachine.class)
public interface OmniBatchCraftingProviderMixin extends OmniBatchCraftingProvider {
    @Shadow
    AbstractMeAeSupport<?> getRecipeAeSupport();

    @Override
    @Nullable
    default OmniBatchAdmission prepareOmniBatch(OmniBatchProbe probe) {
        return OmniBatchCompat.prepare(getRecipeAeSupport(), probe);
    }
}

@Mixin(MeFactoryAeMachine.class)
interface OmniBatchFactoryCraftingProviderMixin extends OmniBatchCraftingProvider {
    @Shadow
    MeFactoryAeSupport getAeSupport();

    @Override
    @Nullable
    default OmniBatchAdmission prepareOmniBatch(OmniBatchProbe probe) {
        return OmniBatchCompat.prepare(getAeSupport(), probe);
    }
}
