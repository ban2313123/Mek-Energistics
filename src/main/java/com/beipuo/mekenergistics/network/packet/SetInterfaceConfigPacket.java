package com.beipuo.mekenergistics.network.packet;

import com.beipuo.mekenergistics.MekEnergistics;
import com.beipuo.mekenergistics.blockentity.api.MeAeMachine;
import com.beipuo.mekenergistics.blockentity.support.AbstractMeAeSupport;
import com.beipuo.mekenergistics.upgrade.MeInterfaceConfig;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.Nullable;

/**
 * Client-to-server edit of one ME output interface config slot. The server revalidates menu,
 * distance, permissions, mode, slot index, key type and amount before writing state.
 */
public record SetInterfaceConfigPacket(BlockPos pos, int slot, @Nullable AEKey key, long amount)
        implements CustomPacketPayload {
    public static final Type<SetInterfaceConfigPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MekEnergistics.MODID, "set_interface_config"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SetInterfaceConfigPacket> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, SetInterfaceConfigPacket::pos,
                    ByteBufCodecs.VAR_INT, SetInterfaceConfigPacket::slot,
                    AEKey.OPTIONAL_STREAM_CODEC, SetInterfaceConfigPacket::key,
                    ByteBufCodecs.VAR_LONG, SetInterfaceConfigPacket::amount,
                    SetInterfaceConfigPacket::new);

    @Override
    public Type<SetInterfaceConfigPacket> type() {
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
                MeInterfaceConfig config = support.getInterfaceConfig();
                if (this.slot < 0 || this.slot >= config.size()) {
                    return;
                }
                if (this.key == null || this.amount <= 0) {
                    config.setStack(this.slot, null);
                    return;
                }
                if (!(this.key instanceof AEItemKey)) {
                    return;
                }
                config.setStack(this.slot, new GenericStack(this.key, this.amount));
            });
        });
    }
}
