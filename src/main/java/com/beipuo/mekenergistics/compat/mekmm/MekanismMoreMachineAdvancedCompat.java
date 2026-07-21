package com.beipuo.mekenergistics.compat.mekmm;

import com.beipuo.mekenergistics.block.attribute.MeUpgradeableAttribute;
import com.beipuo.mekenergistics.blockentity.api.MeFactoryAeMachine;
import com.beipuo.mekenergistics.blockentity.compat.mekmm.factory.MeAdvancedCentrifugingFactoryBlockEntity;
import com.beipuo.mekenergistics.blockentity.compat.mekmm.factory.MeAdvancedChemicalToItemFactoryBlockEntity;
import com.beipuo.mekenergistics.blockentity.compat.mekmm.factory.MeAdvancedDissolvingFactoryBlockEntity;
import com.beipuo.mekenergistics.blockentity.compat.mekmm.factory.MeAdvancedItemToChemicalFactoryBlockEntity;
import com.beipuo.mekenergistics.blockentity.compat.mekmm.factory.MeAdvancedItemToItemFactoryBlockEntity;
import com.beipuo.mekenergistics.blockentity.compat.mekmm.factory.MeAdvancedLiquifyingFactoryBlockEntity;
import com.beipuo.mekenergistics.blockentity.compat.mekmm.factory.MeAdvancedPressurizedReactingFactoryBlockEntity;
import com.beipuo.mekenergistics.blockentity.compat.mekmm.factory.MeAdvancedWashingFactoryBlockEntity;
import com.beipuo.mekenergistics.common.machine.MeMekanismMachine;
import com.beipuo.mekenergistics.compat.catalog.CompatMachineCatalog;
import com.beipuo.mekenergistics.compat.catalog.CompatMod;
import com.beipuo.mekenergistics.compat.catalog.CompatRegistrationRoute;
import com.beipuo.mekenergistics.compat.provider.CompatMachineProviders;
import com.beipuo.mekenergistics.registry.ModBlockEntities;
import com.beipuo.mekenergistics.registry.ModBlocks;
import com.beipuo.mekenergistics.registry.ModMenuTypes;
import com.beipuo.mekenergistics.registry.machine.MachineFactoryRegistrar;
import com.jerry.mekaf.common.block.attribute.AttributeAdvancedFactoryType;
import com.jerry.mekaf.common.content.blocktype.AdvancedFactory;
import com.jerry.mekaf.common.content.blocktype.AdvancedFactoryType;
import java.util.Locale;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeTier;
import mekanism.common.block.attribute.AttributeGui;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tier.FactoryTier;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jetbrains.annotations.Nullable;

public final class MekanismMoreMachineAdvancedCompat {
    private MekanismMoreMachineAdvancedCompat() {
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static TileEntityTypeRegistryObject<? extends TileEntityMekanism> registerAdvancedFactoryMachine(
            MeMekanismMachine machine, MachineFactoryRegistrar registrar) {
        TileEntityTypeRegistryObject<?> registered = switch (advancedFactoryType(machine)) {
            case OXIDIZING, PIGMENT_EXTRACTING -> registrar.register(machine, MeAdvancedItemToChemicalFactoryBlockEntity::new);
            case DISSOLVING -> registrar.register(machine, MeAdvancedDissolvingFactoryBlockEntity::new);
            case WASHING -> registrar.register(machine, MeAdvancedWashingFactoryBlockEntity::new);
            case CRYSTALLIZING -> registrar.register(machine, MeAdvancedChemicalToItemFactoryBlockEntity::new);
            case PRESSURISED_REACTING -> registrar.register(machine, MeAdvancedPressurizedReactingFactoryBlockEntity::new);
            case CENTRIFUGING -> registrar.register(machine, MeAdvancedCentrifugingFactoryBlockEntity::new);
            case LIQUIFYING -> registrar.register(machine, MeAdvancedLiquifyingFactoryBlockEntity::new);
            case PAINTING -> registrar.register(machine, MeAdvancedItemToItemFactoryBlockEntity::new);
        };
        return (TileEntityTypeRegistryObject) registered;
    }

    public static <TILE extends TileEntityMekanism> BlockTypeTile<TILE> createAdvancedFactoryBlockType(
            MeMekanismMachine machine, TileEntityTypeRegistryObject<TILE> tileType) {
        AdvancedFactory.AdvancedFactoryBuilder<?, ?, ?> builder =
                AdvancedFactory.AdvancedFactoryBuilder.createAdvancedFactory(() -> tileType, advancedFactoryType(machine), machine.factoryTier());
        builder.replace(new AttributeGui(() -> ModMenuTypes.ME_ADVANCED_FACTORY, null));
        MeMekanismMachine upgradeTarget = machine.getNextFactory();
        if (upgradeTarget != null) {
            builder.replace(new MeUpgradeableAttribute(() -> ModBlocks.getMachineBlock(upgradeTarget).get()));
            if (upgradeTarget.provider() == CompatMod.MEKE && CompatMachineCatalog.isAvailable(upgradeTarget)) {
                CompatMachineProviders.get(CompatMod.MEKE)
                        .addUpgradeAttribute(builder, () -> ModBlocks.getMachineBlock(upgradeTarget).get());
            }
        }
        @SuppressWarnings("unchecked")
        BlockTypeTile<TILE> built = (BlockTypeTile<TILE>) builder.build();
        return built;
    }

    public static AdvancedFactoryType advancedFactoryType(MeMekanismMachine machine) {
        String name = machine.machineTypeId().toUpperCase(Locale.ROOT);
        return switch (name) {
            case "OXIDIZING" -> AdvancedFactoryType.OXIDIZING;
            case "DISSOLVING" -> AdvancedFactoryType.DISSOLVING;
            case "WASHING" -> AdvancedFactoryType.WASHING;
            case "CRYSTALLIZING" -> AdvancedFactoryType.CRYSTALLIZING;
            case "PRESSURISED_REACTING" -> AdvancedFactoryType.PRESSURISED_REACTING;
            case "CENTRIFUGING" -> AdvancedFactoryType.CENTRIFUGING;
            case "LIQUIFYING" -> AdvancedFactoryType.LIQUIFYING;
            case "PIGMENT_EXTRACTING" -> AdvancedFactoryType.PIGMENT_EXTRACTING;
            case "PAINTING" -> AdvancedFactoryType.PAINTING;
            default -> throw new IllegalStateException("Unknown MEKMM advanced factory type: " + name);
        };
    }

    @Nullable
    public static MeMekanismMachine getFactoryTarget(BlockState state) {
        MeMekanismMachine registryTarget = getFactoryTargetByRegistryName(state);
        if (registryTarget != null) {
            return registryTarget;
        }
        AttributeAdvancedFactoryType attribute = Attribute.get(state, AttributeAdvancedFactoryType.class);
        if (attribute == null) {
            return null;
        }
        String typeName = attribute.getAdvancedFactoryType().getRegistryNameComponent();
        AttributeTier<?> tier = Attribute.get(state, AttributeTier.class);
        if (tier != null && tier.tier() instanceof FactoryTier factoryTier) {
            return MeMekanismMachine.getMoreMachineAdvancedFactory(factoryTier, typeName);
        }
        return MeMekanismMachine.getMoreMachineAdvancedFactory(FactoryTier.BASIC, typeName);
    }

    @Nullable
    private static MeMekanismMachine getFactoryTargetByRegistryName(BlockState state) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        return CompatMachineCatalog.findBySourceBlockId(id)
                .filter(spec -> spec.route() == CompatRegistrationRoute.MEKMM_ADVANCED_FACTORY)
                .map(spec -> spec.machine())
                .orElse(null);
    }

    public static void registerGridNodeHost(
            RegisterCapabilitiesEvent event,
            TileEntityTypeRegistryObject<? extends TileEntityMekanism> holder) {
        ModBlockEntities.registerGridNodeHost(event, holder, MeFactoryAeMachine.class);
    }
}
