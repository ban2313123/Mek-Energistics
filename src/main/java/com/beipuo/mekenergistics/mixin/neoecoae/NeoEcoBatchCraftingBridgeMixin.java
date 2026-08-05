package com.beipuo.mekenergistics.mixin.neoecoae;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.stacks.KeyCounter;
import appeng.crafting.inv.ListCraftingInventory;
import com.beipuo.mekenergistics.compat.neoecoae.NeoEcoBatchCompat;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "cn.dancingsnow.neoecoae.integration.ae2lt.AE2LTBatchCraftingBridge", remap = false)
public class NeoEcoBatchCraftingBridgeMixin {
    @Inject(method = "tryPushBatch", at = @At("HEAD"), cancellable = true)
    private void mekenergistics$pushMeMachineBatch(List<ICraftingProvider> providers,
            IPatternDetails details, KeyCounter[] oneCopyTemplate,
            ListCraftingInventory inventory, IEnergyService energyService,
            double patternPower, long maxCrafts, CallbackInfoReturnable<Integer> cir) {
        int accepted = NeoEcoBatchCompat.tryPushBatch(providers, details, oneCopyTemplate,
                inventory, energyService, patternPower, maxCrafts);
        if (accepted > 0) {
            cir.setReturnValue(accepted);
        }
    }
}
