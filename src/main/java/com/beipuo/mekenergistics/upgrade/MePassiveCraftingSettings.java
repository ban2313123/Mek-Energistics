package com.beipuo.mekenergistics.upgrade;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

public final class MePassiveCraftingSettings {
    public static final int DEFAULT_INTERVAL_TICKS = 20;
    public static final int MIN_INTERVAL_TICKS = 1;
    public static final int MAX_INTERVAL_TICKS = 72_000;
    public static final long MAX_COPIES_PER_TICK = 64;
    public static final int MAX_PATTERN_SCAN_PER_TICK = 8;
    private static final String TAG_RECOVERY_BUFFER = "PassiveCraftingRecoveryBuffer";
    private static final String TAG_RECOVERY_KEY = "Key";

    private int intervalTicks = DEFAULT_INTERVAL_TICKS;
    private long multiplier = 1;
    private int countdown = DEFAULT_INTERVAL_TICKS;
    private int patternScanCursor;
    private final List<RecoveryEntry> recoveryBuffer = new ArrayList<>();
    private int patternsScanned;
    private int patternsRejected;
    private int patternsRestored;
    private int patternsSubmitted;
    private int bufferFlushes;

    public int intervalTicks() { return intervalTicks; }
    public long multiplier() { return multiplier; }
    public int patternScanCursor() { return patternScanCursor; }
    public int patternScanBudget() { return MAX_PATTERN_SCAN_PER_TICK; }

    public long cappedCopies(long requestedCopies) {
        return Math.max(1, Math.min(requestedCopies, MAX_COPIES_PER_TICK));
    }

    public void set(int requestedIntervalTicks, long requestedMultiplier) {
        intervalTicks = Math.clamp(requestedIntervalTicks, MIN_INTERVAL_TICKS, MAX_INTERVAL_TICKS);
        multiplier = cappedCopies(requestedMultiplier);
        countdown = intervalTicks;
    }

    public boolean tick() {
        if (--countdown > 0) return false;
        countdown = intervalTicks;
        return true;
    }

    public void save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("PassiveCraftingInterval", intervalTicks);
        tag.putLong("PassiveCraftingMultiplier", multiplier);
        tag.putInt("PassiveCraftingScanCursor", patternScanCursor);
        if (this.recoveryBuffer.isEmpty()) {
            tag.remove(TAG_RECOVERY_BUFFER);
            return;
        }
        ListTag buffer = new ListTag();
        for (RecoveryEntry entry : this.recoveryBuffer) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.put(TAG_RECOVERY_KEY, GenericStack.writeTag(registries, new GenericStack(entry.key, entry.amount)));
            buffer.add(entryTag);
        }
        tag.put(TAG_RECOVERY_BUFFER, buffer);
    }

    public void load(CompoundTag tag, HolderLookup.Provider registries) {
        set(tag.contains("PassiveCraftingInterval") ? tag.getInt("PassiveCraftingInterval") : DEFAULT_INTERVAL_TICKS,
                tag.contains("PassiveCraftingMultiplier") ? tag.getLong("PassiveCraftingMultiplier") : 1);
        this.patternScanCursor = tag.contains("PassiveCraftingScanCursor") ? tag.getInt("PassiveCraftingScanCursor") : 0;
        this.recoveryBuffer.clear();
        ListTag buffer = tag.getList(TAG_RECOVERY_BUFFER, CompoundTag.TAG_COMPOUND);
        for (int i = 0; i < buffer.size(); i++) {
            try {
                GenericStack stack = GenericStack.readTag(registries, buffer.getCompound(i).getCompound(TAG_RECOVERY_KEY));
                if (stack != null && stack.amount() > 0) {
                    this.recoveryBuffer.add(new RecoveryEntry(stack.what(), stack.amount()));
                }
            } catch (RuntimeException ignored) {
                // A corrupt recovery entry must never fail the machine load; skip it.
            }
        }
    }

    public void bufferRemainder(AEKey key, long amount) {
        if (key == null || amount <= 0) {
            return;
        }
        for (int i = 0; i < this.recoveryBuffer.size(); i++) {
            RecoveryEntry entry = this.recoveryBuffer.get(i);
            if (entry.key.equals(key)) {
                long merged = entry.amount > Long.MAX_VALUE - amount ? Long.MAX_VALUE : entry.amount + amount;
                this.recoveryBuffer.set(i, new RecoveryEntry(key, merged));
                return;
            }
        }
        this.recoveryBuffer.add(new RecoveryEntry(key, amount));
    }

    public List<RecoveryEntry> drainRecoveryBuffer() {
        List<RecoveryEntry> drained = List.copyOf(this.recoveryBuffer);
        this.recoveryBuffer.clear();
        return drained;
    }

    public boolean hasRecoverableRemainders() {
        return !this.recoveryBuffer.isEmpty();
    }

    public int recoveryBufferSize() {
        return this.recoveryBuffer.size();
    }

    public void beginRun() {
        this.patternsScanned = 0;
        this.patternsRejected = 0;
        this.patternsRestored = 0;
        this.patternsSubmitted = 0;
        this.bufferFlushes = 0;
    }

    public void recordPatternScanned() { this.patternsScanned++; }
    public void recordRejected() { this.patternsRejected++; }
    public void recordRestored() { this.patternsRestored++; }
    public void recordSubmitted() { this.patternsSubmitted++; }
    public void recordBufferFlush() { this.bufferFlushes++; }

    public int patternsScanned() { return this.patternsScanned; }
    public int patternsRejected() { return this.patternsRejected; }
    public int patternsRestored() { return this.patternsRestored; }
    public int patternsSubmitted() { return this.patternsSubmitted; }
    public int bufferFlushes() { return this.bufferFlushes; }

    public void setPatternScanCursor(int cursor) {
        this.patternScanCursor = Math.max(0, cursor);
    }

    public record RecoveryEntry(AEKey key, long amount) {
        public RecoveryEntry {
            if (amount <= 0) {
                throw new IllegalArgumentException("Recovery amounts must be positive");
            }
        }
    }
}
