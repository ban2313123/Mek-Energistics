package com.beipuo.mekenergistics.network.packet;

import com.beipuo.mekenergistics.MekEnergistics;
import com.beipuo.mekenergistics.upgrade.MeUpgradeContainer;
import com.beipuo.mekenergistics.upgrade.MeUpgradeStateOwner;
import com.beipuo.mekenergistics.upgrade.MeUpgradeType;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Client-to-server request for the installed ME upgrades of one machine. */
public record RequestUpgradeStatePacket(BlockPos pos) implements CustomPacketPayload {
    public static final Type<RequestUpgradeStatePacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MekEnergistics.MODID, "request_upgrade_state"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestUpgradeStatePacket> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, RequestUpgradeStatePacket::pos,
                    RequestUpgradeStatePacket::new);

    @Override
    public Type<RequestUpgradeStatePacket> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            ServerPacketTarget.find(player, this.pos, MeUpgradeStateOwner.class).ifPresent(owner -> {
                MeUpgradeContainer container = owner.getMeUpgradeContainer();
                if (container == null) {
                    return;
                }
                PacketDistributor.sendToPlayer(player,
                        new UpgradeStateSyncPacket(this.pos, installedList(container)));
            });
        });
    }

    private static List<MeUpgradeType> installedList(MeUpgradeContainer container) {
        List<MeUpgradeType> installed = new ArrayList<>();
        for (MeUpgradeType type : MeUpgradeType.values()) {
            if (container.data().isInstalled(type)) {
                installed.add(type);
            }
        }
        return installed;
    }
}