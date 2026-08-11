package com.beipuo.mekenergistics.item;

import com.beipuo.mekenergistics.MekEnergistics;
import com.beipuo.mekenergistics.upgrade.MeUpgradeConflictPolicy;
import com.beipuo.mekenergistics.upgrade.MeUpgradeContainer;
import com.beipuo.mekenergistics.upgrade.MeUpgradeStateOwner;
import com.beipuo.mekenergistics.upgrade.MeUpgradeType;
import mekanism.api.security.IBlockSecurityUtils;
import mekanism.common.block.BlockBounding;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Server-side install entry point for ME upgrades. Resolves bounding blocks to their main machine,
 * validates security, support, prerequisites and conflicts, writes the new state through the
 * machine's {@link MeUpgradeContainer}, and only then consumes one item.
 */
public final class MeUpgradeInteractionHandler {
    private MeUpgradeInteractionHandler() {
    }

    public static InteractionResult tryInstall(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        ItemStack stack = context.getItemInHand();
        if (player == null || level == null || !(stack.getItem() instanceof MeUpgradeItem upgradeItem)) {
            return InteractionResult.PASS;
        }
        BlockPos pos = resolveMainPos(level, context.getClickedPos());
        if (pos == null) {
            return InteractionResult.PASS;
        }
        BlockEntity tile = WorldUtils.getTileEntity(level, pos);
        if (!(tile instanceof MeUpgradeStateOwner owner)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.CONSUME;
        }
        if (!IBlockSecurityUtils.INSTANCE.canAccessOrDisplayError(player, level, pos, tile)) {
            return InteractionResult.FAIL;
        }
        MeUpgradeType type = upgradeItem.getType();
        MeUpgradeConflictPolicy.Result result = owner.getMeUpgradeContainer().install(type);
        if (!result.successful()) {
            player.displayClientMessage(Component.translatable(
                    "message.mekenergistics.upgrade_install_failed." + reasonKey(result.reason())), true);
            return InteractionResult.FAIL;
        }
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        MekEnergistics.LOGGER.debug("Installed {} on {} at {}", type.getSerializedName(), pos, level.dimension().location());
        return InteractionResult.CONSUME;
    }

    /** Resolves a bounding block position to the main machine position, if any. */
    public static BlockPos resolveMainPos(Level level, BlockPos clicked) {
        BlockState state = level.getBlockState(clicked);
        if (state.is(MekanismBlocks.BOUNDING_BLOCK)) {
            return BlockBounding.getMainBlockPos(level, clicked);
        }
        return clicked;
    }

    private static String reasonKey(MeUpgradeConflictPolicy.Reason reason) {
        return switch (reason) {
            case CONFLICT -> "conflict";
            case UNSUPPORTED -> "unsupported";
            case MISSING_PREREQUISITE -> "missing_prerequisite";
            case LIMIT_REACHED -> "limit_reached";
            case BLOCKED -> "blocked";
            case NONE -> "none";
        };
    }
}
