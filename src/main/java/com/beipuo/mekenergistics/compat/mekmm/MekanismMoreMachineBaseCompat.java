package com.beipuo.mekenergistics.compat.mekmm;

import com.beipuo.mekenergistics.block.attribute.MeUpgradeableAttribute;
import com.beipuo.mekenergistics.blockentity.api.MeFactoryAeMachine;
import com.beipuo.mekenergistics.blockentity.compat.mekmm.factory.MeMoreMachineItemStackToItemStackFactoryBlockEntity;
import com.beipuo.mekenergistics.blockentity.compat.mekmm.factory.MePlantingFactoryBlockEntity;
import com.beipuo.mekenergistics.blockentity.compat.mekmm.factory.MeRecyclingFactoryBlockEntity;
import com.beipuo.mekenergistics.blockentity.compat.mekmm.factory.MeReplicatingFactoryBlockEntity;
import com.beipuo.mekenergistics.blockentity.compat.mekmm.factory.MeStampingFactoryBlockEntity;
import com.beipuo.mekenergistics.blockentity.compat.mekmm.machine.MeLatheBlockEntity;
import com.beipuo.mekenergistics.blockentity.compat.mekmm.machine.MePlantingStationBlockEntity;
import com.beipuo.mekenergistics.blockentity.compat.mekmm.machine.MeRecyclerBlockEntity;
import com.beipuo.mekenergistics.blockentity.compat.mekmm.machine.MeReplicatorBlockEntity;
import com.beipuo.mekenergistics.blockentity.compat.mekmm.machine.MeChemicalReplicatorBlockEntity;
import com.beipuo.mekenergistics.blockentity.compat.mekmm.machine.MeFluidReplicatorBlockEntity;
import com.beipuo.mekenergistics.blockentity.compat.mekmm.machine.MeLargeAntiprotonicNucleosynthesizerBlockEntity;
import com.beipuo.mekenergistics.blockentity.compat.mekmm.machine.MeRollingMillBlockEntity;
import com.beipuo.mekenergistics.blockentity.compat.mekmm.machine.MeStamperBlockEntity;
import com.beipuo.mekenergistics.common.machine.MeMekanismMachine;
import com.beipuo.mekenergistics.compat.catalog.CompatFactoryTierGraph;
import com.beipuo.mekenergistics.compat.catalog.CompatMod;
import com.beipuo.mekenergistics.compat.catalog.CompatRegistrationRoute;
import com.beipuo.mekenergistics.registry.ModBlockEntities;
import com.beipuo.mekenergistics.registry.ModBlocks;
import com.beipuo.mekenergistics.registry.ModMenuTypes;
import com.beipuo.mekenergistics.registry.machine.MachineFactoryRegistrar;
import com.jerry.mekmm.common.block.attribute.MoreMachineAttributeFactoryType;
import com.jerry.mekmm.common.content.blocktype.MoreMachineBlockShapes;
import com.jerry.mekmm.common.content.blocktype.MoreMachineFactory;
import com.jerry.mekmm.common.content.blocktype.MoreMachineFactoryType;
import com.jerry.mekmm.common.block.attribute.MoreMachineBounding;
import com.jerry.meklm.common.content.blocktype.LargeMachineBlockShapes;
import com.jerry.mekmm.common.registries.MoreMachineContainerTypes;
import java.util.Locale;
import mekanism.api.Upgrade;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeHasBounding;
import mekanism.common.block.attribute.AttributeStateFacing;
import mekanism.common.block.attribute.AttributeTier;
import mekanism.common.block.attribute.Attributes;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tier.FactoryTier;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jetbrains.annotations.Nullable;

public final class MekanismMoreMachineBaseCompat {
    private MekanismMoreMachineBaseCompat() {
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static TileEntityTypeRegistryObject<? extends TileEntityMekanism> registerBaseMachine(
            MeMekanismMachine machine, MachineFactoryRegistrar registrar) {
        TileEntityTypeRegistryObject<?> registered = switch (machine) {
            case RECYCLER -> registrar.register(machine, MeRecyclerBlockEntity::new);
            case PLANTING_STATION -> registrar.register(machine, MePlantingStationBlockEntity::new);
            case CNC_STAMPER -> registrar.register(machine, MeStamperBlockEntity::new);
            case CNC_LATHE -> registrar.register(machine, MeLatheBlockEntity::new);
            case CNC_ROLLING_MILL -> registrar.register(machine, MeRollingMillBlockEntity::new);
            case REPLICATOR -> registrar.register(machine, MeReplicatorBlockEntity::new);
            case CHEMICAL_REPLICATOR -> registrar.register(machine, MeChemicalReplicatorBlockEntity::new);
            case FLUID_REPLICATOR -> registrar.register(machine, MeFluidReplicatorBlockEntity::new);
            case LARGE_ANTIPROTONIC_NUCLEOSYNTHESIZER -> registrar.register(machine, MeLargeAntiprotonicNucleosynthesizerBlockEntity::new);
            default -> throw new IllegalStateException("Unknown MEKMM base machine: " + machine);
        };
        return (TileEntityTypeRegistryObject) registered;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static TileEntityTypeRegistryObject<? extends TileEntityMekanism> registerFactoryMachine(
            MeMekanismMachine machine, MachineFactoryRegistrar registrar) {
        TileEntityTypeRegistryObject<?> registered = switch (moreMachineFactoryType(machine)) {
            case RECYCLING -> registrar.register(machine, MeRecyclingFactoryBlockEntity::new);
            case PLANTING_STATION -> registrar.register(machine, MePlantingFactoryBlockEntity::new);
            case CNC_STAMPING -> registrar.register(machine, MeStampingFactoryBlockEntity::new);
            case CNC_LATHING, CNC_ROLLING_MILL -> registrar.register(machine, MeMoreMachineItemStackToItemStackFactoryBlockEntity::new);
            case REPLICATING -> registrar.register(machine, MeReplicatingFactoryBlockEntity::new);
        };
        return (TileEntityTypeRegistryObject) registered;
    }

    public static <TILE extends TileEntityMekanism> BlockTypeTile<TILE> createFactoryBlockType(
            MeMekanismMachine machine, TileEntityTypeRegistryObject<TILE> tileType) {
        MoreMachineFactory.MoreMachineFactoryBuilder<?, ?, ?> builder =
                MoreMachineFactory.MoreMachineFactoryBuilder.createMoreMachineFactory(() -> tileType, moreMachineFactoryType(machine), machine.factoryTier());
        builder.replace(new mekanism.common.block.attribute.AttributeGui(() -> ModMenuTypes.ME_MORE_MACHINE_FACTORY, null));
        MeMekanismMachine upgradeTarget = machine.getNextFactory();
        if (upgradeTarget != null) {
            builder.replace(new MeUpgradeableAttribute(() -> ModBlocks.getMachineBlock(upgradeTarget).get()));
        }
        @SuppressWarnings("unchecked")
        BlockTypeTile<TILE> built = (BlockTypeTile<TILE>) builder.build();
        return built;
    }

    public static <TILE extends TileEntityMekanism> BlockTypeTile<TILE> createBaseBlockType(
            MeMekanismMachine machine, TileEntityTypeRegistryObject<TILE> tileType) {
        if (machine == MeMekanismMachine.LARGE_ANTIPROTONIC_NUCLEOSYNTHESIZER) {
            return createLargeAntiprotonicBlockType(tileType);
        }
        var builder = BlockTypeTile.BlockTileBuilder
                .createBlock(() -> tileType, machine::translationKey)
                .withGui(() -> baseContainer(machine))
                .withEnergyConfig(machine.energyUsage(), machine.energyStorage())
                .with(new AttributeStateFacing(), Attributes.ACTIVE_LIGHT, Attributes.INVENTORY, Attributes.REDSTONE, Attributes.SECURITY, Attributes.COMPARATOR)
                .withSideConfig(machine == MeMekanismMachine.LARGE_ANTIPROTONIC_NUCLEOSYNTHESIZER
                        ? new TransmissionType[] {TransmissionType.ITEM, TransmissionType.ENERGY, TransmissionType.CHEMICAL}
                        : machine == MeMekanismMachine.CHEMICAL_REPLICATOR
                        ? new TransmissionType[] {TransmissionType.ITEM, TransmissionType.ENERGY, TransmissionType.CHEMICAL}
                        : machine == MeMekanismMachine.FLUID_REPLICATOR
                        ? new TransmissionType[] {TransmissionType.ITEM, TransmissionType.ENERGY, TransmissionType.CHEMICAL, TransmissionType.FLUID}
                        : new TransmissionType[] {TransmissionType.ITEM, TransmissionType.ENERGY})
                .withSupportedUpgrades(Upgrade.SPEED, Upgrade.ENERGY);
        if (machine == MeMekanismMachine.LARGE_ANTIPROTONIC_NUCLEOSYNTHESIZER) {
            builder.withCustomShape(LargeMachineBlockShapes.LARGE_ANTIPROTONIC_NUCLEOSYNTHESIZER)
                    .with(MoreMachineBounding.FULL_JAVA_ENTITY);
        }
        if (machine != MeMekanismMachine.CHEMICAL_REPLICATOR && machine != MeMekanismMachine.FLUID_REPLICATOR) {
            builder.with(new MoreMachineAttributeFactoryType(moreMachineFactoryType(machine)));
        }
        switch (machine) {
            case PLANTING_STATION -> builder
                    .withCustomShape(MoreMachineBlockShapes.PLANTING_STATION)
                    .with(AttributeHasBounding.ABOVE_ONLY);
            case REPLICATOR -> builder.withCustomShape(MoreMachineBlockShapes.REPLICATOR);
            default -> {
            }
        }
        MeMekanismMachine upgradeTarget = machine.getBasicFactory();
        if (upgradeTarget != null) {
            builder.with(new MeUpgradeableAttribute(() -> ModBlocks.getMachineBlock(upgradeTarget).get()));
        }
        return builder.build();
    }

    @SuppressWarnings("unchecked")
    private static <TILE extends TileEntityMekanism> BlockTypeTile<TILE> createLargeAntiprotonicBlockType(
            TileEntityTypeRegistryObject<TILE> tileType) {
        var builder = mekanism.common.content.blocktype.BlockTypeTile.BlockTileBuilder
                .createBlock(() -> tileType, com.beipuo.mekenergistics.common.MeLangEntry.of(
                        MeMekanismMachine.LARGE_ANTIPROTONIC_NUCLEOSYNTHESIZER.translationKey()))
                .withGui(() -> ModMenuTypes.ME_LARGE_ANTIPROTONIC_NUCLEOSYNTHESIZER)
                .withEnergyConfig(MeMekanismMachine.LARGE_ANTIPROTONIC_NUCLEOSYNTHESIZER.energyUsage(),
                        MeMekanismMachine.LARGE_ANTIPROTONIC_NUCLEOSYNTHESIZER.energyStorage())
                .withSideConfig(TransmissionType.ITEM, TransmissionType.CHEMICAL, TransmissionType.ENERGY)
                .withCustomShape(com.jerry.meklm.common.content.blocktype.LargeMachineBlockShapes.LARGE_ANTIPROTONIC_NUCLEOSYNTHESIZER)
                .with(com.jerry.mekmm.common.block.attribute.MoreMachineBounding.FULL_JAVA_ENTITY)
                .withSupportedUpgrades(Upgrade.MUFFLING);
        return (BlockTypeTile<TILE>) builder.build();
    }

    public static MoreMachineFactoryType moreMachineFactoryType(MeMekanismMachine machine) {
        String typeName = machine.moreMachineFactoryTypeName() != null ? machine.moreMachineFactoryTypeName() : machine.moreMachineBaseTypeName();
        String name = typeName.toUpperCase(Locale.ROOT);
        return switch (name) {
            case "RECYCLING" -> MoreMachineFactoryType.RECYCLING;
            case "PLANTING" -> MoreMachineFactoryType.PLANTING_STATION;
            case "STAMPING" -> MoreMachineFactoryType.CNC_STAMPING;
            case "LATHING" -> MoreMachineFactoryType.CNC_LATHING;
            case "ROLLING_MILL" -> MoreMachineFactoryType.CNC_ROLLING_MILL;
            case "REPLICATING" -> MoreMachineFactoryType.REPLICATING;
            default -> throw new IllegalStateException("Unknown MEKMM factory type: " + name);
        };
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static ContainerTypeRegistryObject<? extends MekanismTileContainer<?>> baseContainer(MeMekanismMachine machine) {
        return switch (machine) {
            case RECYCLER -> MoreMachineContainerTypes.RECYCLER;
            case PLANTING_STATION -> MoreMachineContainerTypes.PLANTING_STATION;
            case CNC_STAMPER -> MoreMachineContainerTypes.CNC_STAMPER;
            case CNC_LATHE -> MoreMachineContainerTypes.CNC_LATHE;
            case CNC_ROLLING_MILL -> MoreMachineContainerTypes.CNC_ROLLING_MILL;
            case REPLICATOR -> MoreMachineContainerTypes.REPLICATOR;
            case CHEMICAL_REPLICATOR -> MoreMachineContainerTypes.CHEMIcAL_REPLICATOR;
            case FLUID_REPLICATOR -> MoreMachineContainerTypes.FLUID_REPLICATOR;
            case LARGE_ANTIPROTONIC_NUCLEOSYNTHESIZER -> com.jerry.meklm.common.registries.LargeMachineContainerTypes.LARGE_ANTIPROTONIC_NUCLEOSYNTHESIZER;
            default -> (ContainerTypeRegistryObject) ModMenuTypes.getMachineContainer(machine);
        };
    }

    @Nullable
    public static MeMekanismMachine getFactoryTarget(BlockState state) {
        MeMekanismMachine registryTarget = getFactoryTargetByRegistryName(state);
        if (registryTarget != null) {
            return registryTarget;
        }
        MoreMachineAttributeFactoryType attribute = Attribute.get(state, MoreMachineAttributeFactoryType.class);
        if (attribute == null) {
            return null;
        }
        String typeName = attribute.getMoreMachineFactoryType().getRegistryNameComponent();
        AttributeTier<?> tier = Attribute.get(state, AttributeTier.class);
        if (tier != null && tier.tier() instanceof FactoryTier factoryTier) {
            return MeMekanismMachine.getMoreMachineFactory(factoryTier, typeName);
        }
        return getBaseTarget(typeName);
    }

    @Nullable
    private static MeMekanismMachine getFactoryTargetByRegistryName(BlockState state) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        String path = id.getPath();
        if (path.startsWith("me_")) {
            return null;
        }
        MeMekanismMachine baseTarget = CompatFactoryTierGraph.findBySourcePath(
                CompatRegistrationRoute.MEKMM_MACHINE, path);
        if (baseTarget != null) {
            return baseTarget;
        }
        return CompatFactoryTierGraph.findBySourcePath(CompatRegistrationRoute.MEKMM_FACTORY, path);
    }

    @Nullable
    private static MeMekanismMachine getBaseTarget(String typeName) {
        return CompatFactoryTierGraph.findBaseMachine(CompatMod.MEKMM, typeName);
    }

    public static void registerGridNodeHost(
            RegisterCapabilitiesEvent event,
            TileEntityTypeRegistryObject<? extends TileEntityMekanism> holder) {
        ModBlockEntities.registerGridNodeHost(event, holder, MeFactoryAeMachine.class);
    }

    public static void registerBaseGridNodeHost(
            RegisterCapabilitiesEvent event,
            MeMekanismMachine machine,
            TileEntityTypeRegistryObject<? extends TileEntityMekanism> holder) {
        switch (machine) {
            case RECYCLER -> ModBlockEntities.registerGridNodeHost(event, holder, MeRecyclerBlockEntity.class);
            case PLANTING_STATION -> ModBlockEntities.registerGridNodeHost(event, holder, MePlantingStationBlockEntity.class);
            case CNC_STAMPER -> ModBlockEntities.registerGridNodeHost(event, holder, MeStamperBlockEntity.class);
            case CNC_LATHE -> ModBlockEntities.registerGridNodeHost(event, holder, MeLatheBlockEntity.class);
            case CNC_ROLLING_MILL -> ModBlockEntities.registerGridNodeHost(event, holder, MeRollingMillBlockEntity.class);
            case REPLICATOR -> ModBlockEntities.registerGridNodeHost(event, holder, MeReplicatorBlockEntity.class);
            case CHEMICAL_REPLICATOR -> ModBlockEntities.registerGridNodeHost(event, holder, MeChemicalReplicatorBlockEntity.class);
            case FLUID_REPLICATOR -> ModBlockEntities.registerGridNodeHost(event, holder, MeFluidReplicatorBlockEntity.class);
            case LARGE_ANTIPROTONIC_NUCLEOSYNTHESIZER -> ModBlockEntities.registerGridNodeHost(event, holder, MeLargeAntiprotonicNucleosynthesizerBlockEntity.class);
            default -> throw new IllegalStateException("Unknown MEKMM base machine: " + machine);
        }
    }
}
