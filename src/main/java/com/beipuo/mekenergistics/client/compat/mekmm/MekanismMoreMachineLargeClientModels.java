package com.beipuo.mekenergistics.client.compat.mekmm;

import com.beipuo.mekenergistics.common.machine.MeMekanismMachine;
import com.beipuo.mekenergistics.compat.catalog.CompatMachineCatalog;
import com.beipuo.mekenergistics.registry.ModBlockEntities;
import com.jerry.meklm.client.model.bake.LargeAntiprotonicNucleosynthesizerBakedModel;
import com.jerry.meklm.client.model.bake.LargeChemicalInfuserBakedModel;
import com.jerry.meklm.client.model.bake.LargeElectrolyticSeparatorBakedModel;
import com.jerry.meklm.client.model.bake.LargeRotaryCondensentratorBakedModel;
import com.jerry.meklm.client.model.bake.LargeSNABakedModel;
import com.jerry.meklm.client.render.tileentity.RenderLargeAntiprotonicNucleosynthesizer;
import mekanism.client.ClientRegistration;
import mekanism.common.registration.INamedEntry;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public final class MekanismMoreMachineLargeClientModels {
    private MekanismMoreMachineLargeClientModels() {
    }

    public static void registerModels() {
        ClientRegistration.addCustomModel(named(MeMekanismMachine.LARGE_ROTARY_CONDENSENTRATOR),
                (model, event) -> new LargeRotaryCondensentratorBakedModel(model));
        ClientRegistration.addCustomModel(named(MeMekanismMachine.LARGE_CHEMICAL_INFUSER),
                (model, event) -> new LargeChemicalInfuserBakedModel(model));
        ClientRegistration.addCustomModel(named(MeMekanismMachine.LARGE_ELECTROLYTIC_SEPARATOR),
                (model, event) -> new LargeElectrolyticSeparatorBakedModel(model));
        ClientRegistration.addCustomModel(named(MeMekanismMachine.LARGE_SOLAR_NEUTRON_ACTIVATOR),
                (model, event) -> new LargeSNABakedModel(model));
        ClientRegistration.addCustomModel(named(MeMekanismMachine.LARGE_ANTIPROTONIC_NUCLEOSYNTHESIZER),
                (model, event) -> new LargeAntiprotonicNucleosynthesizerBakedModel(model));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                (BlockEntityType) ModBlockEntities.getMachineBlockEntity(
                        MeMekanismMachine.LARGE_ANTIPROTONIC_NUCLEOSYNTHESIZER).get(),
                RenderLargeAntiprotonicNucleosynthesizer::new);
    }

    private static INamedEntry named(MeMekanismMachine machine) {
        return () -> CompatMachineCatalog.get(machine).meBlockId();
    }
}
