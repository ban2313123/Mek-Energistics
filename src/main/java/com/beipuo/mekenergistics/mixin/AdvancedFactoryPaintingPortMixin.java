package com.beipuo.mekenergistics.mixin;

import com.beipuo.mekenergistics.upgrade.AdvancedFactoryUpgradeAccess;
import java.util.List;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.inventory.IInventorySlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

@Pseudo
@Mixin(targets = {
        "com.jerry.mekaf.common.tile.factory.TileEntityPaintingFactory",
        "com.jerry.mekextras.common.integration.mekaf.tile.factory.TileEntityExtraPaintingFactory",
        "io.github.masyumero.emextras.common.integration.mekaf.tile.factory.TileEntityEMExtraPaintingFactory"
}, remap = false)
public abstract class AdvancedFactoryPaintingPortMixin implements AdvancedFactoryUpgradeAccess {
    @Shadow(remap = false) public IChemicalTank chemicalTank;
    @Shadow(remap = false) protected abstract IInventorySlot getExtraSlot();

    @Override public List<? extends IChemicalTank> meUpgradeChemicalInputs() { return List.of(this.chemicalTank); }
    @Override public List<? extends IInventorySlot> meUpgradeExtraItemInputs() { return List.of(getExtraSlot()); }
}
