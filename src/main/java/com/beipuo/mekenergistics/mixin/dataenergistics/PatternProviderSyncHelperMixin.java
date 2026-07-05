package com.beipuo.mekenergistics.mixin.dataenergistics;

import appeng.helpers.patternprovider.PatternContainer;
import com.beipuo.mekenergistics.blockentity.api.MeAeMachine;
import com.beipuo.mekenergistics.blockentity.api.MeFactoryAeMachine;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.fish_dan_.data_energistics.menu.common.PatternProviderSyncHelper", remap = false)
public abstract class PatternProviderSyncHelperMixin {
    @Inject(method = "isRenameableProvider(Lappeng/helpers/patternprovider/PatternContainer;)Z", at = @At("HEAD"), cancellable = true, require = 0)
    private static void mekenergistics$skipMekanismTileReflection(PatternContainer provider, CallbackInfoReturnable<Boolean> cir) {
        mekenergistics$skipMekanismTileReflectionIfNeeded(provider, cir);
    }

    @Inject(method = "isRenameableProvider(Lappeng/helpers/patternprovider/PatternContainer;Lnet/minecraft/network/chat/Component;Lnet/minecraft/resources/ResourceLocation;)Z", at = @At("HEAD"), cancellable = true, require = 0)
    private static void mekenergistics$skipMekanismTileReflection(PatternContainer provider, Component displayName, ResourceLocation iconItemId, CallbackInfoReturnable<Boolean> cir) {
        mekenergistics$skipMekanismTileReflectionIfNeeded(provider, cir);
    }

    private static void mekenergistics$skipMekanismTileReflectionIfNeeded(PatternContainer provider, CallbackInfoReturnable<Boolean> cir) {
        if (provider instanceof MeAeMachine || provider instanceof MeFactoryAeMachine) {
            cir.setReturnValue(false);
        }
    }
}
