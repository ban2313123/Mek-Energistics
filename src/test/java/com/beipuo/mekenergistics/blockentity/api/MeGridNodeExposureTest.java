package com.beipuo.mekenergistics.blockentity.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import appeng.me.helpers.IGridConnectedBlockEntity;
import java.lang.reflect.Method;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

/**
 * AE2 asks a host for a node twice, for two different purposes, and the two must not be confused.
 *
 * <p>{@code getGridNode(Direction)} drives cable discovery, which happens while the node is still
 * booting — it has to hand out the node whether or not it is active yet, so we leave AE2's default
 * alone. {@code getActionableNode()} authorises actions and must refuse an inactive node, so both
 * machine interfaces do override that one. Collapsing the two would either break cable discovery on
 * placement or let an unpowered machine act on the network.
 */
class MeGridNodeExposureTest {
    @Test
    void cableDiscoveryKeepsAe2sUnfilteredNodeAccessorWhileActionsStayGuarded() throws NoSuchMethodException {
        for (Class<?> machineType : new Class<?>[] {MeAeMachine.class, MeFactoryAeMachine.class}) {
            Method cableDiscovery = machineType.getMethod("getGridNode", Direction.class);
            assertEquals(IGridConnectedBlockEntity.class, cableDiscovery.getDeclaringClass(),
                    () -> machineType.getSimpleName()
                            + " must not filter the node AE2 uses to find cables");

            Method actionable = machineType.getMethod("getActionableNode");
            assertNotEquals(IGridConnectedBlockEntity.class, actionable.getDeclaringClass(),
                    () -> machineType.getSimpleName()
                            + " must still refuse to act through an inactive node");
        }
    }
}
