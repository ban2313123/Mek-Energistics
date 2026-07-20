package com.beipuo.mekenergistics.compat.provider;

import com.beipuo.mekenergistics.compat.catalog.CompatMachineSpec;
import com.beipuo.mekenergistics.registry.machine.MachineFactoryRegistrar;
import java.util.Objects;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tile.base.TileEntityMekanism;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public interface CompatMachineFamilyAdapter {
    ContainerTypeRegistryObject<? extends MekanismTileContainer<?>> menuType(CompatMachineSpec spec);

    TileEntityTypeRegistryObject<? extends TileEntityMekanism> registerBlockEntity(
            CompatMachineSpec spec, MachineFactoryRegistrar registrar);

    BlockTypeTile<? extends TileEntityMekanism> createBlockType(
            CompatMachineSpec spec, TileEntityTypeRegistryObject<? extends TileEntityMekanism> tileType);

    void registerGridNodeHost(
            CompatMachineSpec spec,
            RegisterCapabilitiesEvent event,
            TileEntityTypeRegistryObject<? extends TileEntityMekanism> holder);

    static CompatMachineFamilyAdapter of(
            MenuResolver menuResolver,
            BlockEntityRegistrar blockEntityRegistrar,
            BlockTypeFactory blockTypeFactory,
            GridNodeHostRegistrar gridNodeHostRegistrar) {
        return new Default(
                Objects.requireNonNull(menuResolver),
                Objects.requireNonNull(blockEntityRegistrar),
                Objects.requireNonNull(blockTypeFactory),
                Objects.requireNonNull(gridNodeHostRegistrar));
    }

    @FunctionalInterface
    interface MenuResolver {
        ContainerTypeRegistryObject<? extends MekanismTileContainer<?>> resolve(CompatMachineSpec spec);
    }

    @FunctionalInterface
    interface BlockEntityRegistrar {
        TileEntityTypeRegistryObject<? extends TileEntityMekanism> register(
                CompatMachineSpec spec, MachineFactoryRegistrar registrar);
    }

    @FunctionalInterface
    interface BlockTypeFactory {
        BlockTypeTile<? extends TileEntityMekanism> create(
                CompatMachineSpec spec, TileEntityTypeRegistryObject<? extends TileEntityMekanism> tileType);
    }

    @FunctionalInterface
    interface GridNodeHostRegistrar {
        void register(
                CompatMachineSpec spec,
                RegisterCapabilitiesEvent event,
                TileEntityTypeRegistryObject<? extends TileEntityMekanism> holder);
    }

    record Default(
            MenuResolver menuResolver,
            BlockEntityRegistrar blockEntityRegistrar,
            BlockTypeFactory blockTypeFactory,
            GridNodeHostRegistrar gridNodeHostRegistrar) implements CompatMachineFamilyAdapter {

        @Override
        public ContainerTypeRegistryObject<? extends MekanismTileContainer<?>> menuType(CompatMachineSpec spec) {
            return menuResolver.resolve(spec);
        }

        @Override
        public TileEntityTypeRegistryObject<? extends TileEntityMekanism> registerBlockEntity(
                CompatMachineSpec spec, MachineFactoryRegistrar registrar) {
            return blockEntityRegistrar.register(spec, registrar);
        }

        @Override
        public BlockTypeTile<? extends TileEntityMekanism> createBlockType(
                CompatMachineSpec spec, TileEntityTypeRegistryObject<? extends TileEntityMekanism> tileType) {
            return blockTypeFactory.create(spec, tileType);
        }

        @Override
        public void registerGridNodeHost(
                CompatMachineSpec spec,
                RegisterCapabilitiesEvent event,
                TileEntityTypeRegistryObject<? extends TileEntityMekanism> holder) {
            gridNodeHostRegistrar.register(spec, event, holder);
        }
    }
}
