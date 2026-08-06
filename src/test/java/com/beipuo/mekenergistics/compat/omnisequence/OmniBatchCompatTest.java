package com.beipuo.mekenergistics.compat.omnisequence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import com.atir.molecularmanipulator.api.crafting.OmniBatchProbe;
import com.atir.molecularmanipulator.api.crafting.OmniBatchRequest;
import com.beipuo.mekenergistics.testfixture.FakeKey;
import java.util.List;
import java.util.UUID;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

class OmniBatchCompatTest {
    private static final AEKey IRON = new FakeKey("iron");
    private static final AEKey COPPER = new FakeKey("copper");
    private static final AEKey OUTPUT = new FakeKey("output");

    @Test
    void probePreservesSlotsAndMultipleSubstitutedKeys() {
        TestPattern pattern = new TestPattern(2);
        var counters = OmniBatchCompat.toCounters(pattern, List.of(
                new OmniBatchProbe.Input(0, IRON, 2),
                new OmniBatchProbe.Input(0, COPPER, 3),
                new OmniBatchProbe.Input(1, IRON, 5)));

        assertEquals(2, counters.length);
        assertEquals(2, counters[0].get(IRON));
        assertEquals(3, counters[0].get(COPPER));
        assertEquals(5, counters[1].get(IRON));
    }

    @Test
    void deliveryUsesAuthoritativeTotalsInsteadOfMultiplyingTheProbe() {
        TestPattern pattern = new TestPattern(1);
        OmniBatchRequest request = new OmniBatchRequest(
                UUID.randomUUID(), UUID.randomUUID(), pattern, 10,
                List.of(new OmniBatchRequest.Input(0, COPPER, 37)),
                List.of(new GenericStack(OUTPUT, 10)));

        var counters = OmniBatchCompat.toCounters(pattern, request);

        assertEquals(37, counters[0].get(COPPER));
        assertEquals(0, counters[0].get(IRON));
    }

    @Test
    void malformedOrIncompleteSlotLayoutsAreRejected() {
        TestPattern pattern = new TestPattern(2);

        assertNull(OmniBatchCompat.toCounters(pattern,
                List.of(new OmniBatchProbe.Input(0, IRON, 1))));
        assertNull(OmniBatchCompat.toCounters(pattern,
                List.of(new OmniBatchProbe.Input(2, IRON, 1),
                        new OmniBatchProbe.Input(1, COPPER, 1))));
    }

    private static final class TestPattern implements IPatternDetails {
        private final IInput[] inputs;

        private TestPattern(int slots) {
            this.inputs = new IInput[slots];
            for (int i = 0; i < slots; i++) {
                this.inputs[i] = new TestInput();
            }
        }

        @Override
        public appeng.api.stacks.AEItemKey getDefinition() {
            return null;
        }

        @Override
        public IInput[] getInputs() {
            return inputs;
        }

        @Override
        public List<GenericStack> getOutputs() {
            return List.of(new GenericStack(OUTPUT, 1));
        }
    }

    private static final class TestInput implements IPatternDetails.IInput {
        @Override
        public GenericStack[] getPossibleInputs() {
            return new GenericStack[] {new GenericStack(IRON, 1), new GenericStack(COPPER, 1)};
        }

        @Override
        public long getMultiplier() {
            return 1;
        }

        @Override
        public boolean isValid(AEKey input, Level level) {
            return IRON.equals(input) || COPPER.equals(input);
        }

        @Override
        public AEKey getRemainingKey(AEKey template) {
            return null;
        }
    }
}
