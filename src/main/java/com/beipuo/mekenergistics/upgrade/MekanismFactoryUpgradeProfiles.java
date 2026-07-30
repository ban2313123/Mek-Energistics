package com.beipuo.mekenergistics.upgrade;

import com.beipuo.mekenergistics.blockentity.support.io.MeInputLayout;
import com.beipuo.mekenergistics.blockentity.support.io.MeOutputPort;
import com.beipuo.mekenergistics.common.machine.MeMekanismMachine;
import java.util.List;
import mekanism.common.tile.factory.TileEntityFactory;

public final class MekanismFactoryUpgradeProfiles {
    public static MeUpgradeMachineProfile<TileEntityFactory<?>> forTile(TileEntityFactory<?> tile) {
        MeMekanismMachine machine = MeMekanismMachine.getFactory(tile.tier, tile.getFactoryType());
        if (machine == null) {
            return null;
        }
        return new MeUpgradeMachineProfile<>(candidate -> candidate == tile,
                candidate -> ((FactoryIoAccess) candidate).mekenergistics$getFactoryInputLayout(),
                candidate -> ((FactoryIoAccess) candidate).mekenergistics$getFactoryOutputPorts(),
                machine,
                candidate -> new net.minecraft.world.item.ItemStack(candidate.getBlockState().getBlock()),
                candidate -> candidate.getBlockState().getBlock().getName());
    }

    public interface FactoryIoAccess {
        MeInputLayout mekenergistics$getFactoryInputLayout();

        List<? extends MeOutputPort> mekenergistics$getFactoryOutputPorts();
    }

    private MekanismFactoryUpgradeProfiles() {
    }
}
