package com.beipuo.mekenergistics.compat.jade;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class MeAeStatusComponentProvider implements IComponentProvider<BlockAccessor> {
    static final MeAeStatusComponentProvider INSTANCE = new MeAeStatusComponentProvider();

    @Override
    public ResourceLocation getUid() {
        return MekEnergisticsJadePlugin.AE_STATUS;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (!config.get(MekEnergisticsJadePlugin.AE_STATUS) || !accessor.getServerData().contains(MeAeStatusDataProvider.TAG_AE_STATE)) {
            return;
        }
        int stateId = accessor.getServerData().getByte(MeAeStatusDataProvider.TAG_AE_STATE);
        MeAeStatusDataProvider.AeState[] states = MeAeStatusDataProvider.AeState.values();
        MeAeStatusDataProvider.AeState state = stateId >= 0 && stateId < states.length ? states[stateId] : MeAeStatusDataProvider.AeState.OFFLINE;
        // All four states use this mod's own strings. Three of them previously borrowed AE2's The
        // One Probe keys, which this mod already had translations for and never used -- so a Jade
        // tooltip read "Device Online" next to "AE Network: Booting", and the wording depended on
        // another mod continuing to ship keys for an integration unrelated to Jade.
        tooltip.add(switch (state) {
            case ONLINE -> Component.translatable("tooltip.mekenergistics.ae_status.online")
                    .withStyle(style -> style.withColor(0x5555FF).withBold(true));
            case NETWORK_BOOTING -> Component.translatable("tooltip.mekenergistics.ae_status.booting")
                    .withStyle(ChatFormatting.AQUA);
            case MISSING_CHANNEL -> Component.translatable("tooltip.mekenergistics.ae_status.missing_channel")
                    .withStyle(ChatFormatting.RED);
            case OFFLINE -> Component.translatable("tooltip.mekenergistics.ae_status.offline")
                    .withStyle(ChatFormatting.DARK_GRAY);
        });
    }
}
