package com.beipuo.mekenergistics.compat.mekmm;

import com.beipuo.mekenergistics.blockentity.compat.mekmm.machine.MeLargeAntiprotonicNucleosynthesizerBlockEntity;
import com.beipuo.mekenergistics.menu.MePatternMekanismTileContainer;
import com.beipuo.mekenergistics.registry.ModMenuTypes;
import mekanism.common.inventory.container.type.MekanismContainerType;
import mekanism.common.registration.impl.ContainerTypeDeferredRegister;

/** Menu registration for MekLM's large machine, isolated from the common registry. */
public final class MekanismMoreMachineLargeMenuTypes {
    private MekanismMoreMachineLargeMenuTypes() {
    }

    public static void register(ContainerTypeDeferredRegister register) {
        register.registerMenu("me_large_antiprotonic_nucleosynthesizer", () -> MekanismContainerType.tile(
                MeLargeAntiprotonicNucleosynthesizerBlockEntity.class,
                (id, inv, tile) -> new MePatternMekanismTileContainer<>(
                        ModMenuTypes.ME_LARGE_ANTIPROTONIC_NUCLEOSYNTHESIZER, id, inv, tile) {
                    @Override
                    protected int getInventoryXOffset() {
                        return super.getInventoryXOffset() + 10;
                    }

                    @Override
                    protected int getInventoryYOffset() {
                        return super.getInventoryYOffset() + 27;
                    }
                }));
    }
}
