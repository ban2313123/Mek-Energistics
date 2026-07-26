package com.beipuo.mekenergistics.blockentity.api;

import appeng.api.crafting.IPatternDetails;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.security.IActionHost;
import appeng.me.helpers.IGridConnectedBlockEntity;
import com.beipuo.mekenergistics.blockentity.api.AeOutputMode;
import com.beipuo.mekenergistics.blockentity.support.MeFactoryAeSupport;
import com.beipuo.mekenergistics.blockentity.support.MeOwnerHelper;
import com.beipuo.mekenergistics.common.machine.MeMekanismMachine;
import java.util.List;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public interface MeFactoryAeMachine extends MePatternIoOwner, IGridConnectedBlockEntity, MeSmartCableConnection, appeng.helpers.patternprovider.PatternContainer {
    MeFactoryAeSupport getAeSupport();

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
        return getAeSupport().getAvailablePatterns();
    }

    @Override
    default int getPatternPriority() {
        return getAeSupport().getPatternPriority();
    }

    default AeOutputMode getAeOutputMode() {
        return getAeSupport().getAeOutputMode();
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
    default boolean pushPattern(IPatternDetails patternDetails, appeng.api.stacks.KeyCounter[] inputHolder) {
        return getAeSupport().pushPatternWithAdapter(patternDetails, inputHolder);
    }

    @Override
    default boolean isBusy() {
        return getAeSupport().isPatternBusy();
    }

    default void addAeOutputModeTracker(MekanismContainer container) {
        container.track(SyncableInt.create(() -> getAeOutputMode().ordinal(),
                mode -> getAeSupport().setAeOutputMode(AeOutputMode.byId(mode))));
        container.track(mekanism.common.inventory.container.sync.SyncableBoolean.create(
                this::isSmartPatternMultiplicationEnabled, this::setSmartPatternMultiplicationEnabled));
        container.track(mekanism.common.inventory.container.sync.SyncableBoolean.create(
                this::isVisibleInTerminal, this::setVisibleInPatternAccessTerminal));
    }

    default void setOwner(ServerPlayer player) {
        if (this instanceof mekanism.common.tile.base.TileEntityMekanism tile) {
            MeOwnerHelper.setOwner(tile, getMainNode(), player);
        } else {
            getMainNode().setOwningPlayer(player);
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
