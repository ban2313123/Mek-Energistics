package com.beipuo.mekenergistics.client;

import com.beipuo.mekenergistics.client.compat.provider.CompatMachineClientProviders;
import com.beipuo.mekenergistics.client.screen.MeElectricMachineScreen;
import com.beipuo.mekenergistics.client.screen.MeGenericMachineScreen;
import com.beipuo.mekenergistics.client.screen.machine.MeGuiAdvancedElectricMachine;
import com.beipuo.mekenergistics.client.screen.machine.MeGuiChemicalInfuser;
import com.beipuo.mekenergistics.client.screen.machine.MeGuiFactory;
import com.beipuo.mekenergistics.client.screen.machine.MeGuiMetallurgicInfuser;
import com.beipuo.mekenergistics.client.screen.machine.MeGuiPRC;
import com.beipuo.mekenergistics.client.screen.machine.MeGuiCombiner;
import com.beipuo.mekenergistics.client.screen.machine.MeGuiPrecisionSawmill;
import com.beipuo.mekenergistics.client.screen.machine.MeGuiChemicalCrystallizer;
import com.beipuo.mekenergistics.client.screen.machine.MeGuiChemicalDissolutionChamber;
import com.beipuo.mekenergistics.client.screen.machine.MeGuiChemicalOxidizer;
import com.beipuo.mekenergistics.client.screen.machine.MeGuiChemicalWasher;
import com.beipuo.mekenergistics.client.screen.machine.MeGuiElectrolyticSeparator;
import com.beipuo.mekenergistics.client.screen.machine.MeGuiRotaryCondensentrator;
import com.beipuo.mekenergistics.client.screen.machine.MeGuiIsotopicCentrifuge;
import com.beipuo.mekenergistics.client.screen.machine.MeGuiPaintingMachine;
import com.beipuo.mekenergistics.client.screen.machine.MeGuiPigmentExtractor;
import com.beipuo.mekenergistics.client.screen.machine.MeGuiPigmentMixer;
import com.beipuo.mekenergistics.client.screen.machine.MeGuiSolarNeutronActivator;
import com.beipuo.mekenergistics.client.screen.machine.MeGuiNutritionalLiquifier;
import com.beipuo.mekenergistics.client.screen.machine.MeGuiAntiprotonicNucleosynthesizer;
import com.beipuo.mekenergistics.blockentity.machine.process.MeAdvancedElectricMachineBlockEntity;
import com.beipuo.mekenergistics.menu.MePatternMachineContainer;
import com.beipuo.mekenergistics.registry.ModMenuTypes;
import mekanism.client.gui.machine.GuiFormulaicAssemblicator;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public final class ClientSetup {
    private ClientSetup() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(ClientSetup::clientSetup);
        modEventBus.addListener(ClientSetup::registerRenderers);
        modEventBus.addListener(ClientSetup::registerScreens);
    }

    private static void clientSetup(FMLClientSetupEvent event) {
        CompatMachineClientProviders.available().forEach(provider -> provider.registerClientSetup(event));
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        CompatMachineClientProviders.available().forEach(provider -> provider.registerRenderers(event));
    }

    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.ME_ELECTRIC_MACHINE.get(), MeElectricMachineScreen::new);
        event.register(ModMenuTypes.ME_GENERIC_MACHINE.get(), MeGenericMachineScreen::new);
        event.register(ModMenuTypes.ME_ADVANCED_ELECTRIC_MACHINE.get(),
                (MePatternMachineContainer<MeAdvancedElectricMachineBlockEntity> menu, net.minecraft.world.entity.player.Inventory inv, net.minecraft.network.chat.Component title) ->
                        new MeGuiAdvancedElectricMachine<>(menu, inv, title));
        event.register(ModMenuTypes.ME_METALLURGIC_INFUSER.get(), MeGuiMetallurgicInfuser::new);
        event.register(ModMenuTypes.ME_COMBINER.get(), MeGuiCombiner::new);
        event.register(ModMenuTypes.ME_PRECISION_SAWMILL.get(), MeGuiPrecisionSawmill::new);
        event.register(ModMenuTypes.ME_FORMULAIC_ASSEMBLICATOR.get(), GuiFormulaicAssemblicator::new);
        event.register(ModMenuTypes.ME_PRESSURIZED_REACTION_CHAMBER.get(), MeGuiPRC::new);
        event.register(ModMenuTypes.ME_CHEMICAL_CRYSTALLIZER.get(), MeGuiChemicalCrystallizer::new);
        event.register(ModMenuTypes.ME_CHEMICAL_DISSOLUTION_CHAMBER.get(), MeGuiChemicalDissolutionChamber::new);
        event.register(ModMenuTypes.ME_CHEMICAL_INFUSER.get(), MeGuiChemicalInfuser::new);
        event.register(ModMenuTypes.ME_CHEMICAL_OXIDIZER.get(), MeGuiChemicalOxidizer::new);
        event.register(ModMenuTypes.ME_CHEMICAL_WASHER.get(), MeGuiChemicalWasher::new);
        event.register(ModMenuTypes.ME_ROTARY_CONDENSENTRATOR.get(), MeGuiRotaryCondensentrator::new);
        event.register(ModMenuTypes.ME_ELECTROLYTIC_SEPARATOR.get(), MeGuiElectrolyticSeparator::new);
        event.register(ModMenuTypes.ME_SOLAR_NEUTRON_ACTIVATOR.get(), MeGuiSolarNeutronActivator::new);
        event.register(ModMenuTypes.ME_ISOTOPIC_CENTRIFUGE.get(), MeGuiIsotopicCentrifuge::new);
        event.register(ModMenuTypes.ME_NUTRITIONAL_LIQUIFIER.get(), MeGuiNutritionalLiquifier::new);
        event.register(ModMenuTypes.ME_ANTIPROTONIC_NUCLEOSYNTHESIZER.get(), MeGuiAntiprotonicNucleosynthesizer::new);
        event.register(ModMenuTypes.ME_PIGMENT_EXTRACTOR.get(), MeGuiPigmentExtractor::new);
        event.register(ModMenuTypes.ME_PIGMENT_MIXER.get(), MeGuiPigmentMixer::new);
        event.register(ModMenuTypes.ME_PAINTING_MACHINE.get(), MeGuiPaintingMachine::new);
        event.register(ModMenuTypes.ME_FACTORY.get(), MeGuiFactory::new);
        CompatMachineClientProviders.available().forEach(provider -> provider.registerScreens(event));
    }
}
