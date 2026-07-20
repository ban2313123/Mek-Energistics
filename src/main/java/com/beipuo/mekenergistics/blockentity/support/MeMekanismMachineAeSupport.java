package com.beipuo.mekenergistics.blockentity.support;

import com.beipuo.mekenergistics.blockentity.MeMekanismMachineBlockEntity;
import com.beipuo.mekenergistics.blockentity.api.AeOutputMode;
import mekanism.common.inventory.slot.BasicInventorySlot;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

/** Shared AE support for the native configurable Mekanism machine family. */
public final class MeMekanismMachineAeSupport extends AbstractMeAeSupport<MeMekanismMachineBlockEntity> {
    public MeMekanismMachineAeSupport(MeMekanismMachineBlockEntity owner) {
        super(owner);
    }

    public boolean hasSmartPatternWork() {
        return this.smartPatternMultiplication.hasPendingWork();
    }

    public boolean processSmartPatternWork() {
        return processSmartPatternViaAdapter();
    }

    public void saveAeState(CompoundTag tag, HolderLookup.Provider registries, AeOutputMode mode) {
        tag.putInt("AeOutputMode", mode.ordinal());
        saveCommon(tag, registries);
    }

    public AeOutputMode loadAeState(CompoundTag tag, HolderLookup.Provider registries) {
        loadCommon(tag, registries);
        return AeOutputMode.byId(tag.getInt("AeOutputMode"));
    }

    @Override
    protected String patternOwnerName() {
        return this.owner.getMachine().name();
    }

    @Override
    protected boolean hasAeOutputWork() {
        return this.owner.hasAeOutputWork();
    }

    @Override
    protected boolean processAeOutputWork() {
        return this.owner.processAeOutputWork();
    }
}
