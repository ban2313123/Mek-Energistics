package com.beipuo.mekenergistics.mixin;

import com.beipuo.mekenergistics.upgrade.AdvancedFactoryUpgradeAccess;
import java.util.List;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

@Pseudo
@Mixin(targets = {
        "com.jerry.mekaf.common.tile.factory.TileEntityWashingFactory",
        "com.jerry.mekextras.common.integration.mekaf.tile.factory.TileEntityExtraWashingFactory",
        "io.github.masyumero.emextras.common.integration.mekaf.tile.factory.TileEntityEMExtraWashingFactory"
}, remap = false)
public abstract class AdvancedFactoryWashingPortMixin implements AdvancedFactoryUpgradeAccess {
    @Shadow(remap = false) public BasicFluidTank fluidTank;

    @Override public List<? extends IExtendedFluidTank> meUpgradeFluidInputs() { return List.of(this.fluidTank); }
}
