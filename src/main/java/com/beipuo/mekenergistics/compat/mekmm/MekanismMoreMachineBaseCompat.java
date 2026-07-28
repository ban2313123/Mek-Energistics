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
import com.beipuo.mekenergistics.blockentity.compat.mekmm.machine.MeLargeChemicalInfuserBlockEntity;
import com.beipuo.mekenergistics.blockentity.compat.mekmm.machine.MeLargeElectrolyticSeparatorBlockEntity;
import com.beipuo.mekenergistics.blockentity.compat.mekmm.machine.MeLargeAntiprotonicNucleosynthesizerBlockEntity;
import com.beipuo.mekenergistics.blockentity.compat.mekmm.machine.MeLargeRotaryCondensentratorBlockEntity;
import com.beipuo.mekenergistics.blockentity.compat.mekmm.machine.MeLargeSolarNeutronActivatorBlockEntity;
import com.beipuo.mekenergistics.blockentity.compat.mekmm.machine.MeRollingMillBlockEntity;
import com.beipuo.mekenergistics.blockentity.compat.mekmm.machine.MeStamperBlockEntity;
import com.beipuo.mekenergistics.common.machine.MeMekanismMachine;
import com.beipuo.mekenergistics.compat.catalog.CompatFactoryTierGraph;
import com.beipuo.mekenergistics.compat.catalog.CompatMachineCatalog;
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
import com.jerry.meklm.common.registries.LargeMachineContainerTypes;
import com.jerry.mekmm.common.registries.MoreMachineContainerTypes;
import com.jerry.mekmm.common.config.MoreMachineConfig;
import java.util.function.LongSupplier;
import java.util.Locale;
import mekanism.api.Upgrade;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeCustomSelectionBox;
import mekanism.common.block.attribute.AttributeHasBounding;
import mekanism.common.block.attribute.AttributeHasBounding.HandleBoundingBlock;
import mekanism.common.block.attribute.AttributeHasBounding.TriBooleanFunction;
import mekanism.common.block.attribute.AttributeParticleFX;
import mekanism.common.block.attribute.AttributeSideConfig;
import mekanism.common.block.attribute.AttributeStateFacing;
import mekanism.common.block.attribute.AttributeTier;
import mekanism.common.block.attribute.AttributeUpgradeSupport;
import mekanism.common.block.attribute.Attributes;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.content.blocktype.Machine;
import mekanism.common.content.blocktype.Machine.MachineBuilder;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tier.FactoryTier;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.registries.MekanismSounds;
import mekanism.common.util.ChemicalUtil;
import mekanism.api.math.MathUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
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
            case LARGE_ROTARY_CONDENSENTRATOR -> registrar.register(machine, MeLargeRotaryCondensentratorBlockEntity::new);
            case LARGE_SOLAR_NEUTRON_ACTIVATOR -> registrar.register(machine, MeLargeSolarNeutronActivatorBlockEntity::new);
            case LARGE_ELECTROLYTIC_SEPARATOR -> registrar.register(machine, MeLargeElectrolyticSeparatorBlockEntity::new);
            case LARGE_CHEMICAL_INFUSER -> registrar.register(machine, MeLargeChemicalInfuserBlockEntity::new);
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

    /**
     * MoreMachine's own energy config. It lives in an optional mod, so the shared
     * {@code MeMachineEnergyProfile} cannot reach it and falls back to a neutral default for these
     * machines -- the mapping belongs here, behind the compat boundary, alongside the large-machine
     * wiring below.
     */
    private static LongSupplier moreMachineUsage(MeMekanismMachine machine) {
        return switch (machine) {
            case RECYCLER -> MoreMachineConfig.usage.recycler;
            case PLANTING_STATION -> MoreMachineConfig.usage.plantingStation;
            case CNC_STAMPER -> MoreMachineConfig.usage.cnc_stamper;
            case CNC_LATHE -> MoreMachineConfig.usage.cnc_lathe;
            case CNC_ROLLING_MILL -> MoreMachineConfig.usage.cnc_rollingMill;
            case REPLICATOR -> MoreMachineConfig.usage.itemReplicator;
            case CHEMICAL_REPLICATOR -> MoreMachineConfig.usage.chemicalReplicator;
            case FLUID_REPLICATOR -> MoreMachineConfig.usage.fluidReplicator;
            default -> machine.energyUsage();
        };
    }

    private static LongSupplier moreMachineStorage(MeMekanismMachine machine) {
        return switch (machine) {
            case RECYCLER -> MoreMachineConfig.storage.recycler;
            case PLANTING_STATION -> MoreMachineConfig.storage.plantingStation;
            case CNC_STAMPER -> MoreMachineConfig.storage.cnc_stamper;
            case CNC_LATHE -> MoreMachineConfig.storage.cnc_lathe;
            case CNC_ROLLING_MILL -> MoreMachineConfig.storage.cnc_rollingMill;
            case REPLICATOR -> MoreMachineConfig.storage.itemReplicator;
            case CHEMICAL_REPLICATOR -> MoreMachineConfig.storage.chemicalReplicator;
            case FLUID_REPLICATOR -> MoreMachineConfig.storage.fluidReplicator;
            default -> machine.energyStorage();
        };
    }

    public static <TILE extends TileEntityMekanism> BlockTypeTile<TILE> createBaseBlockType(
            MeMekanismMachine machine, TileEntityTypeRegistryObject<TILE> tileType) {
        if (machine.isMekmmLargeMachine()) {
            return createLargeMachineBlockType(machine, tileType);
        }
        var builder = BlockTypeTile.BlockTileBuilder
                .createBlock(() -> tileType, machine::translationKey)
                .withGui(() -> baseContainer(machine))
                .withEnergyConfig(moreMachineUsage(machine), moreMachineStorage(machine))
                .with(new AttributeStateFacing(), Attributes.ACTIVE_LIGHT, Attributes.INVENTORY, Attributes.REDSTONE, Attributes.SECURITY, Attributes.COMPARATOR)
                .withSideConfig(machine == MeMekanismMachine.CHEMICAL_REPLICATOR
                        ? new TransmissionType[] {TransmissionType.ITEM, TransmissionType.ENERGY, TransmissionType.CHEMICAL}
                        : machine == MeMekanismMachine.FLUID_REPLICATOR
                        ? new TransmissionType[] {TransmissionType.ITEM, TransmissionType.ENERGY, TransmissionType.CHEMICAL, TransmissionType.FLUID}
                        : new TransmissionType[] {TransmissionType.ITEM, TransmissionType.ENERGY})
                .withSupportedUpgrades(Upgrade.SPEED, Upgrade.ENERGY);
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
    private static <TILE extends TileEntityMekanism> BlockTypeTile<TILE> createLargeMachineBlockType(
            MeMekanismMachine machine, TileEntityTypeRegistryObject<TILE> tileType) {
        var description = com.beipuo.mekenergistics.common.MeLangEntry.of(machine.translationKey());
        Machine<?> built = switch (machine) {
            case LARGE_ROTARY_CONDENSENTRATOR -> MachineBuilder
                    .createMachine(() -> tileType, description)
                    .withGui(() -> ModMenuTypes.ME_LARGE_ROTARY_CONDENSENTRATOR)
                    .withSound(MekanismSounds.ROTARY_CONDENSENTRATOR)
                    .withEnergyConfig(MoreMachineConfig.usage.largeRotaryCondensentrator,
                            MoreMachineConfig.storage.largeRotaryCondensentrator)
                    .withSideConfig(TransmissionType.CHEMICAL, TransmissionType.FLUID,
                            TransmissionType.ITEM, TransmissionType.ENERGY)
                    .withCustomShape(LargeMachineBlockShapes.LARGE_ROTARY_CONDENSENTRATOR)
                    .with(AttributeCustomSelectionBox.JSON)
                    .with(MoreMachineBounding.FULL_JAVA_ENTITY)
                    .build();
            case LARGE_CHEMICAL_INFUSER -> MachineBuilder
                    .createMachine(() -> tileType, description)
                    .withGui(() -> ModMenuTypes.ME_LARGE_CHEMICAL_INFUSER)
                    .withSound(MekanismSounds.CHEMICAL_INFUSER)
                    .withEnergyConfig(MoreMachineConfig.usage.largeChemicalInfuser,
                            MoreMachineConfig.storage.largeChemicalInfuser)
                    .withSideConfig(TransmissionType.CHEMICAL, TransmissionType.ITEM, TransmissionType.ENERGY)
                    .withCustomShape(LargeMachineBlockShapes.LARGE_CHEMICAL_INFUSER)
                    .with(AttributeCustomSelectionBox.JSON)
                    .with(MoreMachineBounding.FULL_JAVA_ENTITY_BUT_TOP_BACK_2X3)
                    .build();
            case LARGE_ELECTROLYTIC_SEPARATOR -> MachineBuilder
                    .createMachine(() -> tileType, description)
                    .withGui(() -> ModMenuTypes.ME_LARGE_ELECTROLYTIC_SEPARATOR)
                    .withSound(MekanismSounds.ELECTROLYTIC_SEPARATOR)
                    .withEnergyConfig(() -> MathUtils.multiplyClamped(2, ChemicalUtil.hydrogenEnergyDensity()),
                            MoreMachineConfig.storage.largeElectrolyticSeparator)
                    .withSideConfig(TransmissionType.FLUID, TransmissionType.CHEMICAL,
                            TransmissionType.ITEM, TransmissionType.ENERGY)
                    .withCustomShape(LargeMachineBlockShapes.LARGE_ELECTROLYTIC_SEPARATOR)
                    .with(AttributeCustomSelectionBox.JSON)
                    .withBounding(largeElectrolyticBounding())
                    .build();
            case LARGE_SOLAR_NEUTRON_ACTIVATOR -> MachineBuilder
                    .createMachine(() -> tileType, description)
                    .withGui(() -> ModMenuTypes.ME_LARGE_SOLAR_NEUTRON_ACTIVATOR)
                    .without(AttributeParticleFX.class)
                    .withSupportedUpgrades(Upgrade.SPEED, Upgrade.MUFFLING)
                    .withCustomShape(LargeMachineBlockShapes.LARGE_SOLAR_NEUTRON_ACTIVATOR)
                    .with(AttributeCustomSelectionBox.JSON)
                    .withSideConfig(TransmissionType.CHEMICAL, TransmissionType.ITEM)
                    .with(MoreMachineBounding.FULL_JAVA_ENTITY)
                    .replace(Attributes.ACTIVE)
                    .build();
            case LARGE_ANTIPROTONIC_NUCLEOSYNTHESIZER -> MachineBuilder
                    .createMachine(() -> tileType, description)
                    .withGui(() -> ModMenuTypes.ME_LARGE_ANTIPROTONIC_NUCLEOSYNTHESIZER)
                    .withEnergyConfig(MoreMachineConfig.usage.largeAntiprotonicNucleosynthesizer,
                            MoreMachineConfig.storage.largeAntiprotonicNucleosynthesizer)
                    .withSound(MekanismSounds.ANTIPROTONIC_NUCLEOSYNTHESIZER)
                    .with(AttributeUpgradeSupport.MUFFLING_ONLY)
                    .with(AttributeSideConfig.ADVANCED_ELECTRIC_MACHINE)
                    .withCustomShape(LargeMachineBlockShapes.LARGE_ANTIPROTONIC_NUCLEOSYNTHESIZER)
                    .with(MoreMachineBounding.FULL_JAVA_ENTITY)
                    .build();
            default -> throw new IllegalStateException("Unknown MekMM large machine: " + machine);
        };
        return (BlockTypeTile<TILE>) built;
    }

    private static HandleBoundingBlock largeElectrolyticBounding() {
        return new HandleBoundingBlock() {
            @Override
            public <DATA> boolean handle(Level level, BlockPos pos, BlockState state, DATA data,
                    TriBooleanFunction<Level, BlockPos, DATA> consumer) {
                BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
                for (int x = -1; x <= 1; x++) {
                    for (int y = 0; y <= 1; y++) {
                        for (int z = -1; z <= 1; z++) {
                            if ((x != 0 || y != 0 || z != 0)
                                    && !consumer.accept(level, mutable.setWithOffset(pos, x, y, z), data)) {
                                return false;
                            }
                        }
                    }
                }
                return true;
            }
        };
    }

    public static boolean hasAvailableLargeMachines() {
        return CompatMachineCatalog.all()
                .filter(spec -> spec.machine().isMekmmLargeMachine())
                .anyMatch(spec -> CompatMachineCatalog.isAvailable(spec.machine()));
    }

    public static MoreMachineFactoryType moreMachineFactoryType(MeMekanismMachine machine) {
        String typeName = machine.machineTypeId();
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
    public static ContainerTypeRegistryObject<? extends MekanismTileContainer<?>> meBaseContainer(
            MeMekanismMachine machine) {
        return switch (machine) {
            case LARGE_ROTARY_CONDENSENTRATOR -> ModMenuTypes.ME_LARGE_ROTARY_CONDENSENTRATOR;
            case LARGE_SOLAR_NEUTRON_ACTIVATOR -> ModMenuTypes.ME_LARGE_SOLAR_NEUTRON_ACTIVATOR;
            case LARGE_ELECTROLYTIC_SEPARATOR -> ModMenuTypes.ME_LARGE_ELECTROLYTIC_SEPARATOR;
            case LARGE_CHEMICAL_INFUSER -> ModMenuTypes.ME_LARGE_CHEMICAL_INFUSER;
            case LARGE_ANTIPROTONIC_NUCLEOSYNTHESIZER -> ModMenuTypes.ME_LARGE_ANTIPROTONIC_NUCLEOSYNTHESIZER;
            default -> (ContainerTypeRegistryObject) ModMenuTypes.getCoreMachineContainer(machine);
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
            case LARGE_ROTARY_CONDENSENTRATOR -> LargeMachineContainerTypes.LARGE_ROTARY_CONDENSENTRATOR;
            case LARGE_SOLAR_NEUTRON_ACTIVATOR -> LargeMachineContainerTypes.LARGE_SOLAR_NEUTRON_ACTIVATOR;
            case LARGE_ELECTROLYTIC_SEPARATOR -> LargeMachineContainerTypes.LARGE_ELECTROLYTIC_SEPARATOR;
            case LARGE_CHEMICAL_INFUSER -> LargeMachineContainerTypes.LARGE_CHEMICAL_INFUSER;
            case LARGE_ANTIPROTONIC_NUCLEOSYNTHESIZER -> LargeMachineContainerTypes.LARGE_ANTIPROTONIC_NUCLEOSYNTHESIZER;
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
        return CompatMachineCatalog.findBySourceBlockId(id)
                .filter(spec -> spec.route() == CompatRegistrationRoute.MEKMM_MACHINE
                        || spec.route() == CompatRegistrationRoute.MEKMM_FACTORY)
                .map(spec -> spec.machine())
                .orElse(null);
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
            case LARGE_ROTARY_CONDENSENTRATOR, LARGE_SOLAR_NEUTRON_ACTIVATOR,
                    LARGE_ELECTROLYTIC_SEPARATOR, LARGE_CHEMICAL_INFUSER,
                    LARGE_ANTIPROTONIC_NUCLEOSYNTHESIZER -> {
                // The shared node is exposed by one outer bounding block, not by the inaccessible controller.
            }
            default -> throw new IllegalStateException("Unknown MEKMM base machine: " + machine);
        }
    }
}
