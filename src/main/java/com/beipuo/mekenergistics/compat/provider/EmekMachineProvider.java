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
import com.beipuo.mekenergistics.compat.catalog.CompatFactoryTierGraph;
import com.beipuo.mekenergistics.compat.catalog.CompatMachineFamily;
import com.beipuo.mekenergistics.compat.catalog.CompatMachineSpec;
import com.beipuo.mekenergistics.compat.catalog.CompatMod;
import com.beipuo.mekenergistics.compat.eme.EvolvedMekanismCompat;
import com.beipuo.mekenergistics.compat.eme.EvolvedMekanismMachineMenuTypes;
import fr.iglee42.evolvedmekanism.config.EMConfig;
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
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jetbrains.annotations.Nullable;
import java.util.Map;

public final class EmekMachineProvider extends AbstractCompatMachineProvider implements CompatMachineProvider {
    public EmekMachineProvider() {
        super(CompatMod.EMEK, familyAdapters());
    }

    @Override
    public void registerMenus(mekanism.common.registration.impl.ContainerTypeDeferredRegister register) {
        EvolvedMekanismMachineMenuTypes.register(register);
    }

    private static Map<CompatMachineFamily, CompatMachineFamilyAdapter> familyAdapters() {
        return Map.of(
                CompatMachineFamily.EMEK_MACHINE,
                CompatMachineFamilyAdapter.of(
                        spec -> spec.machine() == MeMekanismMachine.THERMALIZER
                                ? EvolvedMekanismMachineMenuTypes.ME_THERMALIZER
                                : ModMenuTypes.getCoreMachineContainer(spec.machine()),
                        (spec, registrar) -> registerMachine(spec.machine(), registrar),
                        EmekMachineProvider::createMachineBlockType,
                        EmekMachineProvider::registerMachineGridNodeHost),
                CompatMachineFamily.EMEK_FACTORY,
                CompatMachineFamilyAdapter.of(
                        spec -> ModMenuTypes.ME_FACTORY,
                        (spec, registrar) -> registerFactory(spec.machine(), registrar),
                        EmekMachineProvider::createMachineBlockType,
                        EmekMachineProvider::registerFactoryGridNodeHost));
    }

    @Override
    public boolean isInstaller(ItemStack stack) {
        return EvolvedMekanismCompat.isInstaller(stack);
    }

    @Override
    @Nullable
    public MeMekanismMachine resolveInstallerUpgrade(MeMekanismMachine current, ItemStack stack) {
        if (!EvolvedMekanismCompat.isInstaller(stack)) {
            return null;
        }
        String targetTier = EMConfig.general.maxInstallerTier.getOrDefault().getLowerName();
        return CompatFactoryTierGraph.forwardFactoryAtTier(current, CompatMod.EMEK, targetTier);
    }

    private static TileEntityTypeRegistryObject<? extends TileEntityMekanism> registerMachine(
            MeMekanismMachine machine, MachineFactoryRegistrar registrar) {
        return switch (machine.identity()) {
            case ALLOYER -> registrar.register(machine, MeAlloyerBlockEntity::new);
            case SOLIDIFICATION_CHAMBER -> registrar.register(machine, MeSolidifierBlockEntity::new);
            case THERMALIZER -> registrar.register(machine, MeThermalizerBlockEntity::new);
            case CHEMIXER -> registrar.register(machine, MeChemixerBlockEntity::new);
            default -> throw new IllegalArgumentException("Unknown EMEK machine " + machine);
        };
    }

    private static TileEntityTypeRegistryObject<? extends TileEntityMekanism> registerFactory(
            MeMekanismMachine machine, MachineFactoryRegistrar registrar) {
        if ("alloying".equals(machine.machineTypeId())) {
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

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static BlockTypeTile<? extends TileEntityMekanism> createMachineBlockType(
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
        } else if ("alloying".equals(machine.machineTypeId())) {
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
        if ("alloying".equals(machine.machineTypeId()) && machine.factoryTier() != null) {
            return EvolvedMekanismCompat.alloyingFactoryShape(machine.factoryTier());
        }
        return switch (machine.identity()) {
            case SOLIDIFICATION_CHAMBER, THERMALIZER, CHEMIXER -> EvolvedMekanismCompat.shapeFor(machine);
            default -> null;
        };
    }

    private static TransmissionType[] sideConfigFor(MeMekanismMachine machine) {
        return switch (machine.identity()) {
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

    private static void registerMachineGridNodeHost(CompatMachineSpec spec, RegisterCapabilitiesEvent event,
            TileEntityTypeRegistryObject<? extends TileEntityMekanism> holder) {
        Class<? extends IInWorldGridNodeHost> host = switch (spec.machine().identity()) {
            case ALLOYER -> MeAlloyerBlockEntity.class;
            case SOLIDIFICATION_CHAMBER -> MeSolidifierBlockEntity.class;
            case THERMALIZER -> MeThermalizerBlockEntity.class;
            case CHEMIXER -> MeChemixerBlockEntity.class;
            default -> throw new IllegalArgumentException("Unknown EMEK machine " + spec.machine());
        };
        ModBlockEntities.registerGridNodeHost(event, holder, host);
    }

    private static void registerFactoryGridNodeHost(CompatMachineSpec spec, RegisterCapabilitiesEvent event,
            TileEntityTypeRegistryObject<? extends TileEntityMekanism> holder) {
        ModBlockEntities.registerGridNodeHost(event, holder, factoryGridHost(spec.machine()));
    }

    private static Class<? extends IInWorldGridNodeHost> factoryGridHost(MeMekanismMachine machine) {
        if ("alloying".equals(machine.machineTypeId())) {
            return MeAlloyingFactoryBlockEntity.class;
        }
        return switch (machine.factoryType()) {
            case SMELTING, ENRICHING, CRUSHING -> MeItemStackToItemStackFactoryBlockEntity.class;
            case COMPRESSING, INJECTING, PURIFYING, INFUSING -> MeItemStackChemicalToItemStackFactoryBlockEntity.class;
            case COMBINING -> MeCombiningFactoryBlockEntity.class;
            case SAWING -> MeSawingFactoryBlockEntity.class;
        };
    }
}
