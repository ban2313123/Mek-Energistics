package com.beipuo.mekenergistics.compat.mekmm;

import com.beipuo.mekenergistics.blockentity.compat.mekmm.machine.MeLargeAntiprotonicNucleosynthesizerBlockEntity;
import com.beipuo.mekenergistics.blockentity.compat.mekmm.machine.MeLargeChemicalInfuserBlockEntity;
import com.beipuo.mekenergistics.blockentity.compat.mekmm.machine.MeLargeElectrolyticSeparatorBlockEntity;
import com.beipuo.mekenergistics.blockentity.compat.mekmm.machine.MeLargeRotaryCondensentratorBlockEntity;
import com.beipuo.mekenergistics.blockentity.compat.mekmm.machine.MeLargeSolarNeutronActivatorBlockEntity;
import com.beipuo.mekenergistics.menu.MePatternMekanismTileContainer;
import com.beipuo.mekenergistics.registry.ModMenuTypes;
import mekanism.common.inventory.container.type.MekanismContainerType;
import mekanism.common.registration.impl.ContainerTypeDeferredRegister;

/** Menu registration for MekLM's optional large machines, isolated from the common registry. */
public final class MekanismMoreMachineLargeMenuTypes {
    private MekanismMoreMachineLargeMenuTypes() {
    }

    public static void register(ContainerTypeDeferredRegister register) {
        register.registerMenu("me_large_rotary_condensentrator", () -> MekanismContainerType.tile(
                MeLargeRotaryCondensentratorBlockEntity.class,
                (id, inv, tile) -> new MePatternMekanismTileContainer<>(
                        ModMenuTypes.ME_LARGE_ROTARY_CONDENSENTRATOR, id, inv, tile)));
        register.registerMenu("me_large_solar_neutron_activator", () -> MekanismContainerType.tile(
                MeLargeSolarNeutronActivatorBlockEntity.class,
                (id, inv, tile) -> new MePatternMekanismTileContainer<>(
                        ModMenuTypes.ME_LARGE_SOLAR_NEUTRON_ACTIVATOR, id, inv, tile)));
        register.registerMenu("me_large_electrolytic_separator", () -> MekanismContainerType.tile(
                MeLargeElectrolyticSeparatorBlockEntity.class,
                (id, inv, tile) -> new MePatternMekanismTileContainer<>(
                        ModMenuTypes.ME_LARGE_ELECTROLYTIC_SEPARATOR, id, inv, tile)));
        register.registerMenu("me_large_chemical_infuser", () -> MekanismContainerType.tile(
                MeLargeChemicalInfuserBlockEntity.class,
                (id, inv, tile) -> new MePatternMekanismTileContainer<>(
                        ModMenuTypes.ME_LARGE_CHEMICAL_INFUSER, id, inv, tile)));
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
