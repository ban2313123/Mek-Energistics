package com.beipuo.mekenergistics.mixin;

import com.beipuo.mekenergistics.upgrade.MePatternAutomationRuntimes;
import mekanism.api.IContentsListener;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hooks that run for every Mekanism tile construction/tick, but only wrap inventory or process
 * pattern I/O when {@link com.beipuo.mekenergistics.upgrade.MePatternAutomationProfiles} says the
 * tile is an SPI/Magic host. This class does <em>not</em> implement {@code MeUpgradeableMachine},
 * so energy cubes and other non-hosts stay ordinary Mekanism tiles.
 *
 * <p>Inventory wrapping intercepts the virtual {@code getInitialInventory} invoke inside the
 * {@code TileEntityMekanism} constructor, so subclass overrides still receive pattern slots.</p>
 */
@Mixin(value = TileEntityMekanism.class, remap = false)
public abstract class TileEntityExternalPatternAutomationMeUpgradeMixin {
    @Shadow
    protected abstract IInventorySlotHolder getInitialInventory(IContentsListener listener);

    @Redirect(method = "<init>", at = @At(value = "INVOKE",
            target = "Lmekanism/common/tile/base/TileEntityMekanism;getInitialInventory(Lmekanism/api/IContentsListener;)Lmekanism/common/capabilities/holder/slot/IInventorySlotHolder;"))
    private IInventorySlotHolder mekenergistics$wrapSpiInventory(TileEntityMekanism tile,
            IContentsListener listener) {
        IInventorySlotHolder original = this.getInitialInventory(listener);
        return MePatternAutomationRuntimes.wrapInventory(tile, original, listener);
    }

    @Inject(method = "tickServer", at = @At("RETURN"))
    private static void mekenergistics$processSpiPatternIo(Level level, BlockPos pos, BlockState state,
            TileEntityMekanism tile, CallbackInfo ci) {
        MePatternAutomationRuntimes.processTick(tile, false);
    }
}
