package com.beipuo.mekenergistics.blockentity.api;

import appeng.api.crafting.IPatternDetails;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IManagedGridNode;
import appeng.me.helpers.IGridConnectedBlockEntity;
import com.beipuo.mekenergistics.blockentity.api.AeOutputMode;
import com.beipuo.mekenergistics.blockentity.support.AbstractMeAeSupport;
import com.beipuo.mekenergistics.blockentity.support.MeFactoryAeSupport;
import com.beipuo.mekenergistics.blockentity.support.MeOwnerHelper;
import com.beipuo.mekenergistics.common.machine.MeMekanismMachine;
import com.beipuo.mekenergistics.upgrade.MeMachineMode;
import java.util.List;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.container.sync.SyncableLong;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public interface MeFactoryAeMachine extends MeAeMachine {
    MeFactoryAeSupport getAeSupport();

    @Override
    default com.beipuo.mekenergistics.blockentity.support.AbstractMeAeSupport<?> getRecipeAeSupport() {
        return getAeSupport();
    }

    MeMekanismMachine getMachine();

    Level getOwnerLevel();

    @Override
    default mekanism.common.tile.base.TileEntityMekanism getAeOwnerTile() {
        return (mekanism.common.tile.base.TileEntityMekanism) this;
    }

    @Override
    default IManagedGridNode getMainNode() {
        return getAeSupport().getMainNode();
    }

    @Override
    default void saveChanges() {
        if (this instanceof net.minecraft.world.level.block.entity.BlockEntity blockEntity) {
            blockEntity.setChanged();
        }
    }

    default List<BasicInventorySlot> getPatternSlots() {
        return getAeSupport().getPatternSlots();
    }

    @Override
    default List<IPatternDetails> getAvailablePatterns() {
        if (MeAeMachine.modeOf(this).isOutputInterface()) {
            return List.of();
        }
        return getAeSupport().getAvailablePatterns();
    }

    @Override
    default int getPatternPriority() {
        return getAeSupport().getPatternPriority();
    }

    default AeOutputMode getAeOutputMode() {
        return getAeSupport().getAeOutputMode();
    }

    default void setAeOutputMode(AeOutputMode mode) {
        getAeSupport().setAeOutputMode(mode);
        saveChanges();
    }

    default String getCustomPatternTerminalName() {
        return getAeSupport().getPatternTerminalName();
    }

    default void setCustomPatternTerminalName(String name) {
        getAeSupport().setPatternTerminalName(name);
    }

    default void cycleAeOutputMode() {
        getAeSupport().cycleAeOutputMode();
    }

    default void cycleAeOutputMode(TransmissionType type) {
        getAeSupport().cycleAeOutputMode(type);
    }

    @Override
    default boolean isVisibleInTerminal() {
        if (MeAeMachine.modeOf(this).isOutputInterface()) {
            return false;
        }
        return getAeSupport().isVisibleInPatternAccessTerminal();
    }

    default void setVisibleInPatternAccessTerminal(boolean visible) {
        getAeSupport().setVisibleInPatternAccessTerminal(visible);
    }

    default boolean isSmartPatternMultiplicationEnabled() {
        return getAeSupport().isSmartPatternMultiplicationEnabled();
    }

    default void setSmartPatternMultiplicationEnabled(boolean enabled) {
        getAeSupport().setSmartPatternMultiplicationEnabled(enabled);
    }

    @Override
    default AbstractMeAeSupport<?> getPatternAeSupport() {
        return getAeSupport();
    }

    @Override
    default boolean pushPattern(IPatternDetails patternDetails, appeng.api.stacks.KeyCounter[] inputHolder) {
        if (MeAeMachine.modeOf(this).isOutputInterface()) {
            return false;
        }
        return getAeSupport().pushPatternWithAdapter(patternDetails, inputHolder);
    }

    @Override
    default long maxAcceptedPatternCopies(appeng.api.stacks.KeyCounter[] oneCraftInputs) {
        if (MeAeMachine.modeOf(this).isOutputInterface()) {
            return 0;
        }
        return getAeSupport().maxAcceptedCopies(oneCraftInputs);
    }

    @Override
    default boolean isBusy() {
        if (MeAeMachine.modeOf(this).isOutputInterface()) {
            return true;
        }
        return getAeSupport().isPatternBusy();
    }

    default void addAeOutputModeTracker(MekanismContainer container) {
        container.track(mekanism.common.inventory.container.sync.SyncableBoolean.create(this::hasPassiveCraftingUpgrade,
                getAeSupport()::setClientPassiveCraftingEnabled));
        container.track(SyncableInt.create(() -> getAeOutputMode().ordinal(),
                mode -> getAeSupport().setAeOutputMode(AeOutputMode.byId(mode))));
        container.track(mekanism.common.inventory.container.sync.SyncableBoolean.create(
                this::isSmartPatternMultiplicationEnabled, this::setSmartPatternMultiplicationEnabled));
        container.track(mekanism.common.inventory.container.sync.SyncableBoolean.create(
                this::isVisibleInTerminal, this::setVisibleInPatternAccessTerminal));
        container.track(SyncableInt.create(() -> getPassiveCraftingSettings().intervalTicks(), value -> getPassiveCraftingSettings().set(value, getPassiveCraftingSettings().multiplier())));
        container.track(SyncableLong.create(() -> getPassiveCraftingSettings().multiplier(), value -> getPassiveCraftingSettings().set(getPassiveCraftingSettings().intervalTicks(), value)));
    }

    default void setOwner(ServerPlayer player) {
        getAeSupport().setOwningPlayer(player);
        if (this instanceof mekanism.common.tile.base.TileEntityMekanism tile) {
            MeOwnerHelper.claimMekanismOwnerIfMissing(tile, player);
        }
    }

    @Override
    default IGrid getGrid() {
        return getAeSupport().getGrid();
    }

    @Override
    default IGridNode getActionableNode() {
        IGridNode node = getMainNode().getNode();
        return node != null && node.isActive() ? node : null;
    }

    @Override
    default InternalInventory getTerminalPatternInventory() {
        return getAeSupport().getTerminalPatternInventory();
    }

    @Override
    default appeng.api.implementations.blockentities.PatternContainerGroup getTerminalGroup() {
        return getAeSupport().getTerminalGroup();
    }
}
