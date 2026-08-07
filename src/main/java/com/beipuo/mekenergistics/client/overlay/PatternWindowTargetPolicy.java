package com.beipuo.mekenergistics.client.overlay;

final class PatternWindowTargetPolicy {
    private PatternWindowTargetPolicy() {
    }

    static boolean shouldShow(boolean nativeMeMachine, boolean upgradeTarget, boolean upgradeActive) {
        return nativeMeMachine || upgradeTarget && upgradeActive;
    }
}
