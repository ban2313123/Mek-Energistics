package com.beipuo.mekenergistics.upgrade.empowered;

import com.beipuo.mekenergistics.upgrade.MePassiveCraftingUpgrade;
import com.beipuo.mekenergistics.upgrade.MePatternUpgradeLang;
import dev.lapis256.mekanism_empowered.core.api.upgrade.AdditionalUpgrade;
import dev.lapis256.mekanism_empowered.core.api.upgrade.AdditionalUpgradeDelegate;
import dev.lapis256.mekanism_empowered.core.api.upgrade.IAdditionalUpgrades;
import dev.lapis256.mekanism_empowered.core.common.upgrade.UpgradeItemRegistry;
import dev.lapis256.mekanism_empowered.core.common.util.AdditionalUpgradeUtil;
import mekanism.api.text.EnumColor;
import mekanism.api.Upgrade;
import mekanism.common.registries.MekanismBlockTypes;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;

public final class EmpoweredMePassiveCraftingUpgradeProvider implements IAdditionalUpgrades {
    public static final AdditionalUpgradeDelegate ME_PASSIVE_CRAFTING = AdditionalUpgrade.register(
            MePassiveCraftingUpgrade.INTERNAL_NAME, MePassiveCraftingUpgrade.SERIALIZED_NAME,
            MePatternUpgradeLang.PASSIVE_NAME, MePatternUpgradeLang.PASSIVE_DESCRIPTION, 1, EnumColor.AQUA);

    public static void registerItem(Holder<Item> item) {
        UpgradeItemRegistry.register(ME_PASSIVE_CRAFTING, item);
    }

    public static void registerSupportedUpgrade() {
        AdditionalUpgradeUtil.addSupported(MekanismBlockTypes.ENRICHMENT_CHAMBER, findUpgrade());
    }

    private static Upgrade findUpgrade() {
        for (Upgrade upgrade : Upgrade.values()) {
            if (MePassiveCraftingUpgrade.SERIALIZED_NAME.equals(upgrade.getSerializedName())) return upgrade;
        }
        throw new IllegalStateException("ME passive crafting upgrade was not registered");
    }
}
