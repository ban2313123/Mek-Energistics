package com.beipuo.mekenergistics.upgrade;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.GenericStack;
import appeng.util.ConfigInventory;
import java.util.List;
import java.util.Objects;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

/**
 * Virtual nine-slot item configuration for the ME output interface upgrade. Each column holds an
 * item key and the amount that should be stocked in the matching interface inventory slot.
 */
public final class MeInterfaceConfig {
    public static final int SLOT_COUNT = 9;
    private static final String TAG_CONFIG = "MeInterfaceConfig";

    private final ConfigInventory inventory;

    public MeInterfaceConfig(Runnable onChange) {
        this.inventory = ConfigInventory.configStacks(SLOT_COUNT)
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
        return slot < 0 || slot >= this.inventory.size() ? null : this.inventory.getStack(slot);
    }

    public void setStack(int slot, @Nullable GenericStack stack) {
        if (slot < 0 || slot >= this.inventory.size()) {
            return;
        }
        this.inventory.setStack(slot, normalize(stack));
    }

    /** Clears non-item keys and non-positive amounts. Overstacked item amounts are kept. */
    @Nullable
    public static GenericStack normalize(@Nullable GenericStack stack) {
        return stack == null ? null : normalize(stack.what(), stack.amount());
    }

    @Nullable
    public static GenericStack normalize(@Nullable AEKey key, long amount) {
        if (!(key instanceof AEItemKey) || amount <= 0) {
            return null;
        }
        return new GenericStack(key, amount);
    }

    public List<GenericStack> toList() {
        return this.inventory.toList();
    }

    public void readFromList(List<GenericStack> stacks) {
        this.inventory.readFromList(stacks);
    }

    /** True when at least one slot is configured with a positive amount. */
    public boolean hasConfiguredSlots() {
        for (int i = 0; i < this.inventory.size(); i++) {
            GenericStack stack = this.inventory.getStack(i);
            if (stack != null && stack.what() != null && stack.amount() > 0 && stack.what() instanceof AEItemKey) {
                return true;
            }
        }
        return false;
    }

    public void save(CompoundTag tag, HolderLookup.Provider registries) {
        this.inventory.writeToChildTag(tag, TAG_CONFIG, registries);
    }

    public void load(CompoundTag tag, HolderLookup.Provider registries) {
        if (tag.contains(TAG_CONFIG)) {
            // ConfigInventory reads only as many entries as it owns. This preserves the first nine
            // columns from the short-lived 36-slot format without needing a separate data fixer.
            this.inventory.readFromChildTag(tag, TAG_CONFIG, registries);
        }
    }
}
