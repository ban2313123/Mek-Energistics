package com.beipuo.mekenergistics.compat.catalog;

import com.beipuo.mekenergistics.common.machine.MeMekanismMachine;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public record CompatMachineSpec(
        CompatMod provider,
        MeMekanismMachine machine,
        ResourceLocation sourceBlockId,
        ResourceLocation meBlockId,
        CompatMachineKind kind,
        @Nullable String tierId,
        String machineTypeId,
        CompatSideConfigProfile sideConfigProfile,
        CompatRegistrationRoute route,
        Set<CompatRequirement> requirements) {

    public CompatMachineSpec {
        requirements = Set.copyOf(requirements);
    }
}
