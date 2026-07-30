package com.beipuo.mekenergistics.mixin;

import appeng.api.crafting.PatternDetailsHelper;
import com.beipuo.mekenergistics.blockentity.api.MeUpgradeableMachine;
import com.beipuo.mekenergistics.menu.MePatternQuickMoveContainer;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.slot.InventoryContainerSlot;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = MekanismContainer.class, remap = false)
public abstract class MekanismContainerPatternQuickMoveMixin {
    @Shadow
    protected abstract ItemStack transferSuccess(@NotNull Slot currentSlot, @NotNull Player player,
            @NotNull ItemStack slotStack, @NotNull ItemStack stackToInsert);

    @Inject(method = "quickMoveStack", at = @At("HEAD"), cancellable = true)
    private void mekenergistics$quickMovePattern(Player player, int slotId,
            CallbackInfoReturnable<ItemStack> cir) {
        MekanismContainer container = (MekanismContainer) (Object) this;
        if (!(container instanceof MekanismTileContainer<?> tileContainer)
                || !(tileContainer.getTileEntity() instanceof MeUpgradeableMachine machine)
                || !machine.isMeUpgradeActive()
                || slotId < 0 || slotId >= container.slots.size()) {
            return;
        }
        Slot currentSlot = container.slots.get(slotId);
        if (currentSlot instanceof InventoryContainerSlot || !currentSlot.hasItem()) {
            return;
        }
        ItemStack slotStack = currentSlot.getItem();
        if (!PatternDetailsHelper.isEncodedPattern(slotStack)) {
            return;
        }
        ItemStack remaining = MePatternQuickMoveContainer.insertIntoPatternSlots(
                slotStack, machine.getPatternSlots());
        if (remaining.getCount() != slotStack.getCount()) {
            cir.setReturnValue(transferSuccess(currentSlot, player, slotStack, remaining));
        }
    }
}
