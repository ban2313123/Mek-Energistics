package com.beipuo.mekenergistics.compat.eme;

import com.beipuo.mekenergistics.blockentity.compat.eme.machine.MeAlloyerBlockEntity;
import com.beipuo.mekenergistics.blockentity.compat.eme.machine.MeChemixerBlockEntity;
import com.beipuo.mekenergistics.blockentity.compat.eme.machine.MeSolidifierBlockEntity;
import com.beipuo.mekenergistics.blockentity.compat.eme.machine.MeThermalizerBlockEntity;
import com.beipuo.mekenergistics.menu.MePatternMekanismTileContainer;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.inventory.container.type.MekanismContainerType;
import mekanism.common.registration.impl.ContainerTypeDeferredRegister;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import net.minecraft.resources.ResourceLocation;

/** Menu types for ME variants whose source machine owns an optional-mod GUI. */
public final class EvolvedMekanismMachineMenuTypes {
    public static final ContainerTypeRegistryObject<MekanismTileContainer<MeAlloyerBlockEntity>> ME_ALLOYER =
            new ContainerTypeRegistryObject<>(ResourceLocation.fromNamespaceAndPath("mekenergistics", "me_alloyer"));
    public static final ContainerTypeRegistryObject<MekanismTileContainer<MeChemixerBlockEntity>> ME_CHEMIXER =
            new ContainerTypeRegistryObject<>(ResourceLocation.fromNamespaceAndPath("mekenergistics", "me_chemixer"));
    public static final ContainerTypeRegistryObject<MekanismTileContainer<MeSolidifierBlockEntity>> ME_SOLIDIFIER =
            new ContainerTypeRegistryObject<>(ResourceLocation.fromNamespaceAndPath("mekenergistics", "me_solidifier"));
    public static final ContainerTypeRegistryObject<MekanismTileContainer<MeThermalizerBlockEntity>> ME_THERMALIZER =
            new ContainerTypeRegistryObject<>(ResourceLocation.fromNamespaceAndPath("mekenergistics", "me_thermalizer"));

    private EvolvedMekanismMachineMenuTypes() {
    }

    public static void register(ContainerTypeDeferredRegister register) {
        register.registerMenu("me_alloyer", () -> MekanismContainerType.tile(MeAlloyerBlockEntity.class,
                (id, inv, tile) -> new MePatternMekanismTileContainer<>(ME_ALLOYER, id, inv, tile)));
        register.registerMenu("me_chemixer", () -> MekanismContainerType.tile(MeChemixerBlockEntity.class,
                (id, inv, tile) -> new MePatternMekanismTileContainer<>(ME_CHEMIXER, id, inv, tile)));
        register.registerMenu("me_solidifier", () -> MekanismContainerType.tile(MeSolidifierBlockEntity.class,
                (id, inv, tile) -> new MePatternMekanismTileContainer<>(ME_SOLIDIFIER, id, inv, tile)));
        register.registerMenu("me_thermalizer", () -> MekanismContainerType.tile(MeThermalizerBlockEntity.class,
                (id, inv, tile) -> new MePatternMekanismTileContainer<>(ME_THERMALIZER, id, inv, tile)));
    }
}
