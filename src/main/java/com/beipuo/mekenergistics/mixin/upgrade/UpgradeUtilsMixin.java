package com.beipuo.mekenergistics.mixin.upgrade;

import com.beipuo.mekenergistics.registry.ModItems;
import com.beipuo.mekenergistics.upgrade.MePatternProviderUpgrade;
import com.beipuo.mekenergistics.upgrade.MePassiveCraftingUpgrade;
import mekanism.api.Upgrade;
import mekanism.common.util.UpgradeUtils;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = UpgradeUtils.class, remap = false)
public abstract class UpgradeUtilsMixin {
    @Inject(method = "getItem", at = @At("HEAD"), cancellable = true)
    private static void mekenergistics$getPatternProviderItem(
          Upgrade upgrade, CallbackInfoReturnable<Holder<Item>> cir) {
        if (upgrade == MePatternProviderUpgrade.get()) {
            cir.setReturnValue(ModItems.ME_PATTERN_PROVIDER_UPGRADE);
        } else if (upgrade == MePassiveCraftingUpgrade.get()) {
            cir.setReturnValue(ModItems.ME_PASSIVE_CRAFTING_UPGRADE);
        }
    }
}
