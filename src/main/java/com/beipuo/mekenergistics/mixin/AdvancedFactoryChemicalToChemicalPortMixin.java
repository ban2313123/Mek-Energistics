package com.beipuo.mekenergistics.mixin;

import com.beipuo.mekenergistics.upgrade.AdvancedFactoryUpgradeAccess;
import java.util.Arrays;
import java.util.List;
import mekanism.api.chemical.IChemicalTank;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

@Pseudo
@Mixin(targets = {
        "com.jerry.mekaf.common.tile.factory.base.TileEntityChemicalToChemicalFactory",
        "com.jerry.mekextras.common.integration.mekaf.tile.factory.base.TileEntityExtraChemicalToChemicalFactory",
        "io.github.masyumero.emextras.common.integration.mekaf.tile.factory.base.TileEntityEMExtraChemicalToChemicalFactory"
}, remap = false)
public abstract class AdvancedFactoryChemicalToChemicalPortMixin implements AdvancedFactoryUpgradeAccess {
    @Shadow(remap = false) protected IChemicalTank[] inputTank;
    @Shadow(remap = false) protected IChemicalTank[] outputTank;

    @Override public List<? extends IChemicalTank> meUpgradeChemicalInputs() { return Arrays.asList(this.inputTank); }
    @Override public List<? extends IChemicalTank> meUpgradeChemicalOutputs() { return Arrays.asList(this.outputTank); }
}
