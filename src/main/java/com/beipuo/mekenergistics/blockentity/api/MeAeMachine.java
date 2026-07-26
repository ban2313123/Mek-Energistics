package com.beipuo.mekenergistics.blockentity.api;

import appeng.api.crafting.IPatternDetails;
import appeng.api.implementations.blockentities.PatternContainerGroup;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.security.IActionHost;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.KeyCounter;
import appeng.helpers.patternprovider.PatternContainer;
import com.beipuo.mekenergistics.blockentity.support.AbstractMeAeSupport;
import com.beipuo.mekenergistics.blockentity.support.MeOwnerHelper;
import com.beipuo.mekenergistics.blockentity.slot.PatternSlotInternalInventory;
import com.beipuo.mekenergistics.common.machine.MeMekanismMachine;
import com.beipuo.mekenergistics.registry.ModBlocks;
import java.util.List;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public interface MeAeMachine extends PatternContainer, MePatternIoOwner, appeng.me.helpers.IGridConnectedBlockEntity {
    int MAX_PATTERN_TERMINAL_NAME_LENGTH = 64;

    AeOutputMode getAeOutputMode();

    @Override
    default TileEntityMekanism getAeOwnerTile() {
        return (TileEntityMekanism) this;
    }

    void cycleAeOutputMode();

    default void cycleAeOutputMode(TransmissionType type) {
        AeOutputMode target = getAeOutputMode().toggle(type);
        for (int i = 0; i < AeOutputMode.values().length && getAeOutputMode() != target; i++) {
            cycleAeOutputMode();
        }
    }

    @Override
    default IManagedGridNode getMainNode() {
        return getRecipeAeSupport().getMainNode();
    }

    @Override
    default void saveChanges() {
        if (this instanceof BlockEntity blockEntity) {
            blockEntity.setChanged();
        }
    }

    default void setOwner(ServerPlayer player) {
        getRecipeAeSupport().setOwningPlayer(player);
        if (this instanceof TileEntityMekanism tile) {
            MeOwnerHelper.claimMekanismOwnerIfMissing(tile, player);
        }
    }

    default List<BasicInventorySlot> getPatternSlots() {
        return getRecipeAeSupport().getPatternSlots();
    }

    MeMekanismMachine getMachine();

    default ItemStack getTerminalIconStack() {
        return new ItemStack(ModBlocks.getMachineBlock(getMachine()).get());
    }

    default String getCustomPatternTerminalName() {
        return getRecipeAeSupport().getPatternTerminalName();
    }

    default void setCustomPatternTerminalName(String name) {
        getRecipeAeSupport().setPatternTerminalName(name);
    }

    @Override
    default boolean isVisibleInTerminal() {
        return getRecipeAeSupport().isVisibleInPatternAccessTerminal();
    }

    default void setVisibleInPatternAccessTerminal(boolean visible) {
        getRecipeAeSupport().setVisibleInPatternAccessTerminal(visible);
    }

    default boolean isSmartPatternMultiplicationEnabled() {
        return getRecipeAeSupport().isSmartPatternMultiplicationEnabled();
    }

    default void setSmartPatternMultiplicationEnabled(boolean enabled) {
        getRecipeAeSupport().setSmartPatternMultiplicationEnabled(enabled);
    }

    AbstractMeAeSupport<?> getRecipeAeSupport();

    default List<IPatternDetails> getAvailablePatterns() {
        return getRecipeAeSupport().getAvailablePatterns();
    }

    default int getPatternPriority() {
        return getRecipeAeSupport().getPatternPriority();
    }

    @Override
    default boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
        return getRecipeAeSupport().pushPatternWithAdapter(patternDetails, inputHolder);
    }

    @Override
    default boolean isBusy() {
        return getRecipeAeSupport().isPatternBusy();
    }

    default Component getPatternTerminalDisplayName() {
        String customName = getCustomPatternTerminalName();
        return customName.isBlank() ? Component.translatable(getMachine().translationKey()) : Component.literal(customName);
    }

    @Override
    default IGrid getGrid() {
        return getRecipeAeSupport().getGrid();
    }

    @Nullable
    @Override
    default IGridNode getActionableNode() {
        IManagedGridNode node = getMainNode();
        IGridNode gridNode = node == null ? null : node.getNode();
        return gridNode != null && gridNode.isActive() ? gridNode : null;
    }

    @Override
    default InternalInventory getTerminalPatternInventory() {
        return new PatternSlotInternalInventory(this);
    }

    @Override
    default long getTerminalSortOrder() {
        return 0;
    }

    @Override
    default PatternContainerGroup getTerminalGroup() {
        ItemStack iconStack = getTerminalIconStack();
        AEItemKey icon = iconStack.isEmpty() ? null : AEItemKey.of(iconStack);
        return new PatternContainerGroup(icon, getPatternTerminalDisplayName(), List.of());
    }

    static String sanitizePatternTerminalName(String name) {
        if (name == null) {
            return "";
        }
        String sanitized = name.trim();
        return sanitized.length() > MAX_PATTERN_TERMINAL_NAME_LENGTH ? sanitized.substring(0, MAX_PATTERN_TERMINAL_NAME_LENGTH) : sanitized;
    }

}
