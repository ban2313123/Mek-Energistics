package com.beipuo.mekenergistics.compat.catalog;

public enum CompatMachineFamily {
    MEKANISM_MACHINE(CompatMod.MEKANISM, CompatRegistrationRoute.MEKANISM_MACHINE, CompatMachineKind.MACHINE),
    MEKANISM_FACTORY(CompatMod.MEKANISM, CompatRegistrationRoute.MEKANISM_FACTORY, CompatMachineKind.FACTORY),
    EMEK_MACHINE(CompatMod.EMEK, CompatRegistrationRoute.EMEK_MACHINE, CompatMachineKind.MACHINE),
    EMEK_FACTORY(CompatMod.EMEK, CompatRegistrationRoute.EMEK_FACTORY, CompatMachineKind.FACTORY),
    MEKE_FACTORY(CompatMod.MEKE, CompatRegistrationRoute.MEKE_FACTORY, CompatMachineKind.FACTORY),
    MEKMM_MACHINE(CompatMod.MEKMM, CompatRegistrationRoute.MEKMM_MACHINE, CompatMachineKind.MACHINE),
    MEKMM_FACTORY(CompatMod.MEKMM, CompatRegistrationRoute.MEKMM_FACTORY, CompatMachineKind.FACTORY),
    MEKMM_ADVANCED_FACTORY(CompatMod.MEKMM, CompatRegistrationRoute.MEKMM_ADVANCED_FACTORY,
            CompatMachineKind.ADVANCED_FACTORY),
    MEKE_MEKMM_FACTORY(CompatMod.MEKE, CompatRegistrationRoute.MEKE_MEKMM_FACTORY, CompatMachineKind.FACTORY),
    MEKE_MEKMM_ADVANCED_FACTORY(CompatMod.MEKE, CompatRegistrationRoute.MEKE_MEKMM_ADVANCED_FACTORY,
            CompatMachineKind.ADVANCED_FACTORY),
    EMEKE_FACTORY(CompatMod.EMEKE, CompatRegistrationRoute.EMEKE_FACTORY, CompatMachineKind.FACTORY),
    EMEKE_MEKAF_ADVANCED_FACTORY(CompatMod.EMEKE, CompatRegistrationRoute.EMEKE_ADVANCED_FACTORY,
            CompatMachineKind.ADVANCED_FACTORY),
    EMEKE_MEKMM_FACTORY(CompatMod.EMEKE, CompatRegistrationRoute.EMEKE_ADVANCED_FACTORY,
            CompatMachineKind.ADVANCED_FACTORY);

    private final CompatMod provider;
    private final CompatRegistrationRoute route;
    private final CompatMachineKind kind;

    CompatMachineFamily(CompatMod provider, CompatRegistrationRoute route, CompatMachineKind kind) {
        this.provider = provider;
        this.route = route;
        this.kind = kind;
    }

    public CompatMod provider() {
        return this.provider;
    }

    public CompatRegistrationRoute route() {
        return this.route;
    }

    public CompatMachineKind kind() {
        return this.kind;
    }
}
