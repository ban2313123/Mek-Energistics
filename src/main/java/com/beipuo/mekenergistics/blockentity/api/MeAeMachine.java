package com.beipuo.mekenergistics.blockentity.api;

import appeng.api.crafting.IPatternDetails;
import appeng.api.implementations.blockentities.PatternContainerGroup;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IManagedGridNode;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.KeyCounter;
import appeng.helpers.patternprovider.PatternContainer;
import com.beipuo.mekenergistics.blockentity.api.MeFactoryAeMachine;
import com.beipuo.mekenergistics.blockentity.support.AbstractMeAeSupport;
import com.beipuo.mekenergistics.upgrade.MePassiveCraftingSettings;
import com.beipuo.mekenergistics.upgrade.MeUpgradeContainer;
import com.beipuo.mekenergistics.upgrade.MeUpgradeStateOwner;
import com.beipuo.mekenergistics.upgrade.MeMachineMode;
import com.beipuo.mekenergistics.upgrade.MeUpgradeType;
import com.beipuo.mekenergistics.upgrade.MePassiveCraftingUpgrade;
import com.beipuo.mekenergistics.blockentity.support.MeOwnerHelper;
import com.beipuo.mekenergistics.blockentity.slot.PatternSlotInternalInventory;
import com.beipuo.mekenergistics.common.machine.MeMekanismMachine;
import com.beipuo.mekenergistics.registry.ModBlocks;
import java.util.List;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
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

    /** Resolves the machine running mode; machines without a self-owned upgrade container stay in
     * pattern mode. The mode itself lives on {@link MeUpgradeStateOwner} so interfaces and mixin
     * classes implementing both {@code MeAeMachine} and {@code MeUpgradeStateOwner} do not inherit
     * conflicting defaults. */
    static MeMachineMode modeOf(MeAeMachine machine) {
        return machine instanceof MeUpgradeStateOwner owner ? owner.getMeMachineMode() : MeMachineMode.PATTERN_PROVIDER;
    }

    AeOutputMode getAeOutputMode();

    @Override
    default TileEntityMekanism getAeOwnerTile() {
        return (TileEntityMekanism) this;
    }

    void cycleAeOutputMode();

    default void setAeOutputMode(AeOutputMode mode) {
        if (mode == null) {
            return;
        }
        for (int i = 0; i < AeOutputMode.values().length && getAeOutputMode() != mode; i++) {
            cycleAeOutputMode();
        }
    }

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

    /**
     * Adds the canonical AE pattern slots to a Mekanism inventory holder. Both standalone
     * machines and factories must use this same structural hook so menus and AE routing share
     * the exact slot instances.
     */
    default IInventorySlotHolder withPatternSlots(IInventorySlotHolder original) {
        return getRecipeAeSupport().withPatternSlots(original);
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
        if (MeAeMachine.modeOf(this).isOutputInterface()) {
            return false;
        }
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

    default boolean hasPassiveCraftingUpgrade() {
        if (MeAeMachine.modeOf(this).isOutputInterface()) {
            return false;
        }
        TileEntityMekanism tile = getAeOwnerTile();
        if (tile.getLevel() != null && tile.getLevel().isClientSide()) {
            Boolean synced = getRecipeAeSupport().getClientPassiveCraftingEnabled();
            if (synced != null) {
                return synced;
            }
        }
        if (this instanceof MeUpgradeStateOwner owner) {
            MeUpgradeContainer container = owner.getMeUpgradeContainer();
            if (container != null) {
                return container.isInstalled(MeUpgradeType.PASSIVE_CRAFTING);
            }
        }
        if (tile.getComponent() == null || !tile.getComponent().isUpgradeInstalled(MePassiveCraftingUpgrade.get())) {
            return false;
        }
        if (this instanceof MeFactoryAeMachine) {
            return true;
        }
        return !(this instanceof MeUpgradeableMachine machine) || machine.isMeUpgradeActive();
    }

    default MePassiveCraftingSettings getPassiveCraftingSettings() {
        return getRecipeAeSupport().getPassiveCraftingSettings();
    }

    default void setPassiveCraftingSettings(int intervalTicks, long multiplier) {
        getRecipeAeSupport().setPassiveCraftingSettings(intervalTicks, multiplier);
    }

    AbstractMeAeSupport<?> getRecipeAeSupport();

    @Override
    default AbstractMeAeSupport<?> getPatternAeSupport() {
        return getRecipeAeSupport();
    }

    default List<IPatternDetails> getAvailablePatterns() {
        if (MeAeMachine.modeOf(this).isOutputInterface()) {
            return List.of();
        }
        return getRecipeAeSupport().getAvailablePatterns();
    }

    default int getPatternPriority() {
        return getRecipeAeSupport().getPatternPriority();
    }

    @Override
    default boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
        if (MeAeMachine.modeOf(this).isOutputInterface()) {
            return false;
        }
        return getRecipeAeSupport().pushPatternWithAdapter(patternDetails, inputHolder);
    }

    @Override
    default long maxAcceptedPatternCopies(KeyCounter[] oneCraftInputs) {
        if (MeAeMachine.modeOf(this).isOutputInterface()) {
            return 0;
        }
        return getRecipeAeSupport().maxAcceptedCopies(oneCraftInputs);
    }

    @Override
    default boolean isBusy() {
        if (MeAeMachine.modeOf(this).isOutputInterface()) {
            return true;
        }
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
