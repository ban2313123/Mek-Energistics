package com.beipuo.mekenergistics.blockentity.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import appeng.api.stacks.AEKey;
import com.beipuo.mekenergistics.blockentity.api.AeOutputMode;
import com.beipuo.mekenergistics.testfixture.FakeKey;
import com.beipuo.mekenergistics.testfixture.FakeOutputPort;
import java.util.List;
import java.util.function.BiFunction;
import org.junit.jupiter.api.Test;

/**
 * Pushing a machine's finished output into the ME network. The network insert commits before the
 * machine is drained, so the two amounts have to agree exactly: crediting the network more than
 * leaves the machine duplicates material, and draining more than the network took voids it.
 *
 * <p>A {@link FakeKey} is neither an item nor a fluid key, so it takes the chemical branch of the
 * output-mode gate.
 */
class MeOutputDrainTest {
    private static final AEKey CHEMICAL = new FakeKey("hydrogen");

    /** Records what the network was asked to take, and accepts up to a fixed ceiling. */
    private static final class RecordingNetwork implements BiFunction<AEKey, Long, Long> {
        private final long accepts;
        private long inserted;
        private int calls;

        private RecordingNetwork(long accepts) {
            this.accepts = accepts;
        }

        @Override
        public Long apply(AEKey key, Long amount) {
            this.calls++;
            long taken = Math.min(amount, this.accepts);
            this.inserted += taken;
            return taken;
        }
    }

    @Test
    void anAcceptedOutputLeavesTheMachineByExactlyTheAmountTheNetworkTook() {
        FakeOutputPort port = new FakeOutputPort(CHEMICAL, 500);
        RecordingNetwork network = new RecordingNetwork(Long.MAX_VALUE);

        assertTrue(AbstractMeAeSupport.drainOutputPorts(AeOutputMode.ALL, List.of(port), network));

        assertEquals(500, network.inserted, "the network should have been credited the whole tank");
        assertEquals(0, port.amountLeft(), "and the tank should be empty");
    }

    @Test
    void aPartiallyFullNetworkOnlyDrainsWhatItAccepted() {
        FakeOutputPort port = new FakeOutputPort(CHEMICAL, 500);
        RecordingNetwork network = new RecordingNetwork(120);

        assertTrue(AbstractMeAeSupport.drainOutputPorts(AeOutputMode.ALL, List.of(port), network));

        assertEquals(120, network.inserted);
        assertEquals(380, port.amountLeft(), "the rest must stay in the machine");
    }

    @Test
    void aNetworkClaimingMoreThanWasOfferedCannotOverdrainTheMachine() {
        FakeOutputPort port = new FakeOutputPort(CHEMICAL, 100);
        BiFunction<AEKey, Long, Long> overreporting = (key, amount) -> amount + 1_000;

        assertTrue(AbstractMeAeSupport.drainOutputPorts(AeOutputMode.ALL, List.of(port), overreporting));

        assertEquals(0, port.amountLeft(), "at most the offered amount may leave");
    }

    @Test
    void aNegativeInsertResultIsTreatedAsHavingTakenNothing() {
        FakeOutputPort port = new FakeOutputPort(CHEMICAL, 100);
        BiFunction<AEKey, Long, Long> broken = (key, amount) -> -5L;

        assertFalse(AbstractMeAeSupport.drainOutputPorts(AeOutputMode.ALL, List.of(port), broken));

        assertEquals(100, port.amountLeft(), "nothing may leave the machine");
    }

    @Test
    void anOutputTheModeDisablesIsNeverOfferedToTheNetwork() {
        FakeOutputPort port = new FakeOutputPort(CHEMICAL, 500);
        RecordingNetwork network = new RecordingNetwork(Long.MAX_VALUE);

        assertFalse(AbstractMeAeSupport.drainOutputPorts(AeOutputMode.ITEMS, List.of(port), network));

        assertEquals(0, network.calls, "a disabled output must not even be offered");
        assertEquals(500, port.amountLeft());
    }

    @Test
    void emptyAndUnsetPortsAreSkipped() {
        FakeOutputPort empty = new FakeOutputPort(CHEMICAL, 0);
        FakeOutputPort unset = new FakeOutputPort(null, 500);
        RecordingNetwork network = new RecordingNetwork(Long.MAX_VALUE);

        assertFalse(AbstractMeAeSupport.drainOutputPorts(AeOutputMode.ALL, List.of(empty, unset), network));

        assertEquals(0, network.calls);
    }

    @Test
    void everyEnabledPortIsDrainedInOnePass() {
        FakeOutputPort first = new FakeOutputPort(CHEMICAL, 60);
        FakeOutputPort second = new FakeOutputPort(CHEMICAL, 40);
        RecordingNetwork network = new RecordingNetwork(Long.MAX_VALUE);

        assertTrue(AbstractMeAeSupport.drainOutputPorts(AeOutputMode.ALL, List.of(first, second), network));

        assertEquals(100, network.inserted);
        assertEquals(0, first.amountLeft());
        assertEquals(0, second.amountLeft());
    }

    /**
     * The network insert commits before the machine is drained, so an extraction that came up short
     * would duplicate the difference. What rules that out is that the loop never asks a port for
     * more than the port itself reported — the production ports always satisfy a request within
     * their contents. Pin that precondition, since it is what keeps the ordering safe.
     */
    @Test
    void aPortIsNeverAskedToGiveUpMoreThanItAdvertised() {
        FakeOutputPort port = new FakeOutputPort(CHEMICAL, 100);
        BiFunction<AEKey, Long, Long> overreporting = (key, amount) -> amount + 1_000;

        AbstractMeAeSupport.drainOutputPorts(AeOutputMode.ALL, List.of(port), overreporting);

        assertTrue(port.largestRequest() <= 100,
                () -> "asked the port for " + port.largestRequest() + " when it held 100");
    }
}
