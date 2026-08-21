package com.beipuo.mekenergistics.api.upgrade;

import java.util.List;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.inventory.IInventorySlot;

/**
 * Stable SPI for third-party Mekanism machines that want ME pattern-provider upgrades.
 *
 * <p>Implement this on a {@code TileEntityMekanism} (or a compatible Mekanism tile). Do not
 * reference AE2 types. Mek Energistics maps these declarations onto pattern I/O, ME cards, and
 * AE cable discovery. Register the machine block with
 * {@link MePatternAutomation#registerBlock(net.minecraft.resources.ResourceLocation)} so ME cards
 * and {@code IN_WORLD_GRID_NODE_HOST} are applied.</p>
 *
 * <p>A tile class that implements this interface will not load if Mek Energistics is absent. For
 * optional integration, put the implementation on a class that loads only when this mod is
 * present.</p>
 *
 * <p>Bump {@link #API_VERSION} only for breaking signature changes. New default methods may appear
 * in compatible revisions. Call {@link #mePatternAutomationApiVersion()} at runtime rather than
 * reading the compile-time constant.</p>
 */
public interface IMePatternAutomationHost {
    int API_VERSION = 1;

    default int mePatternAutomationApiVersion() {
        return API_VERSION;
    }

    /**
     * When false the machine is never an ME-upgrade target: no pattern cards, no AE host, no
     * pattern encoding.
     */
    boolean meSupportsPatternAutomation();

    /** Item inputs that belong in an encoded pattern and may be inserted by AE. */
    List<IInventorySlot> mePatternItemInputs();

    /** Item outputs that may be returned to the ME network. */
    List<IInventorySlot> mePatternItemOutputs();

    /** Fluid inputs that belong in an encoded pattern. */
    default List<IExtendedFluidTank> mePatternFluidInputs() {
        return List.of();
    }

    /** Fluid outputs that may be returned to the ME network. */
    default List<IExtendedFluidTank> mePatternFluidOutputs() {
        return List.of();
    }

    /** Chemical inputs that belong in an encoded pattern. */
    default List<IChemicalTank> mePatternChemicalInputs() {
        return List.of();
    }

    /** Chemical outputs that may be returned to the ME network. */
    default List<IChemicalTank> mePatternChemicalOutputs() {
        return List.of();
    }

    /**
     * Required setup that must not be encoded into every pattern (catalysts, spirit sources,
     * installed tools).
     */
    default List<IInventorySlot> mePersistentItemInputs() {
        return List.of();
    }

    default List<IExtendedFluidTank> mePersistentFluidInputs() {
        return List.of();
    }

    default List<IChemicalTank> mePersistentChemicalInputs() {
        return List.of();
    }

    /**
     * Slots that must never be auto-inserted or encoded (selectors, chalk, manuals).
     */
    default List<IInventorySlot> meManualOnlyItemSlots() {
        return List.of();
    }

    IEnergyContainer meEnergyContainer();

    default boolean meIsBusy() {
        return false;
    }

    /**
     * When true, parallel identical item-input slots are treated as one factory-style grouped port
     * so multi-count recipes are not split across process lanes.
     */
    default boolean meGroupParallelItemInputs() {
        return false;
    }
}
