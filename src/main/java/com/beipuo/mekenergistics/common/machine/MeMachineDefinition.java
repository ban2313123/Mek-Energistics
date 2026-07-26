package com.beipuo.mekenergistics.common.machine;

import com.beipuo.mekenergistics.compat.catalog.CompatMachineFamily;
import com.beipuo.mekenergistics.compat.catalog.CompatMachineKind;
import com.beipuo.mekenergistics.compat.catalog.CompatMod;
import java.util.Locale;
import mekanism.common.content.blocktype.FactoryType;
import mekanism.common.tier.FactoryTier;
import org.jetbrains.annotations.Nullable;

record MeMachineDefinition(
        @Nullable FactoryType factoryType,
        @Nullable FactoryTier factoryTier,
        CompatMachineFamily family,
        @Nullable String tierId,
        String machineTypeId,
        String baseName,
        String englishName) {

    static MeMachineDefinition mekanismMachine(
            @Nullable FactoryType factoryType, String baseName, String englishName) {
        return new MeMachineDefinition(
                factoryType,
                null,
                CompatMachineFamily.MEKANISM_MACHINE,
                null,
                factoryType == null ? baseName : factoryType.getRegistryNameComponent(),
                baseName,
                englishName);
    }

    static MeMachineDefinition familyEntry(
            String nameOrTier, String machineTypeId, String englishName, CompatMachineFamily family) {
        if (family.kind() == CompatMachineKind.MACHINE) {
            return new MeMachineDefinition(
                    null, null, family, null, machineTypeId, nameOrTier, englishName);
        }
        return factory(null, nameOrTier, machineTypeId, englishName, family);
    }

    static MeMachineDefinition mekanismFactory(FactoryTier factoryTier, FactoryType factoryType) {
        String tierId = normalizedTier(factoryTier);
        String machineTypeId = factoryType.getRegistryNameComponent();
        return new MeMachineDefinition(
                factoryType,
                factoryTier,
                CompatMachineFamily.MEKANISM_FACTORY,
                tierId,
                machineTypeId,
                factoryBaseName(tierId, machineTypeId),
                factoryEnglishName(tierId, factoryType.getRegistryNameComponentCapitalized(), false));
    }

    static MeMachineDefinition factory(
            String tierId, FactoryType factoryType, CompatMachineFamily family) {
        return new MeMachineDefinition(
                factoryType,
                null,
                family,
                tierId,
                factoryType.getRegistryNameComponent(),
                factoryBaseName(tierId, factoryType.getRegistryNameComponent()),
                factoryEnglishName(
                        tierId,
                        factoryType.getRegistryNameComponentCapitalized(),
                        family.provider() == CompatMod.EMEKE));
    }

    static MeMachineDefinition factory(
            FactoryTier factoryTier, String machineTypeId, String typeEnglishName,
            CompatMachineFamily family) {
        String tierId = normalizedTier(factoryTier);
        return factory(factoryTier, tierId, machineTypeId, typeEnglishName, family);
    }

    static MeMachineDefinition factory(
            @Nullable FactoryTier factoryTier, String tierId, String machineTypeId,
            String typeEnglishName, CompatMachineFamily family) {
        return new MeMachineDefinition(
                null,
                factoryTier,
                family,
                tierId,
                machineTypeId,
                factoryBaseName(tierId, machineTypeId),
                factoryEnglishName(tierId, typeEnglishName, family.provider() == CompatMod.EMEKE));
    }

    static MeMachineDefinition mekmmMachine(
            String baseName, String machineTypeId, String englishName) {
        return new MeMachineDefinition(
                null,
                null,
                CompatMachineFamily.MEKMM_MACHINE,
                null,
                machineTypeId,
                baseName,
                englishName);
    }

    private static String normalizedTier(FactoryTier factoryTier) {
        return factoryTier.name().toLowerCase(Locale.ROOT);
    }

    private static String factoryBaseName(String tierId, String machineTypeId) {
        return tierId + "_" + machineTypeId + "_factory";
    }

    private static String factoryEnglishName(
            String tierId, String typeEnglishName, boolean displayCompoundTier) {
        String tierName = displayCompoundTier ? displayName(tierId) : capitalize(tierId);
        return "ME " + tierName + " " + typeEnglishName + " Factory";
    }

    private static String displayName(String name) {
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

    private static String capitalize(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        return lower.substring(0, 1).toUpperCase(Locale.ROOT) + lower.substring(1);
    }
}
