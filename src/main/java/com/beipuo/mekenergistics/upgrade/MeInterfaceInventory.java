package com.beipuo.mekenergistics.upgrade;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.GenericStack;
import appeng.util.ConfigInventory;
import java.util.List;
import java.util.Objects;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

/** Persistent nine-slot buffer between ME storage and the machine input layout. */
public final class MeInterfaceInventory {
    private static final String TAG_INVENTORY = "MeInterfaceInventory";

    private final ConfigInventory inventory;

    public MeInterfaceInventory(Runnable onChange) {
        this.inventory = ConfigInventory.storage(MeInterfaceConfig.SLOT_COUNT)
                .supportedType(AEKeyType.items())
                .allowOverstacking(true)
                .changeListener(Objects.requireNonNull(onChange, "onChange"))
                .build();
    }

    public int size() {
        return this.inventory.size();
    }

    @Nullable
    public GenericStack getStack(int slot) {
        return slot < 0 || slot >= size() ? null : this.inventory.getStack(slot);
    }

    public long insert(int slot, AEKey key, long amount, Actionable mode) {
        return slot < 0 || slot >= size() ? 0 : this.inventory.insert(slot, key, amount, mode);
    }

    public long extract(int slot, AEKey key, long amount, Actionable mode) {
        return slot < 0 || slot >= size() ? 0 : this.inventory.extract(slot, key, amount, mode);
    }

    public List<GenericStack> toList() {
        return this.inventory.toList();
    }

    public boolean isEmpty() {
        for (int i = 0; i < size(); i++) {
            if (this.inventory.getStack(i) != null) {
                return false;
            }
        }
        return true;
    }

    public void save(CompoundTag tag, HolderLookup.Provider registries) {
        this.inventory.writeToChildTag(tag, TAG_INVENTORY, registries);
    }

    public void load(CompoundTag tag, HolderLookup.Provider registries) {
        this.inventory.readFromChildTag(tag, TAG_INVENTORY, registries);
    }
}
