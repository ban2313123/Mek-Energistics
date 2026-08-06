package com.beipuo.mekenergistics.upgrade;

import net.minecraft.nbt.CompoundTag;

public final class MePassiveCraftingSettings {
    public static final int DEFAULT_INTERVAL_TICKS = 20;
    public static final int MIN_INTERVAL_TICKS = 1;
    public static final int MAX_INTERVAL_TICKS = 72_000;

    private int intervalTicks = DEFAULT_INTERVAL_TICKS;
    private long multiplier = 1;
    private int countdown = DEFAULT_INTERVAL_TICKS;

    public int intervalTicks() { return intervalTicks; }
    public long multiplier() { return multiplier; }

    public void set(int requestedIntervalTicks, long requestedMultiplier) {
        intervalTicks = Math.clamp(requestedIntervalTicks, MIN_INTERVAL_TICKS, MAX_INTERVAL_TICKS);
        multiplier = Math.max(1, requestedMultiplier);
        countdown = intervalTicks;
    }

    public boolean tick() {
        if (--countdown > 0) return false;
        countdown = intervalTicks;
        return true;
    }

    public void save(CompoundTag tag) {
        tag.putInt("PassiveCraftingInterval", intervalTicks);
        tag.putLong("PassiveCraftingMultiplier", multiplier);
    }

    public void load(CompoundTag tag) {
        set(tag.contains("PassiveCraftingInterval") ? tag.getInt("PassiveCraftingInterval") : DEFAULT_INTERVAL_TICKS,
                tag.contains("PassiveCraftingMultiplier") ? tag.getLong("PassiveCraftingMultiplier") : 1);
    }
}
