package com.beipuo.mekenergistics.compat.catalog;

import java.util.Objects;

public record FactoryTypeKey(CompatRegistrationRoute route, String machineTypeId) {
    public FactoryTypeKey {
        Objects.requireNonNull(route, "route");
        Objects.requireNonNull(machineTypeId, "machineTypeId");
        if (machineTypeId.isBlank()) {
            throw new IllegalArgumentException("machineTypeId must not be blank");
        }
    }

    public static FactoryTypeKey of(CompatMachineSpec spec) {
        Objects.requireNonNull(spec, "spec");
        return new FactoryTypeKey(spec.route(), spec.machineTypeId());
    }
}
