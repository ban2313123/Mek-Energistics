package com.beipuo.mekenergistics.mixin.upgrade;

import com.beipuo.mekenergistics.upgrade.MePatternProviderUpgrade;
import com.beipuo.mekenergistics.upgrade.StandaloneUpgradePersistence;
import java.util.HashMap;
import java.util.Map;
import mekanism.api.Upgrade;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Keeps worlds portable when Empowered Core is added to or removed from the instance. */
@Mixin(value = Upgrade.class, remap = false)
public abstract class UpgradeEmpoweredCompatibilityMixin {
    @Inject(method = "buildMap", at = @At("RETURN"))
    private static void mekenergistics$loadStandaloneTag(CompoundTag tag,
          CallbackInfoReturnable<Map<Upgrade, Integer>> cir) {
        int count = StandaloneUpgradePersistence.loadCount(tag);
        Map<Upgrade, Integer> upgrades = cir.getReturnValue();
        if (count > 0 && (upgrades == null || !upgrades.containsKey(MePatternProviderUpgrade.get()))) {
            if (upgrades == null) {
                upgrades = new HashMap<>();
            }
            upgrades.put(MePatternProviderUpgrade.get(), Math.min(count, MePatternProviderUpgrade.get().getMax()));
            cir.setReturnValue(upgrades);
        }
    }

    @Inject(method = "saveMap", at = @At("RETURN"))
    private static void mekenergistics$saveStandaloneTag(Map<Upgrade, Integer> upgrades,
          CompoundTag tag, CallbackInfo ci) {
        Upgrade upgrade = MePatternProviderUpgrade.get();
        StandaloneUpgradePersistence.saveCount(tag, upgrades.getOrDefault(upgrade, 0));
    }
}
