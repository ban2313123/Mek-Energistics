package com.beipuo.mekenergistics.upgrade;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class MeUpgradeRuntimeStateTest {
    @Test
    void tracksTransitionsAndRefreshesTheRecipeCache() {
        MeUpgradeRuntimeState state = new MeUpgradeRuntimeState();
        AtomicInteger refreshes = new AtomicInteger();
        state.setRecipeCacheListener(refreshes::incrementAndGet);

        assertEquals(MeUpgradeActivationTransition.NONE, state.transitionTo(false));
        assertEquals(MeUpgradeActivationTransition.ACTIVATE, state.transitionTo(true));
        assertEquals(MeUpgradeActivationTransition.NONE, state.transitionTo(true));
        state.refreshRecipeCache();
        assertEquals(MeUpgradeActivationTransition.DEACTIVATE, state.transitionTo(false));
        assertEquals(1, refreshes.get());
    }

    @Test
    void componentStateIsUsedUntilClientSyncArrives() {
        MeUpgradeRuntimeState state = new MeUpgradeRuntimeState();

        assertEquals(true, state.activeFor(null, true, true));
        assertEquals(false, state.activeFor(null, true, false));
    }
}
