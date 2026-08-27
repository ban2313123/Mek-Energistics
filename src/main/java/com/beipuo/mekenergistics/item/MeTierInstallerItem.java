package com.beipuo.mekenergistics.item;

import com.beipuo.mekenergistics.blockentity.api.MeAeMachine;
import com.beipuo.mekenergistics.blockentity.api.MeFactoryAeMachine;
import com.beipuo.mekenergistics.blockentity.api.MeUpgradeableMachine;
import com.beipuo.mekenergistics.blockentity.support.MeOwnerHelper;
import com.beipuo.mekenergistics.blockentity.support.MePatternSlotTransfer;
import com.beipuo.mekenergistics.upgrade.MeUpgradePersistence;
import com.beipuo.mekenergistics.upgrade.MeUpgradeStateOwner;
import com.beipuo.mekenergistics.common.machine.MeMekanismMachine;
import com.beipuo.mekenergistics.registry.ModBlocks;
import java.util.Optional;
import mekanism.api.security.IBlockSecurityUtils;
import mekanism.common.block.BlockBounding;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeHasBounding;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ITierUpgradable;
import mekanism.common.tile.interfaces.ITileDirectional;
import mekanism.common.upgrade.IUpgradeData;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class MeTierInstallerItem extends Item {
    public MeTierInstallerItem(Properties properties) {
        super(properties);
    }

    @NotNull
    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getPlayer() == null || !context.getPlayer().isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        return tryInstall(context.getItemInHand(), context.getLevel(), context.getClickedPos(), context.getPlayer());
    }

    public static InteractionResult tryInstall(ItemStack stack, Level level, BlockPos pos, Player player) {
        if (!player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        BlockState state = level.getBlockState(pos);
        if (state.is(MekanismBlocks.BOUNDING_BLOCK)) {
            BlockPos mainPos = BlockBounding.getMainBlockPos(level, pos);
            if (mainPos == null) {
                return InteractionResult.FAIL;
            }
            pos = mainPos;
            state = level.getBlockState(pos);
        }
        MeMekanismMachine current = ModBlocks.getMachine(state.getBlock());
        if (current != null) {
            return MeInstallerUpgradeHandler.tryUpgrade(stack, state, level, pos, player).result();
        }
        MeMekanismMachine target = MeInstallerTargetResolver.resolve(state);
        if (target == null) {
            return InteractionResult.PASS;
        }
        if (ModBlocks.getMachineBlock(target).get() == state.getBlock()) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.PASS;
        }
        BlockEntity oldTile = WorldUtils.getTileEntity(level, pos);
        if (!IBlockSecurityUtils.INSTANCE.canAccessOrDisplayError(player, level, pos, oldTile)) {
            return InteractionResult.FAIL;
        }
        if (oldTile instanceof TileEntityMekanism tileMek && !tileMek.playersUsing.isEmpty()) {
            return InteractionResult.FAIL;
        }
        IUpgradeData upgradeData = null;
        boolean copyComponents = oldTile instanceof TileEntityMekanism;
        if (oldTile instanceof ITierUpgradable tierUpgradable) {
            upgradeData = tierUpgradable.getUpgradeData(level.registryAccess());
            if (upgradeData == null && tierUpgradable.canBeUpgraded()) {
                return InteractionResult.FAIL;
            }
            copyComponents = upgradeData == null && copyComponents;
        } else if (!copyComponents) {
            return InteractionResult.PASS;
        }
        CompoundTag mePatternSlots = MePatternSlotTransfer.save(oldTile, level.registryAccess());
        CompoundTag meState = MePatternSlotTransfer.saveMeState(oldTile, level.registryAccess());
        // Carry the ME upgrade installation (pattern provider / output interface) across the upgrade.
        // runtime.save only persists the AE support state, so without this the active upgrade would
        // rely entirely on Mekanism's native component transfer and could be lost, leaving the machine
        // inactive and its patterns invisible to the network after the upgrade.
        if (oldTile instanceof MeUpgradeStateOwner oldUpg && !oldUpg.getMeUpgradeContainer().data().isEmpty()) {
            MeUpgradePersistence.save(meState, oldUpg.getMeUpgradeContainer().data());
        }
        Block targetBlock = ModBlocks.getMachineBlock(target).get();
        BlockState upgradeState = BlockStateHelper.copyStateData(state, targetBlock.defaultBlockState());
        AttributeHasBounding upgradeBounding = Attribute.get(upgradeState, AttributeHasBounding.class);
        if (upgradeBounding != null && !canPlaceBoundingBlocks(level, pos, upgradeState, upgradeBounding)) {
            return InteractionResult.FAIL;
        }
        BlockState oldState = level.getBlockState(pos);
        CompoundTag oldTileNbt = oldTile.saveWithoutMetadata(level.registryAccess());
        if (!level.setBlockAndUpdate(pos, upgradeState)) {
            return InteractionResult.FAIL;
        }
        try {
            if (upgradeBounding != null) {
                upgradeBounding.placeBoundingBlocks(level, pos, upgradeState);
            }
            TileEntityMekanism upgradedTile = WorldUtils.getTileEntity(TileEntityMekanism.class, level, pos);
            if (upgradedTile == null) {
                return rollbackUpgrade(level, pos, oldState, oldTileNbt, upgradeBounding, upgradeState);
            }
            if (oldTile instanceof ITileDirectional directional && directional.isDirectional()) {
                upgradedTile.setFacing(directional.getDirection(), false);
            }
            if (upgradeData != null) {
                upgradedTile.parseUpgradeData(level.registryAccess(), upgradeData);
            } else if (copyComponents) {
                MePatternSlotTransfer.copyMekanismComponents(oldTile, upgradedTile, targetBlock);
            }
            MePatternSlotTransfer.load(upgradedTile, level.registryAccess(), mePatternSlots);
            MePatternSlotTransfer.loadMeState(upgradedTile, level.registryAccess(), meState);
            // Push any carried-over ME upgrade data into the upgraded machine's native component so
            // isMeUpgradeActive() reports the upgrade as installed even when parseUpgradeData/copy
            // did not transfer the Mekanism upgrade module.
            if (upgradedTile instanceof MeUpgradeStateOwner upgraded) {
                upgraded.getMeUpgradeContainer().migrateToNativeComponent();
            }
            refreshMeLifecycle(upgradedTile);
            if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                if (upgradedTile instanceof MeAeMachine machine) {
                    machine.setOwner(serverPlayer);
                } else if (upgradedTile instanceof MeFactoryAeMachine machine) {
                    machine.setOwner(serverPlayer);
                } else {
                    MeOwnerHelper.claimMekanismOwnerIfMissing(upgradedTile, serverPlayer);
                }
            }
            upgradedTile.resyncMasterToBounding();
            upgradedTile.sendUpdatePacket();
            upgradedTile.setChanged();
            upgradedTile.invalidateCapabilitiesFull();
            if (!player.isCreative()) {
                stack.shrink(1);
            }
            return InteractionResult.CONSUME;
        } catch (RuntimeException failure) {
            return rollbackUpgrade(level, pos, oldState, oldTileNbt, upgradeBounding, upgradeState);
        }
    }

    private static InteractionResult rollbackUpgrade(Level level, BlockPos pos, BlockState oldState, CompoundTag oldTileNbt,
            AttributeHasBounding upgradeBounding, BlockState upgradeState) {
        if (upgradeBounding != null) {
            upgradeBounding.removeBoundingBlocks(level, pos, upgradeState);
        }
        level.setBlockAndUpdate(pos, oldState);
        BlockEntity restoredTile = WorldUtils.getTileEntity(level, pos);
        if (restoredTile != null && oldTileNbt != null) {
            restoredTile.loadWithComponents(oldTileNbt, level.registryAccess());
        }
        AttributeHasBounding oldBounding = Attribute.get(oldState, AttributeHasBounding.class);
        if (oldBounding != null) {
            oldBounding.placeBoundingBlocks(level, pos, oldState);
        }
        if (restoredTile instanceof TileEntityMekanism tileMek) {
            tileMek.resyncMasterToBounding();
            tileMek.sendUpdatePacket();
            tileMek.setChanged();
            tileMek.invalidateCapabilitiesFull();
        }
        return InteractionResult.FAIL;
    }

    private static void refreshMeLifecycle(TileEntityMekanism upgradedTile) {
        if (upgradedTile instanceof MeAeMachine machine) {
            machine.getRecipeAeSupport().refreshAfterWorldMutation();
        } else if (upgradedTile instanceof MeFactoryAeMachine machine) {
            machine.getAeSupport().refreshAfterWorldMutation();
        } else if (upgradedTile instanceof MeUpgradeableMachine machine) {
            machine.getRecipeAeSupport().refreshAfterWorldMutation();
        }
    }

    private static boolean canPlaceBoundingBlocks(net.minecraft.world.level.Level level, BlockPos pos, BlockState upgradeState, AttributeHasBounding upgradeBounding) {
        return upgradeBounding.handle(level, pos, upgradeState, pos, (world, boundingPos, mainPos) -> {
            Optional<BlockState> blockState = WorldUtils.getBlockState(world, boundingPos);
            if (blockState.isEmpty()) {
                return false;
            }
            BlockState current = blockState.get();
            return current.canBeReplaced()
                    || current.is(MekanismBlocks.BOUNDING_BLOCK) && mainPos.equals(BlockBounding.getMainBlockPos(world, boundingPos));
        });
    }

}
