package com.beipuo.mekenergistics.upgrade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

class MeUpgradePersistenceTest {
    @Test
    void canonicalRoundTripIsSemanticallyEquivalent() {
        CompoundTag tag = new CompoundTag();
        MeUpgradeData data = MeUpgradeData.EMPTY.with(MeUpgradeType.PATTERN_PROVIDER, 1);
        MeUpgradePersistence.save(tag, data);
        MeUpgradeData reloaded = MeUpgradePersistence.load(tag);
        assertEquals(1, reloaded.count(MeUpgradeType.PATTERN_PROVIDER));
        assertTrue(tag.contains(MeUpgradePersistence.TAG_ME_UPGRADES, 10));
        assertEquals(MeUpgradePersistence.SCHEMA,
                tag.getCompound(MeUpgradePersistence.TAG_ME_UPGRADES).getInt("schema"));
    }

    @Test
    void negativeCountsBecomeZero() {
        CompoundTag tag = new CompoundTag();
        CompoundTag root = new CompoundTag();
        root.putInt("schema", MeUpgradePersistence.SCHEMA);
        root.putInt("me_pattern_provider", -5);
        tag.put(MeUpgradePersistence.TAG_ME_UPGRADES, root);
        MeUpgradeData data = MeUpgradePersistence.load(tag);
        assertEquals(0, data.count(MeUpgradeType.PATTERN_PROVIDER));
    }

    @Test
    void overLimitCountsAreClamped() {
        CompoundTag tag = new CompoundTag();
        CompoundTag root = new CompoundTag();
        root.putInt("schema", MeUpgradePersistence.SCHEMA);
        root.putInt("me_pattern_provider", 7);
        tag.put(MeUpgradePersistence.TAG_ME_UPGRADES, root);
        MeUpgradeData data = MeUpgradePersistence.load(tag);
        assertEquals(MeUpgradeType.PATTERN_PROVIDER.getMaxCount(), data.count(MeUpgradeType.PATTERN_PROVIDER));
    }

    @Test
    void unknownNamesArePreservedNotDropped() {
        CompoundTag tag = new CompoundTag();
        CompoundTag root = new CompoundTag();
        root.putInt("schema", MeUpgradePersistence.SCHEMA);
        root.putInt("me_pattern_provider", 1);
        root.putInt("other_mod:upgrade", 4);
        tag.put(MeUpgradePersistence.TAG_ME_UPGRADES, root);
        MeUpgradeData data = MeUpgradePersistence.load(tag);
        assertEquals(4, data.preserved().get("other_mod:upgrade"));
    }

    @Test
    void saveRemovesCanonicalTagWhenStateIsEmpty() {
        CompoundTag tag = new CompoundTag();
        MeUpgradePersistence.save(tag, MeUpgradeData.EMPTY.with(MeUpgradeType.PATTERN_PROVIDER, 1));
        assertTrue(tag.contains(MeUpgradePersistence.TAG_ME_UPGRADES));
        MeUpgradePersistence.save(tag, MeUpgradeData.EMPTY);
        assertFalse(tag.contains(MeUpgradePersistence.TAG_ME_UPGRADES));
    }
}
