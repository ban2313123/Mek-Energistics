package com.beipuo.mekenergistics.upgrade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.beipuo.mekenergistics.testfixture.FakeKey;
import java.util.List;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

class MePassiveCraftingSettingsTest {
    @Test
    void intervalFloorAndCeilingAreEnforcedServerSide() {
        MePassiveCraftingSettings settings = new MePassiveCraftingSettings();
        settings.set(-5, 1);
        assertEquals(MePassiveCraftingSettings.MIN_INTERVAL_TICKS, settings.intervalTicks());
        settings.set(999_999, 1);
        assertEquals(MePassiveCraftingSettings.MAX_INTERVAL_TICKS, settings.intervalTicks());
    }

    @Test
    void copiesCapIsEnforcedServerSide() {
        MePassiveCraftingSettings settings = new MePassiveCraftingSettings();
        settings.set(20, Long.MAX_VALUE);
        assertEquals(MePassiveCraftingSettings.MAX_COPIES_PER_TICK, settings.multiplier());
        assertEquals(MePassiveCraftingSettings.MAX_COPIES_PER_TICK, settings.cappedCopies(10_000));
        assertEquals(1, settings.cappedCopies(-3));
        settings.set(20, -3);
        assertEquals(1, settings.multiplier());
    }

    @Test
    void loadClampsCorruptSavedValues() {
        MePassiveCraftingSettings settings = new MePassiveCraftingSettings();
        CompoundTag tag = new CompoundTag();
        tag.putInt("PassiveCraftingInterval", -100);
        tag.putLong("PassiveCraftingMultiplier", Long.MAX_VALUE);

        settings.load(tag, RegistryAccess.EMPTY);

        assertEquals(MePassiveCraftingSettings.MIN_INTERVAL_TICKS, settings.intervalTicks());
        assertEquals(MePassiveCraftingSettings.MAX_COPIES_PER_TICK, settings.multiplier());
    }

    @Test
    void recoveryBufferMergesAndDrainsWithoutLoss() {
        MePassiveCraftingSettings settings = new MePassiveCraftingSettings();
        FakeKey iron = new FakeKey("recovery_iron");

        settings.bufferRemainder(iron, 5);
        settings.bufferRemainder(iron, 7);

        assertEquals(1, settings.recoveryBufferSize());
        List<MePassiveCraftingSettings.RecoveryEntry> drained = settings.drainRecoveryBuffer();
        assertEquals(1, drained.size());
        assertEquals(12, drained.get(0).amount());
        assertFalse(settings.hasRecoverableRemainders());
    }

    @Test
    void emptyRecoveryBufferRoundTripsThroughNbt() {
        MePassiveCraftingSettings settings = new MePassiveCraftingSettings();
        settings.set(10, 3);
        CompoundTag tag = new CompoundTag();

        settings.save(tag, RegistryAccess.EMPTY);

        MePassiveCraftingSettings restored = new MePassiveCraftingSettings();
        restored.load(tag, RegistryAccess.EMPTY);
        assertEquals(10, restored.intervalTicks());
        assertEquals(3, restored.multiplier());
        assertFalse(restored.hasRecoverableRemainders());
    }
}
