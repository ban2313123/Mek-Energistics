package com.beipuo.mekenergistics.upgrade;

import com.beipuo.mekenergistics.api.upgrade.IMePatternAutomationHost;
import com.beipuo.mekenergistics.blockentity.support.io.MeInputLayout;
import com.beipuo.mekenergistics.blockentity.support.io.MeInputPort;
import com.beipuo.mekenergistics.blockentity.support.io.MeMachineIoAdapter;
import com.beipuo.mekenergistics.blockentity.support.io.MeOutputPort;
import com.beipuo.mekenergistics.common.machine.MeMekanismMachine;
import com.beipuo.mekenergistics.compat.magic.MagicPatternAutomationBridge;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Maps {@link IMePatternAutomationHost} declarations onto ME-upgrade profiles. Persistent and
 * manual-only surfaces are stripped from pattern ports even if an addon also lists them as pattern
 * I/O.
 */
public final class MePatternAutomationProfiles {
    private MePatternAutomationProfiles() {
    }

    public static @Nullable IMePatternAutomationHost resolveHost(Object tile) {
        if (tile instanceof IMePatternAutomationHost host) {
            return host;
        }
        return MagicPatternAutomationBridge.asHost(tile);
    }

    public static boolean isUpgradeTarget(Object tile) {
        IMePatternAutomationHost host = resolveHost(tile);
        return host != null && host.meSupportsPatternAutomation();
    }

    public static MeUpgradeMachineProfile<?> forTile(TileEntityMekanism tile) {
        if (!isUpgradeTarget(tile)) {
            return null;
        }
        return profile(tile);
    }

    private static <TILE extends TileEntityMekanism> MeUpgradeMachineProfile<TILE> profile(TILE tile) {
        return new MeUpgradeMachineProfile<>(
                candidate -> candidate == tile && isUpgradeTarget(candidate),
                candidate -> inputLayout(resolveHost(candidate)),
                candidate -> outputPorts(resolveHost(candidate)),
                MeMekanismMachine.MEKANISM_MAGIC_AUTOMATION,
                candidate -> new ItemStack(candidate.getBlockState().getBlock()),
                candidate -> candidate.getBlockState().getBlock().getName());
    }

    public static MeInputLayout inputLayout(@Nullable IMePatternAutomationHost host) {
        if (host == null || !host.meSupportsPatternAutomation()) {
            return MeInputLayout.empty();
        }
        Set<Object> excluded = excludedSurfaces(host);
        List<IInventorySlot> itemInputs = excludeSlots(host.mePatternItemInputs(), excluded);
        List<MeInputPort> ports = new ArrayList<>();
        if (itemInputs.size() > 1 && host.meGroupParallelItemInputs()) {
            ports.add(MeMachineIoAdapter.autoSortedFactoryItemInput(itemInputs));
        } else {
            for (IInventorySlot slot : itemInputs) {
                ports.add(MeMachineIoAdapter.itemInput(slot));
            }
        }
        for (IExtendedFluidTank tank : excludeFluids(host.mePatternFluidInputs(), excluded)) {
            ports.add(MeMachineIoAdapter.fluidInput(tank));
        }
        for (IChemicalTank tank : excludeChemicals(host.mePatternChemicalInputs(), excluded)) {
            ports.add(MeMachineIoAdapter.chemicalInput(tank));
        }
        return MeInputLayout.unordered(ports);
    }

    public static List<? extends MeOutputPort> outputPorts(@Nullable IMePatternAutomationHost host) {
        if (host == null || !host.meSupportsPatternAutomation()) {
            return List.of();
        }
        Set<Object> excluded = excludedSurfaces(host);
        List<MeOutputPort> ports = new ArrayList<>();
        for (IInventorySlot slot : excludeSlots(host.mePatternItemOutputs(), excluded)) {
            ports.add(MeMachineIoAdapter.itemOutput(slot));
        }
        for (IExtendedFluidTank tank : excludeFluids(host.mePatternFluidOutputs(), excluded)) {
            ports.add(MeMachineIoAdapter.fluidOutput(tank));
        }
        for (IChemicalTank tank : excludeChemicals(host.mePatternChemicalOutputs(), excluded)) {
            ports.add(MeMachineIoAdapter.chemicalOutput(tank));
        }
        return List.copyOf(ports);
    }

    private static Set<Object> excludedSurfaces(IMePatternAutomationHost host) {
        IdentityHashMap<Object, Boolean> excluded = new IdentityHashMap<>();
        addAll(excluded, host.mePersistentItemInputs());
        addAll(excluded, host.mePersistentFluidInputs());
        addAll(excluded, host.mePersistentChemicalInputs());
        addAll(excluded, host.meManualOnlyItemSlots());
        return excluded.keySet();
    }

    private static void addAll(IdentityHashMap<Object, Boolean> excluded, List<?> values) {
        if (values == null) {
            return;
        }
        for (Object value : values) {
            if (value != null) {
                excluded.put(value, Boolean.TRUE);
            }
        }
    }

    private static List<IInventorySlot> excludeSlots(List<IInventorySlot> slots, Set<Object> excluded) {
        if (slots == null || slots.isEmpty()) {
            return List.of();
        }
        List<IInventorySlot> kept = new ArrayList<>(slots.size());
        for (IInventorySlot slot : slots) {
            if (slot != null && excluded.contains(slot)) {
                continue;
            }
            kept.add(slot);
        }
        return kept;
    }

    private static List<IExtendedFluidTank> excludeFluids(List<IExtendedFluidTank> tanks, Set<Object> excluded) {
        if (tanks == null || tanks.isEmpty()) {
            return List.of();
        }
        List<IExtendedFluidTank> kept = new ArrayList<>(tanks.size());
        for (IExtendedFluidTank tank : tanks) {
            if (tank != null && !excluded.contains(tank)) {
                kept.add(tank);
            }
        }
        return kept;
    }

    private static List<IChemicalTank> excludeChemicals(List<IChemicalTank> tanks, Set<Object> excluded) {
        if (tanks == null || tanks.isEmpty()) {
            return List.of();
        }
        List<IChemicalTank> kept = new ArrayList<>(tanks.size());
        for (IChemicalTank tank : tanks) {
            if (tank != null && !excluded.contains(tank)) {
                kept.add(tank);
            }
        }
        return kept;
    }
}
