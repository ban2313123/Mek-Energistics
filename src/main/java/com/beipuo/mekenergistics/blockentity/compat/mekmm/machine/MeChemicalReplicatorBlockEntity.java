package com.beipuo.mekenergistics.blockentity.compat.mekmm.machine;

import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.security.IActionHost;
import com.beipuo.mekenergistics.blockentity.api.AeOutputMode;
import com.beipuo.mekenergistics.blockentity.api.MeAeMachine;
import com.beipuo.mekenergistics.blockentity.api.MeSmartCableConnection;
import com.beipuo.mekenergistics.blockentity.support.MeRecipeMachineAeSupport;
import com.beipuo.mekenergistics.blockentity.support.io.MeInputPort;
import com.beipuo.mekenergistics.blockentity.support.io.MeMachineIoAdapter;
import com.beipuo.mekenergistics.blockentity.support.io.MeInputLayout;
import com.beipuo.mekenergistics.blockentity.support.io.MeOutputPort;
import com.beipuo.mekenergistics.common.machine.MeMekanismMachine;
import com.beipuo.mekenergistics.mixin.TileEntityChemicalReplicatorAccessor;
import com.jerry.mekmm.common.tile.machine.TileEntityChemicalReplicator;
import java.util.ArrayList;
import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.slot.chemical.ChemicalInventorySlot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class MeChemicalReplicatorBlockEntity extends TileEntityChemicalReplicator implements ICraftingProvider, MeSmartCableConnection, IActionHost, MeAeMachine {
    private MeRecipeMachineAeSupport<MeChemicalReplicatorBlockEntity> aeSupport;
    private List<IChemicalTank> tanks = List.of();
    private List<IInventorySlot> conversionSlots = List.of();
    private AeOutputMode outputMode = AeOutputMode.BOTH;

    public MeChemicalReplicatorBlockEntity(MeMekanismMachine machine, BlockPos pos, BlockState state) { super(pos, state); }
    private MeRecipeMachineAeSupport<MeChemicalReplicatorBlockEntity> support() { if (aeSupport == null) aeSupport = new MeRecipeMachineAeSupport<>(this); return aeSupport; }
    @Override public MeRecipeMachineAeSupport<MeChemicalReplicatorBlockEntity> getRecipeAeSupport() { return support(); }
    @Override public IChemicalTankHolder getInitialChemicalTanks(IContentsListener l, IContentsListener c, IContentsListener u) { IChemicalTankHolder h = super.getInitialChemicalTanks(l,c,u); tanks = new ArrayList<>(h.getTanks(null)); return h; }
    @Override protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener l, IContentsListener c, IContentsListener u) {
        EnergyContainerHelper builder = EnergyContainerHelper.forSideWithConfig(this);
        var energy = new MeRecipeMachineAeSupport.AeBackedEnergyContainer<TileEntityChemicalReplicator>(this, support(), u);
        ((TileEntityChemicalReplicatorAccessor) this).mekenergistics$setEnergyContainer(energy);
        builder.addContainer(energy);
        return builder.build();
    }
    @Override protected IInventorySlotHolder getInitialInventory(IContentsListener l, IContentsListener c, IContentsListener u) {
        IInventorySlotHolder original = super.getInitialInventory(l, c, u);
        conversionSlots = original.getInventorySlots(null).stream()
                .filter(ChemicalInventorySlot.class::isInstance)
                .toList();
        return support().withPatternSlots(original);
    }
    @Override protected boolean onUpdateServer() { return support().processPatternIo(outputMode, super.onUpdateServer()); }
    @Override public mekanism.api.recipes.cache.CachedRecipe<com.jerry.mekmm.api.recipes.basic.MMBasicChemicalChemicalToChemicalRecipe> createNewCachedRecipe(@NotNull com.jerry.mekmm.api.recipes.basic.MMBasicChemicalChemicalToChemicalRecipe r, int i) { return support().wrapRecipeEnergy(getEnergyContainer(), super.createNewCachedRecipe(r,i)); }
    @Override public MeInputLayout getPatternInputLayout() {
        if (tanks.size() < 3 || conversionSlots.size() < 2) return MeInputLayout.empty();
        List<MeInputPort> first = List.of(MeMachineIoAdapter.chemicalInput(tanks.get(0)),
                MeMachineIoAdapter.itemInput(conversionSlots.get(0)));
        List<MeInputPort> second = List.of(MeMachineIoAdapter.chemicalInput(tanks.get(1)),
                MeMachineIoAdapter.itemInput(conversionSlots.get(1)));
        return MeInputLayout.lanes(List.of(first, second));
    }
    @Override public List<? extends MeOutputPort> getPatternOutputPorts() { return tanks.size() > 2
            ? List.of(MeMachineIoAdapter.chemicalOutput(tanks.get(2))) : List.of(); }
    @Override public MeMekanismMachine getMachine() { return MeMekanismMachine.CHEMICAL_REPLICATOR; }
    @Override public appeng.api.networking.IManagedGridNode getMainNode() { return support().getMainNode(); }
    @Override public AeOutputMode getAeOutputMode() { return outputMode; }
    @Override public void cycleAeOutputMode() { outputMode = outputMode.next(); setChanged(); }
    @Override public void clearRemoved() { super.clearRemoved(); support().createOnFirstTick(); }
    @Override public void setRemoved() { support().destroyNode(); super.setRemoved(); }
    @Override public void onChunkUnloaded() { support().destroyNode(); super.onChunkUnloaded(); }
    @Override public void addContainerTrackers(MekanismContainer c) { super.addContainerTrackers(c); support().addAeTrackers(c, this::getAeOutputMode, m -> outputMode = m, false); }
    @Override public void saveAdditional(CompoundTag t, HolderLookup.@NotNull Provider r) { super.saveAdditional(t,r); support().saveAeState(t,r,outputMode); }
    @Override public void loadAdditional(CompoundTag t, HolderLookup.@NotNull Provider r) { super.loadAdditional(t,r); outputMode = support().loadAeState(t,r); }
}
