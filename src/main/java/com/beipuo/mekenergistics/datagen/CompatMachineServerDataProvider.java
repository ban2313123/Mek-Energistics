package com.beipuo.mekenergistics.datagen;

import com.beipuo.mekenergistics.compat.catalog.CompatMachineCatalog;
import com.beipuo.mekenergistics.compat.catalog.CompatMachineSpec;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

public final class CompatMachineServerDataProvider implements DataProvider {
    private final PackOutput.PathProvider recipes;
    private final PackOutput.PathProvider blockLootTables;

    public CompatMachineServerDataProvider(PackOutput output) {
        this.recipes = output.createPathProvider(PackOutput.Target.DATA_PACK, "recipe");
        this.blockLootTables = output.createPathProvider(PackOutput.Target.DATA_PACK, "loot_table/blocks");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        List<CompletableFuture<?>> writes = new ArrayList<>();
        machineSpecs().forEach(spec -> {
            writes.add(DataProvider.saveStable(output, CompatMachineDataJson.installerRecipe(spec),
                    this.recipes.json(spec.meBlockId())));
            writes.add(DataProvider.saveStable(output, CompatMachineDataJson.selfDropLootTable(spec),
                    this.blockLootTables.json(spec.meBlockId())));
        });
        return CompletableFuture.allOf(writes.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "Mek Energistics catalog machine server resources";
    }

    private static List<CompatMachineSpec> machineSpecs() {
        return CompatMachineCatalog.all()
                .filter(spec -> spec.machine().hasMeVariant())
                .sorted(Comparator.comparing(spec -> spec.meBlockId().toString()))
                .toList();
    }
}
