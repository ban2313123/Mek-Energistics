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
        // Upgrade.buildMap returns Collections.emptyMap() when all native entries were filtered.
        // Copy before adding standalone entries so the reload path is always mutable.
        Map<Upgrade, Integer> result = upgrades == null ? new LinkedHashMap<>() : new LinkedHashMap<>(upgrades);
        load(result, tag, MePatternProviderUpgrade.get());
        load(result, tag, MePassiveCraftingUpgrade.get());
        return result;
    }

    public static Set<Map.Entry<Upgrade, Integer>> saveAndFilter(
          Set<Map.Entry<Upgrade, Integer>> upgrades, CompoundTag tag) {
        Set<Map.Entry<Upgrade, Integer>> vanilla = new LinkedHashSet<>();
        CompoundTag custom = new CompoundTag();
        for (Map.Entry<Upgrade, Integer> entry : upgrades) {
            if (entry.getKey() == MePatternProviderUpgrade.get() || entry.getKey() == MePassiveCraftingUpgrade.get()) {
                Upgrade upgrade = entry.getKey();
                custom.putInt(upgrade.getSerializedName(), Math.max(0, Math.min(entry.getValue(), upgrade.getMax())));
            } else {
                vanilla.add(entry);
            }
        }
        if (custom.isEmpty()) {
            tag.remove(TAG_UPGRADES);
        } else {
            tag.put(TAG_UPGRADES, custom);
        }
        return vanilla;
    }

    private static void load(Map<Upgrade, Integer> upgrades, CompoundTag tag, Upgrade upgrade) {
        int count = loadCount(tag, upgrade);
        if (count > 0) {
            upgrades.put(upgrade, Math.min(count, upgrade.getMax()));
        }
    }

    public static int loadCount(CompoundTag tag) {
        return loadCount(tag, MePatternProviderUpgrade.get());
    }

    public static int loadCount(CompoundTag tag, Upgrade upgrade) {
        if (tag.contains(TAG_UPGRADES, Tag.TAG_COMPOUND)) {
            return Math.max(0, tag.getCompound(TAG_UPGRADES).getInt(upgrade.getSerializedName()));
        }
        return loadEmpoweredCount(tag, upgrade);
    }

    public static int loadEmpoweredCount(CompoundTag tag) {
        return loadEmpoweredCount(tag, MePatternProviderUpgrade.get());
    }

    public static int loadEmpoweredCount(CompoundTag tag, Upgrade upgrade) {
        for (String key : new String[] {
                "mekanism_empowered_core:additional_upgrades",
                "additional_upgrades"}) {
            if (tag.contains(key, Tag.TAG_COMPOUND)) {
                return Math.max(0, tag.getCompound(key).getInt(upgrade.getSerializedName()));
            }
        }
        return 0;
    }

    public static void saveCount(CompoundTag tag, int count) {
        saveCount(tag, MePatternProviderUpgrade.get(), count);
    }

    public static void saveCount(CompoundTag tag, Upgrade upgrade, int count) {
        CompoundTag custom = tag.contains(TAG_UPGRADES, Tag.TAG_COMPOUND)
                ? tag.getCompound(TAG_UPGRADES).copy() : new CompoundTag();
        if (count > 0) custom.putInt(upgrade.getSerializedName(), count);
        else custom.remove(upgrade.getSerializedName());
        if (custom.isEmpty()) tag.remove(TAG_UPGRADES);
        else tag.put(TAG_UPGRADES, custom);
    }
}
