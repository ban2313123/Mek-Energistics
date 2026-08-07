package com.beipuo.mekenergistics.network.packet;

import com.beipuo.mekenergistics.MekEnergistics;
import com.beipuo.mekenergistics.blockentity.api.AeOutputMode;
import com.beipuo.mekenergistics.blockentity.api.MeAeMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SetAeOutputModePacket(BlockPos pos, AeOutputMode mode) implements CustomPacketPayload {
    public static final Type<SetAeOutputModePacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MekEnergistics.MODID, "set_ae_output_mode"));
    private static final StreamCodec<RegistryFriendlyByteBuf, AeOutputMode> MODE_CODEC =
            StreamCodec.of((buffer, mode) -> buffer.writeVarInt(mode.ordinal()),
                    buffer -> AeOutputMode.byId(buffer.readVarInt()));
    public static final StreamCodec<RegistryFriendlyByteBuf, SetAeOutputModePacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SetAeOutputModePacket::pos,
            MODE_CODEC, SetAeOutputModePacket::mode,
            SetAeOutputModePacket::new
    );

    @Override
    public Type<SetAeOutputModePacket> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            ServerPacketTarget.find(player, this.pos, MeAeMachine.class)
                    .ifPresent(machine -> machine.setAeOutputMode(this.mode));
        });
    }
}
