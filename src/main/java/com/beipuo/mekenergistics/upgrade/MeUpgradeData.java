package com.beipuo.mekenergistics.upgrade;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Immutable snapshot of the installed ME upgrades for one machine.
 *
 * <p>Only {@link MeUpgradeType} keys carry semantics. Unknown names found in old save data are
 * preserved verbatim so a read -&gt; save -&gt; read cycle stays lossless, but they never map to a
 * known upgrade.</p>
 */
public record MeUpgradeData(Map<MeUpgradeType, Integer> counts, Map<String, Integer> preserved) {
    public static final MeUpgradeData EMPTY = new MeUpgradeData(Map.of(), Map.of());

    public MeUpgradeData {
        counts = counts == null ? Map.of() : Map.copyOf(counts);
        preserved = preserved == null ? Map.of() : Map.copyOf(preserved);
    }

    public int count(MeUpgradeType type) {
        return this.counts.getOrDefault(type, 0);
    }

    public boolean isInstalled(MeUpgradeType type) {
        return this.counts.getOrDefault(type, 0) > 0;
    }

    public boolean isEmpty() {
        return this.counts.isEmpty() && this.preserved.isEmpty();
    }

    public MeUpgradeData with(MeUpgradeType type, int count) {
        EnumMap<MeUpgradeType, Integer> next = new EnumMap<>(MeUpgradeType.class);
        next.putAll(this.counts);
        if (count <= 0) {
            next.remove(type);
        } else {
            next.put(type, Math.min(count, type.getMaxCount()));
        }
        return new MeUpgradeData(next, this.preserved);
    }

    public MeUpgradeData withPreserved(Map<String, Integer> extraPreserved) {
        if (extraPreserved == null || extraPreserved.isEmpty()) {
            return this;
        }
        Map<String, Integer> merged = new LinkedHashMap<>(this.preserved);
        merged.putAll(extraPreserved);
        return new MeUpgradeData(this.counts, merged);
    }

    /** Flattens counts and preserved names into one string-keyed map for codecs. */
    public Map<String, Integer> toFlatMap() {
        if (this.counts.isEmpty() && this.preserved.isEmpty()) {
            return Map.of();
        }
        Map<String, Integer> flat = new LinkedHashMap<>();
        for (Map.Entry<MeUpgradeType, Integer> entry : this.counts.entrySet()) {
            if (entry.getValue() != null && entry.getValue() > 0) {
                flat.put(entry.getKey().getSerializedName(), entry.getValue());
            }
        }
        flat.putAll(this.preserved);
        return Collections.unmodifiableMap(flat);
    }

    public static MeUpgradeData fromFlatMap(Map<String, Integer> flat) {
        if (flat == null || flat.isEmpty()) {
            return EMPTY;
        }
        EnumMap<MeUpgradeType, Integer> counts = new EnumMap<>(MeUpgradeType.class);
        Map<String, Integer> preserved = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : flat.entrySet()) {
            MeUpgradeType type = MeUpgradeType.bySerializedName(entry.getKey());
            if (type == null) {
                preserved.put(entry.getKey(), entry.getValue());
            } else if (entry.getValue() != null && entry.getValue() > 0) {
                counts.put(type, Math.min(entry.getValue(), type.getMaxCount()));
            }
        }
        return new MeUpgradeData(counts, preserved);
    }
}
