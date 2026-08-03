package com.beipuo.mekenergistics.crafting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
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
