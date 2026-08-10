package com.beipuo.mekenergistics.compat.dataenergistics;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Bootstrap-safe ABI gate for the DataEnergistics counted-crafting bridge. It never references
 * DataEnergistics classes, so the mixin plugin can load it while mod presence is still being
 * resolved, and unit tests can pin down the warning contract.
 */
public final class DataAbiGate {
    private DataAbiGate() {
    }

    /**
     * Builds the retrievable warning shown when DataEnergistics is loaded but the counted-crafting
     * ABI this bridge compiles against is not present, so the mixin degrades loudly instead of
     * silently skipping while still claiming compatibility.
     */
    public static Optional<String> countedBridgeWarning(boolean dataLoaded,
            boolean providerPresent, boolean admissionPresent) {
        if (!dataLoaded || (providerPresent && admissionPresent)) {
            return Optional.empty();
        }
        List<String> missing = new ArrayList<>(2);
        if (!providerPresent) {
            missing.add("CountedCraftingProvider");
        }
        if (!admissionPresent) {
            missing.add("CountedCraftingAdmission");
        }
        return Optional.of("DataEnergistics counted-crafting bridge disabled: expected ABI class"
                + " missing from the loaded mod: " + String.join(", ", missing));
    }
}
