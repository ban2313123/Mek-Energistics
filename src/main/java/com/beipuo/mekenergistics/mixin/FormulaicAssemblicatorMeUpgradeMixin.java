package com.beipuo.mekenergistics.mixin;

import com.beipuo.mekenergistics.blockentity.api.AeOutputMode;
import com.beipuo.mekenergistics.blockentity.api.MeUpgradeableMachine;
import com.beipuo.mekenergistics.blockentity.support.AbstractMeAeSupport;
import com.beipuo.mekenergistics.blockentity.support.io.MeInputLayout;
import com.beipuo.mekenergistics.blockentity.support.io.MeMachineIoAdapter;
import com.beipuo.mekenergistics.blockentity.support.io.MeOutputPort;
import com.beipuo.mekenergistics.common.machine.MeMekanismMachine;
import com.beipuo.mekenergistics.compat.catalog.CompatMachineCatalog;
import com.beipuo.mekenergistics.upgrade.MeUpgradeMachineProfile;
import com.beipuo.mekenergistics.upgrade.MeUpgradeRecipeMachineRuntime;
import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.tile.machine.TileEntityFormulaicAssemblicator;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.extensions.IBlockEntityExtension;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TileEntityFormulaicAssemblicator.class, remap = false)
public abstract class FormulaicAssemblicatorMeUpgradeMixin implements MeUpgradeableMachine, IBlockEntityExtension {
    @Unique private MeUpgradeRecipeMachineRuntime mekenergistics$runtime;
    @Unique private TileEntityFormulaicAssemblicator mekenergistics$tile() {
        return (TileEntityFormulaicAssemblicator) (Object) this;
    }
    @Unique private MeUpgradeRecipeMachineRuntime mekenergistics$runtime() {
        if (this.mekenergistics$runtime == null) {
            this.mekenergistics$runtime = new MeUpgradeRecipeMachineRuntime(mekenergistics$tile(), AeOutputMode.BOTH);
        }
        return this.mekenergistics$runtime;
    }
    @Unique private boolean mekenergistics$isSourceBlock() {
        var id = BuiltInRegistries.BLOCK.getKey(mekenergistics$tile().getBlockState().getBlock());
        return CompatMachineCatalog.findBySourceBlockId(id)
                .map(spec -> spec.machine() == MeMekanismMachine.FORMULAIC_ASSEMBLICATOR).orElse(false);
    }

    @Override
    public MeUpgradeMachineProfile<?> getMeUpgradeProfile() {
        if (!mekenergistics$isSourceBlock()) return null;
        return new MeUpgradeMachineProfile<>(candidate -> candidate == mekenergistics$tile(),
                candidate -> getPatternInputLayout(), candidate -> getPatternOutputPorts(),
                MeMekanismMachine.FORMULAIC_ASSEMBLICATOR,
                candidate -> new net.minecraft.world.item.ItemStack(candidate.getBlockState().getBlock()),
                candidate -> candidate.getBlockState().getBlock().getName());
    }
    @Override public boolean isMeUpgradeTarget() { return mekenergistics$isSourceBlock(); }
    @Override public boolean isMeUpgradeActive() { return mekenergistics$runtime().active(isMeUpgradeTarget()); }
    @Override public AbstractMeAeSupport<?> getRecipeAeSupport() { return mekenergistics$runtime().support(); }
    @Override public AeOutputMode getAeOutputMode() { return mekenergistics$runtime().outputMode(); }
    @Override public void cycleAeOutputMode() { mekenergistics$runtime().cycleOutputMode(); }
    @Override public MeInputLayout getPatternInputLayout() {
        return MeInputLayout.unordered(((TileEntityFormulaicAssemblicatorAccessor) (Object) this)
                .mekenergistics$getInputSlots().stream().map(MeMachineIoAdapter::itemInput).toList());
    }
    @Override public List<? extends MeOutputPort> getPatternOutputPorts() {
        return ((TileEntityFormulaicAssemblicatorAccessor) (Object) this).mekenergistics$getOutputSlots().stream()
                .map(MeMachineIoAdapter::itemOutput).toList();
    }

    @Inject(method = "getInitialInventory", at = @At("RETURN"), cancellable = true)
    private void mekenergistics$addPatternSlots(IContentsListener listener,
            CallbackInfoReturnable<@NotNull IInventorySlotHolder> cir) {
        if (isMeUpgradeTarget()) cir.setReturnValue(
                mekenergistics$runtime().withPatternSlots(cir.getReturnValue(), listener));
    }
    @Inject(method = "onUpdateServer", at = @At("RETURN"), cancellable = true)
    private void mekenergistics$processPatternIo(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(mekenergistics$runtime().tick(isMeUpgradeActive(), cir.getReturnValue()));
    }

    @Override public void createMeNodeIfActive() { mekenergistics$runtime().createNodeIfActive(isMeUpgradeActive()); }
    @Override public void destroyMeNode() { if (this.mekenergistics$runtime != null) this.mekenergistics$runtime.destroyNode(); }
    @Override public void onChunkUnloaded() { destroyMeNode(); }
    @Override public void addMeTrackers(MekanismContainer container) { mekenergistics$runtime().addTrackers(container, isMeUpgradeTarget()); }
    @Override public void saveMeState(CompoundTag tag, HolderLookup.Provider registries) { if (this.mekenergistics$runtime != null) this.mekenergistics$runtime.save(tag, registries); }
    @Override public void loadMeState(CompoundTag tag, HolderLookup.Provider registries) { if (isMeUpgradeTarget()) mekenergistics$runtime().load(tag, registries); }
}
