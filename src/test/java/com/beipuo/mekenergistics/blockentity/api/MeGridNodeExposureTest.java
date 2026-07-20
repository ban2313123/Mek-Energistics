package com.beipuo.mekenergistics.blockentity.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import appeng.me.helpers.IGridConnectedBlockEntity;
import com.beipuo.mekenergistics.blockentity.compat.eme.factory.MeEvolvedMekanismExtrasFactoryAeMachine;
import com.beipuo.mekenergistics.blockentity.compat.mekmm.factory.MeMoreMachineFactoryAeMachine;
import java.lang.reflect.Method;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

class MeGridNodeExposureTest {
    @Test
    void recipeMachinesUseAe2CableNodeExposure() throws NoSuchMethodException {
        assertUsesAe2GridNodeExposure(MeAeMachine.class);
    }

    @Test
    void factoriesUseAe2CableNodeExposure() throws NoSuchMethodException {
        assertUsesAe2GridNodeExposure(MeFactoryAeMachine.class);
    }

    @Test
    void compatibilityFactoryAliasesKeepTheSharedExposure() throws NoSuchMethodException {
        assertUsesAe2GridNodeExposure(MeEvolvedMekanismExtrasFactoryAeMachine.class);
        assertUsesAe2GridNodeExposure(MeMoreMachineFactoryAeMachine.class);
    }

    private static void assertUsesAe2GridNodeExposure(Class<?> machineType) throws NoSuchMethodException {
        Method method = machineType.getMethod("getGridNode", Direction.class);
        assertEquals(IGridConnectedBlockEntity.class, method.getDeclaringClass(),
                "Cable discovery must expose a created node before it becomes active");
    }
}
