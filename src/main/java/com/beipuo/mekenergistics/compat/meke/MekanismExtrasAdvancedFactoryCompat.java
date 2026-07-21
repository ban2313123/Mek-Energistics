package com.beipuo.mekenergistics.compat.meke;

import com.beipuo.mekenergistics.block.attribute.MeExtraUpgradeableAttribute;
import com.beipuo.mekenergistics.block.attribute.MeUpgradeableAttribute;
import com.beipuo.mekenergistics.blockentity.api.MeFactoryAeMachine;
import com.beipuo.mekenergistics.blockentity.compat.meke.factory.MeExtraAdvancedCentrifugingFactoryBlockEntity;
import com.beipuo.mekenergistics.blockentity.compat.meke.factory.MeExtraAdvancedChemicalToItemFactoryBlockEntity;
import com.beipuo.mekenergistics.blockentity.compat.meke.factory.MeExtraAdvancedDissolvingFactoryBlockEntity;
import com.beipuo.mekenergistics.blockentity.compat.meke.factory.MeExtraAdvancedItemToChemicalFactoryBlockEntity;
import com.beipuo.mekenergistics.blockentity.compat.meke.factory.MeExtraAdvancedItemToItemFactoryBlockEntity;
import com.beipuo.mekenergistics.blockentity.compat.meke.factory.MeExtraAdvancedLiquifyingFactoryBlockEntity;
import com.beipuo.mekenergistics.blockentity.compat.meke.factory.MeExtraAdvancedPressurizedReactingFactoryBlockEntity;
import com.beipuo.mekenergistics.blockentity.compat.meke.factory.MeExtraAdvancedWashingFactoryBlockEntity;
import com.beipuo.mekenergistics.common.machine.MeMekanismMachine;
import com.beipuo.mekenergistics.compat.catalog.CompatFactoryTierGraph;
import com.beipuo.mekenergistics.compat.catalog.CompatMachineCatalog;
import com.beipuo.mekenergistics.compat.catalog.CompatRegistrationRoute;
import com.beipuo.mekenergistics.compat.mekmm.MekanismMoreMachineAdvancedCompat;
import com.beipuo.mekenergistics.registry.ModBlockEntities;
import com.beipuo.mekenergistics.registry.ModBlocks;
import com.beipuo.mekenergistics.registry.ModMenuTypes;
import com.beipuo.mekenergistics.registry.machine.MachineFactoryRegistrar;
import com.jerry.mekaf.common.block.attribute.AttributeAdvancedFactoryType;
import com.jerry.mekextras.common.block.attribute.ExtraAttribute;
import com.jerry.mekextras.common.integration.mekaf.content.blocktype.ExtraAdvancedFactory;
import com.jerry.mekextras.common.tier.ExtraFactoryTier;
import java.util.Locale;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeGui;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jetbrains.annotations.Nullable;

/** MekAF factory tiers supplied by Mekanism Extras; loaded only by the MEKE provider. */
public final class MekanismExtrasAdvancedFactoryCompat {
    private MekanismExtrasAdvancedFactoryCompat() {
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static TileEntityTypeRegistryObject<? extends TileEntityMekanism> registerFactoryMachine(
            MeMekanismMachine machine, MachineFactoryRegistrar registrar) {
        TileEntityTypeRegistryObject<?> registered = switch (
                MekanismMoreMachineAdvancedCompat.advancedFactoryType(machine)) {
            case OXIDIZING, PIGMENT_EXTRACTING -> registrar.register(
                    machine, MeExtraAdvancedItemToChemicalFactoryBlockEntity::new);
            case DISSOLVING -> registrar.register(machine, MeExtraAdvancedDissolvingFactoryBlockEntity::new);
            case WASHING -> registrar.register(machine, MeExtraAdvancedWashingFactoryBlockEntity::new);
            case CRYSTALLIZING -> registrar.register(machine, MeExtraAdvancedChemicalToItemFactoryBlockEntity::new);
            case PRESSURISED_REACTING -> registrar.register(
                    machine, MeExtraAdvancedPressurizedReactingFactoryBlockEntity::new);
            case CENTRIFUGING -> registrar.register(machine, MeExtraAdvancedCentrifugingFactoryBlockEntity::new);
            case LIQUIFYING -> registrar.register(machine, MeExtraAdvancedLiquifyingFactoryBlockEntity::new);
            case PAINTING -> registrar.register(machine, MeExtraAdvancedItemToItemFactoryBlockEntity::new);
        };
        return (TileEntityTypeRegistryObject) registered;
    }

    @SuppressWarnings("unchecked")
    public static <TILE extends TileEntityMekanism> BlockTypeTile<TILE> createFactoryBlockType(
            MeMekanismMachine machine, TileEntityTypeRegistryObject<TILE> tileType) {
        ExtraAdvancedFactory.ExtraAdvancedFactoryBuilder<?, ?, ?> builder =
                ExtraAdvancedFactory.ExtraAdvancedFactoryBuilder.createAdvancedFactory(
                        () -> tileType,
                        MekanismMoreMachineAdvancedCompat.advancedFactoryType(machine),
                        extraFactoryTier(machine));
        builder.replace(new AttributeGui(() -> ModMenuTypes.ME_EXTRA_ADVANCED_FACTORY, null));
        MeMekanismMachine upgradeTarget = machine.getNextFactory();
        if (upgradeTarget != null) {
            builder.replace(new MeUpgradeableAttribute(() -> ModBlocks.getMachineBlock(upgradeTarget).get()));
            builder.replace(new MeExtraUpgradeableAttribute(() -> ModBlocks.getMachineBlock(upgradeTarget).get()));
        }
        return (BlockTypeTile<TILE>) builder.build();
    }

    @Nullable
    public static MeMekanismMachine getFactoryTarget(BlockState state) {
        var sourceId = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        MeMekanismMachine exact = CompatMachineCatalog.findBySourceBlockId(sourceId)
                .filter(spec -> spec.route() == CompatRegistrationRoute.MEKE_MEKMM_ADVANCED_FACTORY)
                .map(spec -> spec.machine())
                .orElse(null);
        if (exact != null) {
            return exact;
        }
        AttributeAdvancedFactoryType type = Attribute.get(state, AttributeAdvancedFactoryType.class);
        ExtraFactoryTier tier = ExtraAttribute.getAdvancedTier(state.getBlock(), ExtraFactoryTier.class);
        if (type == null || tier == null) {
            return null;
        }
        return CompatFactoryTierGraph.findFactory(
                CompatRegistrationRoute.MEKE_MEKMM_ADVANCED_FACTORY,
                tier.name().toLowerCase(Locale.ROOT),
                type.getAdvancedFactoryType().getRegistryNameComponent());
    }

    private static ExtraFactoryTier extraFactoryTier(MeMekanismMachine machine) {
        return ExtraFactoryTier.valueOf(machine.tierId().toUpperCase(Locale.ROOT));
    }

    public static void registerGridNodeHost(
            RegisterCapabilitiesEvent event,
            TileEntityTypeRegistryObject<? extends TileEntityMekanism> holder) {
        ModBlockEntities.registerGridNodeHost(event, holder, MeFactoryAeMachine.class);
    }
}
