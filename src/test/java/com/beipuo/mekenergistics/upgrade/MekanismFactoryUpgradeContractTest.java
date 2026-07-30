package com.beipuo.mekenergistics.upgrade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.beipuo.mekenergistics.common.machine.MeMekanismMachine;
import com.beipuo.mekenergistics.compat.catalog.CompatMachineFamily;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import mekanism.common.content.blocktype.FactoryType;
import mekanism.common.tier.FactoryTier;
import org.junit.jupiter.api.Test;

class MekanismFactoryUpgradeContractTest {
    private static final Path FACTORY_MIXIN = Path.of(
            "src/main/java/com/beipuo/mekenergistics/mixin/TileEntityFactoryMeUpgradeMixin.java");
    private static final Path ITEM_ENERGY_MIXIN = Path.of(
            "src/main/java/com/beipuo/mekenergistics/mixin/TileEntityItemFactoryMeUpgradeEnergyMixin.java");
    private static final Path CHEMICAL_ENERGY_MIXIN = Path.of(
            "src/main/java/com/beipuo/mekenergistics/mixin/TileEntityChemicalFactoryMeUpgradeEnergyMixin.java");
    private static final Path FACTORY_AE_MACHINE = Path.of(
            "src/main/java/com/beipuo/mekenergistics/blockentity/api/MeFactoryAeMachine.java");

    @Test
    void everyVanillaTierAndFactoryTypeHasAnUpgradeDescriptor() {
        int covered = 0;
        for (FactoryTier tier : FactoryTier.values()) {
            for (FactoryType type : FactoryType.values()) {
                MeMekanismMachine machine = MeMekanismMachine.getFactory(tier, type);
                assertNotNull(machine, () -> tier + " " + type);
                assertEquals(CompatMachineFamily.MEKANISM_FACTORY, machine.family());
                assertEquals(tier, machine.factoryTier());
                assertEquals(type, machine.factoryType());
                covered++;
            }
        }
        assertEquals(FactoryTier.values().length * FactoryType.values().length, covered);
    }

    @Test
    void factoryMixinRoutesAllParallelInputsOutputsAndSecondaryResources() throws IOException {
        String source = Files.readString(FACTORY_MIXIN);
        assertTrue(source.contains("autoSortedFactoryItemInput(this.inputSlots)"));
        assertTrue(source.contains("this.outputSlots.stream().map(MeMachineIoAdapter::itemOutput)"));
        assertTrue(source.contains("type == FactoryType.COMBINING"));
        assertTrue(source.contains("chemicalInput(chemicalFactory.getChemicalTank())"));
        assertTrue(source.contains("type != FactoryType.INFUSING"));
        assertTrue(source.contains("chemicalOutput(chemicalFactory.getChemicalTank())"));
    }

    @Test
    void activationOwnsNodeRefreshesRecipesAndInvalidatesCapability() throws IOException {
        String source = Files.readString(FACTORY_MIXIN);
        assertTrue(source.contains("transitionTo(active)"));
        assertTrue(source.contains("syncFactoryOwner(tile)"));
        assertTrue(source.contains("refreshRecipeCache()"));
        assertTrue(source.contains("invalidateCapabilities"));
        assertTrue(source.contains("destroyNode()"));
    }

    @Test
    void existingFactoryInterfaceSpecializesTheSharedAeMachineContract() throws IOException {
        String source = Files.readString(FACTORY_AE_MACHINE);

        assertTrue(source.contains("MeFactoryAeMachine extends MeAeMachine"));
        assertTrue(source.contains("getRecipeAeSupport()"));
        assertTrue(source.contains("return getAeSupport();"));
    }

    @Test
    void factoryMixinProvidesConcreteMainNodeBridge() throws IOException {
        String source = Files.readString(FACTORY_AE_MACHINE);

        assertTrue(source.contains("MeFactoryAeMachine extends MeAeMachine"));
        assertTrue(source.contains("return getAeSupport();"));
    }

    @Test
    void bothFactoryRecipeFamiliesUseNetworkEnergyOnlyWhileActive() throws IOException {
        for (Path mixin : new Path[]{ITEM_ENERGY_MIXIN, CHEMICAL_ENERGY_MIXIN}) {
            String source = Files.readString(mixin);
            assertTrue(source.contains("machine.isMeUpgradeTarget() && machine.isMeUpgradeActive()"));
            assertTrue(source.contains("wrapRecipeEnergy(tile.getEnergyContainer()"));
        }
    }

    @Test
    void factoryItemsAndBlocksReceivePatternSlotsAndGridCapability() throws IOException {
        String attachment = Files.readString(Path.of(
                "src/main/java/com/beipuo/mekenergistics/mixin/EnrichmentChamberItemContainerCreatorMixin.java"));
        String capabilities = Files.readString(Path.of(
                "src/main/java/com/beipuo/mekenergistics/registry/ModBlockEntities.java"));
        assertTrue(attachment.contains("isSupportedFactoryBlockItem(stack)"));
        assertTrue(capabilities.contains("CompatMachineFamily.MEKANISM_FACTORY"));
        assertTrue(capabilities.contains("machine.isMeUpgradeActive() ? machine : null"));
    }
}
