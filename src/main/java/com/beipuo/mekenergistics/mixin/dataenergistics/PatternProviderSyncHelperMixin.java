package com.beipuo.mekenergistics.mixin.dataenergistics;

import appeng.helpers.patternprovider.PatternContainer;
import com.beipuo.mekenergistics.blockentity.api.MeAeSupportOwner;
import com.beipuo.mekenergistics.blockentity.api.MeUpgradeableMachine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.fish_dan_.data_energistics.menu.patternprovider.PatternProviderSyncHelper", remap = false)
public abstract class PatternProviderSyncHelperMixin {
    @Inject(method = "isRenameableProvider(Lappeng/helpers/patternprovider/PatternContainer;)Z", at = @At("HEAD"), cancellable = true, require = 0)
    private static void mekenergistics$skipMekanismTileReflection(PatternContainer provider, CallbackInfoReturnable<Boolean> cir) {
        mekenergistics$skipMekanismTileReflectionIfNeeded(provider, cir);
    }

    private static void mekenergistics$skipMekanismTileReflectionIfNeeded(PatternContainer provider, CallbackInfoReturnable<Boolean> cir) {
        if (provider instanceof MeUpgradeableMachine upgradeable && !upgradeable.isMeUpgradeActive()) {
            cir.setReturnValue(false);
        } else if (provider instanceof MeAeSupportOwner) {
            cir.setReturnValue(false);
        }
    }
}
