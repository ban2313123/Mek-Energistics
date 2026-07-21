package com.beipuo.mekenergistics.datagen;

import com.beipuo.mekenergistics.compat.catalog.CompatMachineCatalog;
import com.beipuo.mekenergistics.compat.catalog.CompatMachineKind;
import com.beipuo.mekenergistics.compat.catalog.CompatMachineSpec;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

public final class CompatMachineClientDataProvider implements DataProvider {
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
            if (!CompatMachineResourceProfile.hasCustomItemModel(spec)) {
                writes.add(DataProvider.saveStable(output, CompatMachineDataJson.itemModel(spec),
                        this.itemModels.json(spec.meBlockId())));
            }
            if (spec.kind() == CompatMachineKind.MACHINE) {
                writes.add(DataProvider.saveStable(output, CompatMachineDataJson.machineBlockState(spec),
                        this.blockStates.json(spec.meBlockId())));
            } else {
                writes.add(DataProvider.saveStable(output, CompatMachineDataJson.factoryBlockState(spec),
                        this.blockStates.json(spec.meBlockId())));
                if (!CompatMachineResourceProfile.hasHandwrittenFactoryBlockModel(spec)) {
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

}
