package com.beipuo.mekenergistics.blockentity.compat.mekmm.machine;

import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.security.IActionHost;
import com.beipuo.mekenergistics.blockentity.api.AeOutputMode;
import com.beipuo.mekenergistics.blockentity.api.MeAeMachine;
import com.beipuo.mekenergistics.blockentity.support.MeRecipeMachineAeSupport;
import com.beipuo.mekenergistics.blockentity.support.io.MeInputLayout;
import com.beipuo.mekenergistics.blockentity.support.io.MeMachineIoAdapter;
import com.beipuo.mekenergistics.blockentity.support.io.MeOutputPort;
import com.beipuo.mekenergistics.common.machine.MeMekanismMachine;
import java.util.ArrayList;
import java.util.List;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.inventory.slot.chemical.ChemicalInventorySlot;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

final class MeMekmmItemChemicalMachineSupport<TILE extends TileEntityMekanism & MeAeMachine & ICraftingProvider & IActionHost> {
    private final TILE owner;
    private final MeMekanismMachine machine;
    private MeRecipeMachineAeSupport<TILE> aeSupport;
    private AeOutputMode aeOutputMode = AeOutputMode.BOTH;
    private InputInventorySlot inputSlot;
    private OutputInventorySlot outputSlot;
    private OutputInventorySlot secondaryOutputSlot;
    private IChemicalTank chemicalTank;
    private IInventorySlot conversionSlot;

    MeMekmmItemChemicalMachineSupport(TILE owner, MeMekanismMachine machine) {
        this.owner = owner;
        this.machine = machine;
    }

    IInventorySlotHolder withPatternSlots(IInventorySlotHolder original) {
        captureSlots(original);
        MeRecipeMachineAeSupport<TILE> support = aeSupport();
        return side -> {
            List<IInventorySlot> slots = new ArrayList<>(original.getInventorySlots(side));
            slots.addAll(support.getPatternSlots());
            return slots;
        };
    }

    MeRecipeMachineAeSupport<TILE> aeSupport() {
        if (this.aeSupport == null) {
            this.aeSupport = new MeRecipeMachineAeSupport<>(this.owner);
        }
        return this.aeSupport;
    }

    IChemicalTankHolder captureChemicalTank(IChemicalTankHolder original) {
        List<IChemicalTank> tanks = original.getTanks(null);
        if (!tanks.isEmpty()) {
            this.chemicalTank = tanks.getFirst();
        }
        return original;
    }

    private void captureSlots(IInventorySlotHolder original) {
        for (IInventorySlot slot : original.getInventorySlots(null)) {
            if (slot instanceof ChemicalInventorySlot conversion && this.conversionSlot == null) {
                this.conversionSlot = conversion;
            } else if (slot instanceof InputInventorySlot input && this.inputSlot == null) {
                this.inputSlot = input;
            } else if (slot instanceof OutputInventorySlot output) {
                if (this.outputSlot == null) {
                    this.outputSlot = output;
                } else if (this.secondaryOutputSlot == null) {
                    this.secondaryOutputSlot = output;
                }
            }
        }
    }

    boolean processPatternIo(boolean sendUpdatePacket) {
        return aeSupport().processPatternIo(this.aeOutputMode, sendUpdatePacket);
    }

    MeInputLayout inputLayout() {
        if (this.inputSlot == null || this.chemicalTank == null || this.conversionSlot == null) {
            return MeInputLayout.empty();
        }
        return MeInputLayout.unordered(List.of(
                MeMachineIoAdapter.itemInput(this.inputSlot),
                MeMachineIoAdapter.chemicalInput(this.chemicalTank),
                MeMachineIoAdapter.itemInput(this.conversionSlot)));
    }

    List<? extends MeOutputPort> outputPorts() {
        List<MeOutputPort> outputs = new ArrayList<>(2);
        if (this.outputSlot != null) outputs.add(MeMachineIoAdapter.itemOutput(this.outputSlot));
        if (this.secondaryOutputSlot != null) outputs.add(MeMachineIoAdapter.itemOutput(this.secondaryOutputSlot));
        return List.copyOf(outputs);
    }

    public MeMekanismMachine getMachine() { return this.machine; }
    public AeOutputMode getAeOutputMode() { return this.aeOutputMode; }
    public void cycleAeOutputMode() { this.aeOutputMode = this.aeOutputMode.next(); this.owner.setChanged(); }
    public <RECIPE extends MekanismRecipe<?>> CachedRecipe<RECIPE> wrapRecipeEnergy(MachineEnergyContainer<?> energyContainer, CachedRecipe<RECIPE> cachedRecipe) { return aeSupport().wrapRecipeEnergy(energyContainer, cachedRecipe); }
    void clearRemoved() { aeSupport().createOnFirstTick(); }
    void setRemoved() { aeSupport().destroyNode(); }
    void onChunkUnloaded() { aeSupport().destroyNode(); }
    void addContainerTrackers(MekanismContainer container) { aeSupport().addAeTrackers(container, this::getAeOutputMode, mode -> this.aeOutputMode = mode, false); }
    void saveAdditional(CompoundTag tag, HolderLookup.@NotNull Provider registries) { aeSupport().saveAeState(tag, registries, this.aeOutputMode); }
    void loadAdditional(CompoundTag tag, HolderLookup.@NotNull Provider registries) { this.aeOutputMode = aeSupport().loadAeState(tag, registries); }
}
