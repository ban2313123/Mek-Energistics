package com.beipuo.mekenergistics.mixin;

import com.beipuo.mekenergistics.registry.ModItems;
import com.beipuo.mekenergistics.upgrade.MeMekanismUpgrades;
import mekanism.api.Upgrade;
import mekanism.common.util.UpgradeUtils;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Teaches Mekanism's upgrade output slot which item represents each ME upgrade. */
@Mixin(value = UpgradeUtils.class, remap = false)
public abstract class UpgradeUtilsMixin {
    @Inject(method = "getItem", at = @At("HEAD"), cancellable = true)
    private static void mekenergistics$getMeUpgradeItem(Upgrade upgrade,
            CallbackInfoReturnable<Holder<Item>> cir) {
        if (upgrade == MeMekanismUpgrades.patternProvider()) {
            cir.setReturnValue(ModItems.ME_PATTERN_PROVIDER_UPGRADE);
        } else if (upgrade == MeMekanismUpgrades.passiveCrafting()) {
            cir.setReturnValue(ModItems.ME_PASSIVE_CRAFTING_UPGRADE);
        } else if (upgrade == MeMekanismUpgrades.outputInterface()) {
            cir.setReturnValue(ModItems.ME_OUTPUT_INTERFACE_UPGRADE);
        }
    }
}
