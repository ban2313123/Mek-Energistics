package com.beipuo.mekenergistics.client.jei;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class OptionalJeiCompatTest {
    @Test
    void requiresBothModAndCatalogFamilyAvailability() {
        assertTrue(OptionalJeiCompat.isEnabled(true, true));
        assertFalse(OptionalJeiCompat.isEnabled(true, false));
        assertFalse(OptionalJeiCompat.isEnabled(false, true));
        assertFalse(OptionalJeiCompat.isEnabled(false, false));
    }
}
