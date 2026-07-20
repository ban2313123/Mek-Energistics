package com.beipuo.mekenergistics.compat.eme;

import com.beipuo.mekenergistics.menu.MePatternMekanismTileContainer;
import com.beipuo.mekenergistics.registry.ModMenuTypes;
import io.github.masyumero.emextras.common.integration.mekaf.tile.factory.base.TileEntityEMExtraAdvancedFactoryBase;
import mekanism.common.inventory.container.type.MekanismContainerType;
import mekanism.common.registration.impl.ContainerTypeDeferredRegister;

/** Menu registration for EMEKE's MEKAF-backed factory integration. */
public final class EvolvedMekanismExtrasAdvancedMenuTypes {
    private EvolvedMekanismExtrasAdvancedMenuTypes() {
    }

    public static void register(ContainerTypeDeferredRegister register) {
        register.registerMenu("me_em_extra_advanced_factory", () -> MekanismContainerType.tile(
                TileEntityEMExtraAdvancedFactoryBase.class,
                (id, inv, tile) -> new MePatternMekanismTileContainer<>(
                        ModMenuTypes.ME_EM_EXTRA_ADVANCED_FACTORY, id, inv, tile)));
    }
}
