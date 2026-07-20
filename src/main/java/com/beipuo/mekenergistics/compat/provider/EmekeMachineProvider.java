package com.beipuo.mekenergistics.compat.provider;

import com.beipuo.mekenergistics.common.machine.MeMekanismMachine;
import com.beipuo.mekenergistics.compat.catalog.CompatMachineSpec;
import com.beipuo.mekenergistics.compat.catalog.CompatMachineCatalog;
import com.beipuo.mekenergistics.compat.catalog.CompatRequirement;
import com.beipuo.mekenergistics.compat.eme.EvolvedMekanismExtrasCompat;
import com.beipuo.mekenergistics.compat.eme.EvolvedMekanismExtrasAdvancedMenuTypes;
import com.beipuo.mekenergistics.compat.eme.EvolvedMekanismExtrasMenuTypes;
import com.beipuo.mekenergistics.compat.eme.EvolvedMekanismExtrasMoreMachineMenuTypes;
import com.beipuo.mekenergistics.registry.ModMenuTypes;
import com.beipuo.mekenergistics.registry.machine.MachineFactoryRegistrar;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.registration.impl.ContainerTypeDeferredRegister;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jetbrains.annotations.Nullable;

public final class EmekeMachineProvider implements CompatMachineProvider {
    public EmekeMachineProvider() {
    }

    @Override
    public void registerMenus(ContainerTypeDeferredRegister register) {
        EvolvedMekanismExtrasMenuTypes.register(register);
        if (hasRequirement(CompatRequirement.EMEKE_ADVANCED_FACTORIES)) {
            EvolvedMekanismExtrasAdvancedMenuTypes.register(register);
        }
        if (hasRequirement(CompatRequirement.EMEKE_MEKMM_FACTORIES)) {
            EvolvedMekanismExtrasMoreMachineMenuTypes.register(register);
        }
    }

    @Override
    public ContainerTypeRegistryObject<? extends MekanismTileContainer<?>> menuType(
            CompatMachineSpec spec) {
        return switch (spec.route()) {
            case EMEKE_FACTORY -> ModMenuTypes.ME_EM_EXTRA_FACTORY;
            case EMEKE_ADVANCED_FACTORY -> spec.requirements().contains(CompatRequirement.EMEKE_ADVANCED_FACTORIES)
                    ? ModMenuTypes.ME_EM_EXTRA_ADVANCED_FACTORY : ModMenuTypes.ME_EM_EXTRA_MORE_MACHINE_FACTORY;
            default -> throw wrongRoute(spec);
        };
    }

    private static boolean hasRequirement(CompatRequirement requirement) {
        return CompatMachineCatalog.available()
                .filter(spec -> spec.provider() == com.beipuo.mekenergistics.compat.catalog.CompatMod.EMEKE)
                .anyMatch(spec -> spec.requirements().contains(requirement));
    }

    @Override
    public TileEntityTypeRegistryObject<? extends TileEntityMekanism> registerBlockEntity(
            CompatMachineSpec spec, MachineFactoryRegistrar registrar) {
        return switch (spec.route()) {
            case EMEKE_FACTORY, EMEKE_ADVANCED_FACTORY ->
                    EvolvedMekanismExtrasCompat.registerFactoryMachine(spec.machine(), registrar);
            default -> throw wrongRoute(spec);
        };
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public BlockTypeTile<? extends TileEntityMekanism> createBlockType(
            CompatMachineSpec spec, TileEntityTypeRegistryObject<? extends TileEntityMekanism> tileType) {
        return switch (spec.route()) {
            case EMEKE_FACTORY, EMEKE_ADVANCED_FACTORY -> EvolvedMekanismExtrasCompat.createFactoryBlockType(
                    spec.machine(), (TileEntityTypeRegistryObject) tileType);
            default -> throw wrongRoute(spec);
        };
    }

    @Override
    public void registerGridNodeHost(CompatMachineSpec spec, RegisterCapabilitiesEvent event,
            TileEntityTypeRegistryObject<? extends TileEntityMekanism> holder) {
        switch (spec.route()) {
            case EMEKE_FACTORY, EMEKE_ADVANCED_FACTORY ->
                    EvolvedMekanismExtrasCompat.registerGridNodeHost(event, holder);
            default -> throw wrongRoute(spec);
        }
    }

    @Override
    @Nullable
    public MeMekanismMachine resolveOriginalMachine(BlockState state) {
        if (hasRequirement(CompatRequirement.EMEKE_ADVANCED_FACTORIES)) {
            MeMekanismMachine advanced = EvolvedMekanismExtrasCompat.getAdvancedFactoryTarget(state);
            if (advanced != null) {
                return advanced;
            }
        }
        if (hasRequirement(CompatRequirement.EMEKE_MEKMM_FACTORIES)) {
            MeMekanismMachine moreMachine = EvolvedMekanismExtrasCompat.getMoreMachineFactoryTarget(state);
            if (moreMachine != null) {
                return moreMachine;
            }
        }
        return EvolvedMekanismExtrasCompat.getBaseFactoryTarget(state);
    }

    @Override
    @Nullable
    public MeMekanismMachine resolveInstallerUpgrade(MeMekanismMachine current, ItemStack stack) {
        return EvolvedMekanismExtrasCompat.getInstallerTarget(current, stack);
    }

    @Override
    public boolean isInstaller(ItemStack stack) {
        return EvolvedMekanismExtrasCompat.isInstaller(stack);
    }

    private static IllegalArgumentException wrongRoute(CompatMachineSpec spec) {
        return new IllegalArgumentException("EMEKE provider cannot handle " + spec.route());
    }
}
