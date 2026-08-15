package com.beipuo.mekenergistics.blockentity.support.io;

import com.beipuo.mekenergistics.blockentity.api.AeOutputMode;

/** Resolves the metallurgic infuser's mutually exclusive reaction and conversion roles. */
public final class MeInfusionModePolicy {
    private MeInfusionModePolicy() {
    }

    /** Chemical-only output selects conversion; modes that also export items remain reaction mode. */
    public static boolean isConversionMode(AeOutputMode mode) {
        return mode != null && mode.chemicals() && !mode.items();
    }

    /** Prevents a reaction-mode infuser from exporting its chemical input tank. */
    public static AeOutputMode effectiveOutputMode(AeOutputMode mode) {
        if (mode == null) {
            return AeOutputMode.NONE;
        }
        if (mode.items()) {
            return AeOutputMode.ITEMS;
        }
        return mode.chemicals() ? AeOutputMode.CHEMICALS : AeOutputMode.NONE;
    }
}
