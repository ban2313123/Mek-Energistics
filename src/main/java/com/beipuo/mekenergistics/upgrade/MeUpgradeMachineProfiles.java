package com.beipuo.mekenergistics.upgrade;

import com.beipuo.mekenergistics.blockentity.support.io.MeMachineIoAdapter;
import com.beipuo.mekenergistics.blockentity.support.io.MeInputLayout;
import com.beipuo.mekenergistics.common.machine.MeMekanismMachine;
import com.beipuo.mekenergistics.compat.catalog.CompatMachineCatalog;
import com.beipuo.mekenergistics.mixin.TileEntityElectricMachineAccessor;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.prefab.TileEntityElectricMachine;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

/** Built-in profiles for Mekanism machines supported through upgrades. */
public final class MeUpgradeMachineProfiles {
    public static final MeUpgradeMachineProfile<TileEntityElectricMachine> ENRICHMENT_CHAMBER =
            new MeUpgradeMachineProfile<>(
                    tile -> tile.getBlockState().is(MekanismBlocks.ENRICHMENT_CHAMBER.value()),
                    tile -> MeInputLayout.unordered(List.of(MeUpgradeMachineIo.itemInput(tile))),
                    tile -> List.of(MeUpgradeMachineIo.itemOutput(tile)),
                    MeMekanismMachine.ENRICHMENT_CHAMBER,
                    tile -> new net.minecraft.world.item.ItemStack(MekanismBlocks.ENRICHMENT_CHAMBER),
                    tile -> MekanismBlocks.ENRICHMENT_CHAMBER.getTextComponent());

    private static final Map<ResourceLocation, MeMekanismMachine> MACHINE_BY_BLOCK = buildMachineIndex();

    /** Resolves the profile for an existing Mekanism/compat electric machine block. */
    public static MeUpgradeMachineProfile<TileEntityElectricMachine> forTile(TileEntityElectricMachine tile) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(tile.getBlockState().getBlock());
        MeMekanismMachine machine = MACHINE_BY_BLOCK.get(id);
        if (machine == null || machine.isFactory()) {
            return null;
        }
        return new MeUpgradeMachineProfile<>(
                candidate -> candidate == tile,
                candidate -> MeInputLayout.unordered(List.of(MeUpgradeMachineIo.itemInput(candidate))),
                candidate -> List.of(MeUpgradeMachineIo.itemOutput(candidate)),
                machine,
                candidate -> new net.minecraft.world.item.ItemStack(candidate.getBlockState().getBlock()),
                candidate -> candidate.getBlockState().getBlock().getName());
    }

    private static Map<ResourceLocation, MeMekanismMachine> buildMachineIndex() {
        Map<ResourceLocation, MeMekanismMachine> index = new ConcurrentHashMap<>();
        CompatMachineCatalog.all()
                .filter(spec -> !spec.machine().isFactory())
                .forEach(spec -> index.put(spec.sourceBlockId(), spec.machine()));
        return Map.copyOf(index);
    }

    private MeUpgradeMachineProfiles() {
    }

    /** Machine-specific slot access kept separate from the runtime and profile metadata. */
    private static final class MeUpgradeMachineIo {
        private static com.beipuo.mekenergistics.blockentity.support.io.MeInputPort itemInput(
                TileEntityElectricMachine tile) {
            return MeMachineIoAdapter.itemInput(((TileEntityElectricMachineAccessor) tile)
                    .mekenergistics$getInputSlot());
        }

        private static com.beipuo.mekenergistics.blockentity.support.io.MeOutputPort itemOutput(
                TileEntityElectricMachine tile) {
            return MeMachineIoAdapter.itemOutput(((TileEntityElectricMachineAccessor) tile)
                    .mekenergistics$getOutputSlot());
        }
    }
}
