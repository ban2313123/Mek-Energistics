package com.example.mekanismmagic.api;

import java.util.List;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.inventory.IInventorySlot;
import net.minecraft.resources.ResourceLocation;

/**
 * Test-time stand-in for Mekanism Magic's published automation surface. Method names and shapes must
 * stay aligned with the real interface so {@code MekanismMagicAutomationAccess} exercises the same
 * reflective entry points it uses against the shipped Magic jar.
 */
public interface IMekanismMagicAutomation {
    int API_VERSION = 1;

    default int mekanismMagicAutomationApiVersion() {
        return API_VERSION;
    }

    ResourceLocation mekanismMagicMachineId();

    List<IInventorySlot> mekanismMagicPatternInputs();

    List<IInventorySlot> mekanismMagicPatternOutputs();

    default List<IInventorySlot> mekanismMagicPersistentInputs() {
        return List.of();
    }

    default List<IInventorySlot> mekanismMagicManualOnlySlots() {
        return List.of();
    }

    IEnergyContainer mekanismMagicEnergyContainer();

    default boolean mekanismMagicSupportsPatternAutomation() {
        return true;
    }

    default boolean mekanismMagicIsBusy() {
        return false;
    }
}
