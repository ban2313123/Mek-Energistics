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
        assertEquals(MeMekanismMachine.values().length, specs.size(),
                "every machine needs a catalog entry");
        assertEquals(specs.size(), specs.stream().map(CompatMachineSpec::machine).distinct().count(),
                "no machine may be described twice");
        assertEquals(specs.size(), specs.stream().map(CompatMachineSpec::meBlockId).distinct().count(),
                "two machines would collide on one registry id");
    }

    /**
     * Only the fields {@code describe} actually derives are worth asserting. The provider, route,
     * kind and family are copied straight off the enum, so comparing them back to it proves nothing.
     */
    @Test
    void catalogDerivesTheFieldsThatAreNotCopiedFromTheEnum() {
        CompatMachineCatalog.all().forEach(spec -> {
            assertFalse(spec.machineTypeId().isBlank(), spec.machine().name());
            assertNotNull(spec.sideConfigProfile(), spec.machine().name());
            if (spec.kind() != CompatMachineKind.MACHINE) {
                assertNotNull(spec.tierId(), spec.machine().name());
            }
        });
    }

    @Test
    void sourceLookupRequiresTheExactNamespaceAndPath() {
        CompatMachineSpec recycler = CompatMachineCatalog.get(MeMekanismMachine.RECYCLER);
        assertTrue(CompatMachineCatalog.findBySourceBlockId(ResourceLocation.fromNamespaceAndPath(
                "unrelated_mod", recycler.sourceBlockId().getPath())).isEmpty(),
                "a matching path under another namespace must not resolve");
        assertTrue(CompatMachineCatalog.findBySourceBlockId(ResourceLocation.fromNamespaceAndPath(
                "mekenergistics", "me_" + recycler.sourceBlockId().getPath())).isEmpty(),
                "our own block id must not resolve back to the source machine");
    }

    @Test
    void optionalFeatureMachinesCarryTheirExactRequirements() {
        assertTrue(CompatMachineCatalog.get(MeMekanismMachine.LARGE_ANTIPROTONIC_NUCLEOSYNTHESIZER)
                .requirements().contains(CompatRequirement.MEKMM_LARGE_MACHINES));
        assertTrue(CompatMachineCatalog.get(MeMekanismMachine.valueOf("ABSOLUTE_OVERCLOCKED_DISSOLVING_FACTORY"))
                .requirements().contains(CompatRequirement.EMEKE_ADVANCED_FACTORIES));
        assertTrue(CompatMachineCatalog.get(MeMekanismMachine.valueOf("ABSOLUTE_OVERCLOCKED_CENTRIFUGING_FACTORY"))
                .requirements().contains(CompatRequirement.EMEKE_ADVANCED_FACTORIES));
        assertTrue(CompatMachineCatalog.get(MeMekanismMachine.valueOf("ABSOLUTE_OVERCLOCKED_PLANTING_FACTORY"))
                .requirements().contains(CompatRequirement.EMEKE_MEKMM_FACTORIES));
        assertEquals(CompatMachineFamily.EMEKE_MEKAF_ADVANCED_FACTORY,
                CompatMachineCatalog.get(MeMekanismMachine.valueOf("ABSOLUTE_OVERCLOCKED_DISSOLVING_FACTORY")).family());
        assertEquals(CompatMachineFamily.EMEKE_MEKMM_FACTORY,
                CompatMachineCatalog.get(MeMekanismMachine.valueOf("ABSOLUTE_OVERCLOCKED_PLANTING_FACTORY")).family());
        var alloyingRequirements = CompatMachineCatalog.get(MeMekanismMachine.valueOf("ABSOLUTE_ALLOYING_FACTORY")).requirements();
        assertEquals(Set.of(CompatRequirement.EMEKE), alloyingRequirements);
        assertEquals(ResourceLocation.fromNamespaceAndPath("emextras", "absolute_alloying_factory"),
                CompatMachineCatalog.get(MeMekanismMachine.valueOf("ABSOLUTE_ALLOYING_FACTORY")).sourceBlockId());
        assertEquals(ResourceLocation.fromNamespaceAndPath("emextras", "supreme_alloying_factory"),
                CompatMachineCatalog.get(MeMekanismMachine.valueOf("SUPREME_ALLOYING_FACTORY")).sourceBlockId());
        assertEquals(ResourceLocation.fromNamespaceAndPath("emextras", "cosmic_alloying_factory"),
                CompatMachineCatalog.get(MeMekanismMachine.valueOf("COSMIC_ALLOYING_FACTORY")).sourceBlockId());
        assertEquals(ResourceLocation.fromNamespaceAndPath("emextras", "infinite_alloying_factory"),
                CompatMachineCatalog.get(MeMekanismMachine.valueOf("INFINITE_ALLOYING_FACTORY")).sourceBlockId());
    }

    @Test
    void mekanismExtras141PressingFactoriesUseTheMoreMachineRoute() {
        CompatMachineSpec pressing = CompatMachineCatalog.get(
                MeMekanismMachine.valueOf("ABSOLUTE_PRESSING_FACTORY"));
        assertEquals(CompatMachineFamily.MEKE_MEKMM_FACTORY, pressing.family());
        assertEquals(CompatRegistrationRoute.MEKE_MEKMM_FACTORY, pressing.route());
        assertEquals("pressing", pressing.machineTypeId());
        assertEquals(ResourceLocation.fromNamespaceAndPath(
                "mekanism_extras", "absolute_pressing_factory"), pressing.sourceBlockId());
        assertEquals(Set.of(CompatRequirement.MEKE, CompatRequirement.MEKMM,
                        CompatRequirement.MEKE_MEKMM_FACTORIES),
                pressing.requirements());
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
                CompatRegistrationRoute.EMEKE_ADVANCED_FACTORY, CompatRegistrationRoute.EMEKE_MEKMM_FACTORY);
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
