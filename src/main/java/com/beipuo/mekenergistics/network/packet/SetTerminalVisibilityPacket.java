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

public record SetTerminalVisibilityPacket(BlockPos pos, boolean visible) implements CustomPacketPayload {
    public static final Type<SetTerminalVisibilityPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MekEnergistics.MODID, "set_terminal_visibility"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SetTerminalVisibilityPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SetTerminalVisibilityPacket::pos,
            ByteBufCodecs.BOOL, SetTerminalVisibilityPacket::visible,
            SetTerminalVisibilityPacket::new
    );

    @Override
    public Type<SetTerminalVisibilityPacket> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            ServerPacketTarget.find(player, this.pos, MeAeSupportOwner.class).ifPresent(owner -> {
                if (owner instanceof MeAeMachine machine) {
                    machine.setVisibleInPatternAccessTerminal(this.visible);
                } else if (owner instanceof MeFactoryAeMachine machine) {
                    machine.setVisibleInPatternAccessTerminal(this.visible);
                }
            });
        });
    }
}
