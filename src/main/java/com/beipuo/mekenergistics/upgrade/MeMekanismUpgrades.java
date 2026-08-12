package com.beipuo.mekenergistics.upgrade;

import mekanism.api.Upgrade;

/** Access to the three Mekanism upgrade enum entries owned by this mod. */
public final class MeMekanismUpgrades {
    public static final String PATTERN_PROVIDER_NAME = "me_pattern_provider";
    public static final String PASSIVE_CRAFTING_NAME = "me_passive_crafting";
    public static final String OUTPUT_INTERFACE_NAME = "me_output_interface";

    private MeMekanismUpgrades() {
    }

    public static Upgrade patternProvider() {
        return Upgrade.valueOf("ME_PATTERN_PROVIDER");
    }

    public static Upgrade passiveCrafting() {
        return Upgrade.valueOf("ME_PASSIVE_CRAFTING");
    }

    public static Upgrade outputInterface() {
        return Upgrade.valueOf("ME_OUTPUT_INTERFACE");
    }

    public static Upgrade forType(MeUpgradeType type) {
        return switch (type) {
            case PATTERN_PROVIDER -> patternProvider();
            case PASSIVE_CRAFTING -> passiveCrafting();
            case OUTPUT_INTERFACE -> outputInterface();
        };
    }

    public static MeUpgradeType toType(Upgrade upgrade) {
        if (upgrade == patternProvider()) {
            return MeUpgradeType.PATTERN_PROVIDER;
        }
        if (upgrade == passiveCrafting()) {
            return MeUpgradeType.PASSIVE_CRAFTING;
        }
        if (upgrade == outputInterface()) {
            return MeUpgradeType.OUTPUT_INTERFACE;
        }
        return null;
    }
}
