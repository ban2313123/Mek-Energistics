package com.beipuo.mekenergistics.compat.jade;

import appeng.api.networking.IGridNode;
import com.beipuo.mekenergistics.blockentity.api.MeAeMachine;
import com.beipuo.mekenergistics.blockentity.api.MeUpgradeableMachine;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

public class MeAeStatusDataProvider implements IServerDataProvider<BlockAccessor> {
    static final MeAeStatusDataProvider INSTANCE = new MeAeStatusDataProvider();
    static final String TAG_AE_STATE = "MekEnergisticsAeState";

    @Override
    public ResourceLocation getUid() {
        return MekEnergisticsJadePlugin.AE_STATUS;
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        BlockEntity blockEntity = accessor.getBlockEntity();
        if (blockEntity instanceof MeUpgradeableMachine upgradeable
                && upgradeable.isMeUpgradeTarget() && !upgradeable.isMeUpgradeActive()) {
            return;
        }
        if (blockEntity instanceof MeAeMachine machine) {
            data.putByte(TAG_AE_STATE, (byte) getAeState(machine).ordinal());
        }
    }

    private static AeState getAeState(MeAeMachine machine) {
        IGridNode node = machine.getMainNode().getNode();
        if (node != null && node.isPowered()) {
            if (!node.hasGridBooted()) {
                return AeState.NETWORK_BOOTING;
            }
            if (!node.meetsChannelRequirements()) {
                return AeState.MISSING_CHANNEL;
            }
            return AeState.ONLINE;
        }
        return AeState.OFFLINE;
    }

    enum AeState {
        OFFLINE,
        NETWORK_BOOTING,
        MISSING_CHANNEL,
        ONLINE
    }
}
