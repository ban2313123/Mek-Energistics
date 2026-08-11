package com.beipuo.mekenergistics.compat.catalog;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.beipuo.mekenergistics.common.machine.MeMekanismMachine;
import com.beipuo.mekenergistics.compat.provider.CompatMachineProvider;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/**
 * The catalog is the traversal entry point for the machine closure contract: every machine that
 * advertises an ME variant must ship the generated recipe, loot table and blockstate, come from a
 * mod with a resolvable provider, and never be left as a nominal block without an AE host.
 */
class CompatMachineCatalogClosureContractTest {
    private static final Path GENERATED = Path.of("src/generated/resources");

    @Test
    void meVariantsHaveGeneratedResourceClosure() {
        var meVariants = CompatMachineCatalog.all()
                .map(CompatMachineSpec::machine)
                .filter(CompatMachineCatalog::hasMeVariant)
                .toList();
        assertFalse(meVariants.isEmpty(), "catalog must contain at least one ME variant");
        for (MeMekanismMachine machine : meVariants) {
            String id = machine.registryName();
            assertTrue(Files.exists(generated("data/mekenergistics/recipe", id)),
                    () -> machine + " is missing its installer recipe");
            assertTrue(Files.exists(generated("data/mekenergistics/loot_table/blocks", id)),
                    () -> machine + " is missing its loot table");
            assertTrue(Files.exists(generated("assets/mekenergistics/blockstates", id)),
                    () -> machine + " is missing its blockstate");
        }
    }

    @Test
    void seismicVibratorIsExplicitlyExcluded() {
        assertFalse(CompatMachineCatalog.hasMeVariant(MeMekanismMachine.SEISMIC_VIBRATOR),
                "SEISMIC_VIBRATOR is a sensor block with no AE-relevant I/O and must not advertise an ME variant");
        assertFalse(Files.exists(generated("data/mekenergistics/recipe", "me_seismic_vibrator")),
                "stale seismic installer recipe must not remain");
        assertFalse(Files.exists(generated("data/mekenergistics/loot_table/blocks", "me_seismic_vibrator")),
                "stale seismic loot table must not remain");
        assertFalse(Files.exists(generated("assets/mekenergistics/blockstates", "me_seismic_vibrator")),
                "stale seismic blockstate must not remain");
        assertFalse(Files.exists(generated("assets/mekenergistics/models/item", "me_seismic_vibrator")),
                "stale seismic item model must not remain");
    }

    @Test
    void everyMeVariantHasAProviderEntry() throws ReflectiveOperationException {
        Map<CompatMod, String> providers = providerTable();
        Set<CompatMod> providersUsed = CompatMachineCatalog.all()
                .map(CompatMachineSpec::machine)
                .filter(CompatMachineCatalog::hasMeVariant)
                .map(MeMekanismMachine::provider)
                .collect(Collectors.toSet());
        assertTrue(providers.keySet().containsAll(providersUsed),
                () -> "catalog me-variants use providers without a table entry: " + providersUsed.stream()
                        .filter(provider -> !providers.containsKey(provider))
                        .toList());

        String mekanismProvider = providers.get(CompatMod.MEKANISM);
        assertNotNull(mekanismProvider, "MEKANISM provider table entry is missing");
        Object provider = Class.forName(mekanismProvider).getDeclaredConstructor().newInstance();
        assertNotNull(provider, "MEKANISM provider could not be constructed");
        assertInstanceOf(CompatMachineProvider.class, provider);
    }

    private static Map<CompatMod, String> providerTable() throws ReflectiveOperationException {
        Field table = Class.forName("com.beipuo.mekenergistics.compat.provider.CompatMachineProviders")
                .getDeclaredField("PROVIDER_CLASSES");
        table.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<CompatMod, String> providers = (Map<CompatMod, String>) table.get(null);
        return providers;
    }

    private static Path generated(String directory, String id) {
        return GENERATED.resolve(directory).resolve(id + ".json");
    }
}
