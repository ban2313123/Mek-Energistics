package com.beipuo.mekenergistics.mixin;

import com.beipuo.mekenergistics.blockentity.api.MeUpgradeableMachine;
import com.beipuo.mekenergistics.blockentity.support.MeRecipeMachineAeSupport;
import com.jerry.mekextras.common.tile.factory.TileEntityExtraAlloyingFactory;
import fr.iglee42.evolvedmekanism.recipes.AlloyerRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TileEntityExtraAlloyingFactory.class, remap = false)
public abstract class MekExtrasAlloyingFactoryMeUpgradeEnergyMixin {
    @Inject(method = "createNewCachedRecipe(Lfr/iglee42/evolvedmekanism/recipes/AlloyerRecipe;I)Lmekanism/api/recipes/cache/CachedRecipe;",
            at = @At("RETURN"), cancellable = true)
    private void mekenergistics$wrapAlloying(AlloyerRecipe recipe, int cacheIndex,
            CallbackInfoReturnable<CachedRecipe<AlloyerRecipe>> cir) {
        MeUpgradeableMachine machine = (MeUpgradeableMachine) this;
        if (machine.isMeUpgradeTarget() && machine.isMeUpgradeActive()) {
            TileEntityExtraAlloyingFactory tile = (TileEntityExtraAlloyingFactory) (Object) this;
            cir.setReturnValue(((MeRecipeMachineAeSupport<?>) machine.getRecipeAeSupport())
                    .wrapRecipeEnergy(tile.getEnergyContainer(), cir.getReturnValue()));
        }
    }
}
