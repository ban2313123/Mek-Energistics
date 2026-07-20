package com.beipuo.mekenergistics.compat.provider;

import com.beipuo.mekenergistics.common.machine.MeMekanismMachine;
import com.beipuo.mekenergistics.compat.catalog.CompatMachineSpec;
import com.beipuo.mekenergistics.registry.machine.MachineFactoryRegistrar;
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

public interface CompatMachineProvider {
    TileEntityTypeRegistryObject<? extends TileEntityMekanism> registerBlockEntity(
            CompatMachineSpec spec, MachineFactoryRegistrar registrar);

    BlockTypeTile<? extends TileEntityMekanism> createBlockType(
            CompatMachineSpec spec, TileEntityTypeRegistryObject<? extends TileEntityMekanism> tileType);

    void registerGridNodeHost(
            CompatMachineSpec spec,
            RegisterCapabilitiesEvent event,
            TileEntityTypeRegistryObject<? extends TileEntityMekanism> holder);

    default void registerMenus(ContainerTypeDeferredRegister register) {
    }

    ContainerTypeRegistryObject<? extends MekanismTileContainer<?>> menuType(
            CompatMachineSpec spec);

    @Nullable
    default MeMekanismMachine resolveOriginalMachine(BlockState state) {
        return null;
    }

    @Nullable
    default MeMekanismMachine resolveInstallerUpgrade(MeMekanismMachine current, ItemStack stack) {
        return null;
    }

    default boolean isInstaller(ItemStack stack) {
        return false;
    }

    default void addUpgradeAttribute(
            BlockTypeTile.BlockTileBuilder<?, ?, ?> builder, Supplier<? extends Block> upgradeBlock) {
    }
}
