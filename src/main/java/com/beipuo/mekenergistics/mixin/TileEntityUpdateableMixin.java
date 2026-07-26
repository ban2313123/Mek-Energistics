package com.beipuo.mekenergistics.mixin;

import com.beipuo.mekenergistics.block.MeMekanismMachineBlock;
import com.beipuo.mekenergistics.registry.ModBlockEntities;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.base.TileEntityUpdateable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = TileEntityUpdateable.class, remap = false)
public abstract class TileEntityUpdateableMixin {
    @ModifyArg(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/entity/BlockEntity;<init>(Lnet/minecraft/world/level/block/entity/BlockEntityType;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V"
            ),
            index = 0
    )
    private static BlockEntityType<?> mekenergistics$useMeBlockEntityType(
            BlockEntityType<?> original,
            BlockPos pos,
            BlockState state
    ) {
        if (state.getBlock() instanceof MeMekanismMachineBlock block) {
            // Nullable by construction: machines that failed availability gating are absent from the
            // registry map. This runs inside a BlockEntity constructor for every Mekanism tile in
            // the game, so an unguarded deref here would be a hard crash on world load rather than a
            // missing machine. Falling back to the original type degrades instead.
            TileEntityTypeRegistryObject<? extends TileEntityMekanism> registered =
                    ModBlockEntities.getMachineBlockEntity(block.getMachine());
            if (registered != null) {
                return registered.get();
            }
        }
        return original;
    }
}
