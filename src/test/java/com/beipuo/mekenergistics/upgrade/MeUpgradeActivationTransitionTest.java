package com.beipuo.mekenergistics.upgrade;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MeUpgradeActivationTransitionTest {
    @Test
    void createsTheNodeOnlyWhenTheUpgradeBecomesActive() {
        assertEquals(MeUpgradeActivationTransition.ACTIVATE,
                MeUpgradeActivationTransition.between(false, true));
        assertEquals(MeUpgradeActivationTransition.NONE,
                MeUpgradeActivationTransition.between(true, true));
    }

    @Test
    void destroysTheNodeOnlyWhenTheUpgradeBecomesInactive() {
        assertEquals(MeUpgradeActivationTransition.DEACTIVATE,
                MeUpgradeActivationTransition.between(true, false));
        assertEquals(MeUpgradeActivationTransition.NONE,
                MeUpgradeActivationTransition.between(false, false));
    }
}
