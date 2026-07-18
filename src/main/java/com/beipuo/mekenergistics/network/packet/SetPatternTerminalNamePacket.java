package com.beipuo.mekenergistics.network.packet;

import com.beipuo.mekenergistics.MekEnergistics;
import com.beipuo.mekenergistics.blockentity.api.MeAeMachine;
import com.beipuo.mekenergistics.blockentity.api.MeAeSupportOwner;
import com.beipuo.mekenergistics.blockentity.api.MeFactoryAeMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SetPatternTerminalNamePacket(BlockPos pos, String name) implements CustomPacketPayload {
    public static final Type<SetPatternTerminalNamePacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MekEnergistics.MODID, "set_pattern_terminal_name"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SetPatternTerminalNamePacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SetPatternTerminalNamePacket::pos,
            ByteBufCodecs.stringUtf8(ServerPacketTarget.MAX_TERMINAL_NAME_LENGTH), SetPatternTerminalNamePacket::name,
            SetPatternTerminalNamePacket::new
    );

    @Override
    public Type<SetPatternTerminalNamePacket> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player) || !ServerPacketTarget.isValidTerminalName(this.name)) {
                return;
            }
            ServerPacketTarget.find(player, this.pos, MeAeSupportOwner.class).ifPresent(owner -> {
                if (owner instanceof MeAeMachine machine) {
                    machine.setCustomPatternTerminalName(this.name);
                } else if (owner instanceof MeFactoryAeMachine machine) {
                    machine.setCustomPatternTerminalName(this.name);
                }
            });
        });
    }
}
