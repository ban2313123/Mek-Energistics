package com.beipuo.mekenergistics.mixin;

import mekanism.common.attachments.containers.creator.BaseContainerCreator;
import mekanism.common.attachments.containers.item.ComponentBackedInventorySlot;
import mekanism.common.attachments.containers.ContainerType;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * Supplies the ME pattern attachment slot for item stacks whose attached item containers exceed the
 * number of vanilla container creators. The tile-side pattern slot can end up copied into an item's
 * ATTACHED_ITEMS component when an upgraded machine is broken, so any factory/machine family with
 * fewer creators than attached containers must be able to fabricate the trailing slot.
 */
@Mixin(value = BaseContainerCreator.class, remap = false)
public abstract class EnrichmentChamberItemContainerCreatorMixin {
    @Shadow
    private List<?> creators;

    @Inject(method = "create", at = @At("HEAD"), cancellable = true)
    private void mekenergistics$createPatternSlot(ContainerType<?, ?, ?> type, ItemStack stack, int index,
            CallbackInfoReturnable<ComponentBackedInventorySlot> cir) {
        if (type == ContainerType.ITEM && index >= creators.size()) {
            cir.setReturnValue(new ComponentBackedInventorySlot(
                    stack, index, (ignored, automation) -> true, (ignored, automation) -> true, ignored -> true));
        }
    }
}
