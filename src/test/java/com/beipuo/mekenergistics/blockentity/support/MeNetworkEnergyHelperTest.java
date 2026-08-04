package com.beipuo.mekenergistics.blockentity.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

class MeNetworkEnergyHelperTest {

    @Test
    void networkEnergyIsNotCappedByTheLocalBufferCapacity() {
        long localEnergy = 0;
        long localBufferCapacity = 20_000;
        long networkEnergy = 50_000;
        long energyPerTick = 50_000;

        AtomicLong requestedFromNetwork = new AtomicLong();
        long available = MeNetworkEnergyHelper.availableWithNetwork(localEnergy, requested -> {
            requestedFromNetwork.set(requested);
            return Math.min(requested, networkEnergy);
        });

        assertEquals(50_000, available);
        assertEquals(Long.MAX_VALUE, requestedFromNetwork.get(),
                "availability must query the network beyond the local FE buffer capacity");
        assertEquals(1, available / energyPerTick,
                "Mekanism must see enough network energy for one operation even when its cost exceeds the local buffer");
        assertTrue(available > localBufferCapacity,
                "network-backed recipe energy must be able to exceed the local FE buffer");
    }

    @Test
    void localAndNetworkEnergyAreCombined() {
        assertEquals(50_000, MeNetworkEnergyHelper.totalAvailableEnergy(10_000, 40_000));
    }

    @Test
    void combinedEnergySaturatesInsteadOfOverflowing() {
        assertEquals(Long.MAX_VALUE, MeNetworkEnergyHelper.totalAvailableEnergy(Long.MAX_VALUE - 10, 20));
    }
}
