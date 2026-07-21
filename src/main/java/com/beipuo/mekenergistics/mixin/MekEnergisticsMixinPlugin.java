package com.beipuo.mekenergistics.mixin;

import java.util.List;
import java.util.Map;
import java.util.Set;
import net.neoforged.fml.loading.LoadingModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public class MekEnergisticsMixinPlugin implements IMixinConfigPlugin {
    private static final Map<String, String> OPTIONAL_MIXINS = Map.ofEntries(
            Map.entry(".TileEntityAlloyerAccessor", "evolvedmekanism"),
            Map.entry(".TileEntitySolidifierAccessor", "evolvedmekanism"),
            Map.entry(".TileEntityMelterAccessor", "evolvedmekanism"),
            Map.entry(".TileEntityChemixerAccessor", "evolvedmekanism"),
            Map.entry(".TileEntityEMExtraDissolvingFactoryAccessor", "emextras"),
            Map.entry(".TileEntityChemicalReplicatorAccessor", "mekmm"),
            Map.entry(".TileEntityFluidReplicatorAccessor", "mekmm"),
            Map.entry(".TileEntityLargeRotaryCondensentratorAccessor", "mekmm"),
            Map.entry(".TileEntityLargeChemicalInfuserAccessor", "mekmm"),
            Map.entry(".TileEntityLargeAntiprotonicNucleosynthesizerAccessor", "mekmm"),
            Map.entry(".extendedae.ContainerRenamerMixin", "extendedae"),
            Map.entry(".dataenergistics.PatternProviderSyncHelperMixin", "data_energistics"),
            Map.entry(".dataenergistics.PatternProviderNameHelperMixin", "data_energistics"));

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
                return isModLoaded(entry.getValue());
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
}
