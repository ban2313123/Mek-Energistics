package com.beipuo.mekenergistics.network.packet;

import com.beipuo.mekenergistics.MekEnergistics;
import com.beipuo.mekenergistics.upgrade.MeUpgradeType;
import java.util.List;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Server-to-client snapshot of the installed ME upgrades of one machine for the uninstall window.
 * Unknown serialized names decode to {@code null} and are filtered out by the compacting
 * constructor.
 */
public record UpgradeStateSyncPacket(BlockPos pos, List<MeUpgradeType> installed)
        implements CustomPacketPayload {
    public static final Type<UpgradeStateSyncPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MekEnergistics.MODID, "upgrade_state_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, UpgradeStateSyncPacket> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, UpgradeStateSyncPacket::pos,
                    ByteBufCodecs.STRING_UTF8.map(MeUpgradeType::bySerializedName, MeUpgradeType::getSerializedName)
                            .apply(ByteBufCodecs.list()),
                    UpgradeStateSyncPacket::installed,
                    UpgradeStateSyncPacket::new);

    public UpgradeStateSyncPacket {
        installed = installed == null ? List.of() : installed.stream().filter(Objects::nonNull).toList();
    }

    @Override
    public Type<UpgradeStateSyncPacket> type() {
        return TYPE;
    }
}