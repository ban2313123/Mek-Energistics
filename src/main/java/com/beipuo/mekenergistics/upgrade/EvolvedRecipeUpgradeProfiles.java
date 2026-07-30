package com.beipuo.mekenergistics.upgrade;

import com.beipuo.mekenergistics.blockentity.support.io.MeInputLayout;
import com.beipuo.mekenergistics.blockentity.support.io.MeMachineIoAdapter;
import com.beipuo.mekenergistics.blockentity.support.io.MeOutputPort;
import com.beipuo.mekenergistics.common.machine.MeMekanismMachine;
import com.beipuo.mekenergistics.mixin.TileEntityAlloyerAccessor;
import com.beipuo.mekenergistics.mixin.TileEntityChemixerAccessor;
import com.beipuo.mekenergistics.mixin.TileEntityMelterAccessor;
import com.beipuo.mekenergistics.mixin.TileEntitySolidifierAccessor;
import fr.iglee42.evolvedmekanism.tiles.machine.TileEntityAlloyer;
import fr.iglee42.evolvedmekanism.tiles.machine.TileEntityChemixer;
import fr.iglee42.evolvedmekanism.tiles.machine.TileEntityMelter;
import fr.iglee42.evolvedmekanism.tiles.machine.TileEntitySolidifier;
import java.util.List;
import java.util.function.Function;
import mekanism.common.tile.base.TileEntityMekanism;

/** Machine surfaces for the four standalone Evolved Mekanism recipe machines. */
public final class EvolvedRecipeUpgradeProfiles {
    public static MeUpgradeMachineProfile<?> forTile(TileEntityMekanism tile) {
        if (tile instanceof TileEntityAlloyer alloyer) {
            TileEntityAlloyerAccessor accessor = (TileEntityAlloyerAccessor) alloyer;
            return profile(alloyer, MeMekanismMachine.ALLOYER,
                    candidate -> MeInputLayout.lanes(List.of(
                            List.of(MeMachineIoAdapter.itemInput(accessor.mekenergistics$getMainInputSlot())),
                            List.of(MeMachineIoAdapter.itemInput(accessor.mekenergistics$getExtraInputSlot())),
                            List.of(MeMachineIoAdapter.itemInput(accessor.mekenergistics$getSecondExtraInputSlot())))),
                    candidate -> List.of(MeMachineIoAdapter.itemOutput(accessor.mekenergistics$getOutputSlot())));
        }
        if (tile instanceof TileEntitySolidifier solidifier) {
            TileEntitySolidifierAccessor accessor = (TileEntitySolidifierAccessor) solidifier;
            return profile(solidifier, MeMekanismMachine.SOLIDIFICATION_CHAMBER,
                    candidate -> MeInputLayout.lanes(List.of(
                            List.of(MeMachineIoAdapter.itemInput(accessor.mekenergistics$getInputSlot())),
                            List.of(MeMachineIoAdapter.fluidInput(accessor.mekenergistics$getInputFluidTank()),
                                    MeMachineIoAdapter.fluidInput(accessor.mekenergistics$getInputFluidExtraTank())),
                            List.of(MeMachineIoAdapter.fluidInput(accessor.mekenergistics$getInputFluidTank()),
                                    MeMachineIoAdapter.fluidInput(accessor.mekenergistics$getInputFluidExtraTank())))),
                    candidate -> List.of(MeMachineIoAdapter.itemOutput(accessor.mekenergistics$getOutputSlot())));
        }
        if (tile instanceof TileEntityMelter melter) {
            TileEntityMelterAccessor accessor = (TileEntityMelterAccessor) melter;
            return profile(melter, MeMekanismMachine.THERMALIZER,
                    candidate -> MeInputLayout.unordered(List.of(
                            MeMachineIoAdapter.itemInput(accessor.mekenergistics$getInputSlot()))),
                    candidate -> List.of(MeMachineIoAdapter.fluidOutput(accessor.mekenergistics$getFluidTank())));
        }
        if (tile instanceof TileEntityChemixer chemixer) {
            TileEntityChemixerAccessor accessor = (TileEntityChemixerAccessor) chemixer;
            return profile(chemixer, MeMekanismMachine.CHEMIXER,
                    candidate -> MeInputLayout.lanes(List.of(
                            List.of(MeMachineIoAdapter.itemInput(accessor.mekenergistics$getMainInputSlot())),
                            List.of(MeMachineIoAdapter.itemInput(accessor.mekenergistics$getExtraInputSlot())),
                            List.of(MeMachineIoAdapter.chemicalInput(accessor.mekenergistics$getInputChemicalTank())))),
                    candidate -> List.of(MeMachineIoAdapter.itemOutput(accessor.mekenergistics$getOutputSlot())));
        }
        return null;
    }

    private static <TILE extends TileEntityMekanism> MeUpgradeMachineProfile<TILE> profile(
            TILE tile, MeMekanismMachine machine, Function<TILE, MeInputLayout> inputs,
            Function<TILE, List<? extends MeOutputPort>> outputs) {
        return new MeUpgradeMachineProfile<>(candidate -> candidate == tile, inputs, outputs, machine,
                candidate -> new net.minecraft.world.item.ItemStack(candidate.getBlockState().getBlock()),
                candidate -> candidate.getBlockState().getBlock().getName());
    }

    private EvolvedRecipeUpgradeProfiles() {
    }
}
