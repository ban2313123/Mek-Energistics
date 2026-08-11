package com.beipuo.mekenergistics.upgrade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

class MeUpgradeDataMigrationTest {
    @Test
    void canonicalTagTakesPriorityOverLegacyTags() {
        CompoundTag tag = new CompoundTag();
        CompoundTag canonical = new CompoundTag();
        canonical.putInt("schema", MeUpgradePersistence.SCHEMA);
        canonical.putInt("me_pattern_provider", 1);
        tag.put(MeUpgradePersistence.TAG_ME_UPGRADES, canonical);
        tag.put(MeUpgradeDataMigration.LEGACY_ROOT_TAG, legacyRoot(0, 1));

        MeUpgradeDataMigration.MeUpgradeMigrationResult result = MeUpgradeDataMigration.migrate(tag);
        assertFalse(result.migratedFromLegacy());
        assertEquals(1, result.data().count(MeUpgradeType.PATTERN_PROVIDER));
        assertEquals(0, result.data().count(MeUpgradeType.PASSIVE_CRAFTING));
    }

    @Test
    void migratesFromCurrent3xRootTag() {
        CompoundTag tag = new CompoundTag();
        tag.put(MeUpgradeDataMigration.LEGACY_ROOT_TAG, legacyRoot(1, 1));
        MeUpgradeDataMigration.MeUpgradeMigrationResult result = MeUpgradeDataMigration.migrate(tag);
        assertTrue(result.migratedFromLegacy());
        assertEquals(1, result.data().count(MeUpgradeType.PATTERN_PROVIDER));
        assertEquals(1, result.data().count(MeUpgradeType.PASSIVE_CRAFTING));
    }

    @Test
    void migratesFromMekanismComponentNestedTag() {
        CompoundTag tag = new CompoundTag();
        CompoundTag components = new CompoundTag();
        components.put(MeUpgradeDataMigration.LEGACY_ROOT_TAG, legacyRoot(1, 0));
        tag.put("mekanism:components", components);
        MeUpgradeDataMigration.MeUpgradeMigrationResult result = MeUpgradeDataMigration.migrate(tag);
        assertTrue(result.migratedFromLegacy());
        assertEquals(1, result.data().count(MeUpgradeType.PATTERN_PROVIDER));
    }

    @Test
    void migratesFromEmpoweredAndAncientTags() {
        CompoundTag empowered = legacyRoot(1, 0);
        empowered.putInt("some_empowered_upgrade", 2);
        CompoundTag tag = new CompoundTag();
        tag.put(MeUpgradeDataMigration.EMPOWERED_TAG, empowered);
        MeUpgradeDataMigration.MeUpgradeMigrationResult result = MeUpgradeDataMigration.migrate(tag);
        assertTrue(result.migratedFromLegacy());
        assertEquals(1, result.data().count(MeUpgradeType.PATTERN_PROVIDER));
        assertEquals(2, result.data().preserved().get("some_empowered_upgrade"));

        CompoundTag ancient = new CompoundTag();
        ancient.putInt("me_passive_crafting", 1);
        tag = new CompoundTag();
        tag.put(MeUpgradeDataMigration.ANCIENT_TAG, ancient);
        result = MeUpgradeDataMigration.migrate(tag);
        assertTrue(result.migratedFromLegacy());
        assertEquals(1, result.data().count(MeUpgradeType.PASSIVE_CRAFTING));
    }

    @Test
    void ordinalSevenIsNeverInterpretedAsAnMeUpgrade() {
        // A native Mekanism upgrade list encoded by ordinal: type 7 must not become
        // "me_pattern_provider" and the raw list must survive in the external copy.
        CompoundTag nativeList = new CompoundTag();
        nativeList.putInt("0", 1);
        nativeList.putInt("7", 1);
        CompoundTag tag = new CompoundTag();
        tag.put("upgrades", nativeList);

        MeUpgradeDataMigration.MeUpgradeMigrationResult result = MeUpgradeDataMigration.migrate(tag);
        assertFalse(result.migratedFromLegacy());
        assertEquals(0, result.data().count(MeUpgradeType.PATTERN_PROVIDER));
        assertEquals(0, result.data().count(MeUpgradeType.PASSIVE_CRAFTING));
        assertTrue(result.externalUpgrades().contains("upgrades"));
        assertEquals(1, result.externalUpgrades().getCompound("upgrades").getInt("7"));
    }

    @Test
    void externalCopyHasClaimedMeNamesRemoved() {
        CompoundTag legacy = legacyRoot(1, 1);
        legacy.putInt("vanilla_upgrade", 5);
        CompoundTag tag = new CompoundTag();
        tag.put(MeUpgradeDataMigration.LEGACY_ROOT_TAG, legacy);

        MeUpgradeDataMigration.MeUpgradeMigrationResult result = MeUpgradeDataMigration.migrate(tag);
        CompoundTag external = result.externalUpgrades().getCompound(MeUpgradeDataMigration.LEGACY_ROOT_TAG);
        assertFalse(external.contains("me_pattern_provider"));
        assertFalse(external.contains("me_passive_crafting"));
        assertEquals(5, external.getInt("vanilla_upgrade"));
    }

    @Test
    void legacyUnknownNamesArePreservedNotDeleted() {
        CompoundTag legacy = legacyRoot(0, 0);
        legacy.putInt("mystery_extra", 3);
        CompoundTag tag = new CompoundTag();
        tag.put(MeUpgradeDataMigration.LEGACY_ROOT_TAG, legacy);
        MeUpgradeDataMigration.MeUpgradeMigrationResult result = MeUpgradeDataMigration.migrate(tag);
        assertEquals(3, result.data().preserved().get("mystery_extra"));
    }

    private static CompoundTag legacyRoot(int patternProvider, int passiveCrafting) {
        CompoundTag root = new CompoundTag();
        if (patternProvider > 0) {
            root.putInt("me_pattern_provider", patternProvider);
        }
        if (passiveCrafting > 0) {
            root.putInt("me_passive_crafting", passiveCrafting);
        }
        return root;
    }
}
