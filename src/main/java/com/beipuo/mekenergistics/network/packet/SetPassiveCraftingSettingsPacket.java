package com.beipuo.mekenergistics.network.packet;

import com.beipuo.mekenergistics.MekEnergistics;
import com.beipuo.mekenergistics.blockentity.api.MeAeMachine;
import com.beipuo.mekenergistics.blockentity.api.MeAeSupportOwner;
import com.beipuo.mekenergistics.upgrade.MePassiveCraftingSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SetPassiveCraftingSettingsPacket(BlockPos pos, int intervalTicks, long multiplier)
        implements CustomPacketPayload {
    public static final Type<SetPassiveCraftingSettingsPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MekEnergistics.MODID, "set_passive_crafting_settings"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SetPassiveCraftingSettingsPacket> STREAM_CODEC =
            StreamCodec.of((buffer, packet) -> {
                BlockPos.STREAM_CODEC.encode(buffer, packet.pos);
                buffer.writeVarInt(packet.intervalTicks);
                buffer.writeVarLong(packet.multiplier);
            }, buffer -> new SetPassiveCraftingSettingsPacket(
                    BlockPos.STREAM_CODEC.decode(buffer), buffer.readVarInt(), buffer.readVarLong()));

    @Override
    public Type<SetPassiveCraftingSettingsPacket> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)
                    || this.intervalTicks < MePassiveCraftingSettings.MIN_INTERVAL_TICKS
                    || this.intervalTicks > MePassiveCraftingSettings.MAX_INTERVAL_TICKS
                    || this.multiplier < 1) {
                return;
            }
            ServerPacketTarget.find(player, this.pos, MeAeSupportOwner.class).ifPresent(owner -> {
                if (owner instanceof MeAeMachine machine && machine.hasPassiveCraftingUpgrade()) {
                    machine.setPassiveCraftingSettings(this.intervalTicks, this.multiplier);
                }
            });
        });
    }
}
