package com.beipuo.mekenergistics.network.packet;

import com.beipuo.mekenergistics.MekEnergistics;
import com.beipuo.mekenergistics.blockentity.api.MeAeMachine;
import com.beipuo.mekenergistics.blockentity.support.AbstractMeAeSupport;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Client-to-server request for the current ME output interface config of one machine. */
public record RequestInterfaceConfigPacket(BlockPos pos) implements CustomPacketPayload {
    public static final Type<RequestInterfaceConfigPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MekEnergistics.MODID, "request_interface_config"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestInterfaceConfigPacket> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, RequestInterfaceConfigPacket::pos,
                    RequestInterfaceConfigPacket::new);

    @Override
    public Type<RequestInterfaceConfigPacket> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            ServerPacketTarget.find(player, this.pos, MeAeMachine.class).ifPresent(machine -> {
                if (!MeAeMachine.modeOf(machine).isOutputInterface()) {
                    return;
                }
                AbstractMeAeSupport<?> support = machine.getRecipeAeSupport();
                PacketDistributor.sendToPlayer(player, new InterfaceConfigSyncPacket(this.pos,
                        support.getInterfaceConfig().toList(), support.getInterfaceInventory().toList()));
            });
        });
    }
}
