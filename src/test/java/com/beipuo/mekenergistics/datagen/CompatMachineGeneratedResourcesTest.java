package com.beipuo.mekenergistics.datagen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.beipuo.mekenergistics.compat.catalog.CompatMachineCatalog;
import com.beipuo.mekenergistics.compat.catalog.CompatMachineKind;
import com.beipuo.mekenergistics.compat.catalog.CompatMachineSpec;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class CompatMachineGeneratedResourcesTest {
    private static final Path MAIN = Path.of("src/main/resources");
    private static final Path GENERATED = Path.of("src/generated/resources");

    @Test
    void generatedFilesMatchTheCurrentCatalogBuilders() throws IOException {
        for (CompatMachineSpec spec : meVariants()) {
            String id = spec.meBlockId().getPath();
            assertGeneratedJsonEquals(CompatMachineDataJson.installerRecipe(spec),
                    "data/mekenergistics/recipe", id);
            assertGeneratedJsonEquals(CompatMachineDataJson.selfDropLootTable(spec),
                    "data/mekenergistics/loot_table/blocks", id);
            JsonObject blockState = spec.kind() == CompatMachineKind.MACHINE
                    ? CompatMachineDataJson.machineBlockState(spec)
                    : CompatMachineDataJson.factoryBlockState(spec);
            assertGeneratedJsonEquals(blockState, "assets/mekenergistics/blockstates", id);
            if (!CompatMachineResourceProfile.hasCustomItemModel(spec)) {
                assertGeneratedJsonEquals(CompatMachineDataJson.itemModel(spec),
                        "assets/mekenergistics/models/item", id);
            }
            if (spec.kind() != CompatMachineKind.MACHINE
                    && !CompatMachineResourceProfile.hasHandwrittenFactoryBlockModel(spec)) {
                assertGeneratedJsonEquals(CompatMachineDataJson.factoryModel(spec, false),
                        "assets/mekenergistics/models/block", id);
                assertGeneratedJsonEquals(CompatMachineDataJson.factoryModel(spec, true),
                        "assets/mekenergistics/models/block", id + "_active");
            }
        }
    }

    @Test
    void generatedTreeContainsOnlyCurrentCatalogResources() throws IOException {
        Set<Path> expected = new TreeSet<>();
        for (CompatMachineSpec spec : meVariants()) {
            String id = spec.meBlockId().getPath() + ".json";
            expected.add(Path.of("data/mekenergistics/recipe").resolve(id));
            expected.add(Path.of("data/mekenergistics/loot_table/blocks").resolve(id));
            expected.add(Path.of("assets/mekenergistics/blockstates").resolve(id));
            if (!CompatMachineResourceProfile.hasCustomItemModel(spec)) {
                expected.add(Path.of("assets/mekenergistics/models/item").resolve(id));
            }
            if (spec.kind() != CompatMachineKind.MACHINE
                    && !CompatMachineResourceProfile.hasHandwrittenFactoryBlockModel(spec)) {
                expected.add(Path.of("assets/mekenergistics/models/block").resolve(id));
                expected.add(Path.of("assets/mekenergistics/models/block")
                        .resolve(spec.meBlockId().getPath() + "_active.json"));
            }
        }
        Set<Path> actual = new TreeSet<>(relativeFiles(GENERATED));
        actual.removeIf(path -> path.startsWith(".cache"));
        assertEquals(expected, actual, "Generated resources contain missing or stale catalog files");
    }

    @Test
    void handwrittenItemModelsKeepTheirCustomTransforms() throws IOException {
        Set<String> expectedModels = new HashSet<>(Set.of(
                "me_electrolytic_separator", "me_isotopic_centrifuge", "me_planting_station",
                "me_large_rotary_condensentrator", "me_large_solar_neutron_activator",
                "me_large_electrolytic_separator", "me_large_chemical_infuser",
                "me_large_antiprotonic_nucleosynthesizer"));
        for (String tier : List.of("basic", "advanced", "elite", "ultimate",
                "absolute", "supreme", "cosmic", "infinite")) {
            expectedModels.add("me_" + tier + "_centrifuging_factory");
            expectedModels.add("me_" + tier + "_planting_factory");
        }
        Set<String> actualModels = new HashSet<>();
        for (CompatMachineSpec spec : meVariants()) {
            String id = spec.meBlockId().getPath();
            Path customModel = resource(MAIN, "assets/mekenergistics/models/item", id);
            if (!Files.isRegularFile(customModel)) {
                continue;
            }
            actualModels.add(id);
            JsonObject json = JsonParser.parseString(Files.readString(customModel)).getAsJsonObject();
            assertTrue(json.has("display"), () -> id + " custom item transforms were lost");
            assertFalse(Files.exists(resource(GENERATED, "assets/mekenergistics/models/item", id)),
                    () -> id + " must not be replaced by the generic generated item model");
        }
        assertEquals(expectedModels, actualModels, "Handwritten item model set changed");
    }

    @Test
    void mainAndGeneratedTreesNeverContainTheSameResourcePath() throws IOException {
        Set<Path> mainPaths = relativeFiles(MAIN);
        Set<Path> generatedPaths = relativeFiles(GENERATED);
        generatedPaths.removeIf(path -> path.startsWith(".cache"));
        Set<Path> duplicates = new HashSet<>(mainPaths);
        duplicates.retainAll(generatedPaths);
        assertTrue(duplicates.isEmpty(), () -> "Duplicate main/generated resources: " + duplicates);
    }

    @Test
    void combinedCentrifugingAndPlantingFactoriesKeepTheirTallModels() throws IOException {
        Map<String, String> ledTextures = Map.of(
                "absolute_overclocked", "emextra_led",
                "supreme_quantum", "supreme_quantum_led",
                "cosmic_dense", "cosmic_dense_led",
                "infinite_multiversal", "infinite_multiversal_led");
        for (String tier : List.of(
                "absolute_overclocked", "supreme_quantum", "cosmic_dense", "infinite_multiversal")) {
            JsonObject centrifuging = generatedBlockModel("me_" + tier + "_centrifuging_factory");
            JsonObject centrifugingChildren = centrifuging.getAsJsonObject("children");
            assertEquals("side", centrifuging.get("gui_light").getAsString());
            assertTrue(centrifugingChildren.has("translucent"), tier);
            assertEquals("mekenergistics:block/factory/" + ledTextures.get(tier),
                    centrifugingChildren.getAsJsonObject("front_led")
                            .getAsJsonObject("textures").get("led").getAsString(), tier);

            JsonObject plantingActive = generatedBlockModel("me_" + tier + "_planting_factory_active");
            JsonObject plantingChildren = plantingActive.getAsJsonObject("children");
            assertEquals("mekmm:block/factory/planting/base_active",
                    plantingChildren.getAsJsonObject("base").get("parent").getAsString(), tier);
            assertTrue(plantingChildren.getAsJsonObject("led").has("elements"), tier);
        }
    }





    private static Path resource(Path root, String directory, String name) {
        return root.resolve(directory).resolve(name + ".json");
    }

    private static JsonObject generatedBlockModel(String name) throws IOException {
        return JsonParser.parseString(Files.readString(resource(
                GENERATED, "assets/mekenergistics/models/block", name))).getAsJsonObject();
    }

    private static void assertGeneratedJsonEquals(JsonObject expected, String directory, String name)
            throws IOException {
        Path path = resource(GENERATED, directory, name);
        JsonObject actual = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
        assertEquals(expected, actual, path.toString());
    }

    private static List<CompatMachineSpec> meVariants() {
        return CompatMachineCatalog.all().filter(spec -> spec.machine().hasMeVariant()).toList();
    }

    private static Set<Path> relativeFiles(Path root) throws IOException {
        try (Stream<Path> paths = Files.walk(root)) {
            return paths.filter(Files::isRegularFile)
                    .map(root::relativize)
                    .collect(Collectors.toSet());
        }
    }
}
