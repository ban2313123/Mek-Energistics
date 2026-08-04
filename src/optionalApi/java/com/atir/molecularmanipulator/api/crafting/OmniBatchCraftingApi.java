package com.atir.molecularmanipulator.api.crafting;

public final class OmniBatchCraftingApi {
    public static final int API_VERSION = 1;

    private OmniBatchCraftingApi() {
    }

    public static int apiVersion() {
        return API_VERSION;
    }

    public static boolean isOmniManagedCpu(Object craftingCpu) {
        return false;
    }
}
