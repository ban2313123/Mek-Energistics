package com.beipuo.mekenergistics.blockentity.api;

import java.util.List;
import com.beipuo.mekenergistics.blockentity.support.io.MeInputLayout;
import com.beipuo.mekenergistics.blockentity.support.io.MeMachineIoAdapter;
import com.beipuo.mekenergistics.blockentity.support.io.MeOutputPort;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.fluid.IExtendedFluidTank;
import java.util.ArrayList;
import java.util.Objects;

/** Shared physical I/O contract for factory owners backed by the common AE support. */
public interface MeFactoryIoOwner extends MeFactoryAeMachine {
    List<IInventorySlot> meInputSlots();

    List<IInventorySlot> meOutputSlots();

    default List<? extends IChemicalTank> meChemicalInputTanks() {
        return List.of();
    }

    default List<? extends IExtendedFluidTank> meFluidInputTanks() {
        return List.of();
    }

    default List<? extends IChemicalTank> meChemicalOutputTanks() {
        return List.of();
    }

    default List<? extends IExtendedFluidTank> meFluidOutputTanks() {
        return List.of();
    }

    void unpauseRecipeMonitors();

    @Override
    default MeInputLayout getPatternInputLayout() {
        List<IInventorySlot> inputs = Objects.requireNonNull(meInputSlots(), "meInputSlots");
        List<com.beipuo.mekenergistics.blockentity.support.io.MeInputPort> ports = new ArrayList<>();
        if (!inputs.isEmpty()) {
            ports.add(MeMachineIoAdapter.autoSortedFactoryItemInput(inputs));
        }
        Objects.requireNonNull(meChemicalInputTanks(), "meChemicalInputTanks").stream()
                .map(MeMachineIoAdapter::chemicalInput).forEach(ports::add);
        Objects.requireNonNull(meFluidInputTanks(), "meFluidInputTanks").stream()
                .map(MeMachineIoAdapter::fluidInput).forEach(ports::add);
        return MeInputLayout.unordered(ports);
    }

    @Override
    default List<? extends MeOutputPort> getPatternOutputPorts() {
        List<IInventorySlot> outputs = Objects.requireNonNull(meOutputSlots(), "meOutputSlots");
        List<MeOutputPort> ports = new ArrayList<>();
        outputs.stream().map(MeMachineIoAdapter::itemOutput).forEach(ports::add);
        Objects.requireNonNull(meChemicalOutputTanks(), "meChemicalOutputTanks").stream()
                .map(MeMachineIoAdapter::chemicalOutput).forEach(ports::add);
        Objects.requireNonNull(meFluidOutputTanks(), "meFluidOutputTanks").stream()
                .map(MeMachineIoAdapter::fluidOutput).forEach(ports::add);
        return List.copyOf(ports);
    }
}
