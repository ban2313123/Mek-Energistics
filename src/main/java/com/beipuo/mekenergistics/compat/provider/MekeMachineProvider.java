package com.beipuo.mekenergistics.compat.provider;

import com.beipuo.mekenergistics.common.machine.MeMekanismMachine;
import com.beipuo.mekenergistics.compat.catalog.CompatMachineSpec;
import com.beipuo.mekenergistics.compat.meke.MekanismExtrasCompat;
import com.beipuo.mekenergistics.compat.meke.MekanismExtrasAdvancedMenuTypes;
import com.beipuo.mekenergistics.compat.meke.MekanismExtrasMenuTypes;
import com.beipuo.mekenergistics.compat.meke.MekanismExtrasMoreMachineMenuTypes;
import com.beipuo.mekenergistics.compat.meke.MekanismExtrasMoreMachineCompat;
import com.beipuo.mekenergistics.compat.mekmm.MekanismMoreMachineAdvancedCompat;
import com.beipuo.mekenergistics.registry.machine.MachineFactoryRegistrar;
import com.beipuo.mekenergistics.registry.ModMenuTypes;
import com.beipuo.mekenergistics.compat.catalog.CompatMachineCatalog;
import com.beipuo.mekenergistics.compat.catalog.CompatRegistrationRoute;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeFactoryType;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.registration.impl.ContainerTypeDeferredRegister;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jetbrains.annotations.Nullable;
import java.util.function.Supplier;

public final class MekeMachineProvider implements CompatMachineProvider {
    public MekeMachineProvider() {
    }

    @Override
    public void registerMenus(ContainerTypeDeferredRegister register) {
        if (CompatMachineCatalog.hasAvailableRoute(CompatRegistrationRoute.MEKE_FACTORY)) {
            MekanismExtrasMenuTypes.register(register);
        }
        if (CompatMachineCatalog.hasAvailableRoute(CompatRegistrationRoute.MEKE_MEKMM_FACTORY)) {
            MekanismExtrasMoreMachineMenuTypes.register(register);
        }
        if (CompatMachineCatalog.hasAvailableRoute(CompatRegistrationRoute.MEKE_MEKMM_ADVANCED_FACTORY)) {
            MekanismExtrasAdvancedMenuTypes.register(register);
        }
    }

    @Override
    public ContainerTypeRegistryObject<? extends MekanismTileContainer<?>> menuType(
            CompatMachineSpec spec) {
        return switch (spec.route()) {
            case MEKE_FACTORY -> ModMenuTypes.ME_EXTRA_FACTORY;
            case MEKE_MEKMM_FACTORY -> ModMenuTypes.ME_EXTRA_MORE_MACHINE_FACTORY;
            case MEKE_MEKMM_ADVANCED_FACTORY -> ModMenuTypes.ME_EXTRA_ADVANCED_FACTORY;
            default -> throw wrongRoute(spec);
        };
    }

    @Override
    public TileEntityTypeRegistryObject<? extends TileEntityMekanism> registerBlockEntity(
            CompatMachineSpec spec, MachineFactoryRegistrar registrar) {
        return switch (spec.route()) {
            case MEKE_FACTORY -> MekanismExtrasCompat.registerFactoryMachine(spec.machine(), registrar);
            case MEKE_MEKMM_FACTORY -> MekanismExtrasMoreMachineCompat.registerFactoryMachine(spec.machine(), registrar);
            case MEKE_MEKMM_ADVANCED_FACTORY ->
                    MekanismMoreMachineAdvancedCompat.registerAdvancedFactoryMachine(spec.machine(), registrar);
            default -> throw wrongRoute(spec);
        };
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public BlockTypeTile<? extends TileEntityMekanism> createBlockType(
            CompatMachineSpec spec, TileEntityTypeRegistryObject<? extends TileEntityMekanism> tileType) {
        return switch (spec.route()) {
            case MEKE_FACTORY -> MekanismExtrasCompat.createFactoryBlockType(spec.machine(), (TileEntityTypeRegistryObject) tileType);
            case MEKE_MEKMM_FACTORY -> MekanismExtrasMoreMachineCompat.createFactoryBlockType(
                    spec.machine(), (TileEntityTypeRegistryObject) tileType);
            case MEKE_MEKMM_ADVANCED_FACTORY -> MekanismMoreMachineAdvancedCompat.createAdvancedFactoryBlockType(
                    spec.machine(), (TileEntityTypeRegistryObject) tileType);
            default -> throw wrongRoute(spec);
        };
    }

    @Override
    public void registerGridNodeHost(CompatMachineSpec spec, RegisterCapabilitiesEvent event,
            TileEntityTypeRegistryObject<? extends TileEntityMekanism> holder) {
        switch (spec.route()) {
            case MEKE_FACTORY -> MekanismExtrasCompat.registerGridNodeHost(event, holder);
            case MEKE_MEKMM_FACTORY -> MekanismExtrasMoreMachineCompat.registerGridNodeHost(event, holder);
            case MEKE_MEKMM_ADVANCED_FACTORY -> MekanismMoreMachineAdvancedCompat.registerGridNodeHost(event, holder);
            default -> throw wrongRoute(spec);
        }
    }

    @Override
    @Nullable
    public MeMekanismMachine resolveOriginalMachine(BlockState state) {
        AttributeFactoryType factoryType = Attribute.get(state, AttributeFactoryType.class);
        return factoryType == null ? null : MekanismExtrasCompat.getFactoryTarget(state, factoryType.getFactoryType());
    }

    @Override
    @Nullable
    public MeMekanismMachine resolveInstallerUpgrade(MeMekanismMachine current, ItemStack stack) {
        return MekanismExtrasCompat.getInstallerTarget(current, stack);
    }

    @Override
    public boolean isInstaller(ItemStack stack) {
        return MekanismExtrasCompat.isInstaller(stack);
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void addUpgradeAttribute(
            BlockTypeTile.BlockTileBuilder<?, ?, ?> builder, Supplier<? extends Block> upgradeBlock) {
        MekanismExtrasCompat.withExtraUpgradeable((BlockTypeTile.BlockTileBuilder) builder, upgradeBlock);
    }

    private static IllegalArgumentException wrongRoute(CompatMachineSpec spec) {
        return new IllegalArgumentException("MEKE provider cannot handle " + spec.route());
    }
}
