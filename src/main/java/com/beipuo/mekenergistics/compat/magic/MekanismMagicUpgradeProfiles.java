package com.beipuo.mekenergistics.compat.magic;

import com.beipuo.mekenergistics.blockentity.support.io.MeInputLayout;
import com.beipuo.mekenergistics.blockentity.support.io.MeMachineIoAdapter;
import com.beipuo.mekenergistics.blockentity.support.io.MeOutputPort;
import com.beipuo.mekenergistics.common.machine.MeMekanismMachine;
import com.beipuo.mekenergistics.upgrade.MeUpgradeMachineProfile;
import java.util.ArrayList;
import java.util.List;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.world.item.ItemStack;

/**
 * Builds ME-upgrade profiles from Mekanism Magic's published automation surface.
 *
 * <p>Pattern inputs/outputs come from {@code IMekanismMagicAutomation}. Persistent setup slots and
 * manual-only slots are intentionally omitted so AE patterns never encode chalk, spirit sources, or
 * ritual selectors.</p>
 */
public final class MekanismMagicUpgradeProfiles {
    private MekanismMagicUpgradeProfiles() {
    }

    public static MeUpgradeMachineProfile<?> forTile(TileEntityMekanism tile) {
        if (!MekanismMagicAutomationAccess.isMagicAutomation(tile)
                || !MekanismMagicAutomationAccess.supportsPatternAutomation(tile)) {
            return null;
        }
        return profile(tile);
    }

    private static <TILE extends TileEntityMekanism> MeUpgradeMachineProfile<TILE> profile(TILE tile) {
        return new MeUpgradeMachineProfile<>(
                candidate -> candidate == tile
                        && MekanismMagicAutomationAccess.isMagicAutomation(candidate)
                        && MekanismMagicAutomationAccess.supportsPatternAutomation(candidate),
                MekanismMagicUpgradeProfiles::inputLayout,
                MekanismMagicUpgradeProfiles::outputPorts,
                MeMekanismMachine.MEKANISM_MAGIC_AUTOMATION,
                candidate -> new ItemStack(candidate.getBlockState().getBlock()),
                candidate -> candidate.getBlockState().getBlock().getName());
    }

    private static MeInputLayout inputLayout(TileEntityMekanism tile) {
        return inputLayoutFor(MekanismMagicAutomationAccess.patternInputs(tile), isSpiritFactory(tile));
    }

    /**
     * Shared mapping from Magic pattern-input slots to the AE input layout. Kept package-visible so
     * unit tests can drive the real shipped function without constructing a Mekanism tile.
     */
    static MeInputLayout inputLayoutFor(List<IInventorySlot> slots, boolean spiritFactory) {
        if (slots == null || slots.isEmpty()) {
            return MeInputLayout.empty();
        }
        // Spirit factories expose one identical process input per lane. Group them so multi-count
        // recipes are not split across lanes the way Mekanism's own factory redistribution expects.
        // Ritual machines keep distinct ports because activation/sacrifice are role-specific slots.
        if (slots.size() > 1 && spiritFactory) {
            return MeInputLayout.unordered(List.of(MeMachineIoAdapter.autoSortedFactoryItemInput(slots)));
        }
        List<com.beipuo.mekenergistics.blockentity.support.io.MeInputPort> ports = new ArrayList<>(slots.size());
        for (IInventorySlot slot : slots) {
            ports.add(MeMachineIoAdapter.itemInput(slot));
        }
        return MeInputLayout.unordered(ports);
    }

    static List<? extends MeOutputPort> outputPortsFor(List<IInventorySlot> slots) {
        if (slots == null || slots.isEmpty()) {
            return List.of();
        }
        List<MeOutputPort> ports = new ArrayList<>(slots.size());
        for (IInventorySlot slot : slots) {
            ports.add(MeMachineIoAdapter.itemOutput(slot));
        }
        return List.copyOf(ports);
    }

    static boolean isSpiritFactory(Object tile) {
        if (tile == null) {
            return false;
        }
        String className = tile.getClass().getName();
        return className.endsWith("SpiritFactoryBlockEntity") || className.contains("SpiritFactory");
    }

    private static List<? extends MeOutputPort> outputPorts(TileEntityMekanism tile) {
        return outputPortsFor(MekanismMagicAutomationAccess.patternOutputs(tile));
    }
}
