package com.beipuo.mekenergistics.mixin;

import com.beipuo.mekenergistics.blockentity.api.AeOutputMode;
import com.beipuo.mekenergistics.blockentity.support.AbstractMeAeSupport;
import com.beipuo.mekenergistics.blockentity.support.io.MeInputLayout;
import com.beipuo.mekenergistics.blockentity.support.io.MeOutputPort;
import com.beipuo.mekenergistics.upgrade.MePatternAutomationProfiles;
import com.beipuo.mekenergistics.upgrade.MeUpgradeContainer;
import com.beipuo.mekenergistics.upgrade.MeUpgradeMachineProfile;
import com.beipuo.mekenergistics.upgrade.MeUpgradeRecipeMachineAdapter;
import com.beipuo.mekenergistics.upgrade.MeUpgradeRecipeMachineRuntime;
import com.example.mekanismmagic.blockentity.NativeMagicMachineBlockEntity;
import java.util.List;
import mekanism.common.tile.base.TileEntityMekanism;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * ME upgrade host for Mekanism Magic's native non-factory machines.
 *
 * <p>I/O comes from Magic's published {@code IMekanismMagicAutomation} surface rather than
 * private slot fields. Factories are handled by the shared factory upgrade mixins plus
 * {@link com.beipuo.mekenergistics.upgrade.MekanismFactoryUpgradeProfiles}' Magic fallback.</p>
 */
@Mixin(value = NativeMagicMachineBlockEntity.class, remap = false)
public abstract class MekanismMagicMachineMeUpgradeMixin implements MeUpgradeRecipeMachineAdapter {
    @Unique
    private MeUpgradeRecipeMachineRuntime mekenergistics$runtime;

    @Unique
    private TileEntityMekanism mekenergistics$tile() {
        return (TileEntityMekanism) (Object) this;
    }

    @Override
    public MeUpgradeRecipeMachineRuntime getOrCreateMeUpgradeRuntime() {
        if (this.mekenergistics$runtime == null) {
            this.mekenergistics$runtime =
                    new MeUpgradeRecipeMachineRuntime(mekenergistics$tile(), AeOutputMode.BOTH);
        }
        return this.mekenergistics$runtime;
    }

    @Override
    public MeUpgradeRecipeMachineRuntime getExistingMeUpgradeRuntime() {
        return this.mekenergistics$runtime;
    }

    @Override
    public MeUpgradeMachineProfile<?> getMeUpgradeProfile() {
        return MePatternAutomationProfiles.forTile(mekenergistics$tile());
    }

    @Override
    public MeUpgradeContainer getMeUpgradeContainer() {
        return MeUpgradeRecipeMachineAdapter.super.getMeUpgradeContainer();
    }

    @Override
    public boolean isMeUpgradeTarget() {
        return MeUpgradeRecipeMachineAdapter.super.isMeUpgradeTarget();
    }

    @Override
    public boolean isMeUpgradeActive() {
        return MeUpgradeRecipeMachineAdapter.super.isMeUpgradeActive();
    }

    @Override
    public AbstractMeAeSupport<?> getRecipeAeSupport() {
        return MeUpgradeRecipeMachineAdapter.super.getRecipeAeSupport();
    }

    @Override
    public MeInputLayout getPatternInputLayout() {
        return MeUpgradeRecipeMachineAdapter.super.getPatternInputLayout();
    }

    @Override
    public List<? extends MeOutputPort> getPatternOutputPorts() {
        return MeUpgradeRecipeMachineAdapter.super.getPatternOutputPorts();
    }

    @Inject(method = "onUpdateServer", at = @At("RETURN"), cancellable = true)
    private void mekenergistics$processPatternIo(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(processMePatternIo(cir.getReturnValue()));
    }
}
