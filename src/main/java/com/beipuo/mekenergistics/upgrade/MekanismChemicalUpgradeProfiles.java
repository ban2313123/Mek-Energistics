package com.beipuo.mekenergistics.upgrade;

import com.beipuo.mekenergistics.blockentity.support.io.MeInputLayout;
import com.beipuo.mekenergistics.blockentity.support.io.MeMachineIoAdapter;
import com.beipuo.mekenergistics.blockentity.support.io.MeOutputPort;
import com.beipuo.mekenergistics.common.machine.MeMekanismMachine;
import com.beipuo.mekenergistics.compat.catalog.CompatMachineCatalog;
import com.beipuo.mekenergistics.mixin.TileEntityAntiprotonicNucleosynthesizerAccessor;
import com.beipuo.mekenergistics.mixin.TileEntityChemicalCrystallizerAccessor;
import com.beipuo.mekenergistics.mixin.TileEntityChemicalDissolutionChamberAccessor;
import com.beipuo.mekenergistics.mixin.TileEntityChemicalOxidizerAccessor;
import com.beipuo.mekenergistics.mixin.TileEntityNutritionalLiquifierAccessor;
import com.beipuo.mekenergistics.mixin.TileEntityPressurizedReactionChamberAccessor;
import com.beipuo.mekenergistics.mixin.TileEntityPaintingMachineAccessor;
import java.util.List;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.machine.TileEntityAntiprotonicNucleosynthesizer;
import mekanism.common.tile.machine.TileEntityChemicalCrystallizer;
import mekanism.common.tile.machine.TileEntityChemicalDissolutionChamber;
import mekanism.common.tile.machine.TileEntityChemicalInfuser;
import mekanism.common.tile.machine.TileEntityChemicalOxidizer;
import mekanism.common.tile.machine.TileEntityChemicalWasher;
import mekanism.common.tile.machine.TileEntityElectrolyticSeparator;
import mekanism.common.tile.machine.TileEntityIsotopicCentrifuge;
import mekanism.common.tile.machine.TileEntityNutritionalLiquifier;
import mekanism.common.tile.machine.TileEntityPigmentExtractor;
import mekanism.common.tile.machine.TileEntityPigmentMixer;
import mekanism.common.tile.machine.TileEntityPaintingMachine;
import mekanism.common.tile.machine.TileEntityPressurizedReactionChamber;
import mekanism.common.tile.machine.TileEntityRotaryCondensentrator;
import mekanism.common.tile.machine.TileEntitySolarNeutronActivator;
import net.minecraft.core.registries.BuiltInRegistries;

/** I/O profiles for Mekanism's standalone chemical and fluid recipe machines. */
public final class MekanismChemicalUpgradeProfiles {
    public static MeUpgradeMachineProfile<?> forTile(TileEntityMekanism tile) {
        var id = BuiltInRegistries.BLOCK.getKey(tile.getBlockState().getBlock());
        MeMekanismMachine machine = CompatMachineCatalog.findBySourceBlockId(id)
                .map(spec -> supported(spec.machine()) ? spec.machine() : null).orElse(null);
        if (machine == null) return null;
        return new MeUpgradeMachineProfile<>(candidate -> candidate == tile,
                candidate -> inputs(tile, machine), candidate -> outputs(tile, machine), machine,
                candidate -> new net.minecraft.world.item.ItemStack(candidate.getBlockState().getBlock()),
                candidate -> candidate.getBlockState().getBlock().getName());
    }

    private static boolean supported(MeMekanismMachine machine) {
        return switch (machine) {
            case ANTIPROTONIC_NUCLEOSYNTHESIZER, CHEMICAL_CRYSTALLIZER,
                    CHEMICAL_DISSOLUTION_CHAMBER, CHEMICAL_INFUSER, CHEMICAL_OXIDIZER,
                    CHEMICAL_WASHER, ELECTROLYTIC_SEPARATOR, ISOTOPIC_CENTRIFUGE,
                    NUTRITIONAL_LIQUIFIER, PAINTING_MACHINE, PIGMENT_EXTRACTOR, PIGMENT_MIXER,
                    PRESSURIZED_REACTION_CHAMBER, ROTARY_CONDENSENTRATOR,
                    SOLAR_NEUTRON_ACTIVATOR -> true;
            default -> false;
        };
    }

    private static MeInputLayout inputs(TileEntityMekanism tile, MeMekanismMachine machine) {
        return switch (machine) {
            case ANTIPROTONIC_NUCLEOSYNTHESIZER -> {
                TileEntityAntiprotonicNucleosynthesizer value = (TileEntityAntiprotonicNucleosynthesizer) tile;
                TileEntityAntiprotonicNucleosynthesizerAccessor accessor =
                        (TileEntityAntiprotonicNucleosynthesizerAccessor) value;
                yield MeInputLayout.unordered(List.of(
                        MeMachineIoAdapter.itemInput(accessor.mekenergistics$getInputSlot()),
                        MeMachineIoAdapter.chemicalInput(value.gasTank),
                        MeMachineIoAdapter.itemInput(accessor.mekenergistics$getGasInputSlot())));
            }
            case CHEMICAL_CRYSTALLIZER -> MeInputLayout.unordered(List.of(MeMachineIoAdapter.chemicalInput(
                    ((TileEntityChemicalCrystallizer) tile).inputTank)));
            case CHEMICAL_DISSOLUTION_CHAMBER -> {
                TileEntityChemicalDissolutionChamber value = (TileEntityChemicalDissolutionChamber) tile;
                TileEntityChemicalDissolutionChamberAccessor accessor =
                        (TileEntityChemicalDissolutionChamberAccessor) value;
                yield MeInputLayout.unordered(List.of(
                        MeMachineIoAdapter.itemInput(accessor.mekenergistics$getInputSlot()),
                        MeMachineIoAdapter.chemicalInput(value.injectTank),
                        MeMachineIoAdapter.itemInput(accessor.mekenergistics$getGasInputSlot())));
            }
            case CHEMICAL_INFUSER -> {
                TileEntityChemicalInfuser value = (TileEntityChemicalInfuser) tile;
                yield MeInputLayout.unordered(List.of(MeMachineIoAdapter.chemicalInput(value.leftTank),
                        MeMachineIoAdapter.chemicalInput(value.rightTank)));
            }
            case CHEMICAL_OXIDIZER -> MeInputLayout.unordered(List.of(MeMachineIoAdapter.itemInput(
                    ((TileEntityChemicalOxidizerAccessor) tile).mekenergistics$getInputSlot())));
            case CHEMICAL_WASHER -> {
                TileEntityChemicalWasher value = (TileEntityChemicalWasher) tile;
                yield MeInputLayout.unordered(List.of(MeMachineIoAdapter.fluidInput(value.fluidTank),
                        MeMachineIoAdapter.chemicalInput(value.inputTank)));
            }
            case ELECTROLYTIC_SEPARATOR -> MeInputLayout.unordered(List.of(MeMachineIoAdapter.fluidInput(
                    ((TileEntityElectrolyticSeparator) tile).fluidTank)));
            case ISOTOPIC_CENTRIFUGE -> MeInputLayout.unordered(List.of(MeMachineIoAdapter.chemicalInput(
                    ((TileEntityIsotopicCentrifuge) tile).inputTank)));
            case NUTRITIONAL_LIQUIFIER -> MeInputLayout.unordered(List.of(MeMachineIoAdapter.itemInput(
                    ((TileEntityNutritionalLiquifierAccessor) tile).mekenergistics$getInputSlot())));
            case PAINTING_MACHINE -> {
                TileEntityPaintingMachine value = (TileEntityPaintingMachine) tile;
                yield MeInputLayout.unordered(List.of(MeMachineIoAdapter.itemInput(
                                ((TileEntityPaintingMachineAccessor) value).mekenergistics$getInputSlot()),
                        MeMachineIoAdapter.chemicalInput(value.pigmentTank)));
            }
            case PIGMENT_EXTRACTOR -> MeInputLayout.unordered(List.of(MeMachineIoAdapter.itemInput(
                    ((TileEntityPigmentExtractor) tile).getInputSlot())));
            case PIGMENT_MIXER -> {
                TileEntityPigmentMixer value = (TileEntityPigmentMixer) tile;
                yield MeInputLayout.unordered(List.of(MeMachineIoAdapter.chemicalInput(value.leftInputTank),
                        MeMachineIoAdapter.chemicalInput(value.rightInputTank)));
            }
            case PRESSURIZED_REACTION_CHAMBER -> {
                TileEntityPressurizedReactionChamber value = (TileEntityPressurizedReactionChamber) tile;
                yield MeInputLayout.unordered(List.of(MeMachineIoAdapter.itemInput(
                                ((TileEntityPressurizedReactionChamberAccessor) value).mekenergistics$getInputSlot()),
                        MeMachineIoAdapter.fluidInput(value.inputFluidTank),
                        MeMachineIoAdapter.chemicalInput(value.inputGasTank)));
            }
            case ROTARY_CONDENSENTRATOR -> {
                TileEntityRotaryCondensentrator value = (TileEntityRotaryCondensentrator) tile;
                yield MeInputLayout.unordered(List.of(value.getMode()
                        ? MeMachineIoAdapter.fluidInput(value.fluidTank)
                        : MeMachineIoAdapter.chemicalInput(value.gasTank)));
            }
            case SOLAR_NEUTRON_ACTIVATOR -> MeInputLayout.unordered(List.of(MeMachineIoAdapter.chemicalInput(
                    ((TileEntitySolarNeutronActivator) tile).inputTank)));
            default -> MeInputLayout.empty();
        };
    }

    private static List<? extends MeOutputPort> outputs(TileEntityMekanism tile, MeMekanismMachine machine) {
        return switch (machine) {
            case ANTIPROTONIC_NUCLEOSYNTHESIZER -> List.of(MeMachineIoAdapter.itemOutput(
                    ((TileEntityAntiprotonicNucleosynthesizerAccessor) tile).mekenergistics$getOutputSlot()));
            case CHEMICAL_CRYSTALLIZER -> List.of(MeMachineIoAdapter.itemOutput(
                    ((TileEntityChemicalCrystallizerAccessor) tile).mekenergistics$getOutputSlot()));
            case CHEMICAL_DISSOLUTION_CHAMBER -> List.of(MeMachineIoAdapter.chemicalOutput(
                    ((TileEntityChemicalDissolutionChamber) tile).outputTank));
            case CHEMICAL_INFUSER -> List.of(MeMachineIoAdapter.chemicalOutput(
                    ((TileEntityChemicalInfuser) tile).centerTank));
            case CHEMICAL_OXIDIZER -> List.of(MeMachineIoAdapter.chemicalOutput(
                    ((TileEntityChemicalOxidizer) tile).gasTank));
            case CHEMICAL_WASHER -> List.of(MeMachineIoAdapter.chemicalOutput(
                    ((TileEntityChemicalWasher) tile).outputTank));
            case ELECTROLYTIC_SEPARATOR -> {
                TileEntityElectrolyticSeparator value = (TileEntityElectrolyticSeparator) tile;
                yield List.of(MeMachineIoAdapter.chemicalOutput(value.leftTank),
                        MeMachineIoAdapter.chemicalOutput(value.rightTank));
            }
            case ISOTOPIC_CENTRIFUGE -> List.of(MeMachineIoAdapter.chemicalOutput(
                    ((TileEntityIsotopicCentrifuge) tile).outputTank));
            case NUTRITIONAL_LIQUIFIER -> {
                TileEntityNutritionalLiquifier value = (TileEntityNutritionalLiquifier) tile;
                yield List.of(MeMachineIoAdapter.itemOutput(
                                ((TileEntityNutritionalLiquifierAccessor) value).mekenergistics$getOutputSlot()),
                        MeMachineIoAdapter.fluidOutput(value.fluidTank));
            }
            case PAINTING_MACHINE -> List.of(MeMachineIoAdapter.itemOutput(
                    ((TileEntityPaintingMachineAccessor) tile).mekenergistics$getOutputSlot()));
            case PIGMENT_EXTRACTOR -> List.of(MeMachineIoAdapter.chemicalOutput(
                    ((TileEntityPigmentExtractor) tile).pigmentTank));
            case PIGMENT_MIXER -> List.of(MeMachineIoAdapter.chemicalOutput(
                    ((TileEntityPigmentMixer) tile).outputTank));
            case PRESSURIZED_REACTION_CHAMBER -> {
                TileEntityPressurizedReactionChamber value = (TileEntityPressurizedReactionChamber) tile;
                yield List.of(MeMachineIoAdapter.itemOutput(
                                ((TileEntityPressurizedReactionChamberAccessor) value).mekenergistics$getOutputSlot()),
                        MeMachineIoAdapter.chemicalOutput(value.outputGasTank));
            }
            case ROTARY_CONDENSENTRATOR -> {
                TileEntityRotaryCondensentrator value = (TileEntityRotaryCondensentrator) tile;
                yield List.of(value.getMode() ? MeMachineIoAdapter.chemicalOutput(value.gasTank)
                        : MeMachineIoAdapter.fluidOutput(value.fluidTank));
            }
            case SOLAR_NEUTRON_ACTIVATOR -> List.of(MeMachineIoAdapter.chemicalOutput(
                    ((TileEntitySolarNeutronActivator) tile).outputTank));
            default -> List.of();
        };
    }

    private MekanismChemicalUpgradeProfiles() {
    }
}
