package com.beipuo.mekenergistics.mixin.ae2;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.stacks.KeyCounter;
import appeng.crafting.execution.CraftingCpuLogic;
import appeng.crafting.execution.ExecutingCraftingJob;
import appeng.crafting.inv.ListCraftingInventory;
import appeng.me.service.CraftingService;
import com.beipuo.mekenergistics.crafting.MeCraftingCpuBatching;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CraftingCpuLogic.class, remap = false)
public abstract class CraftingCpuLogicMixin {
    @Shadow
    private ExecutingCraftingJob job;

    @Shadow
    @Final
    private ListCraftingInventory inventory;

    @Unique
    private IEnergyService mekenergistics$batchEnergyService;

    @Unique
    private Level mekenergistics$batchLevel;

    @Inject(method = "executeCrafting", at = @At("HEAD"))
    private void mekenergistics$beginBatchContext(int maxPatterns, CraftingService craftingService,
            IEnergyService energyService, Level level, CallbackInfoReturnable<Integer> cir) {
        this.mekenergistics$batchEnergyService = energyService;
        this.mekenergistics$batchLevel = level;
    }

    @Inject(method = "executeCrafting", at = @At("RETURN"))
    private void mekenergistics$endBatchContext(int maxPatterns, CraftingService craftingService,
            IEnergyService energyService, Level level, CallbackInfoReturnable<Integer> cir) {
        this.mekenergistics$batchEnergyService = null;
        this.mekenergistics$batchLevel = null;
    }

    @Redirect(
            method = "executeCrafting",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/api/networking/crafting/ICraftingProvider;pushPattern(Lappeng/api/crafting/IPatternDetails;[Lappeng/api/stacks/KeyCounter;)Z"))
    private boolean mekenergistics$batchMeMachineTasks(ICraftingProvider provider,
            IPatternDetails details, KeyCounter[] inputs) {
        IEnergyService energyService = this.mekenergistics$batchEnergyService;
        Level level = this.mekenergistics$batchLevel;
        if (energyService == null || level == null) {
            return provider.pushPattern(details, inputs);
        }
        return MeCraftingCpuBatching.pushPattern(
                provider, details, inputs, this.inventory, this.job, energyService, level);
    }
}
