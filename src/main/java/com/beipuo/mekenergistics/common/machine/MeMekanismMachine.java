package com.beipuo.mekenergistics.common.machine;

import java.util.Locale;
import java.util.function.LongSupplier;
import org.jetbrains.annotations.Nullable;
import com.beipuo.mekenergistics.compat.catalog.CompatMachineCatalog;
import com.beipuo.mekenergistics.compat.catalog.CompatFactoryTierGraph;
import com.beipuo.mekenergistics.compat.catalog.CompatMachineFamily;
import com.beipuo.mekenergistics.compat.catalog.CompatMachineKind;
import com.beipuo.mekenergistics.compat.catalog.CompatMod;
import com.beipuo.mekenergistics.compat.catalog.CompatRegistrationRoute;
import com.beipuo.mekenergistics.compat.OptionalCompatClasses;
import mekanism.api.tier.BaseTier;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.blocktype.FactoryType;
import mekanism.common.tier.FactoryTier;
import net.minecraft.network.chat.TextColor;

public enum MeMekanismMachine {
    ENRICHMENT_CHAMBER(FactoryType.ENRICHING, "enrichment_chamber", "ME Enrichment Chamber"),
    CRUSHER(FactoryType.CRUSHING, "crusher", "ME Crusher"),
    ENERGIZED_SMELTER(FactoryType.SMELTING, "energized_smelter", "ME Energized Smelter"),
    PRECISION_SAWMILL(FactoryType.SAWING, "precision_sawmill", "ME Precision Sawmill"),
    OSMIUM_COMPRESSOR(FactoryType.COMPRESSING, "osmium_compressor", "ME Osmium Compressor"),
    COMBINER(FactoryType.COMBINING, "combiner", "ME Combiner"),
    METALLURGIC_INFUSER(FactoryType.INFUSING, "metallurgic_infuser", "ME Metallurgic Infuser"),
    ALLOYER("alloyer", "alloying", "ME Alloyer", CompatMachineFamily.EMEK_MACHINE),
    SOLIDIFICATION_CHAMBER("solidification_chamber", "solidifying", "ME Solidification Chamber", CompatMachineFamily.EMEK_MACHINE),
    THERMALIZER("thermalizer", "melting", "ME Thermalizer", CompatMachineFamily.EMEK_MACHINE),
    CHEMIXER("chemixer", "chemixing", "ME Chemical Mixer", CompatMachineFamily.EMEK_MACHINE),
    PURIFICATION_CHAMBER(FactoryType.PURIFYING, "purification_chamber", "ME Purification Chamber"),
    CHEMICAL_INJECTION_CHAMBER(FactoryType.INJECTING, "chemical_injection_chamber", "ME Chemical Injection Chamber"),
    PRESSURIZED_REACTION_CHAMBER((FactoryType) null, "pressurized_reaction_chamber", "ME Pressurized Reaction Chamber"),
    CHEMICAL_CRYSTALLIZER((FactoryType) null, "chemical_crystallizer", "ME Chemical Crystallizer"),
    CHEMICAL_DISSOLUTION_CHAMBER((FactoryType) null, "chemical_dissolution_chamber", "ME Chemical Dissolution Chamber"),
    CHEMICAL_INFUSER((FactoryType) null, "chemical_infuser", "ME Chemical Infuser"),
    CHEMICAL_OXIDIZER((FactoryType) null, "chemical_oxidizer", "ME Chemical Oxidizer"),
    CHEMICAL_WASHER((FactoryType) null, "chemical_washer", "ME Chemical Washer"),
    ROTARY_CONDENSENTRATOR((FactoryType) null, "rotary_condensentrator", "ME Rotary Condensentrator"),
    ELECTROLYTIC_SEPARATOR((FactoryType) null, "electrolytic_separator", "ME Electrolytic Separator"),
    DIGITAL_MINER((FactoryType) null, "digital_miner", "ME Digital Miner"),
    FORMULAIC_ASSEMBLICATOR((FactoryType) null, "formulaic_assemblicator", "ME Formulaic Assemblicator"),
    ELECTRIC_PUMP((FactoryType) null, "electric_pump", "ME Electric Pump"),
    FLUIDIC_PLENISHER((FactoryType) null, "fluidic_plenisher", "ME Fluidic Plenisher"),
    SOLAR_NEUTRON_ACTIVATOR((FactoryType) null, "solar_neutron_activator", "ME Solar Neutron Activator"),
    TELEPORTER((FactoryType) null, "teleporter", "ME Teleporter"),
    RESISTIVE_HEATER((FactoryType) null, "resistive_heater", "ME Resistive Heater"),
    SEISMIC_VIBRATOR((FactoryType) null, "seismic_vibrator", "ME Seismic Vibrator"),
    LOGISTICAL_SORTER((FactoryType) null, "logistical_sorter", "ME Logistical Sorter"),
    ISOTOPIC_CENTRIFUGE((FactoryType) null, "isotopic_centrifuge", "ME Isotopic Centrifuge"),
    NUTRITIONAL_LIQUIFIER((FactoryType) null, "nutritional_liquifier", "ME Nutritional Liquifier"),
    ANTIPROTONIC_NUCLEOSYNTHESIZER((FactoryType) null, "antiprotonic_nucleosynthesizer", "ME Antiprotonic Nucleosynthesizer"),
    PIGMENT_EXTRACTOR((FactoryType) null, "pigment_extractor", "ME Pigment Extractor"),
    PIGMENT_MIXER((FactoryType) null, "pigment_mixer", "ME Pigment Mixer"),
    PAINTING_MACHINE((FactoryType) null, "painting_machine", "ME Painting Machine"),
    DIMENSIONAL_STABILIZER((FactoryType) null, "dimensional_stabilizer", "ME Dimensional Stabilizer"),
    OREDICTIONIFICATOR((FactoryType) null, "oredictionificator", "ME Oredictionificator"),
    MODIFICATION_STATION((FactoryType) null, "modification_station", "ME Modification Station"),
    RECYCLER("recycler", "recycling", "ME Recycler"),
    PLANTING_STATION("planting_station", "planting", "ME Planting Station"),
    CNC_STAMPER("cnc_stamper", "stamping", "ME CNC Stamper"),
    CNC_LATHE("cnc_lathe", "lathing", "ME CNC Lathe"),
    CNC_ROLLING_MILL("cnc_rolling_mill", "rolling_mill", "ME CNC Rolling Mill"),
    REPLICATOR("replicator", "replicating", "ME Replicator"),
    CHEMICAL_REPLICATOR("chemical_replicator", "chemical_replicator", "ME Chemical Replicator"),
    FLUID_REPLICATOR("fluid_replicator", "fluid_replicator", "ME Fluid Replicator"),
    LARGE_ROTARY_CONDENSENTRATOR("large_rotary_condensentrator", "large_rotary_condensentrator", "ME Large Rotary Condensentrator"),
    LARGE_SOLAR_NEUTRON_ACTIVATOR("large_solar_neutron_activator", "large_solar_neutron_activator", "ME Large Solar Neutron Activator"),
    LARGE_ELECTROLYTIC_SEPARATOR("large_electrolytic_separator", "large_electrolytic_separator", "ME Large Electrolytic Separator"),
    LARGE_CHEMICAL_INFUSER("large_chemical_infuser", "large_chemical_infuser", "ME Large Chemical Infuser"),
    LARGE_ANTIPROTONIC_NUCLEOSYNTHESIZER("large_antiprotonic_nucleosynthesizer", "large_antiprotonic_nucleosynthesizer", "ME Large Antiprotonic Nucleosynthesizer"),
    BASIC_SMELTING_FACTORY(FactoryTier.BASIC, FactoryType.SMELTING),
    BASIC_ENRICHING_FACTORY(FactoryTier.BASIC, FactoryType.ENRICHING),
    BASIC_CRUSHING_FACTORY(FactoryTier.BASIC, FactoryType.CRUSHING),
    BASIC_COMPRESSING_FACTORY(FactoryTier.BASIC, FactoryType.COMPRESSING),
    BASIC_COMBINING_FACTORY(FactoryTier.BASIC, FactoryType.COMBINING),
    BASIC_PURIFYING_FACTORY(FactoryTier.BASIC, FactoryType.PURIFYING),
    BASIC_INJECTING_FACTORY(FactoryTier.BASIC, FactoryType.INJECTING),
    BASIC_INFUSING_FACTORY(FactoryTier.BASIC, FactoryType.INFUSING),
    BASIC_SAWING_FACTORY(FactoryTier.BASIC, FactoryType.SAWING),
    ADVANCED_SMELTING_FACTORY(FactoryTier.ADVANCED, FactoryType.SMELTING),
    ADVANCED_ENRICHING_FACTORY(FactoryTier.ADVANCED, FactoryType.ENRICHING),
    ADVANCED_CRUSHING_FACTORY(FactoryTier.ADVANCED, FactoryType.CRUSHING),
    ADVANCED_COMPRESSING_FACTORY(FactoryTier.ADVANCED, FactoryType.COMPRESSING),
    ADVANCED_COMBINING_FACTORY(FactoryTier.ADVANCED, FactoryType.COMBINING),
    ADVANCED_PURIFYING_FACTORY(FactoryTier.ADVANCED, FactoryType.PURIFYING),
    ADVANCED_INJECTING_FACTORY(FactoryTier.ADVANCED, FactoryType.INJECTING),
    ADVANCED_INFUSING_FACTORY(FactoryTier.ADVANCED, FactoryType.INFUSING),
    ADVANCED_SAWING_FACTORY(FactoryTier.ADVANCED, FactoryType.SAWING),
    ELITE_SMELTING_FACTORY(FactoryTier.ELITE, FactoryType.SMELTING),
    ELITE_ENRICHING_FACTORY(FactoryTier.ELITE, FactoryType.ENRICHING),
    ELITE_CRUSHING_FACTORY(FactoryTier.ELITE, FactoryType.CRUSHING),
    ELITE_COMPRESSING_FACTORY(FactoryTier.ELITE, FactoryType.COMPRESSING),
    ELITE_COMBINING_FACTORY(FactoryTier.ELITE, FactoryType.COMBINING),
    ELITE_PURIFYING_FACTORY(FactoryTier.ELITE, FactoryType.PURIFYING),
    ELITE_INJECTING_FACTORY(FactoryTier.ELITE, FactoryType.INJECTING),
    ELITE_INFUSING_FACTORY(FactoryTier.ELITE, FactoryType.INFUSING),
    ELITE_SAWING_FACTORY(FactoryTier.ELITE, FactoryType.SAWING),
    ULTIMATE_SMELTING_FACTORY(FactoryTier.ULTIMATE, FactoryType.SMELTING),
    ULTIMATE_ENRICHING_FACTORY(FactoryTier.ULTIMATE, FactoryType.ENRICHING),
    ULTIMATE_CRUSHING_FACTORY(FactoryTier.ULTIMATE, FactoryType.CRUSHING),
    ULTIMATE_COMPRESSING_FACTORY(FactoryTier.ULTIMATE, FactoryType.COMPRESSING),
    ULTIMATE_COMBINING_FACTORY(FactoryTier.ULTIMATE, FactoryType.COMBINING),
    ULTIMATE_PURIFYING_FACTORY(FactoryTier.ULTIMATE, FactoryType.PURIFYING),
    ULTIMATE_INJECTING_FACTORY(FactoryTier.ULTIMATE, FactoryType.INJECTING),
    ULTIMATE_INFUSING_FACTORY(FactoryTier.ULTIMATE, FactoryType.INFUSING),
    ULTIMATE_SAWING_FACTORY(FactoryTier.ULTIMATE, FactoryType.SAWING),
    BASIC_ALLOYING_FACTORY(FactoryTier.BASIC, "alloying", "Alloying", CompatMachineFamily.EMEK_FACTORY),
    ADVANCED_ALLOYING_FACTORY(FactoryTier.ADVANCED, "alloying", "Alloying", CompatMachineFamily.EMEK_FACTORY),
    ELITE_ALLOYING_FACTORY(FactoryTier.ELITE, "alloying", "Alloying", CompatMachineFamily.EMEK_FACTORY),
    ULTIMATE_ALLOYING_FACTORY(FactoryTier.ULTIMATE, "alloying", "Alloying", CompatMachineFamily.EMEK_FACTORY),
    OVERCLOCKED_SMELTING_FACTORY("overclocked", FactoryType.SMELTING, CompatMachineFamily.EMEK_FACTORY),
    OVERCLOCKED_ENRICHING_FACTORY("overclocked", FactoryType.ENRICHING, CompatMachineFamily.EMEK_FACTORY),
    OVERCLOCKED_CRUSHING_FACTORY("overclocked", FactoryType.CRUSHING, CompatMachineFamily.EMEK_FACTORY),
    OVERCLOCKED_COMPRESSING_FACTORY("overclocked", FactoryType.COMPRESSING, CompatMachineFamily.EMEK_FACTORY),
    OVERCLOCKED_COMBINING_FACTORY("overclocked", FactoryType.COMBINING, CompatMachineFamily.EMEK_FACTORY),
    OVERCLOCKED_PURIFYING_FACTORY("overclocked", FactoryType.PURIFYING, CompatMachineFamily.EMEK_FACTORY),
    OVERCLOCKED_INJECTING_FACTORY("overclocked", FactoryType.INJECTING, CompatMachineFamily.EMEK_FACTORY),
    OVERCLOCKED_INFUSING_FACTORY("overclocked", FactoryType.INFUSING, CompatMachineFamily.EMEK_FACTORY),
    OVERCLOCKED_SAWING_FACTORY("overclocked", FactoryType.SAWING, CompatMachineFamily.EMEK_FACTORY),
    QUANTUM_SMELTING_FACTORY("quantum", FactoryType.SMELTING, CompatMachineFamily.EMEK_FACTORY),
    QUANTUM_ENRICHING_FACTORY("quantum", FactoryType.ENRICHING, CompatMachineFamily.EMEK_FACTORY),
    QUANTUM_CRUSHING_FACTORY("quantum", FactoryType.CRUSHING, CompatMachineFamily.EMEK_FACTORY),
    QUANTUM_COMPRESSING_FACTORY("quantum", FactoryType.COMPRESSING, CompatMachineFamily.EMEK_FACTORY),
    QUANTUM_COMBINING_FACTORY("quantum", FactoryType.COMBINING, CompatMachineFamily.EMEK_FACTORY),
    QUANTUM_PURIFYING_FACTORY("quantum", FactoryType.PURIFYING, CompatMachineFamily.EMEK_FACTORY),
    QUANTUM_INJECTING_FACTORY("quantum", FactoryType.INJECTING, CompatMachineFamily.EMEK_FACTORY),
    QUANTUM_INFUSING_FACTORY("quantum", FactoryType.INFUSING, CompatMachineFamily.EMEK_FACTORY),
    QUANTUM_SAWING_FACTORY("quantum", FactoryType.SAWING, CompatMachineFamily.EMEK_FACTORY),
    DENSE_SMELTING_FACTORY("dense", FactoryType.SMELTING, CompatMachineFamily.EMEK_FACTORY),
    DENSE_ENRICHING_FACTORY("dense", FactoryType.ENRICHING, CompatMachineFamily.EMEK_FACTORY),
    DENSE_CRUSHING_FACTORY("dense", FactoryType.CRUSHING, CompatMachineFamily.EMEK_FACTORY),
    DENSE_COMPRESSING_FACTORY("dense", FactoryType.COMPRESSING, CompatMachineFamily.EMEK_FACTORY),
    DENSE_COMBINING_FACTORY("dense", FactoryType.COMBINING, CompatMachineFamily.EMEK_FACTORY),
    DENSE_PURIFYING_FACTORY("dense", FactoryType.PURIFYING, CompatMachineFamily.EMEK_FACTORY),
    DENSE_INJECTING_FACTORY("dense", FactoryType.INJECTING, CompatMachineFamily.EMEK_FACTORY),
    DENSE_INFUSING_FACTORY("dense", FactoryType.INFUSING, CompatMachineFamily.EMEK_FACTORY),
    DENSE_SAWING_FACTORY("dense", FactoryType.SAWING, CompatMachineFamily.EMEK_FACTORY),
    MULTIVERSAL_SMELTING_FACTORY("multiversal", FactoryType.SMELTING, CompatMachineFamily.EMEK_FACTORY),
    MULTIVERSAL_ENRICHING_FACTORY("multiversal", FactoryType.ENRICHING, CompatMachineFamily.EMEK_FACTORY),
    MULTIVERSAL_CRUSHING_FACTORY("multiversal", FactoryType.CRUSHING, CompatMachineFamily.EMEK_FACTORY),
    MULTIVERSAL_COMPRESSING_FACTORY("multiversal", FactoryType.COMPRESSING, CompatMachineFamily.EMEK_FACTORY),
    MULTIVERSAL_COMBINING_FACTORY("multiversal", FactoryType.COMBINING, CompatMachineFamily.EMEK_FACTORY),
    MULTIVERSAL_PURIFYING_FACTORY("multiversal", FactoryType.PURIFYING, CompatMachineFamily.EMEK_FACTORY),
    MULTIVERSAL_INJECTING_FACTORY("multiversal", FactoryType.INJECTING, CompatMachineFamily.EMEK_FACTORY),
    MULTIVERSAL_INFUSING_FACTORY("multiversal", FactoryType.INFUSING, CompatMachineFamily.EMEK_FACTORY),
    MULTIVERSAL_SAWING_FACTORY("multiversal", FactoryType.SAWING, CompatMachineFamily.EMEK_FACTORY),
    CREATIVE_SMELTING_FACTORY("creative", FactoryType.SMELTING, CompatMachineFamily.EMEK_FACTORY),
    CREATIVE_ENRICHING_FACTORY("creative", FactoryType.ENRICHING, CompatMachineFamily.EMEK_FACTORY),
    CREATIVE_CRUSHING_FACTORY("creative", FactoryType.CRUSHING, CompatMachineFamily.EMEK_FACTORY),
    CREATIVE_COMPRESSING_FACTORY("creative", FactoryType.COMPRESSING, CompatMachineFamily.EMEK_FACTORY),
    CREATIVE_COMBINING_FACTORY("creative", FactoryType.COMBINING, CompatMachineFamily.EMEK_FACTORY),
    CREATIVE_PURIFYING_FACTORY("creative", FactoryType.PURIFYING, CompatMachineFamily.EMEK_FACTORY),
    CREATIVE_INJECTING_FACTORY("creative", FactoryType.INJECTING, CompatMachineFamily.EMEK_FACTORY),
    CREATIVE_INFUSING_FACTORY("creative", FactoryType.INFUSING, CompatMachineFamily.EMEK_FACTORY),
    CREATIVE_SAWING_FACTORY("creative", FactoryType.SAWING, CompatMachineFamily.EMEK_FACTORY),
    OVERCLOCKED_ALLOYING_FACTORY("overclocked", "alloying", "Alloying", CompatMachineFamily.EMEK_FACTORY),
    QUANTUM_ALLOYING_FACTORY("quantum", "alloying", "Alloying", CompatMachineFamily.EMEK_FACTORY),
    DENSE_ALLOYING_FACTORY("dense", "alloying", "Alloying", CompatMachineFamily.EMEK_FACTORY),
    MULTIVERSAL_ALLOYING_FACTORY("multiversal", "alloying", "Alloying", CompatMachineFamily.EMEK_FACTORY),
    CREATIVE_ALLOYING_FACTORY("creative", "alloying", "Alloying", CompatMachineFamily.EMEK_FACTORY),
    ABSOLUTE_SMELTING_FACTORY("absolute", FactoryType.SMELTING),
    ABSOLUTE_ENRICHING_FACTORY("absolute", FactoryType.ENRICHING),
    ABSOLUTE_CRUSHING_FACTORY("absolute", FactoryType.CRUSHING),
    ABSOLUTE_COMPRESSING_FACTORY("absolute", FactoryType.COMPRESSING),
    ABSOLUTE_COMBINING_FACTORY("absolute", FactoryType.COMBINING),
    ABSOLUTE_PURIFYING_FACTORY("absolute", FactoryType.PURIFYING),
    ABSOLUTE_INJECTING_FACTORY("absolute", FactoryType.INJECTING),
    ABSOLUTE_INFUSING_FACTORY("absolute", FactoryType.INFUSING),
    ABSOLUTE_SAWING_FACTORY("absolute", FactoryType.SAWING),
    SUPREME_SMELTING_FACTORY("supreme", FactoryType.SMELTING),
    SUPREME_ENRICHING_FACTORY("supreme", FactoryType.ENRICHING),
    SUPREME_CRUSHING_FACTORY("supreme", FactoryType.CRUSHING),
    SUPREME_COMPRESSING_FACTORY("supreme", FactoryType.COMPRESSING),
    SUPREME_COMBINING_FACTORY("supreme", FactoryType.COMBINING),
    SUPREME_PURIFYING_FACTORY("supreme", FactoryType.PURIFYING),
    SUPREME_INJECTING_FACTORY("supreme", FactoryType.INJECTING),
    SUPREME_INFUSING_FACTORY("supreme", FactoryType.INFUSING),
    SUPREME_SAWING_FACTORY("supreme", FactoryType.SAWING),
    COSMIC_SMELTING_FACTORY("cosmic", FactoryType.SMELTING),
    COSMIC_ENRICHING_FACTORY("cosmic", FactoryType.ENRICHING),
    COSMIC_CRUSHING_FACTORY("cosmic", FactoryType.CRUSHING),
    COSMIC_COMPRESSING_FACTORY("cosmic", FactoryType.COMPRESSING),
    COSMIC_COMBINING_FACTORY("cosmic", FactoryType.COMBINING),
    COSMIC_PURIFYING_FACTORY("cosmic", FactoryType.PURIFYING),
    COSMIC_INJECTING_FACTORY("cosmic", FactoryType.INJECTING),
    COSMIC_INFUSING_FACTORY("cosmic", FactoryType.INFUSING),
    COSMIC_SAWING_FACTORY("cosmic", FactoryType.SAWING),
    INFINITE_SMELTING_FACTORY("infinite", FactoryType.SMELTING),
    INFINITE_ENRICHING_FACTORY("infinite", FactoryType.ENRICHING),
    INFINITE_CRUSHING_FACTORY("infinite", FactoryType.CRUSHING),
    INFINITE_COMPRESSING_FACTORY("infinite", FactoryType.COMPRESSING),
    INFINITE_COMBINING_FACTORY("infinite", FactoryType.COMBINING),
    INFINITE_PURIFYING_FACTORY("infinite", FactoryType.PURIFYING),
    INFINITE_INJECTING_FACTORY("infinite", FactoryType.INJECTING),
    INFINITE_INFUSING_FACTORY("infinite", FactoryType.INFUSING),
    INFINITE_SAWING_FACTORY("infinite", FactoryType.SAWING),
    ABSOLUTE_ALLOYING_FACTORY("absolute", "alloying", "Alloying", CompatMachineFamily.MEKE_FACTORY),
    SUPREME_ALLOYING_FACTORY("supreme", "alloying", "Alloying", CompatMachineFamily.MEKE_FACTORY),
    COSMIC_ALLOYING_FACTORY("cosmic", "alloying", "Alloying", CompatMachineFamily.MEKE_FACTORY),
    INFINITE_ALLOYING_FACTORY("infinite", "alloying", "Alloying", CompatMachineFamily.MEKE_FACTORY),
    ABSOLUTE_OVERCLOCKED_SMELTING_FACTORY("absolute_overclocked", FactoryType.SMELTING, CompatMachineFamily.EMEKE_FACTORY),
    ABSOLUTE_OVERCLOCKED_ENRICHING_FACTORY("absolute_overclocked", FactoryType.ENRICHING, CompatMachineFamily.EMEKE_FACTORY),
    ABSOLUTE_OVERCLOCKED_CRUSHING_FACTORY("absolute_overclocked", FactoryType.CRUSHING, CompatMachineFamily.EMEKE_FACTORY),
    ABSOLUTE_OVERCLOCKED_COMPRESSING_FACTORY("absolute_overclocked", FactoryType.COMPRESSING, CompatMachineFamily.EMEKE_FACTORY),
    ABSOLUTE_OVERCLOCKED_COMBINING_FACTORY("absolute_overclocked", FactoryType.COMBINING, CompatMachineFamily.EMEKE_FACTORY),
    ABSOLUTE_OVERCLOCKED_PURIFYING_FACTORY("absolute_overclocked", FactoryType.PURIFYING, CompatMachineFamily.EMEKE_FACTORY),
    ABSOLUTE_OVERCLOCKED_INJECTING_FACTORY("absolute_overclocked", FactoryType.INJECTING, CompatMachineFamily.EMEKE_FACTORY),
    ABSOLUTE_OVERCLOCKED_INFUSING_FACTORY("absolute_overclocked", FactoryType.INFUSING, CompatMachineFamily.EMEKE_FACTORY),
    ABSOLUTE_OVERCLOCKED_SAWING_FACTORY("absolute_overclocked", FactoryType.SAWING, CompatMachineFamily.EMEKE_FACTORY),
    SUPREME_QUANTUM_SMELTING_FACTORY("supreme_quantum", FactoryType.SMELTING, CompatMachineFamily.EMEKE_FACTORY),
    SUPREME_QUANTUM_ENRICHING_FACTORY("supreme_quantum", FactoryType.ENRICHING, CompatMachineFamily.EMEKE_FACTORY),
    SUPREME_QUANTUM_CRUSHING_FACTORY("supreme_quantum", FactoryType.CRUSHING, CompatMachineFamily.EMEKE_FACTORY),
    SUPREME_QUANTUM_COMPRESSING_FACTORY("supreme_quantum", FactoryType.COMPRESSING, CompatMachineFamily.EMEKE_FACTORY),
    SUPREME_QUANTUM_COMBINING_FACTORY("supreme_quantum", FactoryType.COMBINING, CompatMachineFamily.EMEKE_FACTORY),
    SUPREME_QUANTUM_PURIFYING_FACTORY("supreme_quantum", FactoryType.PURIFYING, CompatMachineFamily.EMEKE_FACTORY),
    SUPREME_QUANTUM_INJECTING_FACTORY("supreme_quantum", FactoryType.INJECTING, CompatMachineFamily.EMEKE_FACTORY),
    SUPREME_QUANTUM_INFUSING_FACTORY("supreme_quantum", FactoryType.INFUSING, CompatMachineFamily.EMEKE_FACTORY),
    SUPREME_QUANTUM_SAWING_FACTORY("supreme_quantum", FactoryType.SAWING, CompatMachineFamily.EMEKE_FACTORY),
    COSMIC_DENSE_SMELTING_FACTORY("cosmic_dense", FactoryType.SMELTING, CompatMachineFamily.EMEKE_FACTORY),
    COSMIC_DENSE_ENRICHING_FACTORY("cosmic_dense", FactoryType.ENRICHING, CompatMachineFamily.EMEKE_FACTORY),
    COSMIC_DENSE_CRUSHING_FACTORY("cosmic_dense", FactoryType.CRUSHING, CompatMachineFamily.EMEKE_FACTORY),
    COSMIC_DENSE_COMPRESSING_FACTORY("cosmic_dense", FactoryType.COMPRESSING, CompatMachineFamily.EMEKE_FACTORY),
    COSMIC_DENSE_COMBINING_FACTORY("cosmic_dense", FactoryType.COMBINING, CompatMachineFamily.EMEKE_FACTORY),
    COSMIC_DENSE_PURIFYING_FACTORY("cosmic_dense", FactoryType.PURIFYING, CompatMachineFamily.EMEKE_FACTORY),
    COSMIC_DENSE_INJECTING_FACTORY("cosmic_dense", FactoryType.INJECTING, CompatMachineFamily.EMEKE_FACTORY),
    COSMIC_DENSE_INFUSING_FACTORY("cosmic_dense", FactoryType.INFUSING, CompatMachineFamily.EMEKE_FACTORY),
    COSMIC_DENSE_SAWING_FACTORY("cosmic_dense", FactoryType.SAWING, CompatMachineFamily.EMEKE_FACTORY),
    INFINITE_MULTIVERSAL_SMELTING_FACTORY("infinite_multiversal", FactoryType.SMELTING, CompatMachineFamily.EMEKE_FACTORY),
    INFINITE_MULTIVERSAL_ENRICHING_FACTORY("infinite_multiversal", FactoryType.ENRICHING, CompatMachineFamily.EMEKE_FACTORY),
    INFINITE_MULTIVERSAL_CRUSHING_FACTORY("infinite_multiversal", FactoryType.CRUSHING, CompatMachineFamily.EMEKE_FACTORY),
    INFINITE_MULTIVERSAL_COMPRESSING_FACTORY("infinite_multiversal", FactoryType.COMPRESSING, CompatMachineFamily.EMEKE_FACTORY),
    INFINITE_MULTIVERSAL_COMBINING_FACTORY("infinite_multiversal", FactoryType.COMBINING, CompatMachineFamily.EMEKE_FACTORY),
    INFINITE_MULTIVERSAL_PURIFYING_FACTORY("infinite_multiversal", FactoryType.PURIFYING, CompatMachineFamily.EMEKE_FACTORY),
    INFINITE_MULTIVERSAL_INJECTING_FACTORY("infinite_multiversal", FactoryType.INJECTING, CompatMachineFamily.EMEKE_FACTORY),
    INFINITE_MULTIVERSAL_INFUSING_FACTORY("infinite_multiversal", FactoryType.INFUSING, CompatMachineFamily.EMEKE_FACTORY),
    INFINITE_MULTIVERSAL_SAWING_FACTORY("infinite_multiversal", FactoryType.SAWING, CompatMachineFamily.EMEKE_FACTORY),
    ABSOLUTE_OVERCLOCKED_ALLOYING_FACTORY("absolute_overclocked", "alloying", "Alloying", CompatMachineFamily.EMEKE_FACTORY),
    SUPREME_QUANTUM_ALLOYING_FACTORY("supreme_quantum", "alloying", "Alloying", CompatMachineFamily.EMEKE_FACTORY),
    COSMIC_DENSE_ALLOYING_FACTORY("cosmic_dense", "alloying", "Alloying", CompatMachineFamily.EMEKE_FACTORY),
    INFINITE_MULTIVERSAL_ALLOYING_FACTORY("infinite_multiversal", "alloying", "Alloying", CompatMachineFamily.EMEKE_FACTORY),
    ABSOLUTE_OVERCLOCKED_DISSOLVING_FACTORY("absolute_overclocked", "dissolving", "Dissolving", CompatMachineFamily.EMEKE_MEKAF_ADVANCED_FACTORY),
    ABSOLUTE_OVERCLOCKED_OXIDIZING_FACTORY("absolute_overclocked", "oxidizing", "Oxidizing", CompatMachineFamily.EMEKE_MEKAF_ADVANCED_FACTORY),
    ABSOLUTE_OVERCLOCKED_PIGMENT_EXTRACTING_FACTORY("absolute_overclocked", "pigment_extracting", "Pigment Extracting", CompatMachineFamily.EMEKE_MEKAF_ADVANCED_FACTORY),
    ABSOLUTE_OVERCLOCKED_PAINTING_FACTORY("absolute_overclocked", "painting", "Painting", CompatMachineFamily.EMEKE_MEKAF_ADVANCED_FACTORY),
    ABSOLUTE_OVERCLOCKED_PLANTING_FACTORY("absolute_overclocked", "planting", "Planting", CompatMachineFamily.EMEKE_MEKMM_FACTORY),
    ABSOLUTE_OVERCLOCKED_REPLICATING_FACTORY("absolute_overclocked", "replicating", "Replicating", CompatMachineFamily.EMEKE_MEKMM_FACTORY),
    ABSOLUTE_OVERCLOCKED_WASHING_FACTORY("absolute_overclocked", "washing", "Washing", CompatMachineFamily.EMEKE_MEKAF_ADVANCED_FACTORY),
    ABSOLUTE_OVERCLOCKED_CRYSTALLIZING_FACTORY("absolute_overclocked", "crystallizing", "Crystallizing", CompatMachineFamily.EMEKE_MEKAF_ADVANCED_FACTORY),
    ABSOLUTE_OVERCLOCKED_PRESSURISED_REACTING_FACTORY("absolute_overclocked", "pressurised_reacting", "Pressurised Reacting", CompatMachineFamily.EMEKE_MEKAF_ADVANCED_FACTORY),
    ABSOLUTE_OVERCLOCKED_CENTRIFUGING_FACTORY("absolute_overclocked", "centrifuging", "Centrifuging", CompatMachineFamily.EMEKE_MEKAF_ADVANCED_FACTORY),
    ABSOLUTE_OVERCLOCKED_LIQUIFYING_FACTORY("absolute_overclocked", "liquifying", "Liquifying", CompatMachineFamily.EMEKE_MEKAF_ADVANCED_FACTORY),
    SUPREME_QUANTUM_DISSOLVING_FACTORY("supreme_quantum", "dissolving", "Dissolving", CompatMachineFamily.EMEKE_MEKAF_ADVANCED_FACTORY),
    SUPREME_QUANTUM_OXIDIZING_FACTORY("supreme_quantum", "oxidizing", "Oxidizing", CompatMachineFamily.EMEKE_MEKAF_ADVANCED_FACTORY),
    SUPREME_QUANTUM_PIGMENT_EXTRACTING_FACTORY("supreme_quantum", "pigment_extracting", "Pigment Extracting", CompatMachineFamily.EMEKE_MEKAF_ADVANCED_FACTORY),
    SUPREME_QUANTUM_PAINTING_FACTORY("supreme_quantum", "painting", "Painting", CompatMachineFamily.EMEKE_MEKAF_ADVANCED_FACTORY),
    SUPREME_QUANTUM_PLANTING_FACTORY("supreme_quantum", "planting", "Planting", CompatMachineFamily.EMEKE_MEKMM_FACTORY),
    SUPREME_QUANTUM_REPLICATING_FACTORY("supreme_quantum", "replicating", "Replicating", CompatMachineFamily.EMEKE_MEKMM_FACTORY),
    SUPREME_QUANTUM_WASHING_FACTORY("supreme_quantum", "washing", "Washing", CompatMachineFamily.EMEKE_MEKAF_ADVANCED_FACTORY),
    SUPREME_QUANTUM_CRYSTALLIZING_FACTORY("supreme_quantum", "crystallizing", "Crystallizing", CompatMachineFamily.EMEKE_MEKAF_ADVANCED_FACTORY),
    SUPREME_QUANTUM_PRESSURISED_REACTING_FACTORY("supreme_quantum", "pressurised_reacting", "Pressurised Reacting", CompatMachineFamily.EMEKE_MEKAF_ADVANCED_FACTORY),
    SUPREME_QUANTUM_CENTRIFUGING_FACTORY("supreme_quantum", "centrifuging", "Centrifuging", CompatMachineFamily.EMEKE_MEKAF_ADVANCED_FACTORY),
    SUPREME_QUANTUM_LIQUIFYING_FACTORY("supreme_quantum", "liquifying", "Liquifying", CompatMachineFamily.EMEKE_MEKAF_ADVANCED_FACTORY),
    COSMIC_DENSE_DISSOLVING_FACTORY("cosmic_dense", "dissolving", "Dissolving", CompatMachineFamily.EMEKE_MEKAF_ADVANCED_FACTORY),
    COSMIC_DENSE_OXIDIZING_FACTORY("cosmic_dense", "oxidizing", "Oxidizing", CompatMachineFamily.EMEKE_MEKAF_ADVANCED_FACTORY),
    COSMIC_DENSE_PIGMENT_EXTRACTING_FACTORY("cosmic_dense", "pigment_extracting", "Pigment Extracting", CompatMachineFamily.EMEKE_MEKAF_ADVANCED_FACTORY),
    COSMIC_DENSE_PAINTING_FACTORY("cosmic_dense", "painting", "Painting", CompatMachineFamily.EMEKE_MEKAF_ADVANCED_FACTORY),
    COSMIC_DENSE_PLANTING_FACTORY("cosmic_dense", "planting", "Planting", CompatMachineFamily.EMEKE_MEKMM_FACTORY),
    COSMIC_DENSE_REPLICATING_FACTORY("cosmic_dense", "replicating", "Replicating", CompatMachineFamily.EMEKE_MEKMM_FACTORY),
    COSMIC_DENSE_WASHING_FACTORY("cosmic_dense", "washing", "Washing", CompatMachineFamily.EMEKE_MEKAF_ADVANCED_FACTORY),
    COSMIC_DENSE_CRYSTALLIZING_FACTORY("cosmic_dense", "crystallizing", "Crystallizing", CompatMachineFamily.EMEKE_MEKAF_ADVANCED_FACTORY),
    COSMIC_DENSE_PRESSURISED_REACTING_FACTORY("cosmic_dense", "pressurised_reacting", "Pressurised Reacting", CompatMachineFamily.EMEKE_MEKAF_ADVANCED_FACTORY),
    COSMIC_DENSE_CENTRIFUGING_FACTORY("cosmic_dense", "centrifuging", "Centrifuging", CompatMachineFamily.EMEKE_MEKAF_ADVANCED_FACTORY),
    COSMIC_DENSE_LIQUIFYING_FACTORY("cosmic_dense", "liquifying", "Liquifying", CompatMachineFamily.EMEKE_MEKAF_ADVANCED_FACTORY),
    INFINITE_MULTIVERSAL_DISSOLVING_FACTORY("infinite_multiversal", "dissolving", "Dissolving", CompatMachineFamily.EMEKE_MEKAF_ADVANCED_FACTORY),
    INFINITE_MULTIVERSAL_OXIDIZING_FACTORY("infinite_multiversal", "oxidizing", "Oxidizing", CompatMachineFamily.EMEKE_MEKAF_ADVANCED_FACTORY),
    INFINITE_MULTIVERSAL_PIGMENT_EXTRACTING_FACTORY("infinite_multiversal", "pigment_extracting", "Pigment Extracting", CompatMachineFamily.EMEKE_MEKAF_ADVANCED_FACTORY),
    INFINITE_MULTIVERSAL_PAINTING_FACTORY("infinite_multiversal", "painting", "Painting", CompatMachineFamily.EMEKE_MEKAF_ADVANCED_FACTORY),
    INFINITE_MULTIVERSAL_PLANTING_FACTORY("infinite_multiversal", "planting", "Planting", CompatMachineFamily.EMEKE_MEKMM_FACTORY),
    INFINITE_MULTIVERSAL_REPLICATING_FACTORY("infinite_multiversal", "replicating", "Replicating", CompatMachineFamily.EMEKE_MEKMM_FACTORY),
    INFINITE_MULTIVERSAL_WASHING_FACTORY("infinite_multiversal", "washing", "Washing", CompatMachineFamily.EMEKE_MEKAF_ADVANCED_FACTORY),
    INFINITE_MULTIVERSAL_CRYSTALLIZING_FACTORY("infinite_multiversal", "crystallizing", "Crystallizing", CompatMachineFamily.EMEKE_MEKAF_ADVANCED_FACTORY),
    INFINITE_MULTIVERSAL_PRESSURISED_REACTING_FACTORY("infinite_multiversal", "pressurised_reacting", "Pressurised Reacting", CompatMachineFamily.EMEKE_MEKAF_ADVANCED_FACTORY),
    INFINITE_MULTIVERSAL_CENTRIFUGING_FACTORY("infinite_multiversal", "centrifuging", "Centrifuging", CompatMachineFamily.EMEKE_MEKAF_ADVANCED_FACTORY),
    INFINITE_MULTIVERSAL_LIQUIFYING_FACTORY("infinite_multiversal", "liquifying", "Liquifying", CompatMachineFamily.EMEKE_MEKAF_ADVANCED_FACTORY),
    BASIC_RECYCLING_FACTORY(FactoryTier.BASIC, "recycling", "Recycling"),
    BASIC_PLANTING_FACTORY(FactoryTier.BASIC, "planting", "Planting"),
    BASIC_STAMPING_FACTORY(FactoryTier.BASIC, "stamping", "Stamping"),
    BASIC_LATHING_FACTORY(FactoryTier.BASIC, "lathing", "Lathing"),
    BASIC_ROLLING_MILL_FACTORY(FactoryTier.BASIC, "rolling_mill", "Rolling Mill"),
    BASIC_REPLICATING_FACTORY(FactoryTier.BASIC, "replicating", "Replicating"),
    ADVANCED_RECYCLING_FACTORY(FactoryTier.ADVANCED, "recycling", "Recycling"),
    ADVANCED_PLANTING_FACTORY(FactoryTier.ADVANCED, "planting", "Planting"),
    ADVANCED_STAMPING_FACTORY(FactoryTier.ADVANCED, "stamping", "Stamping"),
    ADVANCED_LATHING_FACTORY(FactoryTier.ADVANCED, "lathing", "Lathing"),
    ADVANCED_ROLLING_MILL_FACTORY(FactoryTier.ADVANCED, "rolling_mill", "Rolling Mill"),
    ADVANCED_REPLICATING_FACTORY(FactoryTier.ADVANCED, "replicating", "Replicating"),
    ELITE_RECYCLING_FACTORY(FactoryTier.ELITE, "recycling", "Recycling"),
    ELITE_PLANTING_FACTORY(FactoryTier.ELITE, "planting", "Planting"),
    ELITE_STAMPING_FACTORY(FactoryTier.ELITE, "stamping", "Stamping"),
    ELITE_LATHING_FACTORY(FactoryTier.ELITE, "lathing", "Lathing"),
    ELITE_ROLLING_MILL_FACTORY(FactoryTier.ELITE, "rolling_mill", "Rolling Mill"),
    ELITE_REPLICATING_FACTORY(FactoryTier.ELITE, "replicating", "Replicating"),
    ULTIMATE_RECYCLING_FACTORY(FactoryTier.ULTIMATE, "recycling", "Recycling"),
    ULTIMATE_PLANTING_FACTORY(FactoryTier.ULTIMATE, "planting", "Planting"),
    ULTIMATE_STAMPING_FACTORY(FactoryTier.ULTIMATE, "stamping", "Stamping"),
    ULTIMATE_LATHING_FACTORY(FactoryTier.ULTIMATE, "lathing", "Lathing"),
    ULTIMATE_ROLLING_MILL_FACTORY(FactoryTier.ULTIMATE, "rolling_mill", "Rolling Mill"),
    ULTIMATE_REPLICATING_FACTORY(FactoryTier.ULTIMATE, "replicating", "Replicating"),
    ABSOLUTE_RECYCLING_FACTORY("absolute", "recycling", "Recycling", CompatMachineFamily.MEKE_MEKMM_FACTORY),
    ABSOLUTE_PLANTING_FACTORY("absolute", "planting", "Planting", CompatMachineFamily.MEKE_MEKMM_FACTORY),
    ABSOLUTE_STAMPING_FACTORY("absolute", "stamping", "Stamping", CompatMachineFamily.MEKE_MEKMM_FACTORY),
    ABSOLUTE_LATHING_FACTORY("absolute", "lathing", "Lathing", CompatMachineFamily.MEKE_MEKMM_FACTORY),
    ABSOLUTE_ROLLING_MILL_FACTORY("absolute", "rolling_mill", "Rolling Mill", CompatMachineFamily.MEKE_MEKMM_FACTORY),
    ABSOLUTE_REPLICATING_FACTORY("absolute", "replicating", "Replicating", CompatMachineFamily.MEKE_MEKMM_FACTORY),
    SUPREME_RECYCLING_FACTORY("supreme", "recycling", "Recycling", CompatMachineFamily.MEKE_MEKMM_FACTORY),
    SUPREME_PLANTING_FACTORY("supreme", "planting", "Planting", CompatMachineFamily.MEKE_MEKMM_FACTORY),
    SUPREME_STAMPING_FACTORY("supreme", "stamping", "Stamping", CompatMachineFamily.MEKE_MEKMM_FACTORY),
    SUPREME_LATHING_FACTORY("supreme", "lathing", "Lathing", CompatMachineFamily.MEKE_MEKMM_FACTORY),
    SUPREME_ROLLING_MILL_FACTORY("supreme", "rolling_mill", "Rolling Mill", CompatMachineFamily.MEKE_MEKMM_FACTORY),
    SUPREME_REPLICATING_FACTORY("supreme", "replicating", "Replicating", CompatMachineFamily.MEKE_MEKMM_FACTORY),
    COSMIC_RECYCLING_FACTORY("cosmic", "recycling", "Recycling", CompatMachineFamily.MEKE_MEKMM_FACTORY),
    COSMIC_PLANTING_FACTORY("cosmic", "planting", "Planting", CompatMachineFamily.MEKE_MEKMM_FACTORY),
    COSMIC_STAMPING_FACTORY("cosmic", "stamping", "Stamping", CompatMachineFamily.MEKE_MEKMM_FACTORY),
    COSMIC_LATHING_FACTORY("cosmic", "lathing", "Lathing", CompatMachineFamily.MEKE_MEKMM_FACTORY),
    COSMIC_ROLLING_MILL_FACTORY("cosmic", "rolling_mill", "Rolling Mill", CompatMachineFamily.MEKE_MEKMM_FACTORY),
    COSMIC_REPLICATING_FACTORY("cosmic", "replicating", "Replicating", CompatMachineFamily.MEKE_MEKMM_FACTORY),
    INFINITE_RECYCLING_FACTORY("infinite", "recycling", "Recycling", CompatMachineFamily.MEKE_MEKMM_FACTORY),
    INFINITE_PLANTING_FACTORY("infinite", "planting", "Planting", CompatMachineFamily.MEKE_MEKMM_FACTORY),
    INFINITE_STAMPING_FACTORY("infinite", "stamping", "Stamping", CompatMachineFamily.MEKE_MEKMM_FACTORY),
    INFINITE_LATHING_FACTORY("infinite", "lathing", "Lathing", CompatMachineFamily.MEKE_MEKMM_FACTORY),
    INFINITE_ROLLING_MILL_FACTORY("infinite", "rolling_mill", "Rolling Mill", CompatMachineFamily.MEKE_MEKMM_FACTORY),
    INFINITE_REPLICATING_FACTORY("infinite", "replicating", "Replicating", CompatMachineFamily.MEKE_MEKMM_FACTORY),
    BASIC_OXIDIZING_FACTORY(FactoryTier.BASIC, "oxidizing", "Oxidizing", CompatMachineFamily.MEKMM_ADVANCED_FACTORY),
    BASIC_DISSOLVING_FACTORY(FactoryTier.BASIC, "dissolving", "Dissolving", CompatMachineFamily.MEKMM_ADVANCED_FACTORY),
    BASIC_WASHING_FACTORY(FactoryTier.BASIC, "washing", "Washing", CompatMachineFamily.MEKMM_ADVANCED_FACTORY),
    BASIC_CRYSTALLIZING_FACTORY(FactoryTier.BASIC, "crystallizing", "Crystallizing", CompatMachineFamily.MEKMM_ADVANCED_FACTORY),
    BASIC_PRESSURISED_REACTING_FACTORY(FactoryTier.BASIC, "pressurised_reacting", "Pressurised Reacting", CompatMachineFamily.MEKMM_ADVANCED_FACTORY),
    BASIC_CENTRIFUGING_FACTORY(FactoryTier.BASIC, "centrifuging", "Centrifuging", CompatMachineFamily.MEKMM_ADVANCED_FACTORY),
    BASIC_LIQUIFYING_FACTORY(FactoryTier.BASIC, "liquifying", "Liquifying", CompatMachineFamily.MEKMM_ADVANCED_FACTORY),
    BASIC_PIGMENT_EXTRACTING_FACTORY(FactoryTier.BASIC, "pigment_extracting", "Pigment Extracting", CompatMachineFamily.MEKMM_ADVANCED_FACTORY),
    BASIC_PAINTING_FACTORY(FactoryTier.BASIC, "painting", "Painting", CompatMachineFamily.MEKMM_ADVANCED_FACTORY),
    ADVANCED_OXIDIZING_FACTORY(FactoryTier.ADVANCED, "oxidizing", "Oxidizing", CompatMachineFamily.MEKMM_ADVANCED_FACTORY),
    ADVANCED_DISSOLVING_FACTORY(FactoryTier.ADVANCED, "dissolving", "Dissolving", CompatMachineFamily.MEKMM_ADVANCED_FACTORY),
    ADVANCED_WASHING_FACTORY(FactoryTier.ADVANCED, "washing", "Washing", CompatMachineFamily.MEKMM_ADVANCED_FACTORY),
    ADVANCED_CRYSTALLIZING_FACTORY(FactoryTier.ADVANCED, "crystallizing", "Crystallizing", CompatMachineFamily.MEKMM_ADVANCED_FACTORY),
    ADVANCED_PRESSURISED_REACTING_FACTORY(FactoryTier.ADVANCED, "pressurised_reacting", "Pressurised Reacting", CompatMachineFamily.MEKMM_ADVANCED_FACTORY),
    ADVANCED_CENTRIFUGING_FACTORY(FactoryTier.ADVANCED, "centrifuging", "Centrifuging", CompatMachineFamily.MEKMM_ADVANCED_FACTORY),
    ADVANCED_LIQUIFYING_FACTORY(FactoryTier.ADVANCED, "liquifying", "Liquifying", CompatMachineFamily.MEKMM_ADVANCED_FACTORY),
    ADVANCED_PIGMENT_EXTRACTING_FACTORY(FactoryTier.ADVANCED, "pigment_extracting", "Pigment Extracting", CompatMachineFamily.MEKMM_ADVANCED_FACTORY),
    ADVANCED_PAINTING_FACTORY(FactoryTier.ADVANCED, "painting", "Painting", CompatMachineFamily.MEKMM_ADVANCED_FACTORY),
    ELITE_OXIDIZING_FACTORY(FactoryTier.ELITE, "oxidizing", "Oxidizing", CompatMachineFamily.MEKMM_ADVANCED_FACTORY),
    ELITE_DISSOLVING_FACTORY(FactoryTier.ELITE, "dissolving", "Dissolving", CompatMachineFamily.MEKMM_ADVANCED_FACTORY),
    ELITE_WASHING_FACTORY(FactoryTier.ELITE, "washing", "Washing", CompatMachineFamily.MEKMM_ADVANCED_FACTORY),
    ELITE_CRYSTALLIZING_FACTORY(FactoryTier.ELITE, "crystallizing", "Crystallizing", CompatMachineFamily.MEKMM_ADVANCED_FACTORY),
    ELITE_PRESSURISED_REACTING_FACTORY(FactoryTier.ELITE, "pressurised_reacting", "Pressurised Reacting", CompatMachineFamily.MEKMM_ADVANCED_FACTORY),
    ELITE_CENTRIFUGING_FACTORY(FactoryTier.ELITE, "centrifuging", "Centrifuging", CompatMachineFamily.MEKMM_ADVANCED_FACTORY),
    ELITE_LIQUIFYING_FACTORY(FactoryTier.ELITE, "liquifying", "Liquifying", CompatMachineFamily.MEKMM_ADVANCED_FACTORY),
    ELITE_PIGMENT_EXTRACTING_FACTORY(FactoryTier.ELITE, "pigment_extracting", "Pigment Extracting", CompatMachineFamily.MEKMM_ADVANCED_FACTORY),
    ELITE_PAINTING_FACTORY(FactoryTier.ELITE, "painting", "Painting", CompatMachineFamily.MEKMM_ADVANCED_FACTORY),
    ULTIMATE_OXIDIZING_FACTORY(FactoryTier.ULTIMATE, "oxidizing", "Oxidizing", CompatMachineFamily.MEKMM_ADVANCED_FACTORY),
    ULTIMATE_DISSOLVING_FACTORY(FactoryTier.ULTIMATE, "dissolving", "Dissolving", CompatMachineFamily.MEKMM_ADVANCED_FACTORY),
    ULTIMATE_WASHING_FACTORY(FactoryTier.ULTIMATE, "washing", "Washing", CompatMachineFamily.MEKMM_ADVANCED_FACTORY),
    ULTIMATE_CRYSTALLIZING_FACTORY(FactoryTier.ULTIMATE, "crystallizing", "Crystallizing", CompatMachineFamily.MEKMM_ADVANCED_FACTORY),
    ULTIMATE_PRESSURISED_REACTING_FACTORY(FactoryTier.ULTIMATE, "pressurised_reacting", "Pressurised Reacting", CompatMachineFamily.MEKMM_ADVANCED_FACTORY),
    ULTIMATE_CENTRIFUGING_FACTORY(FactoryTier.ULTIMATE, "centrifuging", "Centrifuging", CompatMachineFamily.MEKMM_ADVANCED_FACTORY),
    ULTIMATE_LIQUIFYING_FACTORY(FactoryTier.ULTIMATE, "liquifying", "Liquifying", CompatMachineFamily.MEKMM_ADVANCED_FACTORY),
    ULTIMATE_PIGMENT_EXTRACTING_FACTORY(FactoryTier.ULTIMATE, "pigment_extracting", "Pigment Extracting", CompatMachineFamily.MEKMM_ADVANCED_FACTORY),
    ULTIMATE_PAINTING_FACTORY(FactoryTier.ULTIMATE, "painting", "Painting", CompatMachineFamily.MEKMM_ADVANCED_FACTORY),
    ABSOLUTE_OXIDIZING_FACTORY("absolute", "oxidizing", "Oxidizing", CompatMachineFamily.MEKE_MEKMM_ADVANCED_FACTORY),
    ABSOLUTE_DISSOLVING_FACTORY("absolute", "dissolving", "Dissolving", CompatMachineFamily.MEKE_MEKMM_ADVANCED_FACTORY),
    ABSOLUTE_WASHING_FACTORY("absolute", "washing", "Washing", CompatMachineFamily.MEKE_MEKMM_ADVANCED_FACTORY),
    ABSOLUTE_CRYSTALLIZING_FACTORY("absolute", "crystallizing", "Crystallizing", CompatMachineFamily.MEKE_MEKMM_ADVANCED_FACTORY),
    ABSOLUTE_PRESSURISED_REACTING_FACTORY("absolute", "pressurised_reacting", "Pressurised Reacting", CompatMachineFamily.MEKE_MEKMM_ADVANCED_FACTORY),
    ABSOLUTE_CENTRIFUGING_FACTORY("absolute", "centrifuging", "Centrifuging", CompatMachineFamily.MEKE_MEKMM_ADVANCED_FACTORY),
    ABSOLUTE_LIQUIFYING_FACTORY("absolute", "liquifying", "Liquifying", CompatMachineFamily.MEKE_MEKMM_ADVANCED_FACTORY),
    ABSOLUTE_PIGMENT_EXTRACTING_FACTORY("absolute", "pigment_extracting", "Pigment Extracting", CompatMachineFamily.MEKE_MEKMM_ADVANCED_FACTORY),
    ABSOLUTE_PAINTING_FACTORY("absolute", "painting", "Painting", CompatMachineFamily.MEKE_MEKMM_ADVANCED_FACTORY),
    SUPREME_OXIDIZING_FACTORY("supreme", "oxidizing", "Oxidizing", CompatMachineFamily.MEKE_MEKMM_ADVANCED_FACTORY),
    SUPREME_DISSOLVING_FACTORY("supreme", "dissolving", "Dissolving", CompatMachineFamily.MEKE_MEKMM_ADVANCED_FACTORY),
    SUPREME_WASHING_FACTORY("supreme", "washing", "Washing", CompatMachineFamily.MEKE_MEKMM_ADVANCED_FACTORY),
    SUPREME_CRYSTALLIZING_FACTORY("supreme", "crystallizing", "Crystallizing", CompatMachineFamily.MEKE_MEKMM_ADVANCED_FACTORY),
    SUPREME_PRESSURISED_REACTING_FACTORY("supreme", "pressurised_reacting", "Pressurised Reacting", CompatMachineFamily.MEKE_MEKMM_ADVANCED_FACTORY),
    SUPREME_CENTRIFUGING_FACTORY("supreme", "centrifuging", "Centrifuging", CompatMachineFamily.MEKE_MEKMM_ADVANCED_FACTORY),
    SUPREME_LIQUIFYING_FACTORY("supreme", "liquifying", "Liquifying", CompatMachineFamily.MEKE_MEKMM_ADVANCED_FACTORY),
    SUPREME_PIGMENT_EXTRACTING_FACTORY("supreme", "pigment_extracting", "Pigment Extracting", CompatMachineFamily.MEKE_MEKMM_ADVANCED_FACTORY),
    SUPREME_PAINTING_FACTORY("supreme", "painting", "Painting", CompatMachineFamily.MEKE_MEKMM_ADVANCED_FACTORY),
    COSMIC_OXIDIZING_FACTORY("cosmic", "oxidizing", "Oxidizing", CompatMachineFamily.MEKE_MEKMM_ADVANCED_FACTORY),
    COSMIC_DISSOLVING_FACTORY("cosmic", "dissolving", "Dissolving", CompatMachineFamily.MEKE_MEKMM_ADVANCED_FACTORY),
    COSMIC_WASHING_FACTORY("cosmic", "washing", "Washing", CompatMachineFamily.MEKE_MEKMM_ADVANCED_FACTORY),
    COSMIC_CRYSTALLIZING_FACTORY("cosmic", "crystallizing", "Crystallizing", CompatMachineFamily.MEKE_MEKMM_ADVANCED_FACTORY),
    COSMIC_PRESSURISED_REACTING_FACTORY("cosmic", "pressurised_reacting", "Pressurised Reacting", CompatMachineFamily.MEKE_MEKMM_ADVANCED_FACTORY),
    COSMIC_CENTRIFUGING_FACTORY("cosmic", "centrifuging", "Centrifuging", CompatMachineFamily.MEKE_MEKMM_ADVANCED_FACTORY),
    COSMIC_LIQUIFYING_FACTORY("cosmic", "liquifying", "Liquifying", CompatMachineFamily.MEKE_MEKMM_ADVANCED_FACTORY),
    COSMIC_PIGMENT_EXTRACTING_FACTORY("cosmic", "pigment_extracting", "Pigment Extracting", CompatMachineFamily.MEKE_MEKMM_ADVANCED_FACTORY),
    COSMIC_PAINTING_FACTORY("cosmic", "painting", "Painting", CompatMachineFamily.MEKE_MEKMM_ADVANCED_FACTORY),
    INFINITE_OXIDIZING_FACTORY("infinite", "oxidizing", "Oxidizing", CompatMachineFamily.MEKE_MEKMM_ADVANCED_FACTORY),
    INFINITE_DISSOLVING_FACTORY("infinite", "dissolving", "Dissolving", CompatMachineFamily.MEKE_MEKMM_ADVANCED_FACTORY),
    INFINITE_WASHING_FACTORY("infinite", "washing", "Washing", CompatMachineFamily.MEKE_MEKMM_ADVANCED_FACTORY),
    INFINITE_CRYSTALLIZING_FACTORY("infinite", "crystallizing", "Crystallizing", CompatMachineFamily.MEKE_MEKMM_ADVANCED_FACTORY),
    INFINITE_PRESSURISED_REACTING_FACTORY("infinite", "pressurised_reacting", "Pressurised Reacting", CompatMachineFamily.MEKE_MEKMM_ADVANCED_FACTORY),
    INFINITE_CENTRIFUGING_FACTORY("infinite", "centrifuging", "Centrifuging", CompatMachineFamily.MEKE_MEKMM_ADVANCED_FACTORY),
    INFINITE_LIQUIFYING_FACTORY("infinite", "liquifying", "Liquifying", CompatMachineFamily.MEKE_MEKMM_ADVANCED_FACTORY),
    INFINITE_PIGMENT_EXTRACTING_FACTORY("infinite", "pigment_extracting", "Pigment Extracting", CompatMachineFamily.MEKE_MEKMM_ADVANCED_FACTORY),
    INFINITE_PAINTING_FACTORY("infinite", "painting", "Painting", CompatMachineFamily.MEKE_MEKMM_ADVANCED_FACTORY);

    @Nullable
    private final FactoryType factoryType;
    @Nullable
    private final FactoryTier factoryTier;
    private final CompatMachineFamily family;
    @Nullable
    private final String tierId;
    private final String machineTypeId;
    private final String baseName;
    private final String englishName;

    MeMekanismMachine(@Nullable FactoryType factoryType, String baseName, String englishName) {
        this.factoryType = factoryType;
        this.factoryTier = null;
        this.family = CompatMachineFamily.MEKANISM_MACHINE;
        this.tierId = null;
        this.machineTypeId = factoryType == null ? baseName : factoryType.getRegistryNameComponent();
        this.baseName = baseName;
        this.englishName = englishName;
    }

    MeMekanismMachine(String nameOrTier, String machineTypeId, String englishName, CompatMachineFamily family) {
        this.factoryType = null;
        this.factoryTier = null;
        this.family = family;
        this.machineTypeId = machineTypeId;
        if (family.kind() == CompatMachineKind.MACHINE) {
            this.tierId = null;
            this.baseName = nameOrTier;
            this.englishName = englishName;
        } else {
            this.tierId = nameOrTier;
            this.baseName = nameOrTier + "_" + machineTypeId + "_factory";
            String tierName = family.provider() == CompatMod.EMEKE
                    ? displayTierName(nameOrTier) : capitalize(nameOrTier);
            this.englishName = "ME " + tierName + " " + englishName + " Factory";
        }
    }

    MeMekanismMachine(FactoryTier factoryTier, FactoryType factoryType) {
        this.factoryType = factoryType;
        this.factoryTier = factoryTier;
        this.family = CompatMachineFamily.MEKANISM_FACTORY;
        this.tierId = factoryTier.name().toLowerCase(Locale.ROOT);
        this.machineTypeId = factoryType.getRegistryNameComponent();
        this.baseName = factoryTier.name().toLowerCase(Locale.ROOT) + "_" + factoryType.getRegistryNameComponent() + "_factory";
        this.englishName = "ME " + capitalize(factoryTier.name()) + " " + factoryType.getRegistryNameComponentCapitalized() + " Factory";
    }

    MeMekanismMachine(String evolvedFactoryTierName, FactoryType factoryType, CompatMachineFamily family) {
        this.factoryType = factoryType;
        this.factoryTier = null;
        this.family = family;
        this.tierId = evolvedFactoryTierName;
        this.machineTypeId = factoryType.getRegistryNameComponent();
        this.baseName = evolvedFactoryTierName + "_" + factoryType.getRegistryNameComponent() + "_factory";
        String tierName = family.provider() == CompatMod.EMEKE
                ? displayTierName(evolvedFactoryTierName) : capitalize(evolvedFactoryTierName);
        this.englishName = "ME " + tierName + " " + factoryType.getRegistryNameComponentCapitalized() + " Factory";
    }

    MeMekanismMachine(FactoryTier factoryTier, String customFactoryTypeName, String factoryEnglishName,
            CompatMachineFamily family) {
        this.factoryType = null;
        this.factoryTier = factoryTier;
        this.family = family;
        this.tierId = factoryTier.name().toLowerCase(Locale.ROOT);
        this.machineTypeId = customFactoryTypeName;
        this.baseName = factoryTier.name().toLowerCase(Locale.ROOT) + "_" + customFactoryTypeName + "_factory";
        this.englishName = "ME " + capitalize(factoryTier.name()) + " " + factoryEnglishName + " Factory";
    }

    MeMekanismMachine(String extraFactoryTierName, FactoryType factoryType) {
        this.factoryType = factoryType;
        this.factoryTier = null;
        this.family = CompatMachineFamily.MEKE_FACTORY;
        this.tierId = extraFactoryTierName;
        this.machineTypeId = factoryType.getRegistryNameComponent();
        this.baseName = extraFactoryTierName + "_" + factoryType.getRegistryNameComponent() + "_factory";
        this.englishName = "ME " + capitalize(extraFactoryTierName) + " " + factoryType.getRegistryNameComponentCapitalized() + " Factory";
    }

    MeMekanismMachine(FactoryTier factoryTier, String moreMachineFactoryTypeName, String factoryEnglishName) {
        this.factoryType = null;
        this.factoryTier = factoryTier;
        this.family = CompatMachineFamily.MEKMM_FACTORY;
        this.tierId = factoryTier.name().toLowerCase(Locale.ROOT);
        this.machineTypeId = moreMachineFactoryTypeName;
        this.baseName = factoryTier.name().toLowerCase(Locale.ROOT) + "_" + moreMachineFactoryTypeName + "_factory";
        this.englishName = "ME " + capitalize(factoryTier.name()) + " " + factoryEnglishName + " Factory";
    }

    MeMekanismMachine(String baseName, String moreMachineBaseTypeName, String englishName) {
        this.factoryType = null;
        this.factoryTier = null;
        this.family = CompatMachineFamily.MEKMM_MACHINE;
        this.tierId = null;
        this.machineTypeId = moreMachineBaseTypeName;
        this.baseName = baseName;
        this.englishName = englishName;
    }

    @Nullable
    public FactoryType factoryType() {
        return factoryType;
    }

    @Nullable
    public FactoryTier factoryTier() {
        return factoryTier != null ? factoryTier : evolvedFactoryTier();
    }

    public boolean isFactory() {
        return this.family.kind() != CompatMachineKind.MACHINE;
    }

    public boolean isEvolvedMekanismFactory() {
        return this.family.provider() == CompatMod.EMEK && isFactory() && this.factoryTier == null;
    }

    public CompatMod provider() {
        return this.family.provider();
    }

    public CompatRegistrationRoute registrationRoute() {
        return this.family.route();
    }

    public CompatMachineKind machineKind() {
        return this.family.kind();
    }

    public CompatMachineFamily family() {
        return this.family;
    }

    public boolean isAvailable() {
        return CompatMachineCatalog.isAvailable(this);
    }

    public boolean hasMeVariant() {
        return switch (this) {
            case DIGITAL_MINER, ELECTRIC_PUMP, FLUIDIC_PLENISHER, TELEPORTER, RESISTIVE_HEATER,
                    LOGISTICAL_SORTER, DIMENSIONAL_STABILIZER, OREDICTIONIFICATOR, MODIFICATION_STATION -> false;
            default -> true;
        };
    }

    @Nullable
    public BaseTier baseTier() {
        FactoryTier tier = factoryTier();
        return tier == null ? null : tier.getBaseTier();
    }

    public String baseName() {
        return baseName;
    }

    public String registryName() {
        return "me_" + baseName;
    }

    public boolean isMekmmLargeMachine() {
        return switch (this) {
            case LARGE_ROTARY_CONDENSENTRATOR, LARGE_SOLAR_NEUTRON_ACTIVATOR,
                    LARGE_ELECTROLYTIC_SEPARATOR, LARGE_CHEMICAL_INFUSER,
                    LARGE_ANTIPROTONIC_NUCLEOSYNTHESIZER -> true;
            default -> false;
        };
    }

    @Nullable
    public String tierId() {
        return this.tierId;
    }

    public String machineTypeId() {
        return this.machineTypeId;
    }

    public String englishName() {
        return englishName;
    }

    public String translationKey() {
        return "block.mekenergistics." + registryName();
    }

    public String descriptionKey() {
        if (isFactory()) {
            return "description.mekanism.factory";
        }
        if (this.family.provider() == CompatMod.MEKANISM) {
            return "description.mekanism." + baseName;
        }
        return "description.mekenergistics.machine";
    }

    @Nullable
    public TextColor nameColor() {
        if (this.family.provider() == CompatMod.MEKE) {
            return OptionalCompatClasses.getMekanismExtrasTierColor(this.tierId);
        }
        if (this.family.provider() == CompatMod.EMEKE) {
            return OptionalCompatClasses.getEvolvedMekanismExtrasTierColor(this.tierId);
        }
        BaseTier tier = baseTier();
        return tier == null ? null : tier.getColor();
    }

    public String serializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public boolean hasSecondaryItemInput() {
        return this.factoryType == FactoryType.COMBINING || this == CNC_STAMPER || this == CHEMIXER;
    }

    public boolean hasChemicalInput() {
        return this.factoryType == FactoryType.COMPRESSING
                || this.factoryType == FactoryType.INFUSING
                || this.factoryType == FactoryType.INJECTING
                || this.factoryType == FactoryType.PURIFYING
                || this == CHEMIXER;
    }

    public boolean hasRecipeLogic() {
        return this.factoryType != null || switch (this.family.route()) {
            case MEKMM_MACHINE, MEKMM_FACTORY, MEKMM_ADVANCED_FACTORY,
                    MEKE_MEKMM_FACTORY, MEKE_MEKMM_ADVANCED_FACTORY -> true;
            default -> false;
        };
    }

    public boolean hasAdvancedChemicalInput() {
        return this.factoryType == FactoryType.COMPRESSING
                || this.factoryType == FactoryType.INJECTING
                || this.factoryType == FactoryType.PURIFYING;
    }

    public boolean hasSecondaryOutput() {
        return this.factoryType == FactoryType.SAWING;
    }

    public SlotLayout slotLayout() {
        if (hasSecondaryItemInput()) {
            return SlotLayout.DOUBLE_ITEM;
        }
        if (this == CNC_STAMPER) {
            return SlotLayout.DOUBLE_ITEM;
        }
        if (hasChemicalInput()) {
            return SlotLayout.ITEM_CHEMICAL;
        }
        if (hasSecondaryOutput()) {
            return SlotLayout.SAWING;
        }
        return SlotLayout.SINGLE_ITEM;
    }

    public LongSupplier energyUsage() {
        if (isFactory()) {
            return () -> 50L;
        }
        return switch (this) {
            case ENRICHMENT_CHAMBER -> MekanismConfig.usage.enrichmentChamber;
            case CRUSHER -> MekanismConfig.usage.crusher;
            case ENERGIZED_SMELTER -> MekanismConfig.usage.energizedSmelter;
            case PRECISION_SAWMILL -> MekanismConfig.usage.precisionSawmill;
            case OSMIUM_COMPRESSOR -> MekanismConfig.usage.osmiumCompressor;
            case COMBINER -> MekanismConfig.usage.combiner;
            case METALLURGIC_INFUSER -> MekanismConfig.usage.metallurgicInfuser;
            case PURIFICATION_CHAMBER -> MekanismConfig.usage.purificationChamber;
            case CHEMICAL_INJECTION_CHAMBER -> MekanismConfig.usage.chemicalInjectionChamber;
            default -> () -> 50L;
        };
    }

    public LongSupplier energyStorage() {
        if (isFactory()) {
            return () -> 2_000_000L;
        }
        return switch (this) {
            case ENRICHMENT_CHAMBER -> MekanismConfig.storage.enrichmentChamber;
            case CRUSHER -> MekanismConfig.storage.crusher;
            case ENERGIZED_SMELTER -> MekanismConfig.storage.energizedSmelter;
            case PRECISION_SAWMILL -> MekanismConfig.storage.precisionSawmill;
            case OSMIUM_COMPRESSOR -> MekanismConfig.storage.osmiumCompressor;
            case COMBINER -> MekanismConfig.storage.combiner;
            case METALLURGIC_INFUSER -> MekanismConfig.storage.metallurgicInfuser;
            case PURIFICATION_CHAMBER -> MekanismConfig.storage.purificationChamber;
            case CHEMICAL_INJECTION_CHAMBER -> MekanismConfig.storage.chemicalInjectionChamber;
            default -> () -> 2_000_000L;
        };
    }

    @Nullable
    public MeMekanismMachine getBasicFactory() {
        return CompatFactoryTierGraph.basicFactory(this);
    }

    @Nullable
    public MeMekanismMachine getNextFactory() {
        return CompatFactoryTierGraph.nextFactory(this);
    }

    @Nullable
    public static MeMekanismMachine getBaseMachine(FactoryType type) {
        return type == null ? null : CompatFactoryTierGraph.findBaseMachine(
                CompatMod.MEKANISM, type.getRegistryNameComponent());
    }

    @Nullable
    public static MeMekanismMachine getFactory(FactoryTier tier, FactoryType type) {
        return tier == null || type == null ? null : CompatFactoryTierGraph.findFactory(
                CompatMod.MEKANISM, tier.name().toLowerCase(Locale.ROOT), type.getRegistryNameComponent());
    }

    @Nullable
    public static MeMekanismMachine getEvolvedFactory(String tierName, FactoryType type) {
        return type == null ? null : getEvolvedFactory(tierName, type.getRegistryNameComponent());
    }

    @Nullable
    public static MeMekanismMachine getEvolvedFactory(String tierName, String typeName) {
        return CompatFactoryTierGraph.findFactory(CompatMod.EMEK, tierName, typeName);
    }

    @Nullable
    public static MeMekanismMachine getFactory(FactoryTier tier, String typeName) {
        if (tier == null || typeName == null) {
            return null;
        }
        String tierName = tier.name().toLowerCase(Locale.ROOT);
        MeMekanismMachine core = CompatFactoryTierGraph.findFactory(CompatMod.MEKANISM, tierName, typeName);
        return core != null ? core : CompatFactoryTierGraph.findFactory(CompatMod.EMEK, tierName, typeName);
    }

    @Nullable
    public static MeMekanismMachine getEvolvedMekanismExtrasFactory(String tierName, FactoryType type) {
        return type == null ? null : getEvolvedMekanismExtrasFactory(tierName, type.getRegistryNameComponent());
    }

    @Nullable
    public static MeMekanismMachine getEvolvedMekanismExtrasFactory(String tierName, String typeName) {
        return CompatFactoryTierGraph.findFactory(CompatMod.EMEKE, tierName, typeName);
    }

    @Nullable
    public static MeMekanismMachine getMoreMachineFactory(FactoryTier tier, String typeName) {
        return tier == null ? null : CompatFactoryTierGraph.findFactory(
                CompatRegistrationRoute.MEKMM_FACTORY, tier.name().toLowerCase(Locale.ROOT), typeName);
    }

    @Nullable
    public static MeMekanismMachine getExtraMoreMachineFactory(String tierName, String typeName) {
        return CompatFactoryTierGraph.findFactory(
                CompatRegistrationRoute.MEKE_MEKMM_FACTORY, tierName, typeName);
    }

    @Nullable
    public static MeMekanismMachine getMoreMachineAdvancedFactory(FactoryTier tier, String typeName) {
        return tier == null ? null : CompatFactoryTierGraph.findFactory(
                CompatRegistrationRoute.MEKMM_ADVANCED_FACTORY, tier.name().toLowerCase(Locale.ROOT), typeName);
    }

    @Nullable
    public static MeMekanismMachine getExtraMoreMachineAdvancedFactory(String tierName, String typeName) {
        return CompatFactoryTierGraph.findFactory(
                CompatRegistrationRoute.MEKE_MEKMM_ADVANCED_FACTORY, tierName, typeName);
    }

    @Nullable
    public static MeMekanismMachine getExtraFactory(String tierName, FactoryType type) {
        return type == null ? null : getExtraFactory(tierName, type.getRegistryNameComponent());
    }

    @Nullable
    public static MeMekanismMachine getExtraFactory(String tierName, String typeName) {
        return CompatFactoryTierGraph.findFactory(CompatRegistrationRoute.MEKE_FACTORY, tierName, typeName);
    }

    @Nullable
    public static MeMekanismMachine getByRegistryName(String registryName) {
        return CompatFactoryTierGraph.findByRegistryName(registryName);
    }

    @Nullable
    private FactoryTier evolvedFactoryTier() {
        return isEvolvedMekanismFactory() ? OptionalCompatClasses.getEvolvedFactoryTier(this.tierId) : null;
    }

    private static String capitalize(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        return lower.substring(0, 1).toUpperCase(Locale.ROOT) + lower.substring(1);
    }

    private static String displayTierName(String name) {
        String[] parts = name.split("_");
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            if (!result.isEmpty()) {
                result.append(' ');
            }
            result.append(capitalize(part));
        }
        return result.toString();
    }

    public enum SlotLayout {
        SINGLE_ITEM,
        ITEM_CHEMICAL,
        DOUBLE_ITEM,
        SAWING
    }
}
