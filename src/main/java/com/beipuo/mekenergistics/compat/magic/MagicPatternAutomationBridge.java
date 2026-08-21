package com.beipuo.mekenergistics.compat.magic;

import com.beipuo.mekenergistics.api.upgrade.IMePatternAutomationHost;
import java.util.List;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.inventory.IInventorySlot;
import org.jetbrains.annotations.Nullable;

/**
 * Adapts Mekanism Magic's published automation surface onto {@link IMePatternAutomationHost}
 * without requiring Magic tiles to implement the Mek Energistics SPI.
 */
public final class MagicPatternAutomationBridge {
    private MagicPatternAutomationBridge() {
    }

    public static @Nullable IMePatternAutomationHost asHost(Object tile) {
        if (!MekanismMagicAutomationAccess.isMagicAutomation(tile)) {
            return null;
        }
        return new Host(tile);
    }

    private static final class Host implements IMePatternAutomationHost {
        private final Object tile;

        private Host(Object tile) {
            this.tile = tile;
        }

        @Override
        public boolean meSupportsPatternAutomation() {
            return MekanismMagicAutomationAccess.supportsPatternAutomation(this.tile);
        }

        @Override
        public List<IInventorySlot> mePatternItemInputs() {
            return MekanismMagicAutomationAccess.patternInputs(this.tile);
        }

        @Override
        public List<IInventorySlot> mePatternItemOutputs() {
            return MekanismMagicAutomationAccess.patternOutputs(this.tile);
        }

        @Override
        public List<IExtendedFluidTank> mePatternFluidInputs() {
            return List.of();
        }

        @Override
        public List<IExtendedFluidTank> mePatternFluidOutputs() {
            return List.of();
        }

        @Override
        public List<IChemicalTank> mePatternChemicalInputs() {
            return List.of();
        }

        @Override
        public List<IChemicalTank> mePatternChemicalOutputs() {
            return List.of();
        }

        @Override
        public List<IInventorySlot> mePersistentItemInputs() {
            return MekanismMagicAutomationAccess.persistentInputs(this.tile);
        }

        @Override
        public List<IInventorySlot> meManualOnlyItemSlots() {
            return MekanismMagicAutomationAccess.manualOnlySlots(this.tile);
        }

        @Override
        public IEnergyContainer meEnergyContainer() {
            return MekanismMagicAutomationAccess.energyContainer(this.tile);
        }

        @Override
        public boolean meIsBusy() {
            return MekanismMagicAutomationAccess.isBusy(this.tile);
        }

        @Override
        public boolean meGroupParallelItemInputs() {
            return MekanismMagicUpgradeProfiles.isSpiritFactory(this.tile);
        }
    }
}
