package com.beipuo.mekenergistics.blockentity.api;

import mekanism.common.lib.transmitter.TransmissionType;

public enum AeOutputMode {
    // Keep the original four entries in the same order for old NBT.
    BOTH("AE: Item/Chem", true, true, false),
    ITEMS("AE: Item", true, false, false),
    CHEMICALS("AE: Chem", false, true, false),
    NONE("AE: Off", false, false, false),
    ALL("AE: All", true, true, true),
    ITEMS_FLUIDS("AE: Item/Fluid", true, false, true),
    CHEMICALS_FLUIDS("AE: Chem/Fluid", false, true, true),
    FLUIDS("AE: Fluid", false, false, true);

    private static final AeOutputMode[] VALUES = values();
    private final String label;
    private final boolean items;
    private final boolean chemicals;
    private final boolean fluids;

    AeOutputMode(String label, boolean items, boolean chemicals, boolean fluids) {
        this.label = label;
        this.items = items;
        this.chemicals = chemicals;
        this.fluids = fluids;
    }

    public String label() {
        return this.label;
    }

    public boolean items() {
        return this.items;
    }

    public boolean chemicals() {
        return this.chemicals;
    }

    public boolean fluids() {
        return this.fluids;
    }

    public AeOutputMode next() {
        return VALUES[(ordinal() + 1) % VALUES.length];
    }

    public AeOutputMode toggle(TransmissionType type) {
        return switch (type) {
            case ITEM -> byFlags(!this.items, this.chemicals, this.fluids);
            case CHEMICAL -> byFlags(this.items, !this.chemicals, this.fluids);
            case FLUID -> byFlags(this.items, this.chemicals, !this.fluids);
            default -> this;
        };
    }

    private static AeOutputMode byFlags(boolean items, boolean chemicals, boolean fluids) {
        for (AeOutputMode mode : VALUES) {
            if (mode.items == items && mode.chemicals == chemicals && mode.fluids == fluids) {
                return mode;
            }
        }
        return BOTH;
    }

    public static AeOutputMode byId(int id) {
        return id < 0 || id >= VALUES.length ? BOTH : VALUES[id];
    }
}
