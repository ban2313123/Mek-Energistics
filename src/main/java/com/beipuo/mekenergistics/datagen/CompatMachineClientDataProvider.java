package com.beipuo.mekenergistics.datagen;

import com.beipuo.mekenergistics.compat.catalog.CompatMachineCatalog;
import com.beipuo.mekenergistics.compat.catalog.CompatMachineKind;
import com.beipuo.mekenergistics.compat.catalog.CompatMachineSpec;
import com.beipuo.mekenergistics.common.machine.MeMekanismMachine;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

public final class CompatMachineClientDataProvider implements DataProvider {
    private static final List<String> CUSTOM_FACTORY_MODEL_TIERS = List.of(
            "basic", "advanced", "elite", "ultimate", "absolute", "supreme", "cosmic", "infinite");
    private static final List<String> CUSTOM_FACTORY_MODEL_TYPES = List.of("centrifuging", "planting");
    private static final List<MeMekanismMachine> CUSTOM_MACHINE_ITEM_MODELS = List.of(
            MeMekanismMachine.ELECTROLYTIC_SEPARATOR,
            MeMekanismMachine.ISOTOPIC_CENTRIFUGE,
            MeMekanismMachine.PLANTING_STATION);
    private final PackOutput.PathProvider blockStates;
    private final PackOutput.PathProvider blockModels;
    private final PackOutput.PathProvider itemModels;

    public CompatMachineClientDataProvider(PackOutput output) {
        this.blockStates = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "blockstates");
        this.blockModels = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models/block");
        this.itemModels = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models/item");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        List<CompletableFuture<?>> writes = new ArrayList<>();
        machineSpecs().forEach(spec -> {
            if (!hasCustomItemModel(spec)) {
                writes.add(DataProvider.saveStable(output, CompatMachineDataJson.itemModel(spec),
                        this.itemModels.json(spec.meBlockId())));
            }
            if (spec.kind() != CompatMachineKind.MACHINE) {
                writes.add(DataProvider.saveStable(output, CompatMachineDataJson.factoryBlockState(spec),
                        this.blockStates.json(spec.meBlockId())));
                if (!hasCustomFactoryModel(spec)) {
                    writes.add(DataProvider.saveStable(output, CompatMachineDataJson.factoryModel(spec, false),
                            this.blockModels.json(spec.meBlockId())));
                    ResourceLocation activeModel = spec.meBlockId().withSuffix("_active");
                    writes.add(DataProvider.saveStable(output, CompatMachineDataJson.factoryModel(spec, true),
                            this.blockModels.json(activeModel)));
                }
            }
        });
        return CompletableFuture.allOf(writes.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "Mek Energistics catalog machine client resources";
    }

    private static List<CompatMachineSpec> machineSpecs() {
        return CompatMachineCatalog.all()
                .filter(spec -> spec.machine().hasMeVariant())
                .sorted(Comparator.comparing(spec -> spec.meBlockId().toString()))
                .toList();
    }

    private static boolean hasCustomFactoryModel(CompatMachineSpec spec) {
        return spec.tierId() != null && CUSTOM_FACTORY_MODEL_TIERS.contains(spec.tierId())
                && CUSTOM_FACTORY_MODEL_TYPES.contains(spec.machineTypeId());
    }

    private static boolean hasCustomItemModel(CompatMachineSpec spec) {
        return hasCustomFactoryModel(spec) || CUSTOM_MACHINE_ITEM_MODELS.contains(spec.machine());
    }
}
