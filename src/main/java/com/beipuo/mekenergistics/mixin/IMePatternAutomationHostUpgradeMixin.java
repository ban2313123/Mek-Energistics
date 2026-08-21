package com.beipuo.mekenergistics.mixin;

import com.beipuo.mekenergistics.api.upgrade.IMePatternAutomationHost;
import com.beipuo.mekenergistics.upgrade.MePatternAutomationProfiles;
import com.beipuo.mekenergistics.upgrade.MePatternAutomationRuntimes;
import com.beipuo.mekenergistics.upgrade.MeUpgradeMachineProfile;
import com.beipuo.mekenergistics.upgrade.MeUpgradeRecipeMachineAdapter;
import com.beipuo.mekenergistics.upgrade.MeUpgradeRecipeMachineRuntime;
import mekanism.common.tile.base.TileEntityMekanism;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Adds the ME-upgrade adapter only to tiles that actually implement the public SPI, not to every
 * {@link TileEntityMekanism}.
 */
@Mixin(IMePatternAutomationHost.class)
public interface IMePatternAutomationHostUpgradeMixin extends MeUpgradeRecipeMachineAdapter {
    @Override
    default MeUpgradeRecipeMachineRuntime getOrCreateMeUpgradeRuntime() {
        return MePatternAutomationRuntimes.getOrCreate((TileEntityMekanism) this);
    }

    @Override
    default MeUpgradeRecipeMachineRuntime getExistingMeUpgradeRuntime() {
        return MePatternAutomationRuntimes.getExisting((TileEntityMekanism) this);
    }

    @Override
    default MeUpgradeMachineProfile<?> getMeUpgradeProfile() {
        return MePatternAutomationProfiles.forTile((TileEntityMekanism) this);
    }
}
