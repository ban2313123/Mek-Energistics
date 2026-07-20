package com.beipuo.mekenergistics.compat.provider;

import com.beipuo.mekenergistics.common.machine.MeMekanismMachine;
import com.beipuo.mekenergistics.compat.catalog.CompatMachineFamily;
import com.beipuo.mekenergistics.compat.meke.MekanismExtrasCompat;
import com.beipuo.mekenergistics.compat.meke.MekanismExtrasAdvancedMenuTypes;
import com.beipuo.mekenergistics.compat.meke.MekanismExtrasAdvancedFactoryCompat;
import com.beipuo.mekenergistics.compat.meke.MekanismExtrasMenuTypes;
import com.beipuo.mekenergistics.compat.meke.MekanismExtrasMoreMachineMenuTypes;
import com.beipuo.mekenergistics.compat.meke.MekanismExtrasMoreMachineCompat;
import com.beipuo.mekenergistics.registry.ModMenuTypes;
import com.beipuo.mekenergistics.compat.catalog.CompatMachineCatalog;
import com.beipuo.mekenergistics.compat.catalog.CompatMod;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeFactoryType;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.registration.impl.ContainerTypeDeferredRegister;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import java.util.Map;
import java.util.function.Supplier;

public final class MekeMachineProvider extends AbstractCompatMachineProvider implements CompatMachineProvider {
    public MekeMachineProvider() {
        super(CompatMod.MEKE, familyAdapters());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Map<CompatMachineFamily, CompatMachineFamilyAdapter> familyAdapters() {
        return Map.of(
                CompatMachineFamily.MEKE_FACTORY,
                CompatMachineFamilyAdapter.of(
                        spec -> ModMenuTypes.ME_EXTRA_FACTORY,
                        (spec, registrar) -> MekanismExtrasCompat.registerFactoryMachine(spec.machine(), registrar),
                        (spec, tileType) -> MekanismExtrasCompat.createFactoryBlockType(
                                spec.machine(), (TileEntityTypeRegistryObject) tileType),
                        (spec, event, holder) -> MekanismExtrasCompat.registerGridNodeHost(event, holder)),
                CompatMachineFamily.MEKE_MEKMM_FACTORY,
                CompatMachineFamilyAdapter.of(
                        spec -> ModMenuTypes.ME_EXTRA_MORE_MACHINE_FACTORY,
                        (spec, registrar) -> MekanismExtrasMoreMachineCompat.registerFactoryMachine(
                                spec.machine(), registrar),
                        (spec, tileType) -> MekanismExtrasMoreMachineCompat.createFactoryBlockType(
                                spec.machine(), (TileEntityTypeRegistryObject) tileType),
                        (spec, event, holder) -> MekanismExtrasMoreMachineCompat.registerGridNodeHost(event, holder)),
                CompatMachineFamily.MEKE_MEKMM_ADVANCED_FACTORY,
                CompatMachineFamilyAdapter.of(
                        spec -> ModMenuTypes.ME_EXTRA_ADVANCED_FACTORY,
                        (spec, registrar) -> MekanismExtrasAdvancedFactoryCompat.registerFactoryMachine(
                                spec.machine(), registrar),
                        (spec, tileType) -> MekanismExtrasAdvancedFactoryCompat.createFactoryBlockType(
                                spec.machine(), (TileEntityTypeRegistryObject) tileType),
                        (spec, event, holder) -> MekanismExtrasAdvancedFactoryCompat.registerGridNodeHost(event, holder)));
    }

    @Override
    public void registerMenus(ContainerTypeDeferredRegister register) {
        if (CompatMachineCatalog.hasAvailableFamily(CompatMachineFamily.MEKE_FACTORY)) {
            MekanismExtrasMenuTypes.register(register);
        }
        if (CompatMachineCatalog.hasAvailableFamily(CompatMachineFamily.MEKE_MEKMM_FACTORY)) {
            MekanismExtrasMoreMachineMenuTypes.register(register);
        }
        if (CompatMachineCatalog.hasAvailableFamily(CompatMachineFamily.MEKE_MEKMM_ADVANCED_FACTORY)) {
            MekanismExtrasAdvancedMenuTypes.register(register);
        }
    }

    @Override
    @Nullable
    public MeMekanismMachine resolveOriginalMachine(BlockState state) {
        if (CompatMachineCatalog.hasAvailableFamily(CompatMachineFamily.MEKE_MEKMM_ADVANCED_FACTORY)) {
            MeMekanismMachine advanced = MekanismExtrasAdvancedFactoryCompat.getFactoryTarget(state);
            if (advanced != null) {
                return advanced;
            }
        }
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
}
