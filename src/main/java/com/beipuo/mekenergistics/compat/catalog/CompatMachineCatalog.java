package com.beipuo.mekenergistics.compat.catalog;

import com.beipuo.mekenergistics.common.machine.MeMekanismMachine;
import com.beipuo.mekenergistics.compat.OptionalCompatClasses;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;

public final class CompatMachineCatalog {
    private static final Map<MeMekanismMachine, CompatMachineSpec> MACHINES = buildCatalog();
    private static final Map<ResourceLocation, CompatMachineSpec> MACHINES_BY_SOURCE_ID = buildSourceIdIndex();

    private CompatMachineCatalog() {
    }

    public static CompatMachineSpec get(MeMekanismMachine machine) {
        CompatMachineSpec spec = MACHINES.get(machine);
        if (spec == null) {
            throw new IllegalArgumentException("Machine is missing from compatibility catalog: " + machine);
        }
        return spec;
    }

    public static Stream<CompatMachineSpec> all() {
        return MACHINES.values().stream();
    }

    public static Stream<CompatMachineSpec> available() {
        return all().filter(CompatMachineCatalog::isAvailable);
    }

    public static boolean isAvailable(MeMekanismMachine machine) {
        return isAvailable(get(machine));
    }

    public static boolean hasAvailableRoute(CompatRegistrationRoute route) {
        return available().anyMatch(spec -> spec.route() == route);
    }

    public static boolean hasAvailableFamily(CompatMachineFamily family) {
        return available().anyMatch(spec -> spec.family() == family);
    }

    public static Optional<CompatMachineSpec> findBySourceBlockId(ResourceLocation sourceBlockId) {
        return Optional.ofNullable(MACHINES_BY_SOURCE_ID.get(sourceBlockId));
    }

    private static Map<ResourceLocation, CompatMachineSpec> buildSourceIdIndex() {
        Map<ResourceLocation, CompatMachineSpec> index = new HashMap<>();
        MACHINES.values().forEach(spec -> {
            CompatMachineSpec previous = index.put(spec.sourceBlockId(), spec);
            if (previous != null) {
                throw new IllegalStateException("Duplicate source block ID " + spec.sourceBlockId());
            }
        });
        return Map.copyOf(index);
    }

    private static Map<MeMekanismMachine, CompatMachineSpec> buildCatalog() {
        EnumMap<MeMekanismMachine, CompatMachineSpec> catalog = new EnumMap<>(MeMekanismMachine.class);
        for (MeMekanismMachine machine : MeMekanismMachine.values()) {
            CompatMachineSpec previous = catalog.put(machine, describe(machine));
            if (previous != null) {
                throw new IllegalStateException("Duplicate compatibility catalog entry for " + machine);
            }
        }
        if (catalog.size() != MeMekanismMachine.values().length) {
            throw new IllegalStateException("Compatibility catalog does not cover every ME machine");
        }
        return Collections.unmodifiableMap(catalog);
    }

    private static CompatMachineSpec describe(MeMekanismMachine machine) {
        CompatMod provider = machine.provider();
        CompatRegistrationRoute route = machine.registrationRoute();
        CompatMachineKind kind = machine.machineKind();
        CompatMachineFamily family = CompatMachineFamily.resolve(route, machine.declaredMachineTypeName());
        Set<CompatRequirement> requirements = requirements(machine, provider, family);
        return new CompatMachineSpec(
                provider,
                machine,
                sourceBlockId(machine, provider),
                ResourceLocation.fromNamespaceAndPath("mekenergistics", machine.registryName()),
                kind,
                machine.declaredTierName(),
                machine.declaredMachineTypeName(),
                sideConfigProfile(machine, route, kind),
                route,
                family,
                requirements);
    }

    private static ResourceLocation sourceBlockId(MeMekanismMachine machine, CompatMod provider) {
        // The four MEKE-tier alloying factories are supplied by Evolved Mekanism Extras,
        // which combines Mekanism Extras' tiers with Evolved Mekanism's alloying type.
        String namespace = provider == CompatMod.MEKE
                && "alloying".equals(machine.declaredMachineTypeName())
                ? CompatMod.EMEKE.modId()
                : provider.modId();
        return ResourceLocation.fromNamespaceAndPath(namespace, machine.baseName());
    }

    private static CompatSideConfigProfile sideConfigProfile(MeMekanismMachine machine,
            CompatRegistrationRoute route, CompatMachineKind kind) {
        if (machine.factoryType() != null && kind != CompatMachineKind.MACHINE) {
            return switch (machine.factoryType()) {
                case SMELTING, ENRICHING, CRUSHING, SAWING -> CompatSideConfigProfile.ELECTRIC;
                case COMPRESSING, INFUSING -> CompatSideConfigProfile.ADVANCED;
                case COMBINING -> CompatSideConfigProfile.EXTRA;
                case PURIFYING, INJECTING -> CompatSideConfigProfile.ADVANCED_INPUT_ONLY;
            };
        }
        if (route == CompatRegistrationRoute.MEKMM_FACTORY
                || route == CompatRegistrationRoute.MEKE_MEKMM_FACTORY) {
            return switch (machine.declaredMachineTypeName()) {
                case "stamping", "pressing" -> CompatSideConfigProfile.EXTRA;
                case "planting", "replicating" -> CompatSideConfigProfile.ADVANCED_INPUT_ONLY;
                default -> CompatSideConfigProfile.ELECTRIC;
            };
        }
        if (kind == CompatMachineKind.ADVANCED_FACTORY) {
            return switch (machine.declaredMachineTypeName()) {
                case "oxidizing", "pigment_extracting" -> CompatSideConfigProfile.CHEMICAL_OUT;
                case "dissolving" -> CompatSideConfigProfile.DISSOLUTION;
                case "washing" -> CompatSideConfigProfile.WASHER;
                case "pressurised_reacting" -> CompatSideConfigProfile.REACTION;
                case "crystallizing" -> CompatSideConfigProfile.CRYSTALLIZER;
                case "centrifuging" -> CompatSideConfigProfile.CENTRIFUGE;
                case "liquifying" -> CompatSideConfigProfile.LIQUIFIER;
                case "painting" -> CompatSideConfigProfile.PAINTING;
                case "planting", "replicating" -> CompatSideConfigProfile.ADVANCED_INPUT_ONLY;
                default -> CompatSideConfigProfile.ELECTRIC;
            };
        }
        return switch (machine) {
            case PLANTING_STATION, REPLICATOR -> CompatSideConfigProfile.ADVANCED_INPUT_ONLY;
            case ENRICHMENT_CHAMBER, CRUSHER, ENERGIZED_SMELTER, PRECISION_SAWMILL ->
                    CompatSideConfigProfile.ELECTRIC;
            case OSMIUM_COMPRESSOR, METALLURGIC_INFUSER -> CompatSideConfigProfile.ADVANCED;
            case ALLOYER, COMBINER, FORMULAIC_ASSEMBLICATOR -> CompatSideConfigProfile.EXTRA;
            case PURIFICATION_CHAMBER, CHEMICAL_INJECTION_CHAMBER, ANTIPROTONIC_NUCLEOSYNTHESIZER ->
                    CompatSideConfigProfile.ADVANCED_INPUT_ONLY;
            case PRESSURIZED_REACTION_CHAMBER -> CompatSideConfigProfile.REACTION;
            case CHEMICAL_CRYSTALLIZER -> CompatSideConfigProfile.CRYSTALLIZER;
            case CHEMICAL_DISSOLUTION_CHAMBER -> CompatSideConfigProfile.DISSOLUTION;
            case CHEMICAL_INFUSER -> CompatSideConfigProfile.CHEMICAL_INFUSING;
            case CHEMICAL_OXIDIZER, PIGMENT_EXTRACTOR -> CompatSideConfigProfile.CHEMICAL_OUT;
            case CHEMICAL_WASHER -> CompatSideConfigProfile.WASHER;
            case ROTARY_CONDENSENTRATOR -> CompatSideConfigProfile.ROTARY;
            case ELECTROLYTIC_SEPARATOR -> CompatSideConfigProfile.SEPARATOR;
            case SOLAR_NEUTRON_ACTIVATOR -> CompatSideConfigProfile.SOLAR_NEUTRON_ACTIVATOR;
            case ISOTOPIC_CENTRIFUGE -> CompatSideConfigProfile.CENTRIFUGE;
            case NUTRITIONAL_LIQUIFIER -> CompatSideConfigProfile.LIQUIFIER;
            case PIGMENT_MIXER -> CompatSideConfigProfile.PIGMENT_MIXER;
            case PAINTING_MACHINE -> CompatSideConfigProfile.PAINTING;
            case OREDICTIONIFICATOR -> CompatSideConfigProfile.OREDICTIONIFICATOR;
            default -> CompatSideConfigProfile.ELECTRIC;
        };
    }

    private static Set<CompatRequirement> requirements(MeMekanismMachine machine, CompatMod provider,
            CompatMachineFamily family) {
        EnumSet<CompatRequirement> requirements = EnumSet.noneOf(CompatRequirement.class);
        boolean emekeAlloying = provider == CompatMod.MEKE
                && "alloying".equals(machine.declaredMachineTypeName());
        if (emekeAlloying) {
            requirements.add(CompatRequirement.EMEKE);
        } else {
            switch (provider) {
                case MEKANISM -> {
                }
                case MEKMM -> requirements.add(CompatRequirement.MEKMM);
                case MEKE -> requirements.add(CompatRequirement.MEKE);
                case EMEK -> requirements.add(CompatRequirement.EMEK);
                case EMEKE -> requirements.add(CompatRequirement.EMEKE);
            }
        }
        switch (family) {
            case MEKMM_MACHINE -> {
                if (machine == MeMekanismMachine.LARGE_ANTIPROTONIC_NUCLEOSYNTHESIZER) {
                    requirements.add(CompatRequirement.MEKMM_LARGE_MACHINES);
                }
            }
            case MEKMM_ADVANCED_FACTORY -> requirements.add(CompatRequirement.MEKMM_ADVANCED_FACTORIES);
            case MEKE_MEKMM_FACTORY -> {
                requirements.add(CompatRequirement.MEKMM);
                requirements.add(CompatRequirement.MEKE_MEKMM_FACTORIES);
            }
            case MEKE_MEKMM_ADVANCED_FACTORY -> {
                requirements.add(CompatRequirement.MEKMM);
                requirements.add(CompatRequirement.MEKE_ADVANCED_FACTORIES);
            }
            case EMEKE_MEKAF_ADVANCED_FACTORY -> requirements.add(CompatRequirement.EMEKE_ADVANCED_FACTORIES);
            case EMEKE_MEKMM_FACTORY -> requirements.add(CompatRequirement.EMEKE_MEKMM_FACTORIES);
            default -> {
            }
        }
        return requirements;
    }

    private static boolean isAvailable(CompatMachineSpec spec) {
        if (!spec.machine().hasMeVariant()) {
            return false;
        }
        for (CompatRequirement requirement : spec.requirements()) {
            boolean available = switch (requirement) {
                case MEKMM -> OptionalCompatClasses.hasMekmm();
                case MEKE -> OptionalCompatClasses.hasMekanismExtras();
                case EMEK -> OptionalCompatClasses.hasEvolvedMekanism();
                case EMEKE -> OptionalCompatClasses.hasEvolvedMekanismExtras();
                case MEKMM_LARGE_MACHINES -> OptionalCompatClasses.hasMekmmLargeMachines();
                case MEKMM_ADVANCED_FACTORIES -> OptionalCompatClasses.hasMekmmAdvancedFactories();
                case MEKE_MEKMM_FACTORIES -> OptionalCompatClasses.hasMekanismExtrasMoreMachineFactories();
                case MEKE_ADVANCED_FACTORIES -> OptionalCompatClasses.hasMekanismExtrasAdvancedFactories();
                case EMEKE_ADVANCED_FACTORIES -> OptionalCompatClasses.hasEvolvedMekanismExtrasAdvancedFactories();
                case EMEKE_MEKMM_FACTORIES -> OptionalCompatClasses.hasEvolvedMekanismExtrasMoreMachineFactories();
            };
            if (!available) {
                return false;
            }
        }
        if (spec.provider() == CompatMod.EMEK && spec.tierId() != null
                && spec.machine().isEvolvedMekanismFactory()) {
            return OptionalCompatClasses.getEvolvedFactoryTier(spec.tierId()) != null;
        }
        if (spec.provider() == CompatMod.EMEKE && spec.tierId() != null) {
            return OptionalCompatClasses.getEvolvedMekanismExtrasFactoryTier(spec.tierId()) != null;
        }
        return true;
    }
}
