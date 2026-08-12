package com.beipuo.mekenergistics.item;

import com.beipuo.mekenergistics.upgrade.MeUpgradeType;
import mekanism.common.item.ItemUpgrade;

/**
 * Native Mekanism upgrade item backed by this mod's extended {@code Upgrade} entries.
 */
public final class MeUpgradeItem extends ItemUpgrade {
    private final MeUpgradeType type;

    public MeUpgradeItem(MeUpgradeType type, Properties properties) {
        super(com.beipuo.mekenergistics.upgrade.MeMekanismUpgrades.forType(type), properties);
        this.type = type;
    }

    public MeUpgradeType getType() {
        return this.type;
    }

}
