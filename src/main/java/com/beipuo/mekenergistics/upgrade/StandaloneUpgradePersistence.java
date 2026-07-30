package com.beipuo.mekenergistics.upgrade;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import mekanism.api.Upgrade;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;

public final class StandaloneUpgradePersistence {
    static final String TAG_UPGRADES = "mekenergistics_upgrades";

    private StandaloneUpgradePersistence() {
    }

    public static Map<Upgrade, Integer> load(@Nullable Map<Upgrade, Integer> upgrades,
          @Nullable CompoundTag tag) {
        if (tag == null || !tag.contains(TAG_UPGRADES, Tag.TAG_COMPOUND)) {
            return upgrades;
        }
        int count = loadCount(tag);
        if (count == 0) {
            return upgrades;
        }
        Map<Upgrade, Integer> result = upgrades == null ? new LinkedHashMap<>() : upgrades;
        result.put(MePatternProviderUpgrade.get(), Math.min(count, MePatternProviderUpgrade.get().getMax()));
        return result;
    }

    public static Set<Map.Entry<Upgrade, Integer>> saveAndFilter(
          Set<Map.Entry<Upgrade, Integer>> upgrades, CompoundTag tag) {
        Set<Map.Entry<Upgrade, Integer>> vanilla = new LinkedHashSet<>();
        int count = 0;
        Upgrade patternUpgrade = MePatternProviderUpgrade.get();
        for (Map.Entry<Upgrade, Integer> entry : upgrades) {
            if (entry.getKey() == patternUpgrade) {
                count = Math.max(0, Math.min(entry.getValue(), patternUpgrade.getMax()));
            } else {
                vanilla.add(entry);
            }
        }
        saveCount(tag, count);
        return vanilla;
    }

    public static int loadCount(CompoundTag tag) {
        if (tag.contains(TAG_UPGRADES, Tag.TAG_COMPOUND)) {
            return Math.max(0, tag.getCompound(TAG_UPGRADES).getInt(MePatternProviderUpgrade.SERIALIZED_NAME));
        }
        return loadEmpoweredCount(tag);
    }

    public static int loadEmpoweredCount(CompoundTag tag) {
        for (String key : new String[] {
                "mekanism_empowered_core:additional_upgrades",
                "additional_upgrades"}) {
            if (tag.contains(key, Tag.TAG_COMPOUND)) {
                return Math.max(0, tag.getCompound(key).getInt(MePatternProviderUpgrade.SERIALIZED_NAME));
            }
        }
        return 0;
    }

    public static void saveCount(CompoundTag tag, int count) {
        if (count > 0) {
            CompoundTag custom = new CompoundTag();
            custom.putInt(MePatternProviderUpgrade.SERIALIZED_NAME, count);
            tag.put(TAG_UPGRADES, custom);
        } else {
            tag.remove(TAG_UPGRADES);
        }
    }
}
