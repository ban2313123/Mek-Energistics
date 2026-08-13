package com.beipuo.mekenergistics.network.packet;

import com.beipuo.mekenergistics.MekEnergistics;
import appeng.api.stacks.GenericStack;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Server-to-client snapshot of one machine's ME output interface config. */
public record InterfaceConfigSyncPacket(BlockPos pos, List<GenericStack> config, List<GenericStack> inventory)
        implements CustomPacketPayload {
    public static final Type<InterfaceConfigSyncPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MekEnergistics.MODID, "interface_config_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, InterfaceConfigSyncPacket> STREAM_CODEC =
            StreamCodec.<RegistryFriendlyByteBuf, InterfaceConfigSyncPacket>of(
                    InterfaceConfigSyncPacket::write, InterfaceConfigSyncPacket::read);

    private static void write(RegistryFriendlyByteBuf buffer, InterfaceConfigSyncPacket packet) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeVarInt(packet.config.size());
        for (GenericStack stack : packet.config) {
            GenericStack.writeBuffer(stack, buffer);
        }
        buffer.writeVarInt(packet.inventory.size());
        for (GenericStack stack : packet.inventory) {
            GenericStack.writeBuffer(stack, buffer);
        }
    }

    private static InterfaceConfigSyncPacket read(RegistryFriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        int size = buffer.readVarInt();
        List<GenericStack> config = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            config.add(GenericStack.readBuffer(buffer));
        }
        int inventorySize = buffer.readVarInt();
        List<GenericStack> inventory = new ArrayList<>(inventorySize);
        for (int i = 0; i < inventorySize; i++) {
            inventory.add(GenericStack.readBuffer(buffer));
        }
        return new InterfaceConfigSyncPacket(pos, config, inventory);
    }

    @Override
    public Type<InterfaceConfigSyncPacket> type() {
        return TYPE;
    }
}
