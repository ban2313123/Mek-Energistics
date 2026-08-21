package com.beipuo.mekenergistics.client;

import com.beipuo.mekenergistics.MekEnergistics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.AddPackFindersEvent;

public final class MeBuiltinPacks {
    private static final String PACK_PATH = "mek_energistics_pack";
    private static final String TITLE_KEY = "pack.mekenergistics.mek_pack.title";

    private MeBuiltinPacks() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(MeBuiltinPacks::onAddPackFinders);
    }

    private static void onAddPackFinders(AddPackFindersEvent event) {
        event.addPackFinders(
                ResourceLocation.fromNamespaceAndPath(MekEnergistics.MODID, PACK_PATH),
                PackType.CLIENT_RESOURCES,
                Component.translatable(TITLE_KEY),
                PackSource.DEFAULT,
                false,
                Pack.Position.TOP);
    }
}
