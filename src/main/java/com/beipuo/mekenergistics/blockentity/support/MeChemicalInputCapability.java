package com.beipuo.mekenergistics.blockentity.support;

import java.util.Locale;
import java.util.Set;

/**
 * Capability classification shared by machine adapters.
 *
 * <p>This is deliberately based on stable recipe ids/families, rather than
 * Mekanism recipe implementation classes. The {@code chemical_conversion}
 * id describes a slot capability and is not itself a machine recipe.</p>
 */
public enum MeChemicalInputCapability {
    NONE,
    CHEMICAL_OR_CONVERSION_SLOT,
    ITEM_TO_CHEMICAL_OUTPUT;

    private static final Set<String> CHEMICAL_INPUT_RECIPES = Set.of(
            "compressing", "purifying", "injecting", "infusing", "metallurgic_infusing",
            "painting", "dissolution", "dissolving", "nucleosynthesizing",
            "large_antiprotonic_nucleosynthesizer",
            "planting", "replicating", "chemical_replicator", "fluid_replicator"
    );
    private static final Set<String> ITEM_TO_CHEMICAL_RECIPES = Set.of(
            "oxidizing", "pigment_extracting"
    );

    /** Classifies a namespaced recipe id, accepting either {@code ns:path} or {@code path}. */
    public static MeChemicalInputCapability forRecipeId(String recipeId) {
        if (recipeId == null || recipeId.isBlank()) {
            return NONE;
        }
        String path = recipeId.toLowerCase(Locale.ROOT);
        int separator = path.indexOf(':');
        if (separator >= 0) {
            path = path.substring(separator + 1);
        }
        if ("chemical_conversion".equals(path)) {
            // This is ChemicalInventorySlot.fillOrConvert capability, not a machine recipe.
            return CHEMICAL_OR_CONVERSION_SLOT;
        }
        if (ITEM_TO_CHEMICAL_RECIPES.contains(path)) {
            return ITEM_TO_CHEMICAL_OUTPUT;
        }
        return CHEMICAL_INPUT_RECIPES.contains(path) ? CHEMICAL_OR_CONVERSION_SLOT : NONE;
    }

    /** Classifies a factory/machine registry type when no recipe instance is available. */
    public static MeChemicalInputCapability forMachineType(String machineType) {
        return forRecipeId(machineType);
    }

    public boolean acceptsConversionCarrier() {
        return this == CHEMICAL_OR_CONVERSION_SLOT;
    }

    public boolean producesChemicalFromItem() {
        return this == ITEM_TO_CHEMICAL_OUTPUT;
    }
}
