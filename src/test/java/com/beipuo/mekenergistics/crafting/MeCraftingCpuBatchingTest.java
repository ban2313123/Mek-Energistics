package com.beipuo.mekenergistics.crafting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.crafting.inv.ListCraftingInventory;
import com.beipuo.mekenergistics.mixin.ae2.CraftingTaskProgressAccessor;
import com.beipuo.mekenergistics.mixin.ae2.ElapsedTimeTrackerAccessor;
import com.beipuo.mekenergistics.testfixture.FakeKey;
import java.util.List;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

class MeCraftingCpuBatchingTest {
    @Test
    void doesNotBatchTheOnlyRemainingTask() {
        TestPattern pattern = new TestPattern(2, 3);

        assertEquals(0, MeCraftingCpuBatching.maxAdditionalCopies(pattern, 1));
    }

    @Test
    void batchesOnlyTheCopiesStillRecordedByTheCpuTask() {
        TestPattern pattern = new TestPattern(2, 3);

        assertEquals(99, MeCraftingCpuBatching.maxAdditionalCopies(pattern, 100));
    }

    @Test
    void doesNotClampSafeBatchesToOneMillionCopies() {
        TestPattern pattern = new TestPattern(1, 1);

        assertEquals(1_999_999, MeCraftingCpuBatching.maxAdditionalCopies(pattern, 2_000_000));
    }

    @Test
    void scalesInputMultipliersAndOutputsWithoutChangingPatternIdentity() {
        TestPattern pattern = new TestPattern(2, 3);
        MeCraftingCpuBatching.ScaledPatternDetails scaled =
                new MeCraftingCpuBatching.ScaledPatternDetails(pattern, 5);

        assertSame(pattern.getDefinition(), scaled.getDefinition());
        assertEquals(5, scaled.getInputs()[0].getMultiplier());
        assertEquals(15, scaled.getOutputs().get(0).amount());
        assertSame(pattern.getInputs()[0].getPossibleInputs()[0], scaled.getInputs()[0].getPossibleInputs()[0]);
    }

    @Test
    void rejectsOverflowBeforeAttemptingAnUnboundedBatch() {
        TestPattern pattern = new TestPattern(Long.MAX_VALUE, Long.MAX_VALUE);

        assertEquals(0, MeCraftingCpuBatching.maxAdditionalCopies(pattern, Long.MAX_VALUE));
    }

    @Test
    void commitsEveryLedgerMutationWhenNothingFails() {
        FakeKey output = new FakeKey("output");
        ListCraftingInventory waitingFor = new ListCraftingInventory(key -> {
        });
        FakeTaskProgress taskProgress = new FakeTaskProgress(10);
        FakeEnergyService energy = new FakeEnergyService(100);
        MeCraftingCpuBatching.Batch batch = new MeCraftingCpuBatching.Batch(
                4, emptyInputs(), counter(output, 2), emptyCounter(), 5, taskProgress);

        MeCraftingCpuBatching.commitBatch(batch, waitingFor, new FakeTimeTracker(), energy);

        assertEquals(6, taskProgress.value);
        assertEquals(2, waitingFor.list.get(output));
        assertEquals(95, energy.stored, 0.0001);
    }

    @Test
    void restoresTaskProgressWaitingForAndPowerWhenOutputInsertFails() {
        FakeKey acceptedOutput = new FakeKey("accepted_output");
        FakeKey failingOutput = new FakeKey("failing_output");
        ListCraftingInventory waitingFor = new ListCraftingInventory(key -> {
            if (failingOutput.equals(key)) {
                throw new IllegalStateException("injected output insert failure");
            }
        });
        FakeTaskProgress taskProgress = new FakeTaskProgress(10);
        FakeEnergyService energy = new FakeEnergyService(100);
        KeyCounter outputs = new KeyCounter();
        outputs.add(acceptedOutput, 2);
        outputs.add(failingOutput, 3);
        MeCraftingCpuBatching.Batch batch = new MeCraftingCpuBatching.Batch(
                4, emptyInputs(), outputs, emptyCounter(), 5, taskProgress);

        assertThrows(IllegalStateException.class, () -> MeCraftingCpuBatching.commitBatch(
                batch, waitingFor, new FakeTimeTracker(), energy));

        assertEquals(10, taskProgress.value);
        assertEquals(0, waitingFor.list.get(acceptedOutput));
        assertEquals(0, waitingFor.list.get(failingOutput));
        assertEquals(0, waitingFor.list.size());
        assertEquals(100, energy.stored, 0.0001);
    }

    @Test
    void restoresTaskProgressWaitingForAndPowerWhenContainerInsertFails() {
        FakeKey output = new FakeKey("output");
        FakeKey container = new FakeKey("container");
        FakeKey existing = new FakeKey("existing");
        ListCraftingInventory waitingFor = new ListCraftingInventory(key -> {
            if (container.equals(key)) {
                throw new IllegalStateException("injected container insert failure");
            }
        });
        waitingFor.list.add(existing, 3);
        FakeTaskProgress taskProgress = new FakeTaskProgress(10);
        FakeEnergyService energy = new FakeEnergyService(100);
        MeCraftingCpuBatching.Batch batch = new MeCraftingCpuBatching.Batch(
                4, emptyInputs(), counter(output, 2), counter(container, 1), 5, taskProgress);

        assertThrows(IllegalStateException.class, () -> MeCraftingCpuBatching.commitBatch(
                batch, waitingFor, new FakeTimeTracker(), energy));

        assertEquals(10, taskProgress.value);
        assertEquals(3, waitingFor.list.get(existing));
        assertEquals(0, waitingFor.list.get(output));
        assertEquals(0, waitingFor.list.get(container));
        assertEquals(1, waitingFor.list.size());
        assertEquals(100, energy.stored, 0.0001);
    }

    @Test
    void restoresTaskProgressWaitingForAndPowerWhenTaskProgressDecrementFails() {
        FakeKey output = new FakeKey("output");
        ListCraftingInventory waitingFor = new ListCraftingInventory(key -> {
        });
        FakeTaskProgress taskProgress = new FakeTaskProgress(10) {
            @Override
            public void mekenergistics$setValue(long value) {
                if (value == 6) {
                    throw new IllegalStateException("injected task progress failure");
                }
                super.mekenergistics$setValue(value);
            }
        };
        FakeEnergyService energy = new FakeEnergyService(100);
        MeCraftingCpuBatching.Batch batch = new MeCraftingCpuBatching.Batch(
                4, emptyInputs(), counter(output, 2), emptyCounter(), 5, taskProgress);

        assertThrows(IllegalStateException.class, () -> MeCraftingCpuBatching.commitBatch(
                batch, waitingFor, new FakeTimeTracker(), energy));

        assertEquals(10, taskProgress.value);
        assertEquals(0, waitingFor.list.get(output));
        assertEquals(100, energy.stored, 0.0001);
    }

    @Test
    void restoresTaskProgressWaitingForAndPowerWhenPowerExtractFails() {
        FakeKey output = new FakeKey("output");
        ListCraftingInventory waitingFor = new ListCraftingInventory(key -> {
        });
        FakeTaskProgress taskProgress = new FakeTaskProgress(10);
        FakeEnergyService energy = new FakeEnergyService(100);
        energy.failOnExtract = true;
        MeCraftingCpuBatching.Batch batch = new MeCraftingCpuBatching.Batch(
                4, emptyInputs(), counter(output, 2), emptyCounter(), 5, taskProgress);

        assertThrows(IllegalStateException.class, () -> MeCraftingCpuBatching.commitBatch(
                batch, waitingFor, new FakeTimeTracker(), energy));

        assertEquals(10, taskProgress.value);
        assertEquals(0, waitingFor.list.get(output));
        assertEquals(100, energy.stored, 0.0001);
    }

    @Test
    void rollsBackPrePushFailureWithoutLosingOrDuplicatingInputs() {
        FakeKey input = new FakeKey("input");
        KeyCounter baseInput = new KeyCounter();
        baseInput.add(input, 12);
        KeyCounter extraInput = new KeyCounter();
        extraInput.add(input, 4);
        ListCraftingInventory inventory = new ListCraftingInventory(key -> {
        });
        MeCraftingCpuBatching.Batch batch = new MeCraftingCpuBatching.Batch(
                4, new KeyCounter[] {extraInput}, emptyCounter(), emptyCounter(), 0,
                new FakeTaskProgress(10));

        MeCraftingCpuBatching.rollbackBatch(batch, new KeyCounter[] {baseInput}, inventory);

        assertEquals(8, baseInput.get(input));
        assertEquals(4, inventory.list.get(input));
    }

    private static KeyCounter counter(AEKey key, long amount) {
        KeyCounter counter = new KeyCounter();
        counter.add(key, amount);
        return counter;
    }

    private static KeyCounter emptyCounter() {
        return new KeyCounter();
    }

    private static KeyCounter[] emptyInputs() {
        return new KeyCounter[0];
    }

    private static class FakeTaskProgress implements CraftingTaskProgressAccessor {
        private long value;

        private FakeTaskProgress(long value) {
            this.value = value;
        }

        @Override
        public long mekenergistics$getValue() {
            return this.value;
        }

        @Override
        public void mekenergistics$setValue(long value) {
            this.value = value;
        }
    }

    private static final class FakeTimeTracker implements ElapsedTimeTrackerAccessor {
        @Override
        public void mekenergistics$addMaxItems(long amount, AEKeyType keyType) {
        }
    }

    private static final class FakeEnergyService implements IEnergyService {
        private double stored;
        private boolean failOnExtract;

        private FakeEnergyService(double stored) {
            this.stored = stored;
        }

        @Override
        public double extractAEPower(double amt, Actionable mode, PowerMultiplier usePowerMultiplier) {
            double extracted = Math.min(this.stored, amt * usePowerMultiplier.multiplier);
            if (mode == Actionable.MODULATE) {
                this.stored -= extracted;
                if (this.failOnExtract) {
                    throw new IllegalStateException("injected power extract failure");
                }
            }
            return extracted;
        }

        @Override
        public double injectPower(double amt, Actionable mode) {
            if (mode == Actionable.MODULATE) {
                this.stored += amt;
            }
            return 0;
        }

        @Override
        public double getStoredPower() {
            return this.stored;
        }

        @Override
        public double getIdlePowerUsage() {
            return 0;
        }

        @Override
        public double getChannelPowerUsage() {
            return 0;
        }

        @Override
        public double getAvgPowerUsage() {
            return 0;
        }

        @Override
        public double getAvgPowerInjection() {
            return 0;
        }

        @Override
        public boolean isNetworkPowered() {
            return true;
        }

        @Override
        public double getMaxStoredPower() {
            return this.stored;
        }

        @Override
        public double getEnergyDemand(double maxRequired) {
            return 0;
        }
    }

    private static final class TestPattern implements IPatternDetails {
        private final FakeKey input = new FakeKey("input");
        private final FakeKey output = new FakeKey("output");
        private final IInput[] inputs;
        private final List<GenericStack> outputs;

        private TestPattern(long inputAmount, long outputAmount) {
            GenericStack possible = new GenericStack(this.input, inputAmount);
            this.inputs = new IInput[] {new TestInput(possible, 1)};
            this.outputs = List.of(new GenericStack(this.output, outputAmount));
        }

        @Override
        public appeng.api.stacks.AEItemKey getDefinition() {
            return null;
        }

        @Override
        public IInput[] getInputs() {
            return this.inputs;
        }

        @Override
        public List<GenericStack> getOutputs() {
            return this.outputs;
        }
    }

    private record TestInput(GenericStack possible, long multiplier) implements IPatternDetails.IInput {
        @Override
        public GenericStack[] getPossibleInputs() {
            return new GenericStack[] {this.possible};
        }

        @Override
        public long getMultiplier() {
            return this.multiplier;
        }

        @Override
        public boolean isValid(AEKey input, Level level) {
            return this.possible.what().equals(input);
        }

        @Override
        public AEKey getRemainingKey(AEKey template) {
            return null;
        }
    }

}