package com.beipuo.mekenergistics.client;

import com.beipuo.mekenergistics.MekEnergistics;
import com.beipuo.mekenergistics.client.overlay.MeInterfaceWindowOverlay;
import com.beipuo.mekenergistics.network.packet.InterfaceConfigSyncPacket;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@Mod(value = MekEnergistics.MODID, dist = Dist.CLIENT)
public final class MekEnergisticsClient {
    public MekEnergisticsClient(IEventBus modEventBus, ModContainer container) {
        ClientSetup.register(modEventBus);
        modEventBus.addListener(MekEnergisticsClient::registerClientPayloads);
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    private static void registerClientPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(InterfaceConfigSyncPacket.TYPE,
                InterfaceConfigSyncPacket.STREAM_CODEC, MeInterfaceWindowOverlay::handleConfigSync);
    }
}
