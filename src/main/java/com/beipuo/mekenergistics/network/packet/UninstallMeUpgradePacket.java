package com.beipuo.mekenergistics.network.packet;

import com.beipuo.mekenergistics.MekEnergistics;
import com.beipuo.mekenergistics.registry.ModItems;
import com.beipuo.mekenergistics.upgrade.MeUpgradeContainer;
import com.beipuo.mekenergistics.upgrade.MeUpgradeStateOwner;
import com.beipuo.mekenergistics.upgrade.MeUpgradeType;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.Nullable;

/**
 * Client-to-server request to uninstall one ME upgrade from a machine. The server revalidates the
 * open menu, distance, permissions, machine and installed state through {@link ServerPacketTarget}
 * and the self-owned {@link MeUpgradeContainer}; the returned item goes into the player inventory
 * first and only drops when the inventory is full. This deliberately replaces Mekanism's upgrade
 * window flow and never touches {@code TileComponentUpgrade}.
 */
public record UninstallMeUpgradePacket(BlockPos pos, @Nullable MeUpgradeType upgradeType)
        implements CustomPacketPayload {
    public static final Type<UninstallMeUpgradePacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MekEnergistics.MODID, "uninstall_me_upgrade"));
    public static final StreamCodec<RegistryFriendlyByteBuf, UninstallMeUpgradePacket> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, UninstallMeUpgradePacket::pos,
                    ByteBufCodecs.STRING_UTF8.map(MeUpgradeType::bySerializedName, MeUpgradeType::getSerializedName),
                    UninstallMeUpgradePacket::upgradeType,
                    UninstallMeUpgradePacket::new);

    @Override
    public Type<UninstallMeUpgradePacket> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            ServerPacketTarget.find(player, this.pos, MeUpgradeStateOwner.class).ifPresent(owner -> {
                MeUpgradeContainer container = owner.getMeUpgradeContainer();
                if (container == null || this.upgradeType == null || !container.isInstalled(this.upgradeType)) {
                    player.displayClientMessage(Component.translatable(
                            "message.mekenergistics.upgrade_uninstall_failed.not_installed"), true);
                    return;
                }
                if (!container.uninstall(this.upgradeType)) {
                    player.displayClientMessage(Component.translatable(
                            "message.mekenergistics.upgrade_uninstall_failed.guards"), true);
                    return;
                }
                if (!player.getAbilities().instabuild) {
                    ItemStack stack = upgradeStack(this.upgradeType);
                    player.getInventory().add(stack);
                    if (!stack.isEmpty()) {
                        player.level().addFreshEntity(new ItemEntity(player.level(),
                                player.getX(), player.getY(), player.getZ(), stack));
                    }
                }
                MekEnergistics.LOGGER.debug("Uninstalled {} from {} at {}", this.upgradeType.getSerializedName(),
                        this.pos, player.level().dimension().location());
                PacketDistributor.sendToPlayer(player,
                        new UpgradeStateSyncPacket(this.pos, installedList(container)));
            });
        });
    }

    private static ItemStack upgradeStack(MeUpgradeType type) {
        return switch (type) {
            case PATTERN_PROVIDER -> new ItemStack(ModItems.ME_PATTERN_PROVIDER_UPGRADE.get());
            case PASSIVE_CRAFTING -> new ItemStack(ModItems.ME_PASSIVE_CRAFTING_UPGRADE.get());
            case OUTPUT_INTERFACE -> new ItemStack(ModItems.ME_OUTPUT_INTERFACE_UPGRADE.get());
        };
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