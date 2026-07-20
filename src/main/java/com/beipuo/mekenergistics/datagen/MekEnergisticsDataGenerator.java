package com.beipuo.mekenergistics.datagen;

import com.beipuo.mekenergistics.MekEnergistics;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = MekEnergistics.MODID)
public final class MekEnergisticsDataGenerator {

    private MekEnergisticsDataGenerator() {
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        generator.addProvider(event.includeClient(), new CompatMachineClientDataProvider(output));
        generator.addProvider(event.includeServer(), new CompatMachineServerDataProvider(output));
    }
}
