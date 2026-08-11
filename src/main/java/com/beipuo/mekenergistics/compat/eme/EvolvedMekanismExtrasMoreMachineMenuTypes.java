package com.beipuo.mekenergistics.compat.eme;

import com.beipuo.mekenergistics.menu.compat.eme.MePatternEMExtraMoreMachineFactoryContainer;
import io.github.masyumero.emextras.common.integration.mekmm.tile.factory.TileEntityEMExtraMoreMachineFactory;
import mekanism.common.inventory.container.type.MekanismContainerType;
import mekanism.common.registration.impl.ContainerTypeDeferredRegister;

/** Menu registration for EMEKE's MEKMM-backed factory integration. */
public final class EvolvedMekanismExtrasMoreMachineMenuTypes {
    private EvolvedMekanismExtrasMoreMachineMenuTypes() {
    }

    public static void register(ContainerTypeDeferredRegister register) {
        register.registerMenu("me_em_extra_more_machine_factory", () -> MekanismContainerType.tile(
                TileEntityEMExtraMoreMachineFactory.class,
                (id, inv, tile) -> new MePatternEMExtraMoreMachineFactoryContainer(id, inv, tile)));
    }
}
