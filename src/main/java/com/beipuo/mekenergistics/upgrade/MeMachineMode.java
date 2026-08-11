package com.beipuo.mekenergistics.upgrade;

/**
 * Unified machine running mode derived from the installed ME upgrades. Passive crafting is an
 * additive capability of {@link #PATTERN_PROVIDER}, never a mode of its own.
 */
public enum MeMachineMode {
    /** No ME upgrade installed; the machine behaves like a plain Mekanism machine. */
    NONE,
    /** ME pattern provider upgrade installed (optionally with passive crafting). */
    PATTERN_PROVIDER,
    /** ME output interface upgrade installed; overrides native pattern behavior. */
    OUTPUT_INTERFACE;

    public boolean isPatternProvider() {
        return this == PATTERN_PROVIDER;
    }

    public boolean isOutputInterface() {
        return this == OUTPUT_INTERFACE;
    }
}
