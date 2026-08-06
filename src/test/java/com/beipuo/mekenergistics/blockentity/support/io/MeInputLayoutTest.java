package com.beipuo.mekenergistics.blockentity.support.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import com.beipuo.mekenergistics.testfixture.FakeInputPort;
import com.beipuo.mekenergistics.testfixture.FakeKey;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * How a machine declares where a pattern's ingredients may go. Around seventy machines build their
 * layout through these two factories, and a machine that ends up with an empty layout can never
 * accept a pattern again — silently, since nothing throws.
 *
 * <p>The two halves are mutually exclusive: unordered ports let the router put any ingredient in any
 * matching port, lanes pin each ingredient to a position. Routing and capacity must always pick the
 * same half, or the scheduler is told a batch fits and then has it refused.
 */
class MeInputLayoutTest {
    private static final AEKey IRON = new FakeKey("iron");

    private static KeyCounter[] request(long amount) {
        KeyCounter counter = new KeyCounter();
        counter.add(IRON, amount);
        return new KeyCounter[] {counter};
    }

    @Test
    void aLayoutCannotBeUnorderedAndLaneBasedAtOnce() {
        List<FakeInputPort> ports = List.of(new FakeInputPort(IRON, 64));

        assertThrows(IllegalArgumentException.class,
                () -> new MeInputLayout(ports, List.of(ports)),
                "the router would have to guess which half to use");
    }

    @Test
    void aMissingOrEmptyPortListCollapsesToTheEmptyLayout() {
        assertSame(MeInputLayout.empty(), MeInputLayout.unordered(null));
        assertSame(MeInputLayout.empty(), MeInputLayout.unordered(List.of()));
        assertSame(MeInputLayout.empty(), MeInputLayout.lanes(null));
        assertSame(MeInputLayout.empty(), MeInputLayout.lanes(List.of()));
    }

    @Test
    void anEmptyLayoutRefusesEverythingAndPromisesNothing() {
        // These two have to agree: a layout that advertised capacity while refusing to route would
        // leave the batching scheduler retrying the same pattern forever.
        assertTrue(MeInputLayout.empty().isEmpty());
        assertFalse(MeInputLayout.empty().route(request(1)), "an empty layout accepts nothing");
        assertEquals(0, MeInputLayout.empty().maxAcceptedCopies(request(1)),
                "and must not promise room it does not have");
    }

    @Test
    void anUnorderedLayoutRoutesAndSizesThroughItsPorts() {
        FakeInputPort port = new FakeInputPort(IRON, 64);
        MeInputLayout layout = MeInputLayout.unordered(List.of(port));

        assertFalse(layout.isEmpty());
        assertEquals(64, layout.maxAcceptedCopies(request(1)));
        assertTrue(layout.route(request(10)));
        assertEquals(10, port.amount());
    }

    @Test
    void capacitySizingIsNotClampedToOneMillionCopies() {
        FakeInputPort port = new FakeInputPort(IRON, 2_000_000);
        MeInputLayout layout = MeInputLayout.unordered(List.of(port));

        assertEquals(2_000_000, layout.maxAcceptedCopies(request(1)));
    }

    @Test
    void aLaneLayoutRoutesAndSizesThroughItsLanes() {
        FakeInputPort first = new FakeInputPort(IRON, 8);
        FakeInputPort second = new FakeInputPort(IRON, 8);
        MeInputLayout layout = MeInputLayout.lanes(List.of(List.of(first), List.of(second)));

        assertFalse(layout.isEmpty());

        KeyCounter left = new KeyCounter();
        left.add(IRON, 3);
        KeyCounter right = new KeyCounter();
        right.add(IRON, 5);
        assertTrue(layout.route(new KeyCounter[] {left, right}));

        assertEquals(3, first.amount(), "each lane takes its own share");
        assertEquals(5, second.amount());
    }

    @Test
    void whicheverHalfIsPopulatedIsUsedByBothRoutingAndSizing() {
        FakeInputPort port = new FakeInputPort(IRON, 12);

        MeInputLayout unordered = MeInputLayout.unordered(List.of(port));
        long unorderedEstimate = unordered.maxAcceptedCopies(request(1));
        assertTrue(unordered.route(request(unorderedEstimate)),
                "the unordered estimate must be honoured by the unordered route");

        FakeInputPort lanePort = new FakeInputPort(IRON, 12);
        MeInputLayout laneBased = MeInputLayout.lanes(List.of(List.of(lanePort)));
        long laneEstimate = laneBased.maxAcceptedCopies(request(1));
        assertTrue(laneBased.route(request(laneEstimate)),
                "the lane estimate must be honoured by the lane route");
    }

    @Test
    void theLayoutKeepsItsOwnCopyOfThePortsItWasGiven() {
        List<FakeInputPort> mutable = new ArrayList<>();
        mutable.add(new FakeInputPort(IRON, 64));
        MeInputLayout layout = MeInputLayout.unordered(mutable);

        mutable.clear();

        assertFalse(layout.isEmpty(), "a machine's layout must not change under it");
        assertEquals(64, layout.maxAcceptedCopies(request(1)));
    }
}
