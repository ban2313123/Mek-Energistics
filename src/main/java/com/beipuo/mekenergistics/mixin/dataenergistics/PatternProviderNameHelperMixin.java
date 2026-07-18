package com.beipuo.mekenergistics.mixin.dataenergistics;

import com.beipuo.mekenergistics.blockentity.api.MeAeSupportOwner;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.fish_dan_.data_energistics.util.PatternProviderNameHelper", remap = false)
public abstract class PatternProviderNameHelperMixin {
    @Inject(method = "canRename(Ljava/lang/Object;)Z", at = @At("HEAD"), cancellable = true, require = 0)
    private static void mekenergistics$skipMekanismTileReflection(Object target, CallbackInfoReturnable<Boolean> cir) {
        if (mekenergistics$isMePatternProvider(target)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "getCustomName(Ljava/lang/Object;)Lnet/minecraft/network/chat/Component;", at = @At("HEAD"), cancellable = true, require = 0)
    private static void mekenergistics$skipMekanismTileNameRead(Object target, CallbackInfoReturnable<Component> cir) {
        if (mekenergistics$isMePatternProvider(target)) {
            cir.setReturnValue(null);
        }
    }

    @Inject(method = "setCustomName(Ljava/lang/Object;Lnet/minecraft/network/chat/Component;)Z", at = @At("HEAD"), cancellable = true, require = 0)
    private static void mekenergistics$skipMekanismTileNameWrite(Object target, Component customName, CallbackInfoReturnable<Boolean> cir) {
        if (mekenergistics$isMePatternProvider(target)) {
            cir.setReturnValue(false);
        }
    }

    private static boolean mekenergistics$isMePatternProvider(Object target) {
        return target instanceof MeAeSupportOwner;
    }
}
