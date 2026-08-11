package com.beipuo.mekenergistics.upgrade;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;

/**
 * Read-only migration from every historical ME upgrade tag into {@link MeUpgradeData}.
 *
 * <p>Sources, in priority order:</p>
 * <ol>
 *   <li>canonical {@code mekenergistics:me_upgrades} (schema 1)</li>
 *   <li>current 3.x root tag {@code mekenergistics_upgrades}</li>
 *   <li>the same tag nested inside the Mekanism upgrades component container</li>
 *   <li>historical {@code mekanism_empowered_core:additional_upgrades}</li>
 *   <li>ancient {@code additional_upgrades}</li>
 * </ol>
 *
 * <p>Only {@code me_pattern_provider} and {@code me_passive_crafting} are extracted from legacy
 * sources. Ordinal keys (for example native upgrade {@code type = 7}) are never interpreted as ME
 * upgrades. The external copy handed to outside upgrade decoders has the claimed ME names
 * removed; unknown names are preserved verbatim.</p>
 */
public final class MeUpgradeDataMigration {
    public static final String LEGACY_ROOT_TAG = "mekenergistics_upgrades";
    public static final String EMPOWERED_TAG = "mekanism_empowered_core:additional_upgrades";
    public static final String ANCIENT_TAG = "additional_upgrades";
    private static final String COMPONENTS_TAG = "mekanism:components";

    private MeUpgradeDataMigration() {
    }

    public static MeUpgradeMigrationResult migrate(@Nullable CompoundTag tag) {
        if (tag == null) {
            return MeUpgradeMigrationResult.NONE;
        }
        MeUpgradeData canonical = MeUpgradePersistence.load(tag);
        if (!canonical.isEmpty()) {
            return new MeUpgradeMigrationResult(canonical, false, stripClaimed(tag, false));
        }

        Map<MeUpgradeType, Integer> counts = new LinkedHashMap<>();
        Map<String, Integer> preserved = new LinkedHashMap<>();
        CompoundTag source = findLegacySource(tag);
        boolean migrated = false;
        if (source != null) {
            migrated = true;
            for (String key : source.getAllKeys()) {
                if (key.equals(MeUpgradeType.PATTERN_PROVIDER.getSerializedName())
                        || key.equals(MeUpgradeType.PASSIVE_CRAFTING.getSerializedName())) {
                    int value = source.getInt(key);
                    MeUpgradeType type = MeUpgradeType.bySerializedName(key);
                    if (value > 0) {
                        counts.put(type, Math.min(value, type.getMaxCount()));
                    }
                } else {
                    int value = source.getInt(key);
                    if (value != 0) {
                        preserved.put(key, value);
                    }
                }
            }
        }
        return new MeUpgradeMigrationResult(new MeUpgradeData(counts, preserved), migrated,
                stripClaimed(tag, true));
    }

    /**
     * Returns the legacy compound carrying ME upgrade counts, searching the documented sources in
     * priority order, or null when none exists.
     */
    @Nullable
    private static CompoundTag findLegacySource(CompoundTag tag) {
        CompoundTag root = readCompound(tag, LEGACY_ROOT_TAG);
        if (root != null) {
            return root;
        }
        CompoundTag components = readCompound(tag, COMPONENTS_TAG);
        if (components != null) {
            CompoundTag nested = readCompound(components, LEGACY_ROOT_TAG);
            if (nested != null) {
                return nested;
            }
        }
        CompoundTag empowered = readCompound(tag, EMPOWERED_TAG);
        if (empowered != null) {
            return empowered;
        }
        return readCompound(tag, ANCIENT_TAG);
    }

    /**
     * Produces a copy of the external upgrade data with every claimed ME name key removed, so
     * outside decoders never see ME upgrades we have taken over. When {@code includeCanonical} is
     * true the canonical tag itself is excluded from the copy because it is our own layout.
     */
    public static CompoundTag stripClaimed(CompoundTag tag, boolean includeCanonical) {
        CompoundTag result = new CompoundTag();
        if (tag == null) {
            return result;
        }
        for (String key : tag.getAllKeys()) {
            if (includeCanonical && key.equals(MeUpgradePersistence.TAG_ME_UPGRADES)) {
                continue;
            }
            if (key.equals(LEGACY_ROOT_TAG) || key.equals(EMPOWERED_TAG) || key.equals(ANCIENT_TAG)) {
                CompoundTag copy = tag.getCompound(key).copy();
                copy.remove(MeUpgradeType.PATTERN_PROVIDER.getSerializedName());
                copy.remove(MeUpgradeType.PASSIVE_CRAFTING.getSerializedName());
                result.put(key, copy);
            } else {
                result.put(key, tag.get(key).copy());
            }
        }
        return result;
    }

    @Nullable
    private static CompoundTag readCompound(CompoundTag tag, String key) {
        return tag.contains(key, Tag.TAG_COMPOUND) ? tag.getCompound(key) : null;
    }

    /** Result of a migration pass. */
    public record MeUpgradeMigrationResult(MeUpgradeData data, boolean migratedFromLegacy,
            CompoundTag externalUpgrades) {
        public static final MeUpgradeMigrationResult NONE =
                new MeUpgradeMigrationResult(MeUpgradeData.EMPTY, false, new CompoundTag());
    }
}
