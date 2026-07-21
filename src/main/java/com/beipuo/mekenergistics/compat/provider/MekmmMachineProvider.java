package com.beipuo.mekenergistics.compat.provider;

import com.beipuo.mekenergistics.common.machine.MeMekanismMachine;
import com.beipuo.mekenergistics.compat.catalog.CompatMachineFamily;
import com.beipuo.mekenergistics.compat.catalog.CompatMachineCatalog;
import com.beipuo.mekenergistics.compat.catalog.CompatMod;
import com.beipuo.mekenergistics.compat.mekmm.MekanismMoreMachineAdvancedCompat;
import com.beipuo.mekenergistics.compat.mekmm.MekanismMoreMachineAdvancedMenuTypes;
import com.beipuo.mekenergistics.compat.mekmm.MekanismMoreMachineBaseCompat;
import com.beipuo.mekenergistics.compat.mekmm.MekanismMoreMachineLargeMenuTypes;
import com.beipuo.mekenergistics.compat.mekmm.MekanismMoreMachineMenuTypes;
import com.beipuo.mekenergistics.registry.ModMenuTypes;
import java.util.Map;
import mekanism.common.registration.impl.ContainerTypeDeferredRegister;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class MekmmMachineProvider extends AbstractCompatMachineProvider implements CompatMachineProvider {
    public MekmmMachineProvider() {
        super(CompatMod.MEKMM, familyAdapters());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Map<CompatMachineFamily, CompatMachineFamilyAdapter> familyAdapters() {
        return Map.of(
                CompatMachineFamily.MEKMM_MACHINE,
                CompatMachineFamilyAdapter.of(
                        spec -> MekanismMoreMachineBaseCompat.meBaseContainer(spec.machine()),
                        (spec, registrar) -> MekanismMoreMachineBaseCompat.registerBaseMachine(
                                spec.machine(), registrar),
                        (spec, tileType) -> MekanismMoreMachineBaseCompat.createBaseBlockType(
                                spec.machine(), (TileEntityTypeRegistryObject) tileType),
                        (spec, event, holder) -> MekanismMoreMachineBaseCompat.registerBaseGridNodeHost(
                                event, spec.machine(), holder)),
                CompatMachineFamily.MEKMM_FACTORY,
                CompatMachineFamilyAdapter.of(
                        spec -> ModMenuTypes.ME_MORE_MACHINE_FACTORY,
                        (spec, registrar) -> MekanismMoreMachineBaseCompat.registerFactoryMachine(
                                spec.machine(), registrar),
                        (spec, tileType) -> MekanismMoreMachineBaseCompat.createFactoryBlockType(
                                spec.machine(), (TileEntityTypeRegistryObject) tileType),
                        (spec, event, holder) -> MekanismMoreMachineBaseCompat.registerGridNodeHost(event, holder)),
                CompatMachineFamily.MEKMM_ADVANCED_FACTORY,
                CompatMachineFamilyAdapter.of(
                        spec -> ModMenuTypes.ME_ADVANCED_FACTORY,
                        (spec, registrar) -> MekanismMoreMachineAdvancedCompat.registerAdvancedFactoryMachine(
                                spec.machine(), registrar),
                        (spec, tileType) -> MekanismMoreMachineAdvancedCompat.createAdvancedFactoryBlockType(
                                spec.machine(), (TileEntityTypeRegistryObject) tileType),
                        (spec, event, holder) -> MekanismMoreMachineAdvancedCompat.registerGridNodeHost(event, holder)));
    }

    @Override
    public void registerMenus(ContainerTypeDeferredRegister register) {
        if (CompatMachineCatalog.hasAvailableFamily(CompatMachineFamily.MEKMM_MACHINE)
                || CompatMachineCatalog.hasAvailableFamily(CompatMachineFamily.MEKMM_FACTORY)) {
            MekanismMoreMachineMenuTypes.register(register);
        }
        if (CompatMachineCatalog.hasAvailableFamily(CompatMachineFamily.MEKMM_ADVANCED_FACTORY)) {
            MekanismMoreMachineAdvancedMenuTypes.register(register);
        }
        if (MekanismMoreMachineBaseCompat.hasAvailableLargeMachines()) {
            MekanismMoreMachineLargeMenuTypes.register(register);
        }
    }

    @Override
    @Nullable
    public MeMekanismMachine resolveOriginalMachine(BlockState state) {
        if (CompatMachineCatalog.hasAvailableFamily(CompatMachineFamily.MEKMM_ADVANCED_FACTORY)) {
            MeMekanismMachine advanced = MekanismMoreMachineAdvancedCompat.getFactoryTarget(state);
            if (advanced != null) {
                return advanced;
            }
        }
        return MekanismMoreMachineBaseCompat.getFactoryTarget(state);
    }
}
