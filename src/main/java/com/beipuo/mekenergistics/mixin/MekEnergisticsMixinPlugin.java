package com.beipuo.mekenergistics.mixin;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.neoforged.fml.loading.LoadingModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.service.MixinService;

public class MekEnergisticsMixinPlugin implements IMixinConfigPlugin {
    /**
     * Gate for an optional mixin. {@code modId} is the mod that must be loaded; {@code targetClass}
     * is the binary name of the class the mixin actually targets, or null when the mod being loaded
     * already implies the target exists.
     *
     * <p>The two are not interchangeable. Some optional mods ship their mixin targets in a
     * conditional sub-module, so mod presence does not guarantee the class is there. The
     * {@code Large*} accessors target {@code com.jerry.meklm.*}, a sub-module of {@code mekmm} that
     * {@link com.beipuo.mekenergistics.compat.OptionalCompatClasses#hasMekmmLargeMachines()} probes
     * for at registration time for exactly this reason; gating them on the mod id alone left the
     * mixin enabled against a class that need not be present.
     */
    private record Gate(String modId, String targetClass) {
        static Gate mod(String modId) {
            return new Gate(modId, null);
        }

        static Gate target(String modId, String targetClass) {
            return new Gate(modId, targetClass);
        }
    }

    private static final String MEKLM_MACHINE = "com.jerry.meklm.common.tile.machine.";
    private static final String EMEXTRAS_MEKAF_FACTORY =
            "io.github.masyumero.emextras.common.integration.mekaf.tile.factory.";

    private static final Map<String, Gate> OPTIONAL_MIXINS = Map.ofEntries(
            Map.entry(".TileEntityAlloyerAccessor", Gate.mod("evolvedmekanism")),
            Map.entry(".TileEntitySolidifierAccessor", Gate.mod("evolvedmekanism")),
            Map.entry(".TileEntityMelterAccessor", Gate.mod("evolvedmekanism")),
            Map.entry(".TileEntityChemixerAccessor", Gate.mod("evolvedmekanism")),
            Map.entry(".TileEntityEMExtraDissolvingFactoryAccessor",
                    Gate.target("emextras", EMEXTRAS_MEKAF_FACTORY + "TileEntityEMExtraDissolvingFactory")),
            Map.entry(".TileEntityChemicalReplicatorAccessor", Gate.mod("mekmm")),
            Map.entry(".TileEntityFluidReplicatorAccessor", Gate.mod("mekmm")),
            Map.entry(".TileEntityLargeRotaryCondensentratorAccessor",
                    Gate.target("mekmm", MEKLM_MACHINE + "TileEntityLargeRotaryCondensentrator")),
            Map.entry(".TileEntityLargeChemicalInfuserAccessor",
                    Gate.target("mekmm", MEKLM_MACHINE + "TileEntityLargeChemicalInfuser")),
            Map.entry(".TileEntityLargeAntiprotonicNucleosynthesizerAccessor",
                    Gate.target("mekmm", MEKLM_MACHINE + "TileEntityLargeAntiprotonicNucleosynthesizer")),
            Map.entry(".extendedae.ContainerRenamerMixin", Gate.mod("extendedae")),
            Map.entry(".dataenergistics.PatternProviderSyncHelperMixin", Gate.mod("data_energistics")),
            Map.entry(".dataenergistics.PatternProviderNameHelperMixin", Gate.mod("data_energistics")));

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        for (var entry : OPTIONAL_MIXINS.entrySet()) {
            if (mixinClassName.endsWith(entry.getKey())) {
                Gate gate = entry.getValue();
                if (!isModLoaded(gate.modId())) {
                    return false;
                }
                return gate.targetClass() == null || isClassPresent(gate.targetClass());
            }
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    private static boolean isModLoaded(String modId) {
        return LoadingModList.get() != null && LoadingModList.get().getModFileById(modId) != null;
    }

    /**
     * Probes for a class without loading it. Uses the Mixin service rather than a {@link ClassLoader}
     * because this runs during mixin bootstrap, before the game class loader is usable.
     */
    private static boolean isClassPresent(String binaryName) {
        String resource = binaryName.replace('.', '/') + ".class";
        try (InputStream stream = MixinService.getService().getResourceAsStream(resource)) {
            return stream != null;
        } catch (IOException | RuntimeException e) {
            return false;
        }
    }
}
