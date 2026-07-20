package com.beipuo.mekenergistics.compat.catalog;

public enum CompatMachineFamily {
    MEKANISM_MACHINE,
    MEKANISM_FACTORY,
    EMEK_MACHINE,
    EMEK_FACTORY,
    MEKE_FACTORY,
    MEKMM_MACHINE,
    MEKMM_FACTORY,
    MEKMM_ADVANCED_FACTORY,
    MEKE_MEKMM_FACTORY,
    MEKE_MEKMM_ADVANCED_FACTORY,
    EMEKE_FACTORY,
    EMEKE_MEKAF_ADVANCED_FACTORY,
    EMEKE_MEKMM_FACTORY;

    public static CompatMachineFamily resolve(CompatRegistrationRoute route, String machineTypeId) {
        return switch (route) {
            case MEKANISM_MACHINE -> MEKANISM_MACHINE;
            case MEKANISM_FACTORY -> MEKANISM_FACTORY;
            case EMEK_MACHINE -> EMEK_MACHINE;
            case EMEK_FACTORY -> EMEK_FACTORY;
            case MEKE_FACTORY -> MEKE_FACTORY;
            case MEKMM_MACHINE -> MEKMM_MACHINE;
            case MEKMM_FACTORY -> MEKMM_FACTORY;
            case MEKMM_ADVANCED_FACTORY -> MEKMM_ADVANCED_FACTORY;
            case MEKE_MEKMM_FACTORY -> MEKE_MEKMM_FACTORY;
            case MEKE_MEKMM_ADVANCED_FACTORY -> MEKE_MEKMM_ADVANCED_FACTORY;
            case EMEKE_FACTORY -> EMEKE_FACTORY;
            case EMEKE_ADVANCED_FACTORY -> switch (machineTypeId) {
                case "planting", "replicating" -> EMEKE_MEKMM_FACTORY;
                default -> EMEKE_MEKAF_ADVANCED_FACTORY;
            };
        };
    }
}
