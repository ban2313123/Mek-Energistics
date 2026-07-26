package com.beipuo.mekenergistics.mixin.extendedae;

import java.util.function.Consumer;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = com.glodblock.github.extendedae.container.ContainerRenamer.class, remap = false)
public abstract class ContainerRenamerMixin {
    // require = 0 to match the dataenergistics injections: ContainerRenamer.setter is a private
    // static helper, so ExtendedAE is free to rename it in any release. Losing the ability to rename
    // ME machines from its renamer is a degraded feature; failing to apply the mixin would be a
    // startup crash.
    @Inject(method = "setter", at = @At("HEAD"), cancellable = true, require = 0)
    private static void mekenergistics$setterForMekanismTile(Object target, CallbackInfoReturnable<Consumer<String>> cir) {
        if (target instanceof TileEntityMekanism tile) {
            cir.setReturnValue(name -> {
                Component customName = name.isBlank() ? null : Component.literal(name);
                tile.setCustomName(customName);
            });
        }
    }
}
