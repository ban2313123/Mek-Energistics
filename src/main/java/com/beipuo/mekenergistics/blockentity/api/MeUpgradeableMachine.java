package com.beipuo.mekenergistics.blockentity.api;

import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.security.IActionHost;
import com.beipuo.mekenergistics.common.machine.MeMekanismMachine;
import com.beipuo.mekenergistics.upgrade.MeUpgradeMachineProfile;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** ME functionality attached to an existing Mekanism machine by an installed upgrade. */
public interface MeUpgradeableMachine extends MeAeMachine, MeSmartCableConnection, ICraftingProvider, IActionHost {
    MeUpgradeMachineProfile<?> getMeUpgradeProfile();

    boolean isMeUpgradeTarget();

    boolean isMeUpgradeActive();

    void createMeNodeIfActive();

    void destroyMeNode();

    void addMeTrackers(MekanismContainer container);

    void saveMeState(CompoundTag tag, HolderLookup.Provider registries);

    void loadMeState(CompoundTag tag, HolderLookup.Provider registries);

    @Override
    default MeMekanismMachine getMachine() {
        return getMeUpgradeProfile().machine();
    }

    @Override
    default ItemStack getTerminalIconStack() {
        return isMeUpgradeTarget()
                ? profileTerminalIcon()
                : MeAeMachine.super.getTerminalIconStack();
    }

    @Override
    default Component getPatternTerminalDisplayName() {
        if (!isMeUpgradeTarget()) {
                return MeAeMachine.super.getPatternTerminalDisplayName();
        }
        String customName = getCustomPatternTerminalName();
        return customName.isBlank() ? profileTerminalName() : Component.literal(customName);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private ItemStack profileTerminalIcon() {
        return ((MeUpgradeMachineProfile) getMeUpgradeProfile()).terminalIconFor((TileEntityMekanism) this);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Component profileTerminalName() {
        return ((MeUpgradeMachineProfile) getMeUpgradeProfile()).terminalNameFor((TileEntityMekanism) this);
    }
}
