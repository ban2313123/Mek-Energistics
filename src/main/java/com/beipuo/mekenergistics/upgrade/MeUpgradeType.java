package com.beipuo.mekenergistics.upgrade;

import java.util.Locale;
import org.jetbrains.annotations.Nullable;

/**
 * Self-owned ME upgrade types. The serialized name is the stable save identity; ordinal values
 * are never used for persistence or migration.
 */
public enum MeUpgradeType {
    PATTERN_PROVIDER("me_pattern_provider", 1, "item.mekenergistics.upgrade_me_pattern_provider"),
    PASSIVE_CRAFTING("me_passive_crafting", 1, "item.mekenergistics.upgrade_me_passive_crafting"),
    OUTPUT_INTERFACE("me_output_interface", 1, "item.mekenergistics.upgrade_me_output_interface");

    private final String serializedName;
    private final int maxCount;
    private final String itemLangKey;

    MeUpgradeType(String serializedName, int maxCount, String itemLangKey) {
        this.serializedName = serializedName;
        this.maxCount = maxCount;
        this.itemLangKey = itemLangKey;
    }

    public String getSerializedName() {
        return this.serializedName;
    }

    public int getMaxCount() {
        return this.maxCount;
    }

    public String getItemLangKey() {
        return this.itemLangKey;
    }

    public String getInternalName() {
        return name().toUpperCase(Locale.ROOT);
    }

    @Nullable
    public static MeUpgradeType bySerializedName(String serializedName) {
        if (serializedName == null) {
            return null;
        }
        for (MeUpgradeType type : values()) {
            if (type.serializedName.equals(serializedName)) {
                return type;
            }
        }
        return null;
    }
}
