package com.beipuo.mekenergistics.mixin;

import mekanism.common.attachments.containers.creator.BaseContainerCreator;
import mekanism.common.attachments.containers.item.ComponentBackedInventorySlot;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.registries.MekanismBlocks;
import com.beipuo.mekenergistics.upgrade.MekanismRecipeUpgradeProfiles;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/** Supplies the ME pattern attachment slot for the vanilla enrichment chamber item. */
@Mixin(value = BaseContainerCreator.class, remap = false)
public abstract class EnrichmentChamberItemContainerCreatorMixin {
    @Shadow
    private List<?> creators;

    @Inject(method = "create", at = @At("HEAD"), cancellable = true)
    private void mekenergistics$createPatternSlot(ContainerType<?, ?, ?> type, ItemStack stack, int index,
            CallbackInfoReturnable<ComponentBackedInventorySlot> cir) {
        if ((stack.getItem() == MekanismBlocks.ENRICHMENT_CHAMBER.value().asItem()
                || MekanismRecipeUpgradeProfiles.isSupportedBlockItem(stack)
                || MekanismRecipeUpgradeProfiles.isSupportedFactoryBlockItem(stack))
                && index >= creators.size()) {
            cir.setReturnValue(new ComponentBackedInventorySlot(
                    stack, index, (ignored, automation) -> true, (ignored, automation) -> true, ignored -> true));
        }
    }
}
