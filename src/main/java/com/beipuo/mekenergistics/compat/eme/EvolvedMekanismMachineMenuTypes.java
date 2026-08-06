package com.beipuo.mekenergistics.compat.eme;

import com.beipuo.mekenergistics.blockentity.compat.eme.machine.MeThermalizerBlockEntity;
import com.beipuo.mekenergistics.menu.MePatternMekanismTileContainer;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.inventory.container.type.MekanismContainerType;
import mekanism.common.registration.impl.ContainerTypeDeferredRegister;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import net.minecraft.resources.ResourceLocation;

/** Menu types for ME variants whose source machine owns an optional-mod GUI. */
public final class EvolvedMekanismMachineMenuTypes {
    public static final ContainerTypeRegistryObject<MekanismTileContainer<MeThermalizerBlockEntity>> ME_THERMALIZER =
            new ContainerTypeRegistryObject<>(ResourceLocation.fromNamespaceAndPath("mekenergistics", "me_thermalizer"));

    private EvolvedMekanismMachineMenuTypes() {
    }

    public static void register(ContainerTypeDeferredRegister register) {
        register.registerMenu("me_thermalizer", () -> MekanismContainerType.tile(MeThermalizerBlockEntity.class,
                (id, inv, tile) -> new MePatternMekanismTileContainer<>(ME_THERMALIZER, id, inv, tile)));
    }
}
