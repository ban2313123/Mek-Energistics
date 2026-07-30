package com.beipuo.mekenergistics.mixin.upgrade;

import com.beipuo.mekenergistics.upgrade.MePatternProviderUpgrade;
import com.beipuo.mekenergistics.upgrade.MePatternUpgradeLang;
import com.beipuo.mekenergistics.upgrade.StandaloneUpgradePersistence;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import mekanism.api.Upgrade;
import mekanism.api.text.EnumColor;
import mekanism.api.text.ILangEntry;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Upgrade.class, remap = false)
public abstract class UpgradeMixin {
    @Shadow
    @Final
    @Mutable
    private static Upgrade[] $VALUES;

    @Invoker("<init>")
    private static Upgrade mekenergistics$create(String internalName, int ordinal, String serializedName,
          ILangEntry name, ILangEntry description, int max, EnumColor color) {
        throw new AssertionError("Mixin invoker was not applied");
    }

    @Inject(method = "<clinit>", at = @At(value = "INVOKE",
          target = "Lmekanism/api/Upgrade;values()[Lmekanism/api/Upgrade;", ordinal = 0))
    private static void mekenergistics$addPatternProviderUpgrade(CallbackInfo ci) {
        Upgrade upgrade = mekenergistics$create(
                MePatternProviderUpgrade.INTERNAL_NAME,
                $VALUES.length,
                MePatternProviderUpgrade.SERIALIZED_NAME,
                MePatternUpgradeLang.NAME,
                MePatternUpgradeLang.DESCRIPTION,
                1,
                EnumColor.AQUA);
        $VALUES = Arrays.copyOf($VALUES, $VALUES.length + 1);
        $VALUES[$VALUES.length - 1] = upgrade;
        MePatternProviderUpgrade.setStandaloneUpgrade(upgrade);
    }

    @ModifyVariable(method = "buildMap", at = @At(value = "STORE", ordinal = 0), name = "upgrades")
    private static Map<Upgrade, Integer> mekenergistics$loadStableUpgrade(
          @Nullable Map<Upgrade, Integer> upgrades, @Nullable CompoundTag tag) {
        return StandaloneUpgradePersistence.load(upgrades, tag);
    }

    @ModifyExpressionValue(method = "saveMap",
          at = @At(value = "INVOKE", target = "Ljava/util/Map;entrySet()Ljava/util/Set;"))
    private static Set<Map.Entry<Upgrade, Integer>> mekenergistics$saveStableUpgrade(
          Set<Map.Entry<Upgrade, Integer>> upgrades, @Local(argsOnly = true) CompoundTag tag) {
        return StandaloneUpgradePersistence.saveAndFilter(upgrades, tag);
    }
}
