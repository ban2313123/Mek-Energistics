package com.beipuo.mekenergistics.upgrade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

class MePatternProviderUpgradeTest {
    @Test
    void selectsExactlyOneUpgradeBackend() {
        assertEquals(MePatternProviderUpgrade.Backend.STANDALONE_MIXIN,
                MePatternProviderUpgrade.backend(false));
        assertEquals(MePatternProviderUpgrade.Backend.EMPOWERED_CORE,
                MePatternProviderUpgrade.backend(true));
    }

    @Test
    void standaloneCountUsesStableStringKey() {
        CompoundTag tag = new CompoundTag();

        StandaloneUpgradePersistence.saveCount(tag, 1);

        assertEquals(1, StandaloneUpgradePersistence.loadCount(tag));
        assertEquals(1, tag.getCompound(StandaloneUpgradePersistence.TAG_UPGRADES)
                .getInt(MePatternProviderUpgrade.SERIALIZED_NAME));
    }

    @Test
    void standaloneCountRemovesEmptyStateAndRejectsNegativeData() {
        CompoundTag tag = new CompoundTag();
        StandaloneUpgradePersistence.saveCount(tag, 1);
        StandaloneUpgradePersistence.saveCount(tag, 0);
        assertFalse(tag.contains(StandaloneUpgradePersistence.TAG_UPGRADES));

        CompoundTag malformed = new CompoundTag();
        CompoundTag upgrades = new CompoundTag();
        upgrades.putInt(MePatternProviderUpgrade.SERIALIZED_NAME, -3);
        malformed.put(StandaloneUpgradePersistence.TAG_UPGRADES, upgrades);
        assertEquals(0, StandaloneUpgradePersistence.loadCount(malformed));
    }

    @Test
    void readsEmpoweredCoreTagWhenTheOptionalBackendIsPresent() {
        CompoundTag tag = new CompoundTag();
        CompoundTag empowered = new CompoundTag();
        empowered.putInt(MePatternProviderUpgrade.SERIALIZED_NAME, 1);
        tag.put("mekanism_empowered_core:additional_upgrades", empowered);

        assertEquals(1, StandaloneUpgradePersistence.loadEmpoweredCount(tag));
        assertEquals(1, StandaloneUpgradePersistence.loadCount(tag));
    }
}
