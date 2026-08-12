package com.beipuo.mekenergistics.mixin;

import com.beipuo.mekenergistics.MekEnergistics;
import com.beipuo.mekenergistics.upgrade.MeMekanismUpgrades;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import mekanism.api.Upgrade;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Persists this mod's enum extensions by stable name instead of a load-order-dependent ordinal. */
@Mixin(value = Upgrade.class, remap = false)
public abstract class UpgradePersistenceMixin {
    private static final String TAG = MekEnergistics.MODID + ":native_upgrades";

    @Inject(method = "buildMap", at = @At("RETURN"), cancellable = true)
    private static void mekenergistics$readMeUpgrades(CompoundTag nbtTags,
            CallbackInfoReturnable<Map<Upgrade, Integer>> cir) {
        if (nbtTags == null || !nbtTags.contains(TAG, Tag.TAG_COMPOUND)) {
            return;
        }
        Map<Upgrade, Integer> result = cir.getReturnValue().isEmpty()
                ? new EnumMap<>(Upgrade.class)
                : new EnumMap<>(cir.getReturnValue());
        CompoundTag saved = nbtTags.getCompound(TAG);
        read(saved, MeMekanismUpgrades.patternProvider(), result);
        read(saved, MeMekanismUpgrades.passiveCrafting(), result);
        read(saved, MeMekanismUpgrades.outputInterface(), result);
        cir.setReturnValue(result);
    }

    @ModifyExpressionValue(method = "saveMap", at = @At(value = "INVOKE",
            target = "Ljava/util/Map;entrySet()Ljava/util/Set;"))
    private static Set<Map.Entry<Upgrade, Integer>> mekenergistics$writeMeUpgrades(
            Set<Map.Entry<Upgrade, Integer>> entries, @Local(argsOnly = true) CompoundTag nbtTags) {
        CompoundTag saved = new CompoundTag();
        Set<Map.Entry<Upgrade, Integer>> vanilla = new LinkedHashSet<>();
        for (Map.Entry<Upgrade, Integer> entry : entries) {
            if (MeMekanismUpgrades.toType(entry.getKey()) == null) {
                vanilla.add(entry);
            } else if (entry.getValue() != null && entry.getValue() > 0) {
                saved.putInt(entry.getKey().getSerializedName(), entry.getValue());
            }
        }
        if (saved.isEmpty()) {
            nbtTags.remove(TAG);
        } else {
            nbtTags.put(TAG, saved);
        }
        return vanilla;
    }

    private static void read(CompoundTag saved, Upgrade upgrade, Map<Upgrade, Integer> result) {
        int count = Math.min(Math.max(saved.getInt(upgrade.getSerializedName()), 0), upgrade.getMax());
        if (count > 0) {
            result.put(upgrade, count);
        }
    }
}
