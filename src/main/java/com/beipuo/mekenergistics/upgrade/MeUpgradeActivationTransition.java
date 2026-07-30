package com.beipuo.mekenergistics.upgrade;

public enum MeUpgradeActivationTransition {
    NONE,
    ACTIVATE,
    DEACTIVATE;

    public static MeUpgradeActivationTransition between(boolean wasActive, boolean isActive) {
        if (wasActive == isActive) {
            return NONE;
        }
        return isActive ? ACTIVATE : DEACTIVATE;
    }
}
