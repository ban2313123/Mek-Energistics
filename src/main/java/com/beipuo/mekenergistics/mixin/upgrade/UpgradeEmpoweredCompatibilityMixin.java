package com.beipuo.mekenergistics.mixin.upgrade;

import com.beipuo.mekenergistics.upgrade.MePatternProviderUpgrade;
import com.beipuo.mekenergistics.upgrade.MePassiveCraftingUpgrade;
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
    @Inject(method = "buildMap", at = @At("RETURN"), cancellable = true)
    private static void mekenergistics$loadStandaloneTag(CompoundTag tag,
          CallbackInfoReturnable<Map<Upgrade, Integer>> cir) {
        Map<Upgrade, Integer> upgrades = cir.getReturnValue();
        upgrades = mekenergistics$loadUpgrade(tag, upgrades, MePatternProviderUpgrade.get());
        upgrades = mekenergistics$loadUpgrade(tag, upgrades, MePassiveCraftingUpgrade.get());
        cir.setReturnValue(upgrades);
    }

    @Inject(method = "saveMap", at = @At("RETURN"))
    private static void mekenergistics$saveStandaloneTag(Map<Upgrade, Integer> upgrades,
          CompoundTag tag, CallbackInfo ci) {
        Upgrade patternProvider = MePatternProviderUpgrade.get();
        Upgrade passiveCrafting = MePassiveCraftingUpgrade.get();
        StandaloneUpgradePersistence.saveCount(tag, patternProvider, upgrades.getOrDefault(patternProvider, 0));
        StandaloneUpgradePersistence.saveCount(tag, passiveCrafting, upgrades.getOrDefault(passiveCrafting, 0));
    }

    private static Map<Upgrade, Integer> mekenergistics$loadUpgrade(
            CompoundTag tag, Map<Upgrade, Integer> upgrades, Upgrade upgrade) {
        int count = StandaloneUpgradePersistence.loadCount(tag, upgrade);
        if (count <= 0 || (upgrades != null && upgrades.containsKey(upgrade))) {
            return upgrades;
        }
        Map<Upgrade, Integer> result = upgrades == null ? new HashMap<>() : upgrades;
        result.put(upgrade, Math.min(count, upgrade.getMax()));
        return result;
    }
}
