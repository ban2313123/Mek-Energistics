package com.beipuo.mekenergistics.blockentity.api;

import java.util.List;
import com.beipuo.mekenergistics.blockentity.support.io.MeInputLayout;
import com.beipuo.mekenergistics.blockentity.support.io.MeMachineIoAdapter;
import com.beipuo.mekenergistics.blockentity.support.io.MeOutputPort;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.common.content.blocktype.FactoryType;
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

    default boolean isInfusingFactory() {
        return getMachine().factoryType() == FactoryType.INFUSING;
    }

    /**
     * Keeps metallurgic infusing factories in reaction mode until chemical AE output is enabled.
     * Other item + chemical factories retain their existing three-port layout.
     */
    default MeInputLayout itemChemicalFactoryInputLayout(IChemicalTank chemicalTank, IInventorySlot extraSlot) {
        Objects.requireNonNull(chemicalTank, "chemicalTank");
        Objects.requireNonNull(extraSlot, "extraSlot");
        if (isInfusingFactory()) {
            if (getAeOutputMode().chemicals()) {
                return MeInputLayout.unordered(List.of(MeMachineIoAdapter.manualItemInput(extraSlot)));
            }
            return MeInputLayout.unordered(List.of(
                    MeMachineIoAdapter.autoSortedFactoryItemInput(meInputSlots()),
                    MeMachineIoAdapter.chemicalInput(chemicalTank)));
        }
        return MeInputLayout.unordered(List.of(
                MeMachineIoAdapter.autoSortedFactoryItemInput(meInputSlots()),
                MeMachineIoAdapter.chemicalInput(chemicalTank),
                MeMachineIoAdapter.itemInput(extraSlot)));
    }

    default List<? extends IChemicalTank> itemChemicalFactoryOutputTanks(IChemicalTank chemicalTank) {
        return isInfusingFactory() ? List.of(Objects.requireNonNull(chemicalTank, "chemicalTank")) : List.of();
    }

    default void initializeFactoryAeOutputMode() {
        if (isInfusingFactory()) {
            getAeSupport().setAeOutputMode(AeOutputMode.ITEMS);
        }
    }

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
