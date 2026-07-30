package com.beipuo.mekenergistics.mixin;

import com.beipuo.mekenergistics.blockentity.api.MeUpgradeableMachine;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntity.class)
public abstract class BlockEntityMeUpgradeLifecycleMixin {
    @Inject(method = "clearRemoved", at = @At("RETURN"))
    private void mekenergistics$createMeNode(CallbackInfo ci) {
        if ((Object) this instanceof MeUpgradeableMachine machine) {
            machine.createMeNodeIfActive();
        }
    }
}
