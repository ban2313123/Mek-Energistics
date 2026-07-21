package com.beipuo.mekenergistics.compat.catalog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.beipuo.mekenergistics.common.machine.MeMekanismMachine;
import java.lang.reflect.RecordComponent;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class CompatMachineCatalogTest {
    @Test
    void catalogCoversEveryMachineExactlyOnce() {
        var specs = CompatMachineCatalog.all().toList();
        assertEquals(MeMekanismMachine.values().length, specs.size());
        assertEquals(specs.size(), specs.stream().map(CompatMachineSpec::machine).distinct().count());
        assertEquals(specs.size(), specs.stream().map(CompatMachineSpec::sourceBlockId).distinct().count());
        assertEquals(specs.size(), specs.stream().map(CompatMachineSpec::meBlockId).distinct().count());
    }

    @Test
    void catalogKeepsStableMeRegistryIdsAndMachineMetadata() {
        CompatMachineCatalog.all().forEach(spec -> {
            assertEquals(ResourceLocation.fromNamespaceAndPath("mekenergistics", spec.machine().registryName()),
                    spec.meBlockId());
            assertFalse(spec.machineTypeId().isBlank(), spec.machine().name());
            assertNotNull(spec.route(), spec.machine().name());
            assertNotNull(spec.family(), spec.machine().name());
            assertNotNull(spec.sideConfigProfile(), spec.machine().name());
            assertEquals(spec.machine().provider(), spec.provider(), spec.machine().name());
            assertEquals(spec.machine().registrationRoute(), spec.route(), spec.machine().name());
            assertEquals(spec.machine().family(), spec.family(), spec.machine().name());
            assertEquals(spec.family().provider(), spec.provider(), spec.machine().name());
            assertEquals(spec.family().route(), spec.route(), spec.machine().name());
            assertEquals(spec.family().kind(), spec.kind(), spec.machine().name());
            assertEquals(spec.machine().machineKind(), spec.kind(), spec.machine().name());
            if (spec.kind() != CompatMachineKind.MACHINE) {
                assertNotNull(spec.tierId(), spec.machine().name());
            }
        });
    }

    @Test
    void sourceLookupRequiresTheExactNamespaceAndPath() {
        CompatMachineSpec recycler = CompatMachineCatalog.get(MeMekanismMachine.RECYCLER);
        assertEquals(MeMekanismMachine.RECYCLER,
                CompatMachineCatalog.findBySourceBlockId(recycler.sourceBlockId())
                        .map(CompatMachineSpec::machine).orElse(null));
        assertTrue(CompatMachineCatalog.findBySourceBlockId(ResourceLocation.fromNamespaceAndPath(
                "unrelated_mod", recycler.sourceBlockId().getPath())).isEmpty());
        assertTrue(CompatMachineCatalog.findBySourceBlockId(ResourceLocation.fromNamespaceAndPath(
                "mekenergistics", "me_" + recycler.sourceBlockId().getPath())).isEmpty());
    }

    @Test
    void optionalFeatureMachinesCarryTheirExactRequirements() {
        assertTrue(CompatMachineCatalog.get(MeMekanismMachine.LARGE_ANTIPROTONIC_NUCLEOSYNTHESIZER)
                .requirements().contains(CompatRequirement.MEKMM_LARGE_MACHINES));
        assertTrue(CompatMachineCatalog.get(MeMekanismMachine.ABSOLUTE_OVERCLOCKED_DISSOLVING_FACTORY)
                .requirements().contains(CompatRequirement.EMEKE_ADVANCED_FACTORIES));
        assertTrue(CompatMachineCatalog.get(MeMekanismMachine.ABSOLUTE_OVERCLOCKED_CENTRIFUGING_FACTORY)
                .requirements().contains(CompatRequirement.EMEKE_ADVANCED_FACTORIES));
        assertTrue(CompatMachineCatalog.get(MeMekanismMachine.ABSOLUTE_OVERCLOCKED_PLANTING_FACTORY)
                .requirements().contains(CompatRequirement.EMEKE_MEKMM_FACTORIES));
        assertEquals(CompatMachineFamily.EMEKE_MEKAF_ADVANCED_FACTORY,
                CompatMachineCatalog.get(MeMekanismMachine.ABSOLUTE_OVERCLOCKED_DISSOLVING_FACTORY).family());
        assertEquals(CompatMachineFamily.EMEKE_MEKMM_FACTORY,
                CompatMachineCatalog.get(MeMekanismMachine.ABSOLUTE_OVERCLOCKED_PLANTING_FACTORY).family());
        var alloyingRequirements = CompatMachineCatalog.get(MeMekanismMachine.ABSOLUTE_ALLOYING_FACTORY).requirements();
        assertEquals(Set.of(CompatRequirement.EMEKE), alloyingRequirements);
        assertEquals(ResourceLocation.fromNamespaceAndPath("emextras", "absolute_alloying_factory"),
                CompatMachineCatalog.get(MeMekanismMachine.ABSOLUTE_ALLOYING_FACTORY).sourceBlockId());
        assertEquals(ResourceLocation.fromNamespaceAndPath("emextras", "supreme_alloying_factory"),
                CompatMachineCatalog.get(MeMekanismMachine.SUPREME_ALLOYING_FACTORY).sourceBlockId());
        assertEquals(ResourceLocation.fromNamespaceAndPath("emextras", "cosmic_alloying_factory"),
                CompatMachineCatalog.get(MeMekanismMachine.COSMIC_ALLOYING_FACTORY).sourceBlockId());
        assertEquals(ResourceLocation.fromNamespaceAndPath("emextras", "infinite_alloying_factory"),
                CompatMachineCatalog.get(MeMekanismMachine.INFINITE_ALLOYING_FACTORY).sourceBlockId());
    }

    @Test
    void providerFamiliesUseOnlyTheirDeclaredRoutes() {
        assertRoutes(CompatMod.MEKANISM, CompatRegistrationRoute.MEKANISM_MACHINE,
                CompatRegistrationRoute.MEKANISM_FACTORY);
        assertRoutes(CompatMod.EMEK, CompatRegistrationRoute.EMEK_MACHINE, CompatRegistrationRoute.EMEK_FACTORY);
        assertRoutes(CompatMod.MEKE, CompatRegistrationRoute.MEKE_FACTORY,
                CompatRegistrationRoute.MEKE_MEKMM_FACTORY, CompatRegistrationRoute.MEKE_MEKMM_ADVANCED_FACTORY);
        assertRoutes(CompatMod.MEKMM, CompatRegistrationRoute.MEKMM_MACHINE, CompatRegistrationRoute.MEKMM_FACTORY,
                CompatRegistrationRoute.MEKMM_ADVANCED_FACTORY);
        assertRoutes(CompatMod.EMEKE, CompatRegistrationRoute.EMEKE_FACTORY,
                CompatRegistrationRoute.EMEKE_ADVANCED_FACTORY);
        assertEquals(Set.of(CompatRegistrationRoute.values()), CompatMachineCatalog.all()
                .map(CompatMachineSpec::route)
                .collect(Collectors.toSet()));
    }

    @Test
    void dataRecordDoesNotRetainOptionalClassesOrRegistrationCallbacks() {
        for (RecordComponent component : CompatMachineSpec.class.getRecordComponents()) {
            Class<?> type = component.getType();
            assertFalse(type == Class.class || Supplier.class.isAssignableFrom(type), component.getName());
            assertFalse(type.getName().startsWith("com.jerry."), component.getName());
            assertFalse(type.getName().startsWith("fr.iglee42."), component.getName());
            assertFalse(type.getName().startsWith("io.github.masyumero."), component.getName());
        }
    }

    @Test
    void machineDeclarationsDoNotUsePrimitiveConstructorDiscriminators() {
        for (var constructor : MeMekanismMachine.class.getDeclaredConstructors()) {
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            // Enum bytecode prepends the synthetic name and ordinal parameters.
            for (int i = 2; i < parameterTypes.length; i++) {
                assertFalse(parameterTypes[i].isPrimitive(), constructor.toString());
            }
        }
    }

    private static void assertRoutes(CompatMod provider, CompatRegistrationRoute... allowedRoutes) {
        Set<CompatRegistrationRoute> allowed = Set.of(allowedRoutes);
        Set<CompatRegistrationRoute> actual = CompatMachineCatalog.all()
                .filter(spec -> spec.provider() == provider)
                .map(CompatMachineSpec::route)
                .collect(Collectors.toSet());
        assertFalse(actual.isEmpty(), provider.name());
        assertTrue(allowed.containsAll(actual), () -> provider + " has unexpected routes " + actual);
    }
}
