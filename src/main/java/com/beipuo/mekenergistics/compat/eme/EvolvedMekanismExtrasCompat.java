package com.beipuo.mekenergistics.compat.eme;

import com.beipuo.mekenergistics.block.attribute.MeUpgradeableAttribute;
import com.beipuo.mekenergistics.blockentity.compat.eme.factory.MeEMExtraAlloyingFactoryBlockEntity;
import com.beipuo.mekenergistics.blockentity.compat.eme.factory.MeEMExtraCombiningFactoryBlockEntity;
import com.beipuo.mekenergistics.blockentity.compat.eme.factory.MeEMExtraItemStackChemicalToItemStackFactoryBlockEntity;
import com.beipuo.mekenergistics.blockentity.compat.eme.factory.MeEMExtraItemStackToItemStackFactoryBlockEntity;
import com.beipuo.mekenergistics.blockentity.compat.eme.factory.MeEMExtraDissolvingFactoryBlockEntity;
import com.beipuo.mekenergistics.blockentity.compat.eme.factory.MeEMExtraWashingFactoryBlockEntity;
import com.beipuo.mekenergistics.blockentity.compat.eme.factory.MeEMExtraCrystallizingFactoryBlockEntity;
import com.beipuo.mekenergistics.blockentity.compat.eme.factory.MeEMExtraPressurizedReactingFactoryBlockEntity;
import com.beipuo.mekenergistics.blockentity.compat.eme.factory.MeEMExtraCentrifugingFactoryBlockEntity;
import com.beipuo.mekenergistics.blockentity.compat.eme.factory.MeEMExtraLiquifyingFactoryBlockEntity;
import com.beipuo.mekenergistics.blockentity.compat.eme.factory.MeEMExtraPaintingFactoryBlockEntity;
import com.beipuo.mekenergistics.blockentity.compat.eme.factory.MeEMExtraPlantingFactoryBlockEntity;
import com.beipuo.mekenergistics.blockentity.compat.eme.factory.MeEMExtraReplicatingFactoryBlockEntity;
import com.beipuo.mekenergistics.blockentity.compat.eme.factory.MeEMExtraItemToChemicalFactoryBlockEntity;
import com.beipuo.mekenergistics.blockentity.compat.eme.factory.MeEMExtraSawingFactoryBlockEntity;
import com.beipuo.mekenergistics.common.machine.MeMekanismMachine;
import com.beipuo.mekenergistics.compat.catalog.CompatFactoryTierGraph;
import com.beipuo.mekenergistics.compat.catalog.CompatMachineCatalog;
import com.beipuo.mekenergistics.compat.catalog.CompatMachineFamily;
import com.beipuo.mekenergistics.compat.catalog.CompatMachineKind;
import com.beipuo.mekenergistics.compat.catalog.CompatMod;
import com.beipuo.mekenergistics.registry.ModBlockEntities;
import com.beipuo.mekenergistics.registry.ModBlocks;
import com.beipuo.mekenergistics.registry.ModMenuTypes;
import com.beipuo.mekenergistics.registry.machine.MachineFactoryRegistrar;
import com.jerry.mekextras.common.block.attribute.ExtraAttributeUpgradeSupport;
import fr.iglee42.evolvedmekanism.registries.EMFactoryType;
import io.github.masyumero.emextras.common.block.attribute.EMExtraAttribute;
import io.github.masyumero.emextras.common.block.attribute.EMExtraAttributeFactoryType;
import io.github.masyumero.emextras.common.block.attribute.EMExtraAttributeTier;
import io.github.masyumero.emextras.common.content.blocktype.EMExtraBlockShapes;
import io.github.masyumero.emextras.common.content.blocktype.EMExtraFactoryType;
import io.github.masyumero.emextras.api.tier.EMExtraTier;
import io.github.masyumero.emextras.common.item.EMExtraItemTierInstaller;
import io.github.masyumero.emextras.common.tier.EMExtraFactoryTier;
import com.jerry.mekaf.common.content.blocktype.AdvancedFactoryType;
import com.jerry.mekaf.common.block.attribute.AttributeAdvancedFactoryType;
import com.jerry.mekmm.common.content.blocktype.MoreMachineFactoryType;
import com.jerry.mekmm.common.block.attribute.MoreMachineAttributeFactoryType;
import io.github.masyumero.emextras.common.integration.mekaf.content.blocktype.EMExtraAdvancedFactory;
import io.github.masyumero.emextras.common.integration.mekmm.content.blocktype.EMExtraMoreMachineFactory;
import java.util.Locale;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeFactoryType;
import mekanism.common.block.attribute.AttributeGui;
import mekanism.common.block.attribute.AttributeStateFacing;
import mekanism.common.block.attribute.AttributeUpgradeSupport;
import mekanism.common.block.attribute.Attributes;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.content.blocktype.FactoryType;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tier.FactoryTier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jetbrains.annotations.Nullable;

public final class EvolvedMekanismExtrasCompat {
    private EvolvedMekanismExtrasCompat() {
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static TileEntityTypeRegistryObject<? extends TileEntityMekanism> registerFactoryMachine(
            MeMekanismMachine machine, MachineFactoryRegistrar registrar) {
        if (CompatMachineCatalog.get(machine).kind() == CompatMachineKind.ADVANCED_FACTORY) {
            return registerAdvancedFactoryMachine(machine, registrar);
        }
        if ("alloying".equals(machine.machineTypeId())) {
            return registrar.register(machine, MeEMExtraAlloyingFactoryBlockEntity::new);
        }
        TileEntityTypeRegistryObject<?> registered = switch (machine.factoryType()) {
            case SMELTING, ENRICHING, CRUSHING -> registrar.register(machine, MeEMExtraItemStackToItemStackFactoryBlockEntity::new);
            case COMPRESSING, INJECTING, PURIFYING, INFUSING -> registrar.register(machine, MeEMExtraItemStackChemicalToItemStackFactoryBlockEntity::new);
            case COMBINING -> registrar.register(machine, MeEMExtraCombiningFactoryBlockEntity::new);
            case SAWING -> registrar.register(machine, MeEMExtraSawingFactoryBlockEntity::new);
        };
        return (TileEntityTypeRegistryObject) registered;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static TileEntityTypeRegistryObject<? extends TileEntityMekanism> registerAdvancedFactoryMachine(
            MeMekanismMachine machine, MachineFactoryRegistrar registrar) {
        TileEntityTypeRegistryObject<?> registered = switch (machine.machineTypeId()) {
            case "oxidizing", "pigment_extracting" -> registrar.register(machine, MeEMExtraItemToChemicalFactoryBlockEntity::new);
            case "dissolving" -> registrar.register(machine, MeEMExtraDissolvingFactoryBlockEntity::new);
            case "painting" -> registrar.register(machine, MeEMExtraPaintingFactoryBlockEntity::new);
            case "planting" -> registrar.register(machine, MeEMExtraPlantingFactoryBlockEntity::new);
            case "replicating" -> registrar.register(machine, MeEMExtraReplicatingFactoryBlockEntity::new);
            case "washing" -> registrar.register(machine, MeEMExtraWashingFactoryBlockEntity::new);
            case "crystallizing" -> registrar.register(machine, MeEMExtraCrystallizingFactoryBlockEntity::new);
            case "pressurised_reacting" -> registrar.register(machine, MeEMExtraPressurizedReactingFactoryBlockEntity::new);
            case "centrifuging" -> registrar.register(machine, MeEMExtraCentrifugingFactoryBlockEntity::new);
            case "liquifying" -> registrar.register(machine, MeEMExtraLiquifyingFactoryBlockEntity::new);
            default -> throw new IllegalStateException("Unknown EMEKE advanced factory: " + machine.machineTypeId());
        };
        return (TileEntityTypeRegistryObject) registered;
    }

    public static <TILE extends TileEntityMekanism> BlockTypeTile<TILE> createFactoryBlockType(
            MeMekanismMachine machine, TileEntityTypeRegistryObject<TILE> tileType) {
        if (CompatMachineCatalog.get(machine).kind() == CompatMachineKind.ADVANCED_FACTORY) {
            return createAdvancedFactoryBlockType(machine, tileType);
        }
        EMExtraFactoryType type = emExtraFactoryType(machine.machineTypeId());
        var builder = BlockTypeTile.BlockTileBuilder
                .createBlock(() -> tileType, machine::translationKey)
                .withGui(() -> ModMenuTypes.ME_EM_EXTRA_FACTORY)
                .withEnergyConfig(machine.energyUsage(), machine.energyStorage())
                .with(new AttributeStateFacing(), Attributes.ACTIVE_LIGHT, Attributes.INVENTORY, Attributes.REDSTONE, Attributes.SECURITY, Attributes.COMPARATOR)
                .withSideConfig(machine.hasChemicalInput()
                        ? new TransmissionType[] {TransmissionType.ITEM, TransmissionType.ENERGY, TransmissionType.CHEMICAL}
                        : new TransmissionType[] {TransmissionType.ITEM, TransmissionType.ENERGY})
                .with(emExtraUpgradeSupport(machine.machineTypeId()))
                .with(new AttributeFactoryType(attributeFactoryType(machine)))
                .with(new EMExtraAttributeFactoryType(type))
                .with(new EMExtraAttributeTier<>(emExtraTier(machine)))
                .withCustomShape(EMExtraBlockShapes.getShape(type));
        MeMekanismMachine upgradeTarget = machine.getNextFactory();
        if (upgradeTarget != null) {
            builder.with(new MeUpgradeableAttribute(() -> ModBlocks.getMachineBlock(upgradeTarget).get()));
        }
        return builder.build();
    }

    @SuppressWarnings("unchecked")
    private static <TILE extends TileEntityMekanism> BlockTypeTile<TILE> createAdvancedFactoryBlockType(
            MeMekanismMachine machine, TileEntityTypeRegistryObject<TILE> tileType) {
        EMExtraFactoryTier tier = emExtraTier(machine);
        CompatMachineFamily family = CompatMachineCatalog.get(machine).family();
        if (family == CompatMachineFamily.EMEKE_MEKAF_ADVANCED_FACTORY) {
            var builder = EMExtraAdvancedFactory.EMExtraAdvancedFactoryBuilder.createAdvancedFactory(
                    () -> tileType, AdvancedFactoryType.valueOf(machine.machineTypeId().toUpperCase(Locale.ROOT)), tier);
            builder.replace(new AttributeGui(() -> ModMenuTypes.ME_EM_EXTRA_ADVANCED_FACTORY, null));
            return (BlockTypeTile<TILE>) builder.build();
        }
        if (family != CompatMachineFamily.EMEKE_MEKMM_FACTORY) {
            throw new IllegalStateException("Unsupported EMEKE factory family: " + family);
        }
        var builder = EMExtraMoreMachineFactory.EMExtraMoreMachineFactoryBuilder.createMoreMachineFactory(
                () -> tileType, MoreMachineFactoryType.valueOf(machine.machineTypeId().equals("planting") ? "PLANTING_STATION" : "REPLICATING"), tier);
        builder.replace(new AttributeGui(() -> ModMenuTypes.ME_EM_EXTRA_MORE_MACHINE_FACTORY, null));
        return (BlockTypeTile<TILE>) builder.build();
    }

    private static AttributeUpgradeSupport emExtraUpgradeSupport(String typeName) {
        if ("alloying".equals(typeName)) {
            return ExtraAttributeUpgradeSupport.EXTRA_MACHINE_UPGRADES;
        }
        return switch (FactoryType.valueOf(typeName.toUpperCase(Locale.ROOT))) {
            case PURIFYING, INJECTING -> ExtraAttributeUpgradeSupport.EXTRA_ADVANCED_MACHINE_UPGRADES;
            case SMELTING, ENRICHING, CRUSHING, COMPRESSING, COMBINING, INFUSING, SAWING -> ExtraAttributeUpgradeSupport.EXTRA_MACHINE_UPGRADES;
        };
    }

    public static EMExtraFactoryTier emExtraTier(MeMekanismMachine machine) {
        return EMExtraFactoryTier.valueOf(machine.tierId().toUpperCase(Locale.ROOT));
    }

    public static EMExtraFactoryType emExtraFactoryType(FactoryType type) {
        return EMExtraFactoryType.valueOf(type.name());
    }

    public static EMExtraFactoryType emExtraFactoryType(String typeName) {
        return EMExtraFactoryType.valueOf(typeName.toUpperCase(Locale.ROOT));
    }

    private static FactoryType attributeFactoryType(MeMekanismMachine machine) {
        return "alloying".equals(machine.machineTypeId()) ? EMFactoryType.ALLOYING : machine.factoryType();
    }

    @Nullable
    public static MeMekanismMachine getAdvancedFactoryTarget(BlockState state) {
        EMExtraFactoryTier advancedTier = EMExtraAttribute.getEMExtraTier(state.getBlock(), EMExtraFactoryTier.class);
        if (advancedTier == null) {
            return null;
        }
        AttributeAdvancedFactoryType advanced = Attribute.get(state, AttributeAdvancedFactoryType.class);
        return advanced == null ? null : MeMekanismMachine.getEvolvedMekanismExtrasFactory(
                advancedTier.name().toLowerCase(Locale.ROOT),
                advanced.getAdvancedFactoryType().getRegistryNameComponent());
    }

    @Nullable
    public static MeMekanismMachine getMoreMachineFactoryTarget(BlockState state) {
        EMExtraFactoryTier advancedTier = EMExtraAttribute.getEMExtraTier(state.getBlock(), EMExtraFactoryTier.class);
        if (advancedTier == null) {
            return null;
        }
        MoreMachineAttributeFactoryType moreMachine = Attribute.get(state, MoreMachineAttributeFactoryType.class);
        return moreMachine == null ? null : MeMekanismMachine.getEvolvedMekanismExtrasFactory(
                advancedTier.name().toLowerCase(Locale.ROOT),
                moreMachine.getMoreMachineFactoryType().getRegistryNameComponent());
    }

    @Nullable
    public static MeMekanismMachine getBaseFactoryTarget(BlockState state) {
        EMExtraAttributeFactoryType typeAttribute = Attribute.get(state, EMExtraAttributeFactoryType.class);
        EMExtraFactoryTier tier = EMExtraAttribute.getEMExtraTier(state.getBlock(), EMExtraFactoryTier.class);
        if (typeAttribute == null || tier == null) {
            return null;
        }
        return MeMekanismMachine.getEvolvedMekanismExtrasFactory(
                tier.name().toLowerCase(Locale.ROOT),
                typeAttribute.getFactoryType().getRegistryNameComponent());
    }

    @Nullable
    public static MeMekanismMachine getInstallerTarget(MeMekanismMachine current, ItemStack stack) {
        if (!(stack.getItem() instanceof EMExtraItemTierInstaller installer)) {
            return null;
        }
        EMExtraTier currentTier = current.provider() != CompatMod.EMEKE
                ? null
                : emExtraTier(current).getEMExtraTier();
        if (currentTier != installer.getFromTier() || currentTier == installer.getToTier()) {
            return null;
        }
        MeMekanismMachine target = currentTier == null
                ? getFirstEmExtraFactoryTarget(current, installer.getToTier())
                : getEmExtraFactoryTarget(current, installer.getToTier());
        if (target == null || target.provider() != CompatMod.EMEKE) {
            return null;
        }
        EMExtraTier targetTier = emExtraTier(target).getEMExtraTier();
        return targetTier == installer.getToTier() ? target : null;
    }

    public static boolean isInstaller(ItemStack stack) {
        return stack.getItem() instanceof EMExtraItemTierInstaller;
    }

    @Nullable
    private static MeMekanismMachine getFirstEmExtraFactoryTarget(MeMekanismMachine current, EMExtraTier toTier) {
        if (current.factoryTier() != FactoryTier.ULTIMATE && !isTerminalEvolvedFactory(current)) {
            return null;
        }
        return getEmExtraFactoryTarget(current, toTier);
    }

    @Nullable
    private static MeMekanismMachine getEmExtraFactoryTarget(MeMekanismMachine current, EMExtraTier toTier) {
        return CompatFactoryTierGraph.factoryAtTier(
                current, CompatMod.EMEKE, toTier.name().toLowerCase(Locale.ROOT));
    }

    private static boolean isTerminalEvolvedFactory(MeMekanismMachine machine) {
        if (!machine.isEvolvedMekanismFactory()) {
            return false;
        }
        MeMekanismMachine next = machine.getNextFactory();
        return next == null || next.provider() == CompatMod.EMEKE;
    }

    public static void registerGridNodeHost(
            RegisterCapabilitiesEvent event,
            TileEntityTypeRegistryObject<? extends TileEntityMekanism> holder) {
        ModBlockEntities.registerGridNodeHost(event, holder, com.beipuo.mekenergistics.blockentity.compat.eme.factory.MeEvolvedMekanismExtrasFactoryAeMachine.class);
    }
}
