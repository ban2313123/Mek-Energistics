package com.beipuo.mekenergistics.compat.catalog;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class CompatProviderBoundaryTest {
    @Test
    void commonRegistriesDependOnlyOnProviderBoundary() throws IOException {
        for (String file : new String[] {
                "src/main/java/com/beipuo/mekenergistics/registry/ModBlockEntities.java",
                "src/main/java/com/beipuo/mekenergistics/registry/ModBlockTypes.java",
                "src/main/java/com/beipuo/mekenergistics/block/MeMekanismMachineBlock.java",
                "src/main/java/com/beipuo/mekenergistics/item/MeInstallerTargetResolver.java",
                "src/main/java/com/beipuo/mekenergistics/item/MeInstallerUpgradeHandler.java"
        }) {
            String source = Files.readString(Path.of(file));
            assertTrue(source.contains("CompatMachineProvider"), file);
            assertNoOptionalImplementationReference(file, source);
            if (!file.endsWith("MeMekanismMachineBlock.java")) {
                assertFalse(source.contains("net.neoforged.fml.ModList"), file);
            }
        }
    }

    @Test
    void allCommonMachineEntrypointsKeepOptionalImplementationsBehindProviders() throws IOException {
        for (String file : new String[] {
                "src/main/java/com/beipuo/mekenergistics/registry/ModBlocks.java",
                "src/main/java/com/beipuo/mekenergistics/registry/ModMenuTypes.java",
                "src/main/java/com/beipuo/mekenergistics/client/ClientSetup.java",
                "src/main/java/com/beipuo/mekenergistics/blockentity/MeMekanismMachineBlockEntity.java",
                "src/main/java/com/beipuo/mekenergistics/compat/catalog/CompatMachineCatalog.java"
        }) {
            String source = Files.readString(Path.of(file));
            assertNoOptionalImplementationReference(file, source);
        }
        assertTrue(Files.readString(Path.of(
                "src/main/java/com/beipuo/mekenergistics/registry/ModMenuTypes.java"))
                .contains("CompatMachineProviders"));
        assertTrue(Files.readString(Path.of(
                "src/main/java/com/beipuo/mekenergistics/client/ClientSetup.java"))
                .contains("CompatMachineClientProviders"));
    }

    @Test
    void providerClassNameTablesPointToConcreteSourceProviders() throws IOException {
        assertProviderClassNamesExist(
                "src/main/java/com/beipuo/mekenergistics/compat/provider/CompatMachineProviders.java",
                "CompatMachineProvider");
        assertProviderClassNamesExist(
                "src/main/java/com/beipuo/mekenergistics/client/compat/provider/CompatMachineClientProviders.java",
                "CompatMachineClientProvider");
    }

    @Test
    void serverProvidersOwnMenuSelectionForEveryCatalogRoute() throws IOException {
        Map<CompatMod, String> providers = Map.of(
                CompatMod.MEKANISM, "MekanismMachineProvider.java",
                CompatMod.MEKMM, "MekmmMachineProvider.java",
                CompatMod.MEKE, "MekeMachineProvider.java",
                CompatMod.EMEK, "EmekMachineProvider.java",
                CompatMod.EMEKE, "EmekeMachineProvider.java");
        for (var entry : providers.entrySet()) {
            String source = Files.readString(Path.of(
                    "src/main/java/com/beipuo/mekenergistics/compat/provider", entry.getValue()));
            assertTrue(source.contains("menuType("), entry.getKey().name());
            Set<CompatRegistrationRoute> routes = CompatMachineCatalog.all()
                    .filter(spec -> spec.provider() == entry.getKey())
                    .map(CompatMachineSpec::route)
                    .collect(java.util.stream.Collectors.toSet());
            for (CompatRegistrationRoute route : routes) {
                assertTrue(source.contains(route.name()), entry.getKey() + " menu missing " + route);
            }
        }
    }

    @Test
    void optionalClientProvidersCoverAllOptionalMenuFamilies() throws IOException {
        assertContainsAll("MekmmMachineClientProvider.java",
                "MekanismMoreMachineClientScreens", "MekanismMoreMachineAdvancedClientScreens",
                "MekanismMoreMachineLargeClientScreens", "MEKMM_MACHINE", "MEKMM_FACTORY",
                "MEKMM_ADVANCED_FACTORY");
        assertContainsAll("MekeMachineClientProvider.java",
                "MekanismExtrasClientScreens", "MekanismExtrasMoreMachineClientScreens",
                "MekanismExtrasAdvancedClientScreens", "MEKE_FACTORY", "MEKE_MEKMM_FACTORY",
                "MEKE_MEKMM_ADVANCED_FACTORY");
        assertContainsAll("EmekeMachineClientProvider.java",
                "EvolvedMekanismExtrasClientScreens", "EvolvedMekanismExtrasAdvancedClientScreens",
                "EvolvedMekanismExtrasMoreMachineClientScreens", "EMEKE_ADVANCED_FACTORIES",
                "EMEKE_MEKMM_FACTORIES");
    }

    @Test
    void optionalFeatureResolversAreGuardedByCatalogRequirements() throws IOException {
        String mekmm = Files.readString(Path.of(
                "src/main/java/com/beipuo/mekenergistics/compat/provider/MekmmMachineProvider.java"));
        assertTrue(mekmm.contains("hasAvailableRoute(CompatRegistrationRoute.MEKMM_ADVANCED_FACTORY)"));
        assertTrue(mekmm.indexOf("hasAvailableRoute(CompatRegistrationRoute.MEKMM_ADVANCED_FACTORY)")
                < mekmm.indexOf("MekanismMoreMachineAdvancedCompat.getFactoryTarget(state)"));

        String emeke = Files.readString(Path.of(
                "src/main/java/com/beipuo/mekenergistics/compat/provider/EmekeMachineProvider.java"));
        assertTrue(emeke.contains("hasRequirement(CompatRequirement.EMEKE_ADVANCED_FACTORIES)"));
        assertTrue(emeke.contains("hasRequirement(CompatRequirement.EMEKE_MEKMM_FACTORIES)"));
        assertTrue(emeke.contains("EvolvedMekanismExtrasCompat.getBaseFactoryTarget(state)"));
        assertTrue(emeke.contains("EvolvedMekanismExtrasCompat.getAdvancedFactoryTarget(state)"));
        assertTrue(emeke.contains("EvolvedMekanismExtrasCompat.getMoreMachineFactoryTarget(state)"));
    }

    private static void assertContainsAll(String providerFile, String... tokens) throws IOException {
        String source = Files.readString(Path.of(
                "src/main/java/com/beipuo/mekenergistics/client/compat/provider", providerFile));
        for (String token : tokens) {
            assertTrue(source.contains(token), providerFile + " missing " + token);
        }
    }

    private static void assertProviderClassNamesExist(String loaderFile, String contract) throws IOException {
        String source = Files.readString(Path.of(loaderFile));
        var matcher = Pattern.compile("\"(com\\.beipuo\\.mekenergistics\\.[^\"]+Provider)\"").matcher(source);
        int count = 0;
        while (matcher.find()) {
            String className = matcher.group(1);
            Path providerSource = Path.of("src/main/java", className.replace('.', '/') + ".java");
            assertTrue(Files.isRegularFile(providerSource), className);
            assertTrue(Files.readString(providerSource).contains("implements " + contract), className);
            count++;
        }
        assertTrue(count > 0, loaderFile);
    }

    private static void assertNoOptionalImplementationReference(String file, String source) {
        assertFalse(source.contains("compat.mekmm."), file);
        assertFalse(source.contains("compat.meke."), file);
        assertFalse(source.contains("compat.eme."), file);
        assertFalse(source.contains("import com.jerry."), file);
        assertFalse(source.contains("import fr.iglee42."), file);
        assertFalse(source.contains("import io.github.masyumero."), file);
    }

    @Test
    void commonMachineBlockDelegatesOptionalInstallerDetectionToProviders() throws IOException {
        String source = Files.readString(Path.of(
                "src/main/java/com/beipuo/mekenergistics/block/MeMekanismMachineBlock.java"));
        assertTrue(source.contains("CompatMachineProviders.isInstaller(stack)"));
        assertFalse(source.contains("MekanismExtrasCompat.isInstaller"));
        assertFalse(source.contains("EvolvedMekanismCompat.isInstaller"));
        assertFalse(source.contains("EvolvedMekanismExtrasCompat.isInstaller"));
    }

    @Test
    void coreGenericMachineDoesNotLinkOptionalRecipeHelpers() throws IOException {
        String source = Files.readString(Path.of(
                "src/main/java/com/beipuo/mekenergistics/blockentity/MeMekanismMachineBlockEntity.java"));
        assertFalse(source.contains("MekanismMoreMachineRecipeHelper"));
        assertFalse(source.contains("compat.mekmm."));
        assertFalse(source.contains("com.jerry."));
    }

    @Test
    void catalogDataTypesDoNotStoreHandlers() throws IOException {
        Path catalog = Path.of("src/main/java/com/beipuo/mekenergistics/compat/catalog");
        try (var files = Files.list(catalog)) {
            files.filter(path -> path.toString().endsWith(".java")).forEach(path -> {
                try {
                    String source = Files.readString(path);
                    assertFalse(source.contains("Class<?>"), path.toString());
                    assertFalse(source.contains("Supplier<"), path.toString());
                    assertFalse(source.contains("compat.provider"), path.toString());
                } catch (IOException exception) {
                    throw new AssertionError(exception);
                }
            });
        }
    }

    @Test
    void onlyCatalogEnumeratesAllMachines() throws IOException {
        Path sourceRoot = Path.of("src/main/java/com/beipuo/mekenergistics");
        try (var files = Files.walk(sourceRoot)) {
            files.filter(path -> path.toString().endsWith(".java"))
                    .filter(path -> !path.endsWith(Path.of("compat/catalog/CompatMachineCatalog.java")))
                    .forEach(path -> {
                        try {
                            String source = Files.readString(path);
                            assertFalse(source.contains("MeMekanismMachine.values()"), path.toString());
                            assertFalse(source.contains("for (MeMekanismMachine machine : values())"), path.toString());
                        } catch (IOException exception) {
                            throw new AssertionError(exception);
                        }
                    });
        }
    }
}
