package com.beipuo.mekenergistics.client.screen.machine;

/**
 * Shared geometry for factory screens whose width grows with the tier.
 *
 * <p>These formulas come from Mekanism Extras' layout. They are approximate: at large tier indices
 * they can drift by a pixel or two, which has never been verified against the real textures. That
 * caveat used to be repeated as a comment at six call sites across two files, where it had already
 * drifted -- one copy said "细微的便宜" (slightly cheap) instead of "细微的偏移" (slight offset).
 *
 * <p>All methods take a zero-based tier index. Extras tier enums are already zero-based; screens
 * driven by Mekanism's own {@code FactoryTier} reach these tiers at ordinal 4 and must subtract
 * {@link #MEKANISM_TIER_OFFSET} first.
 */
public final class MeFactoryGuiLayout {
    /** Ordinal of the first tier in Mekanism's FactoryTier that these formulas apply to. */
    public static final int MEKANISM_TIER_OFFSET = 4;

    private MeFactoryGuiLayout() {
    }

    public static int imageWidthDelta(int tierIndex) {
        return (36 * (tierIndex + 2)) + (2 * tierIndex);
    }

    public static int inventoryLabelX(int tierIndex) {
        return (22 * (tierIndex + 2)) - (3 * tierIndex);
    }

    public static int barWidth(int tierIndex) {
        return 210 + 38 * tierIndex;
    }

    public static int buttonX(int tierIndex) {
        return 220 + 38 * tierIndex;
    }
}
