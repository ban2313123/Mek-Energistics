package com.beipuo.mekenergistics.upgrade;

import com.beipuo.mekenergistics.blockentity.support.io.MeInputLayout;
import com.beipuo.mekenergistics.blockentity.support.io.MeInputPort;
import com.beipuo.mekenergistics.blockentity.support.io.MeMachineIoAdapter;
import com.beipuo.mekenergistics.blockentity.support.io.MeOutputPort;
import java.util.ArrayList;
import java.util.List;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.capabilities.energy.MachineEnergyContainer;

public interface AdvancedFactoryUpgradeAccess extends CatalogFactoryUpgradeAdapter {
    MachineEnergyContainer<?> meUpgradeEnergyContainer();

    default List<? extends IInventorySlot> meUpgradeItemInputs() {
        return List.of();
    }

    default List<? extends IChemicalTank> meUpgradeChemicalInputs() {
        return List.of();
    }

    default List<? extends IExtendedFluidTank> meUpgradeFluidInputs() {
        return List.of();
    }

    default List<? extends IInventorySlot> meUpgradeExtraItemInputs() {
        return List.of();
    }

    default List<? extends IInventorySlot> meUpgradeItemOutputs() {
        return List.of();
    }

    default List<? extends IChemicalTank> meUpgradeChemicalOutputs() {
        return List.of();
    }

    default List<? extends IExtendedFluidTank> meUpgradeFluidOutputs() {
        return List.of();
    }

    @Override
    default MeInputLayout mekenergistics$getFactoryInputLayout() {
        List<MeInputPort> ports = new ArrayList<>();
        List<? extends IInventorySlot> itemInputs = meUpgradeItemInputs();
        if (!itemInputs.isEmpty()) {
            ports.add(MeMachineIoAdapter.autoSortedFactoryItemInput(itemInputs));
        }
        meUpgradeChemicalInputs().stream().map(MeMachineIoAdapter::chemicalInput).forEach(ports::add);
        meUpgradeFluidInputs().stream().map(MeMachineIoAdapter::fluidInput).forEach(ports::add);
        meUpgradeExtraItemInputs().stream().map(MeMachineIoAdapter::itemInput).forEach(ports::add);
        return MeInputLayout.unordered(ports);
    }

    @Override
    default List<? extends MeOutputPort> mekenergistics$getFactoryOutputPorts() {
        List<MeOutputPort> ports = new ArrayList<>();
        meUpgradeItemOutputs().stream().map(MeMachineIoAdapter::itemOutput).forEach(ports::add);
        meUpgradeChemicalOutputs().stream().map(MeMachineIoAdapter::chemicalOutput).forEach(ports::add);
        meUpgradeFluidOutputs().stream().map(MeMachineIoAdapter::fluidOutput).forEach(ports::add);
        return List.copyOf(ports);
    }
}
