package com.beipuo.mekenergistics.item;

import com.beipuo.mekenergistics.common.machine.MeMekanismMachine;
import com.beipuo.mekenergistics.compat.catalog.CompatMachineCatalog;
import com.beipuo.mekenergistics.compat.provider.CompatMachineProvider;
import com.beipuo.mekenergistics.compat.provider.CompatMachineProviders;
import com.beipuo.mekenergistics.registry.ModBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

final class MeInstallerTargetResolver {
    private MeInstallerTargetResolver() {
    }

    @Nullable
    static MeMekanismMachine resolve(BlockState state) {
        if (ModBlocks.getMachine(state.getBlock()) != null) {
            return null;
        }
        MeMekanismMachine directTarget = getTargetByRegistryName(state);
        if (directTarget != null) {
            return directTarget;
        }
        for (CompatMachineProvider provider : CompatMachineProviders.available().toList()) {
            MeMekanismMachine target = provider.resolveOriginalMachine(state);
            if (target != null && CompatMachineCatalog.isAvailable(target)) {
                return target;
            }
        }
        return null;
    }

    @Nullable
    private static MeMekanismMachine getTargetByRegistryName(BlockState state) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if ("mekenergistics".equals(id.getNamespace())) {
            return null;
        }
        return CompatMachineCatalog.findBySourceBlockId(id)
                .filter(spec -> CompatMachineCatalog.isAvailable(spec.machine()))
                .map(spec -> spec.machine())
                .orElse(null);
    }
}
