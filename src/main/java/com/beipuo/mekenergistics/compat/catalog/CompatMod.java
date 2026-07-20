package com.beipuo.mekenergistics.compat.catalog;

public enum CompatMod {
    MEKANISM("mekanism"),
    MEKMM("mekmm"),
    MEKE("mekanism_extras"),
    EMEK("evolvedmekanism"),
    EMEKE("emextras");

    private final String modId;

    CompatMod(String modId) {
        this.modId = modId;
    }

    public String modId() {
        return this.modId;
    }

    public static CompatMod byModId(String modId) {
        if (modId == null) {
            return MEKANISM;
        }
        for (CompatMod provider : values()) {
            if (provider.modId.equals(modId)) {
                return provider;
            }
        }
        throw new IllegalArgumentException("Unknown compatibility mod: " + modId);
    }
}
