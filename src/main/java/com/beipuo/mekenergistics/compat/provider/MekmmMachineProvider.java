package com.beipuo.mekenergistics.compat.provider;

import com.beipuo.mekenergistics.common.machine.MeMekanismMachine;
import com.beipuo.mekenergistics.compat.catalog.CompatMachineSpec;
import com.beipuo.mekenergistics.compat.catalog.CompatMachineCatalog;
import com.beipuo.mekenergistics.compat.catalog.CompatRegistrationRoute;
import com.beipuo.mekenergistics.compat.mekmm.MekanismMoreMachineAdvancedCompat;
import com.beipuo.mekenergistics.compat.mekmm.MekanismMoreMachineAdvancedMenuTypes;
import com.beipuo.mekenergistics.compat.mekmm.MekanismMoreMachineBaseCompat;
import com.beipuo.mekenergistics.compat.mekmm.MekanismMoreMachineLargeMenuTypes;
import com.beipuo.mekenergistics.compat.mekmm.MekanismMoreMachineMenuTypes;
import com.beipuo.mekenergistics.registry.ModMenuTypes;
import com.beipuo.mekenergistics.registry.machine.MachineFactoryRegistrar;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.registration.impl.ContainerTypeDeferredRegister;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jetbrains.annotations.Nullable;

public final class MekmmMachineProvider implements CompatMachineProvider {
    public MekmmMachineProvider() {
    }

    @Override
    public void registerMenus(ContainerTypeDeferredRegister register) {
        if (CompatMachineCatalog.hasAvailableRoute(com.beipuo.mekenergistics.compat.catalog.CompatRegistrationRoute.MEKMM_MACHINE)
                || CompatMachineCatalog.hasAvailableRoute(com.beipuo.mekenergistics.compat.catalog.CompatRegistrationRoute.MEKMM_FACTORY)) {
            MekanismMoreMachineMenuTypes.register(register);
        }
        if (CompatMachineCatalog.hasAvailableRoute(com.beipuo.mekenergistics.compat.catalog.CompatRegistrationRoute.MEKMM_ADVANCED_FACTORY)) {
            MekanismMoreMachineAdvancedMenuTypes.register(register);
        }
        if (CompatMachineCatalog.isAvailable(MeMekanismMachine.LARGE_ANTIPROTONIC_NUCLEOSYNTHESIZER)) {
            MekanismMoreMachineLargeMenuTypes.register(register);
        }
    }

    @Override
    public ContainerTypeRegistryObject<? extends MekanismTileContainer<?>> menuType(
            CompatMachineSpec spec) {
        return switch (spec.route()) {
            case MEKMM_MACHINE -> ModMenuTypes.getCoreMachineContainer(spec.machine());
            case MEKMM_FACTORY -> ModMenuTypes.ME_MORE_MACHINE_FACTORY;
            case MEKMM_ADVANCED_FACTORY -> ModMenuTypes.ME_ADVANCED_FACTORY;
            default -> throw wrongRoute(spec);
        };
    }

    @Override
    public TileEntityTypeRegistryObject<? extends TileEntityMekanism> registerBlockEntity(
            CompatMachineSpec spec, MachineFactoryRegistrar registrar) {
        return switch (spec.route()) {
            case MEKMM_MACHINE -> MekanismMoreMachineBaseCompat.registerBaseMachine(spec.machine(), registrar);
            case MEKMM_FACTORY -> MekanismMoreMachineBaseCompat.registerFactoryMachine(spec.machine(), registrar);
            case MEKMM_ADVANCED_FACTORY ->
                    MekanismMoreMachineAdvancedCompat.registerAdvancedFactoryMachine(spec.machine(), registrar);
            default -> throw wrongRoute(spec);
        };
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public BlockTypeTile<? extends TileEntityMekanism> createBlockType(
            CompatMachineSpec spec, TileEntityTypeRegistryObject<? extends TileEntityMekanism> tileType) {
        return switch (spec.route()) {
            case MEKMM_MACHINE -> MekanismMoreMachineBaseCompat.createBaseBlockType(spec.machine(), (TileEntityTypeRegistryObject) tileType);
            case MEKMM_FACTORY -> MekanismMoreMachineBaseCompat.createFactoryBlockType(spec.machine(), (TileEntityTypeRegistryObject) tileType);
            case MEKMM_ADVANCED_FACTORY -> MekanismMoreMachineAdvancedCompat.createAdvancedFactoryBlockType(
                    spec.machine(), (TileEntityTypeRegistryObject) tileType);
            default -> throw wrongRoute(spec);
        };
    }

    @Override
    public void registerGridNodeHost(CompatMachineSpec spec, RegisterCapabilitiesEvent event,
            TileEntityTypeRegistryObject<? extends TileEntityMekanism> holder) {
        switch (spec.route()) {
            case MEKMM_MACHINE -> MekanismMoreMachineBaseCompat.registerBaseGridNodeHost(event, spec.machine(), holder);
            case MEKMM_FACTORY -> MekanismMoreMachineBaseCompat.registerGridNodeHost(event, holder);
            case MEKMM_ADVANCED_FACTORY -> MekanismMoreMachineAdvancedCompat.registerGridNodeHost(event, holder);
            default -> throw wrongRoute(spec);
        }
    }

    @Override
    @Nullable
    public MeMekanismMachine resolveOriginalMachine(BlockState state) {
        if (CompatMachineCatalog.hasAvailableRoute(CompatRegistrationRoute.MEKMM_ADVANCED_FACTORY)) {
            MeMekanismMachine advanced = MekanismMoreMachineAdvancedCompat.getFactoryTarget(state);
            if (advanced != null) {
                return advanced;
            }
        }
        return MekanismMoreMachineBaseCompat.getFactoryTarget(state);
    }

    private static IllegalArgumentException wrongRoute(CompatMachineSpec spec) {
        return new IllegalArgumentException("MEKMM provider cannot handle " + spec.route());
    }
}
