package com.beipuo.mekenergistics.upgrade;

import mekanism.common.tile.base.TileEntityMekanism;

public interface CatalogFactoryUpgradeAdapter extends MeUpgradeRecipeMachineAdapter,
        MekanismFactoryUpgradeProfiles.FactoryIoAccess {
    TileEntityMekanism meUpgradeTile();

    @Override
    default MeUpgradeMachineProfile<?> getMeUpgradeProfile() {
        return MekanismFactoryUpgradeProfiles.forTile(meUpgradeTile(), this);
    }
}
