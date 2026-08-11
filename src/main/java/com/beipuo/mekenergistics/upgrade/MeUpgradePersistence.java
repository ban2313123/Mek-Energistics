package com.beipuo.mekenergistics.upgrade;

import com.mojang.serialization.Codec;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * Canonical persistence for {@link MeUpgradeData}. The same codec backs world NBT and the
 * {@code mekenergistics:me_upgrades} item data component.
 *
 * <p>Layout (schema 1):</p>
 * <pre>
 * mekenergistics:me_upgrades: {
 *   schema: 1,
 *   me_pattern_provider: 1,
 *   me_passive_crafting: 0,
 *   me_output_interface: 0
 * }
 * </pre>
 */
public final class MeUpgradePersistence {
    public static final String TAG_ME_UPGRADES = "mekenergistics:me_upgrades";
    public static final int SCHEMA = 1;
    private static final String TAG_SCHEMA = "schema";
    private static final String TAG_PRESERVED = "preserved";

    public static final Codec<MeUpgradeData> CODEC = Codec.unboundedMap(Codec.STRING, Codec.INT)
            .xmap(MeUpgradeData::fromFlatMap, MeUpgradeData::toFlatMap);

    public static final StreamCodec<RegistryFriendlyByteBuf, MeUpgradeData> STREAM_CODEC = StreamCodec.of(
            MeUpgradePersistence::encode,
            MeUpgradePersistence::decode);

    private MeUpgradePersistence() {
    }

    /** Writes the canonical tag into the given world/block-entity NBT. */
    public static CompoundTag save(CompoundTag tag, MeUpgradeData data) {
        CompoundTag root = new CompoundTag();
        root.putInt(TAG_SCHEMA, SCHEMA);
        for (MeUpgradeType type : MeUpgradeType.values()) {
            int count = data.count(type);
            if (count > 0) {
                root.putInt(type.getSerializedName(), count);
            }
        }
        if (!data.preserved().isEmpty()) {
            CompoundTag preserved = new CompoundTag();
            data.preserved().forEach(preserved::putInt);
            root.put(TAG_PRESERVED, preserved);
        }
        if (root.size() == 1) {
            tag.remove(TAG_ME_UPGRADES);
        } else {
            tag.put(TAG_ME_UPGRADES, root);
        }
        return tag;
    }

    /** Reads the canonical tag. Unknown names are preserved; negatives become zero; over-limit
     * counts are clamped. Returns {@link MeUpgradeData#EMPTY} when no canonical tag exists. */
    public static MeUpgradeData load(CompoundTag tag) {
        if (tag == null || !tag.contains(TAG_ME_UPGRADES, Tag.TAG_COMPOUND)) {
            return MeUpgradeData.EMPTY;
        }
        CompoundTag root = tag.getCompound(TAG_ME_UPGRADES);
        Map<String, Integer> flat = new LinkedHashMap<>();
        for (String key : root.getAllKeys()) {
            if (key.equals(TAG_SCHEMA) || key.equals(TAG_PRESERVED)) {
                continue;
            }
            flat.put(key, root.getInt(key));
        }
        if (root.contains(TAG_PRESERVED, Tag.TAG_COMPOUND)) {
            CompoundTag preserved = root.getCompound(TAG_PRESERVED);
            for (String key : preserved.getAllKeys()) {
                flat.put(key, preserved.getInt(key));
            }
        }
        return MeUpgradeData.fromFlatMap(flat);
    }

    private static void encode(RegistryFriendlyByteBuf buffer, MeUpgradeData data) {
        Map<String, Integer> flat = data.toFlatMap();
        buffer.writeVarInt(flat.size());
        for (Map.Entry<String, Integer> entry : flat.entrySet()) {
            buffer.writeUtf(entry.getKey());
            buffer.writeVarInt(entry.getValue());
        }
    }

    private static MeUpgradeData decode(RegistryFriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        Map<String, Integer> flat = new LinkedHashMap<>();
        for (int i = 0; i < size; i++) {
            flat.put(buffer.readUtf(), buffer.readVarInt());
        }
        return MeUpgradeData.fromFlatMap(flat);
    }
}

