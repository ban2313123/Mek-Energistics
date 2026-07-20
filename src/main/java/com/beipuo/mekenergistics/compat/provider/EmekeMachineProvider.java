package com.beipuo.mekenergistics.compat.provider;

import com.beipuo.mekenergistics.common.machine.MeMekanismMachine;
import com.beipuo.mekenergistics.compat.catalog.CompatMachineFamily;
import com.beipuo.mekenergistics.compat.catalog.CompatMachineCatalog;
import com.beipuo.mekenergistics.compat.catalog.CompatMod;
import com.beipuo.mekenergistics.compat.eme.EvolvedMekanismExtrasCompat;
import com.beipuo.mekenergistics.compat.eme.EvolvedMekanismExtrasAdvancedMenuTypes;
import com.beipuo.mekenergistics.compat.eme.EvolvedMekanismExtrasMenuTypes;
import com.beipuo.mekenergistics.compat.eme.EvolvedMekanismExtrasMoreMachineMenuTypes;
import com.beipuo.mekenergistics.registry.ModMenuTypes;
import java.util.Map;
import mekanism.common.registration.impl.ContainerTypeDeferredRegister;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class EmekeMachineProvider extends AbstractCompatMachineProvider implements CompatMachineProvider {
    public EmekeMachineProvider() {
        super(CompatMod.EMEKE, familyAdapters());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Map<CompatMachineFamily, CompatMachineFamilyAdapter> familyAdapters() {
        CompatMachineFamilyAdapter baseFactory = CompatMachineFamilyAdapter.of(
                spec -> ModMenuTypes.ME_EM_EXTRA_FACTORY,
                (spec, registrar) -> EvolvedMekanismExtrasCompat.registerFactoryMachine(spec.machine(), registrar),
                (spec, tileType) -> EvolvedMekanismExtrasCompat.createFactoryBlockType(
                        spec.machine(), (TileEntityTypeRegistryObject) tileType),
                (spec, event, holder) -> EvolvedMekanismExtrasCompat.registerGridNodeHost(event, holder));
        CompatMachineFamilyAdapter mekafFactory = CompatMachineFamilyAdapter.of(
                spec -> ModMenuTypes.ME_EM_EXTRA_ADVANCED_FACTORY,
                (spec, registrar) -> EvolvedMekanismExtrasCompat.registerFactoryMachine(spec.machine(), registrar),
                (spec, tileType) -> EvolvedMekanismExtrasCompat.createFactoryBlockType(
                        spec.machine(), (TileEntityTypeRegistryObject) tileType),
                (spec, event, holder) -> EvolvedMekanismExtrasCompat.registerGridNodeHost(event, holder));
        CompatMachineFamilyAdapter mekmmFactory = CompatMachineFamilyAdapter.of(
                spec -> ModMenuTypes.ME_EM_EXTRA_MORE_MACHINE_FACTORY,
                (spec, registrar) -> EvolvedMekanismExtrasCompat.registerFactoryMachine(spec.machine(), registrar),
                (spec, tileType) -> EvolvedMekanismExtrasCompat.createFactoryBlockType(
                        spec.machine(), (TileEntityTypeRegistryObject) tileType),
                (spec, event, holder) -> EvolvedMekanismExtrasCompat.registerGridNodeHost(event, holder));
        return Map.of(
                CompatMachineFamily.EMEKE_FACTORY, baseFactory,
                CompatMachineFamily.EMEKE_MEKAF_ADVANCED_FACTORY, mekafFactory,
                CompatMachineFamily.EMEKE_MEKMM_FACTORY, mekmmFactory);
    }

    @Override
    public void registerMenus(ContainerTypeDeferredRegister register) {
        if (CompatMachineCatalog.hasAvailableFamily(CompatMachineFamily.EMEKE_FACTORY)) {
            EvolvedMekanismExtrasMenuTypes.register(register);
        }
        if (CompatMachineCatalog.hasAvailableFamily(CompatMachineFamily.EMEKE_MEKAF_ADVANCED_FACTORY)) {
            EvolvedMekanismExtrasAdvancedMenuTypes.register(register);
        }
        if (CompatMachineCatalog.hasAvailableFamily(CompatMachineFamily.EMEKE_MEKMM_FACTORY)) {
            EvolvedMekanismExtrasMoreMachineMenuTypes.register(register);
        }
    }

    @Override
    @Nullable
    public MeMekanismMachine resolveOriginalMachine(BlockState state) {
        if (CompatMachineCatalog.hasAvailableFamily(CompatMachineFamily.EMEKE_MEKAF_ADVANCED_FACTORY)) {
            MeMekanismMachine advanced = EvolvedMekanismExtrasCompat.getAdvancedFactoryTarget(state);
            if (advanced != null) {
                return advanced;
            }
        }
        if (CompatMachineCatalog.hasAvailableFamily(CompatMachineFamily.EMEKE_MEKMM_FACTORY)) {
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
}
