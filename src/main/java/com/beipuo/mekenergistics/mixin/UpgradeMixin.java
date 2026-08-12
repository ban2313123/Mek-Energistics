package com.beipuo.mekenergistics.mixin;

import com.beipuo.mekenergistics.common.MeLangEntry;
import com.beipuo.mekenergistics.upgrade.MeMekanismUpgrades;
import java.util.Arrays;
import mekanism.api.Upgrade;
import mekanism.api.text.EnumColor;
import mekanism.api.text.ILangEntry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Adds this mod's upgrade cards to Mekanism's native upgrade enum. */
@Mixin(value = Upgrade.class, remap = false)
public abstract class UpgradeMixin {
    @Shadow
    @Final
    @Mutable
    private static Upgrade[] $VALUES;

    @Invoker("<init>")
    private static Upgrade mekenergistics$create(String internalName, int ordinal, String serializedName,
            ILangEntry langKey, ILangEntry descriptionKey, int max, EnumColor color) {
        throw new AssertionError("Mixin failed to apply");
    }

    @Inject(method = "<clinit>", at = @At(value = "INVOKE",
            target = "Lmekanism/api/Upgrade;values()[Lmekanism/api/Upgrade;", ordinal = 0))
    private static void mekenergistics$addMeUpgrades(CallbackInfo ci) {
        int ordinal = $VALUES.length;
        Upgrade[] expanded = Arrays.copyOf($VALUES, ordinal + 3);
        expanded[ordinal] = create("ME_PATTERN_PROVIDER", ordinal,
                MeMekanismUpgrades.PATTERN_PROVIDER_NAME, EnumColor.AQUA);
        expanded[ordinal + 1] = create("ME_PASSIVE_CRAFTING", ordinal + 1,
                MeMekanismUpgrades.PASSIVE_CRAFTING_NAME, EnumColor.BRIGHT_GREEN);
        expanded[ordinal + 2] = create("ME_OUTPUT_INTERFACE", ordinal + 2,
                MeMekanismUpgrades.OUTPUT_INTERFACE_NAME, EnumColor.PURPLE);
        $VALUES = expanded;
    }

    private static Upgrade create(String internalName, int ordinal, String serializedName, EnumColor color) {
        return mekenergistics$create(internalName, ordinal, serializedName,
                MeLangEntry.of("upgrade.mekenergistics." + serializedName),
                MeLangEntry.of("upgrade.mekenergistics." + serializedName + ".description"),
                1, color);
    }
}
