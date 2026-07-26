package com.beipuo.mekenergistics.blockentity.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * A machine deserialises before it has a level, so restored patterns cannot be published to the ME
 * network until the managed node exists. {@link AbstractMeAeSupport.CraftingUpdateState} is what
 * defers that work, and losing a deferred update means restored patterns never reach the terminal.
 */
class AbstractMeAeSupportSchemaTest {
    @Test
    void restoredPatternsArePublishedWhenTheNodeBecomesActive() {
        AbstractMeAeSupport.CraftingUpdateState state = new AbstractMeAeSupport.CraftingUpdateState();
        int[] updates = {0};

        state.markPending();
        state.request(false, () -> updates[0]++);

        assertTrue(state.isPending(), "an inactive node must hold the update back");
        assertEquals(0, updates[0], "nothing may be published before the node exists");

        state.flush(() -> updates[0]++);

        assertFalse(state.isPending(), "flushing must clear the backlog");
        assertEquals(1, updates[0], "the deferred update must run exactly once");
    }

    @Test
    void activePatternChangesRefreshImmediatelyWithoutLeavingPendingWork() {
        AbstractMeAeSupport.CraftingUpdateState state = new AbstractMeAeSupport.CraftingUpdateState();
        int[] updates = {0};

        state.request(true, () -> updates[0]++);
        state.flush(() -> updates[0]++);

        assertFalse(state.isPending(), "an active node publishes straight away");
        assertEquals(1, updates[0], "a later flush must not publish the same update again");
    }
}
