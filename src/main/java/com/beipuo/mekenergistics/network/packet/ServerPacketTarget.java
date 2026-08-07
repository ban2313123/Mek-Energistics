package com.beipuo.mekenergistics.network.packet;

import com.beipuo.mekenergistics.block.MeMekanismMachineBlock;
import com.beipuo.mekenergistics.blockentity.api.MeUpgradeableMachine;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import mekanism.common.inventory.container.tile.MekanismTileContainer;

final class ServerPacketTarget {
    static final int MAX_TERMINAL_NAME_LENGTH = 64;
    private static final double MAX_DISTANCE_SQUARED = 64.0D;

    private ServerPacketTarget() {
    }

    static <T> Optional<T> find(ServerPlayer player, BlockPos pos, Class<T> type) {
        if (!isInRange(player, pos) || !player.level().isLoaded(pos)) {
            return Optional.empty();
        }
        BlockEntity blockEntity = player.level().getBlockEntity(pos);
        if (!type.isInstance(blockEntity) || !isOpenTarget(player, blockEntity)) {
            return Optional.empty();
        }
        if (blockEntity instanceof MeUpgradeableMachine machine
                && !(blockEntity.getBlockState().getBlock() instanceof MeMekanismMachineBlock)) {
            if (!machine.isMeUpgradeTarget() || !machine.isMeUpgradeActive()) {
                return Optional.empty();
            }
        }
        return Optional.of(type.cast(blockEntity));
    }

    static boolean isInRange(ServerPlayer player, BlockPos pos) {
        return isInRange(player.position(), pos);
    }

    static boolean isInRange(Vec3 playerPosition, BlockPos pos) {
        return playerPosition.distanceToSqr(Vec3.atCenterOf(pos)) <= MAX_DISTANCE_SQUARED;
    }

    static boolean isValidTerminalName(String name) {
        return name != null && name.length() <= MAX_TERMINAL_NAME_LENGTH;
    }

    private static boolean isOpenTarget(ServerPlayer player, BlockEntity blockEntity) {
        if (!(player.containerMenu instanceof MekanismTileContainer<?> container)
                || container.getTileEntity() != blockEntity) {
            return false;
        }
        return container.canPlayerAccess(player);
    }

}
