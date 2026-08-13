package com.beipuo.mekenergistics.upgrade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import appeng.api.stacks.GenericStack;
import com.beipuo.mekenergistics.testfixture.FakeKey;
import org.junit.jupiter.api.Test;

class MeInterfaceConfigTest {
    @Test
    void normalizeRejectsMissingKeysNonItemsAndNonPositiveAmounts() {
        FakeKey item = new FakeKey("iron");
        assertNull(MeInterfaceConfig.normalize(null, 64));
        assertNull(MeInterfaceConfig.normalize(item, 64));
        assertNull(MeInterfaceConfig.normalize(item, 0));
        assertNull(MeInterfaceConfig.normalize(item, -8));
        assertNull(MeInterfaceConfig.normalize(null));
        assertNull(MeInterfaceConfig.normalize(new GenericStack(item, 64)));
    }

    @Test
    void normalizeRejectsNonItemsAndNonPositiveAmounts() {
        FakeKey item = new FakeKey("iron");
        assertNull(MeInterfaceConfig.normalize(null, 64));
        assertNull(MeInterfaceConfig.normalize(item, 64));
        assertNull(MeInterfaceConfig.normalize(item, 0));
        assertNull(MeInterfaceConfig.normalize(item, -8));
        assertNull(MeInterfaceConfig.normalize(null));
        assertNull(MeInterfaceConfig.normalize(new GenericStack(item, 64)));
    }
}
