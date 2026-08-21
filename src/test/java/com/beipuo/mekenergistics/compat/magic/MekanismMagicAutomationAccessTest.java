package com.beipuo.mekenergistics.compat.magic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.mekanismmagic.api.IMekanismMagicAutomation;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.inventory.IInventorySlot;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Drives the real reflective accessor against a stand-in of Magic's published API. A passing test
 * means the shipped method names and return shapes still match what Mek Energistics invokes.
 */
class MekanismMagicAutomationAccessTest {
    @BeforeEach
    void installApi() {
        MekanismMagicAutomationAccess.resetForTest();
        MekanismMagicAutomationAccess.installApiForTest(IMekanismMagicAutomation.class);
    }

    @AfterEach
    void clearApi() {
        MekanismMagicAutomationAccess.resetForTest();
    }

    @Test
    void detectsHostsAndReadsPatternPortsThroughPublishedMethods() {
        FakeEnergy energy = new FakeEnergy();
        // Empty lists still prove the reflective call path; inventory slot identity is checked by
        // inventorySlots() accepting only IInventorySlot entries from the real API return type.
        RecordingHost host = new RecordingHost(List.of(), List.of(), energy, true, false);

        assertTrue(MekanismMagicAutomationAccess.isMagicAutomation(host));
        assertTrue(MekanismMagicAutomationAccess.supportsPatternAutomation(host));
        assertEquals(List.of(), MekanismMagicAutomationAccess.patternInputs(host));
        assertEquals(List.of(), MekanismMagicAutomationAccess.patternOutputs(host));
        assertSame(energy, MekanismMagicAutomationAccess.energyContainer(host));
        assertFalse(MekanismMagicAutomationAccess.isBusy(host));

        assertEquals(1, host.supportsCalls.get());
        assertEquals(1, host.inputCalls.get());
        assertEquals(1, host.outputCalls.get());
        assertEquals(1, host.energyCalls.get());
        assertEquals(1, host.busyCalls.get());
    }

    @Test
    void patternAutomationFalseRejectsHostsTheApiMarksUnsupported() {
        RecordingHost host = new RecordingHost(List.of(), List.of(), new FakeEnergy(), false, true);

        assertTrue(MekanismMagicAutomationAccess.isMagicAutomation(host));
        assertFalse(MekanismMagicAutomationAccess.supportsPatternAutomation(host));
        assertTrue(MekanismMagicAutomationAccess.isBusy(host));
    }

    @Test
    void nonMagicObjectsNeverLookLikeAutomationHosts() {
        assertFalse(MekanismMagicAutomationAccess.isMagicAutomation("not-a-tile"));
        assertFalse(MekanismMagicAutomationAccess.supportsPatternAutomation("not-a-tile"));
        assertTrue(MekanismMagicAutomationAccess.patternInputs("not-a-tile").isEmpty());
        assertTrue(MekanismMagicAutomationAccess.patternOutputs("not-a-tile").isEmpty());
        assertNull(MekanismMagicAutomationAccess.energyContainer("not-a-tile"));
    }

    private static final class RecordingHost implements IMekanismMagicAutomation {
        private final List<IInventorySlot> inputs;
        private final List<IInventorySlot> outputs;
        private final IEnergyContainer energy;
        private final boolean supports;
        private final boolean busy;
        private final AtomicInteger supportsCalls = new AtomicInteger();
        private final AtomicInteger inputCalls = new AtomicInteger();
        private final AtomicInteger outputCalls = new AtomicInteger();
        private final AtomicInteger energyCalls = new AtomicInteger();
        private final AtomicInteger busyCalls = new AtomicInteger();

        private RecordingHost(List<IInventorySlot> inputs, List<IInventorySlot> outputs,
                IEnergyContainer energy, boolean supports, boolean busy) {
            this.inputs = inputs;
            this.outputs = outputs;
            this.energy = energy;
            this.supports = supports;
            this.busy = busy;
        }

        @Override
        public ResourceLocation mekanismMagicMachineId() {
            return ResourceLocation.parse("mekanism_magic:test_host");
        }

        @Override
        public List<IInventorySlot> mekanismMagicPatternInputs() {
            inputCalls.incrementAndGet();
            return inputs;
        }

        @Override
        public List<IInventorySlot> mekanismMagicPatternOutputs() {
            outputCalls.incrementAndGet();
            return outputs;
        }

        @Override
        public IEnergyContainer mekanismMagicEnergyContainer() {
            energyCalls.incrementAndGet();
            return energy;
        }

        @Override
        public boolean mekanismMagicSupportsPatternAutomation() {
            supportsCalls.incrementAndGet();
            return supports;
        }

        @Override
        public boolean mekanismMagicIsBusy() {
            busyCalls.incrementAndGet();
            return busy;
        }
    }

    private static final class FakeEnergy implements IEnergyContainer {
        @Override
        public long getEnergy() {
            return 0;
        }

        @Override
        public void setEnergy(long energy) {
        }

        @Override
        public long getMaxEnergy() {
            return 0;
        }

        @Override
        public long getNeeded() {
            return 0;
        }

        @Override
        public long insert(long amount, Action action, AutomationType automationType) {
            return 0;
        }

        @Override
        public long extract(long amount, Action action, AutomationType automationType) {
            return 0;
        }

        @Override
        public CompoundTag serializeNBT(HolderLookup.Provider provider) {
            return new CompoundTag();
        }

        @Override
        public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        }

        @Override
        public void onContentsChanged() {
        }
    }
}
