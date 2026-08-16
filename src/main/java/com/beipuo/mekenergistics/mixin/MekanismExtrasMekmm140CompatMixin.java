package com.beipuo.mekenergistics.mixin;

import com.jerry.mekmm.common.content.blocktype.MoreMachineFactoryType;
import com.jerry.mekmm.common.util.MoreMachineEnumUtils;
import java.util.Arrays;
import net.neoforged.fml.loading.LoadingModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = {
        "com.jerry.mekextras.common.integration.mekmm.registries.ExtraMoreMachineBlockTypes",
        "com.jerry.mekextras.common.integration.mekmm.registries.ExtraMoreMachineBlocks"
}, remap = false)
public abstract class MekanismExtrasMekmm140CompatMixin {
    @Redirect(
            method = "<clinit>",
            at = @At(
                    value = "FIELD",
                    target = "Lcom/jerry/mekmm/common/util/MoreMachineEnumUtils;MM_FACTORY_TYPES:[Lcom/jerry/mekmm/common/content/blocktype/MoreMachineFactoryType;"),
            require = 0)
    private static MoreMachineFactoryType[] mekenergistics$factoryTypesSupportedByMekanismExtras() {
        if (!isVersion("mekmm", "1.4.0") || !isVersion("mekanism_extras", "1.4.0")) {
            return MoreMachineEnumUtils.MM_FACTORY_TYPES;
        }
        return Arrays.stream(MoreMachineEnumUtils.MM_FACTORY_TYPES)
                .filter(type -> type != MoreMachineFactoryType.PRESSING)
                .toArray(MoreMachineFactoryType[]::new);
    }

    private static boolean isVersion(String modId, String version) {
        var loadingModList = LoadingModList.get();
        if (loadingModList == null) {
            return false;
        }
        var modFile = loadingModList.getModFileById(modId);
        return modFile != null && version.equals(modFile.versionString());
    }
}
