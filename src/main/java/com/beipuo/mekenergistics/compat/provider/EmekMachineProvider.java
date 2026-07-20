package com.beipuo.mekenergistics.compat.provider;

import appeng.api.networking.IInWorldGridNodeHost;
import com.beipuo.mekenergistics.block.attribute.MeUpgradeableAttribute;
import com.beipuo.mekenergistics.blockentity.compat.eme.factory.MeAlloyingFactoryBlockEntity;
import com.beipuo.mekenergistics.blockentity.compat.eme.machine.MeAlloyerBlockEntity;
import com.beipuo.mekenergistics.blockentity.compat.eme.machine.MeChemixerBlockEntity;
import com.beipuo.mekenergistics.blockentity.compat.eme.machine.MeSolidifierBlockEntity;
import com.beipuo.mekenergistics.blockentity.compat.eme.machine.MeThermalizerBlockEntity;
import com.beipuo.mekenergistics.blockentity.factory.MeCombiningFactoryBlockEntity;
import com.beipuo.mekenergistics.blockentity.factory.MeItemStackChemicalToItemStackFactoryBlockEntity;
import com.beipuo.mekenergistics.blockentity.factory.MeItemStackToItemStackFactoryBlockEntity;
import com.beipuo.mekenergistics.blockentity.factory.MeSawingFactoryBlockEntity;
import com.beipuo.mekenergistics.common.machine.MeMekanismMachine;
import com.beipuo.mekenergistics.compat.catalog.CompatMachineCatalog;
import com.beipuo.mekenergistics.compat.catalog.CompatMachineKind;
import com.beipuo.mekenergistics.compat.catalog.CompatMachineSpec;
import com.beipuo.mekenergistics.compat.catalog.CompatMod;
import com.beipuo.mekenergistics.compat.eme.EvolvedMekanismCompat;
import com.beipuo.mekenergistics.registry.ModBlockEntities;
import com.beipuo.mekenergistics.registry.ModBlocks;
import com.beipuo.mekenergistics.registry.ModMenuTypes;
import com.beipuo.mekenergistics.registry.machine.MachineFactoryRegistrar;
import mekanism.api.Upgrade;
import mekanism.common.block.attribute.AttributeFactoryType;
import mekanism.common.block.attribute.AttributeStateFacing;
import mekanism.common.block.attribute.AttributeTier;
import mekanism.common.block.attribute.AttributeUpgradeSupport;
import mekanism.common.block.attribute.Attributes;
import mekanism.common.content.blocktype.BlockShapes;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jetbrains.annotations.Nullable;

public final class EmekMachineProvider implements CompatMachineProvider {
    public EmekMachineProvider() {
    }

    @Override
    public boolean isInstaller(ItemStack stack) {
        return EvolvedMekanismCompat.isInstaller(stack);
    }

    @Override
    public ContainerTypeRegistryObject<? extends MekanismTileContainer<?>> menuType(
            CompatMachineSpec spec) {
        return switch (spec.route()) {
            case EMEK_FACTORY -> ModMenuTypes.ME_FACTORY;
            case EMEK_MACHINE -> ModMenuTypes.getCoreMachineContainer(spec.machine());
            default -> throw wrongRoute(spec);
        };
    }

    @Override
    public TileEntityTypeRegistryObject<? extends TileEntityMekanism> registerBlockEntity(
            CompatMachineSpec spec, MachineFactoryRegistrar registrar) {
        return switch (spec.route()) {
            case EMEK_MACHINE -> switch (spec.machine()) {
                case ALLOYER -> registrar.register(spec.machine(), MeAlloyerBlockEntity::new);
                case SOLIDIFICATION_CHAMBER -> registrar.register(spec.machine(), MeSolidifierBlockEntity::new);
                case THERMALIZER -> registrar.register(spec.machine(), MeThermalizerBlockEntity::new);
                case CHEMIXER -> registrar.register(spec.machine(), MeChemixerBlockEntity::new);
                default -> throw new IllegalArgumentException("Unknown EMEK machine " + spec.machine());
            };
            case EMEK_FACTORY -> registerFactory(spec.machine(), registrar);
            default -> throw wrongRoute(spec);
        };
    }

    private static TileEntityTypeRegistryObject<? extends TileEntityMekanism> registerFactory(
            MeMekanismMachine machine, MachineFactoryRegistrar registrar) {
        if ("alloying".equals(machine.customFactoryTypeName())) {
            return registrar.register(machine, MeAlloyingFactoryBlockEntity::new);
        }
        return switch (machine.factoryType()) {
            case SMELTING, ENRICHING, CRUSHING -> registrar.register(machine, MeItemStackToItemStackFactoryBlockEntity::new);
            case COMPRESSING, INJECTING, PURIFYING, INFUSING ->
                    registrar.register(machine, MeItemStackChemicalToItemStackFactoryBlockEntity::new);
            case COMBINING -> registrar.register(machine, MeCombiningFactoryBlockEntity::new);
            case SAWING -> registrar.register(machine, MeSawingFactoryBlockEntity::new);
        };
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public BlockTypeTile<? extends TileEntityMekanism> createBlockType(
            CompatMachineSpec spec, TileEntityTypeRegistryObject<? extends TileEntityMekanism> tileType) {
        return createBlockTypeTyped(spec, (TileEntityTypeRegistryObject) tileType);
    }

    private static <TILE extends TileEntityMekanism> BlockTypeTile<TILE> createBlockTypeTyped(
            CompatMachineSpec spec, TileEntityTypeRegistryObject<TILE> tileType) {
        MeMekanismMachine machine = spec.machine();
        var builder = BlockTypeTile.BlockTileBuilder
                .createBlock(() -> tileType, machine::translationKey)
                .withGui(() -> ModMenuTypes.getMachineContainer(machine))
                .withEnergyConfig(machine.energyUsage(), machine.energyStorage())
                .with(new AttributeStateFacing(), Attributes.ACTIVE_LIGHT, Attributes.INVENTORY, Attributes.REDSTONE,
                        Attributes.SECURITY, Attributes.COMPARATOR)
                .withSideConfig(sideConfigFor(machine))
                .with(machine.isEvolvedMekanismFactory()
                        ? AttributeUpgradeSupport.DEFAULT_MACHINE_UPGRADES
                        : AttributeUpgradeSupport.create(Upgrade.SPEED, Upgrade.ENERGY));
        if (machine.factoryType() != null) {
            builder.with(new AttributeFactoryType(machine.factoryType()));
        } else if ("alloying".equals(machine.customFactoryTypeName())) {
            EvolvedMekanismCompat.withAlloyingFactoryType(builder);
        }
        if (machine.factoryTier() != null) {
            builder.with(new AttributeTier<>(machine.factoryTier()));
        }
        VoxelShape[] shape = customShapeFor(machine);
        if (shape != null) {
            builder.withCustomShape(shape);
        }
        MeMekanismMachine target = machine.isFactory() ? machine.getNextFactory() : machine.getBasicFactory();
        if (target != null) {
            builder.with(new MeUpgradeableAttribute(() -> ModBlocks.getMachineBlock(target).get()));
            if (target.provider() == CompatMod.MEKE && CompatMachineCatalog.isAvailable(target)) {
                CompatMachineProviders.get(CompatMod.MEKE)
                        .addUpgradeAttribute(builder, () -> ModBlocks.getMachineBlock(target).get());
            }
        }
        return builder.build();
    }

    @Nullable
    private static VoxelShape[] customShapeFor(MeMekanismMachine machine) {
        if (machine.factoryType() != null && machine.factoryTier() != null) {
            return BlockShapes.getShape(machine.factoryTier(), machine.factoryType());
        }
        if ("alloying".equals(machine.customFactoryTypeName()) && machine.factoryTier() != null) {
            return EvolvedMekanismCompat.alloyingFactoryShape(machine.factoryTier());
        }
        return switch (machine) {
            case SOLIDIFICATION_CHAMBER, THERMALIZER, CHEMIXER -> EvolvedMekanismCompat.shapeFor(machine);
            default -> null;
        };
    }

    private static TransmissionType[] sideConfigFor(MeMekanismMachine machine) {
        return switch (machine) {
            case SOLIDIFICATION_CHAMBER ->
                    new TransmissionType[] {TransmissionType.FLUID, TransmissionType.ITEM, TransmissionType.ENERGY};
            case THERMALIZER ->
                    new TransmissionType[] {TransmissionType.ITEM, TransmissionType.FLUID, TransmissionType.HEAT};
            case CHEMIXER ->
                    new TransmissionType[] {TransmissionType.ITEM, TransmissionType.ENERGY, TransmissionType.CHEMICAL};
            default -> machine.hasChemicalInput()
                    ? new TransmissionType[] {TransmissionType.ITEM, TransmissionType.ENERGY, TransmissionType.CHEMICAL}
                    : new TransmissionType[] {TransmissionType.ITEM, TransmissionType.ENERGY};
        };
    }

    @Override
    public void registerGridNodeHost(CompatMachineSpec spec, RegisterCapabilitiesEvent event,
            TileEntityTypeRegistryObject<? extends TileEntityMekanism> holder) {
        Class<? extends IInWorldGridNodeHost> host = spec.kind() == CompatMachineKind.MACHINE
                ? switch (spec.machine()) {
                    case ALLOYER -> MeAlloyerBlockEntity.class;
                    case SOLIDIFICATION_CHAMBER -> MeSolidifierBlockEntity.class;
                    case THERMALIZER -> MeThermalizerBlockEntity.class;
                    case CHEMIXER -> MeChemixerBlockEntity.class;
                    default -> throw new IllegalArgumentException("Unknown EMEK machine " + spec.machine());
                }
                : factoryGridHost(spec.machine());
        ModBlockEntities.registerGridNodeHost(event, holder, host);
    }

    private static Class<? extends IInWorldGridNodeHost> factoryGridHost(MeMekanismMachine machine) {
        if ("alloying".equals(machine.customFactoryTypeName())) {
            return MeAlloyingFactoryBlockEntity.class;
        }
        return switch (machine.factoryType()) {
            case SMELTING, ENRICHING, CRUSHING -> MeItemStackToItemStackFactoryBlockEntity.class;
            case COMPRESSING, INJECTING, PURIFYING, INFUSING -> MeItemStackChemicalToItemStackFactoryBlockEntity.class;
            case COMBINING -> MeCombiningFactoryBlockEntity.class;
            case SAWING -> MeSawingFactoryBlockEntity.class;
        };
    }

    private static IllegalArgumentException wrongRoute(CompatMachineSpec spec) {
        return new IllegalArgumentException("EMEK provider cannot handle " + spec.route());
    }
}
