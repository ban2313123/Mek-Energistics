package com.beipuo.mekenergistics.upgrade;

import com.beipuo.mekenergistics.blockentity.support.io.MeInputLayout;
import com.beipuo.mekenergistics.blockentity.support.io.MeOutputPort;
import com.beipuo.mekenergistics.common.machine.MeMekanismMachine;
import com.beipuo.mekenergistics.compat.catalog.CompatMachineCatalog;
import com.beipuo.mekenergistics.compat.magic.MekanismMagicUpgradeProfiles;
import java.util.List;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.factory.TileEntityFactory;
import net.minecraft.core.registries.BuiltInRegistries;

public final class MekanismFactoryUpgradeProfiles {
    public static MeUpgradeMachineProfile<TileEntityFactory<?>> forTile(TileEntityFactory<?> tile) {
        return forTile(tile, (FactoryIoAccess) tile);
    }

    public static MeUpgradeMachineProfile<TileEntityFactory<?>> forTile(
            TileEntityFactory<?> tile, FactoryIoAccess ioAccess) {
        return profile(tile, ioAccess);
    }

    public static MeUpgradeMachineProfile<TileEntityMekanism> forTile(
            TileEntityMekanism tile, FactoryIoAccess ioAccess) {
        return profile(tile, ioAccess);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static <TILE extends TileEntityMekanism> MeUpgradeMachineProfile<TILE> profile(
            TILE tile, FactoryIoAccess ioAccess) {
        var sourceBlockId = BuiltInRegistries.BLOCK.getKey(tile.getBlockState().getBlock());
        MeMekanismMachine machine = CompatMachineCatalog.findBySourceBlockId(sourceBlockId)
                .map(spec -> spec.machine().isFactory() ? spec.machine() : null)
                .orElse(null);
        if (machine == null) {
            // Spirit factories publish the same automation surface as the single-process machines.
            MeUpgradeMachineProfile<?> magic = MekanismMagicUpgradeProfiles.forTile(tile);
            return magic == null ? null : (MeUpgradeMachineProfile) magic;
        }
        return new MeUpgradeMachineProfile<>(candidate -> candidate == tile,
                candidate -> ioAccess.mekenergistics$getFactoryInputLayout(),
                candidate -> ioAccess.mekenergistics$getFactoryOutputPorts(),
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
