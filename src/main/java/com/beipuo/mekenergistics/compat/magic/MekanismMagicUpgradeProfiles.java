package com.beipuo.mekenergistics.compat.magic;

import com.beipuo.mekenergistics.api.upgrade.IMePatternAutomationHost;
import com.beipuo.mekenergistics.blockentity.support.io.MeInputLayout;
import com.beipuo.mekenergistics.blockentity.support.io.MeOutputPort;
import com.beipuo.mekenergistics.upgrade.MePatternAutomationProfiles;
import com.beipuo.mekenergistics.upgrade.MeUpgradeMachineProfile;
import java.util.List;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.tile.base.TileEntityMekanism;

/**
 * Magic-facing facade over {@link MePatternAutomationProfiles}. Existing call sites and tests keep
 * these entry points; mapping is the generic SPI path plus {@link MagicPatternAutomationBridge}.
 */
public final class MekanismMagicUpgradeProfiles {
    private MekanismMagicUpgradeProfiles() {
    }

    public static MeUpgradeMachineProfile<?> forTile(TileEntityMekanism tile) {
        return MePatternAutomationProfiles.forTile(tile);
    }

    /**
     * Shared mapping from Magic pattern-input slots to the AE input layout. Kept package-visible so
     * unit tests can drive the real shipped function without constructing a Mekanism tile.
     */
    static MeInputLayout inputLayoutFor(List<IInventorySlot> slots, boolean spiritFactory) {
        return MePatternAutomationProfiles.inputLayout(new SlotOnlyHost(slots, List.of(), spiritFactory, true));
    }

    static List<? extends MeOutputPort> outputPortsFor(List<IInventorySlot> slots) {
        return MePatternAutomationProfiles.outputPorts(new SlotOnlyHost(List.of(), slots, false, true));
    }

    static boolean isSpiritFactory(Object tile) {
        if (tile == null) {
            return false;
        }
        String className = tile.getClass().getName();
        return className.endsWith("SpiritFactoryBlockEntity") || className.contains("SpiritFactory");
    }

    private static final class SlotOnlyHost implements IMePatternAutomationHost {
        private final List<IInventorySlot> inputs;
        private final List<IInventorySlot> outputs;
        private final boolean group;
        private final boolean enabled;

        private SlotOnlyHost(List<IInventorySlot> inputs, List<IInventorySlot> outputs, boolean group,
                boolean enabled) {
            this.inputs = inputs == null ? List.of() : inputs;
            this.outputs = outputs == null ? List.of() : outputs;
            this.group = group;
            this.enabled = enabled;
        }

        @Override
        public boolean meSupportsPatternAutomation() {
            return this.enabled;
        }

        @Override
        public List<IInventorySlot> mePatternItemInputs() {
            return this.inputs;
        }

        @Override
        public List<IInventorySlot> mePatternItemOutputs() {
            return this.outputs;
        }

        @Override
        public IEnergyContainer meEnergyContainer() {
            return null;
        }

        @Override
        public boolean meGroupParallelItemInputs() {
            return this.group;
        }
    }
}
