package com.beipuo.mekenergistics.upgrade;

import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import com.beipuo.mekenergistics.MekEnergistics;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.world.level.Level;

public final class MePassiveCraftingDispatcher {
    private MePassiveCraftingDispatcher() {
    }

    public static boolean submitAvailable(List<IPatternDetails> patterns, long copies, Level level,
            MEStorage storage, IActionSource source, Predicate<KeyCounter[]> submitter,
            MePassiveCraftingSettings settings) {
        settings.beginRun();
        boolean submitted = flushRecoveryBuffer(settings, storage, source);
        int size = patterns.size();
        int cursor = settings.patternScanCursor();
        if (cursor < 0 || cursor >= size) {
            cursor = 0;
        }
        settings.setPatternScanCursor(cursor);
        int budget = settings.patternScanBudget();
        long requested = settings.cappedCopies(copies);
        int cursorSteps = 0;
        for (int scanned = 0; scanned < budget && cursorSteps < size; scanned++) {
            cursorSteps++;
            IPatternDetails pattern = patterns.get(cursor);
            settings.recordPatternScanned();
            KeyCounter[] inputs = plan(pattern, requested, level, storage, source);
            if (inputs == null) {
                settings.recordRejected();
                cursor = advance(cursor, size);
                continue;
            }
            KeyCounter extracted = extract(inputs, storage, source, settings);
            if (extracted == null) {
                settings.recordRestored();
                cursor = advance(cursor, size);
                continue;
            }
            if (submitter.test(inputs)) {
                submitted = true;
                settings.recordSubmitted();
                cursor = advance(cursor, size);
                continue;
            }
            restoreRemainder(extracted, storage, source, settings);
            settings.recordRestored();
            cursor = advance(cursor, size);
        }
        settings.setPatternScanCursor(cursor);
        logRunMetrics(settings);
        return submitted;
    }

    private static int advance(int cursor, int size) {
        return size <= 0 ? 0 : (cursor + 1) % size;
    }

    /**
     * Returns the extracted inputs to the network. Anything the network cannot accept right now is
     * persisted into the durable recovery buffer instead of being silently dropped.
     */
    private static void restoreRemainder(KeyCounter extracted, MEStorage storage, IActionSource source,
            MePassiveCraftingSettings settings) {
        for (var entry : extracted) {
            long amount = entry.getLongValue();
            if (amount <= 0) {
                continue;
            }
            long inserted = storage.insert(entry.getKey(), amount, Actionable.MODULATE, source);
            long remainder = amount - inserted;
            if (remainder > 0) {
                settings.bufferRemainder(entry.getKey(), remainder);
            }
        }
    }

    /** Retries the durable recovery buffer first; returns true when any buffered remainder was returned. */
    private static boolean flushRecoveryBuffer(MePassiveCraftingSettings settings, MEStorage storage,
            IActionSource source) {
        boolean changed = false;
        for (MePassiveCraftingSettings.RecoveryEntry entry : settings.drainRecoveryBuffer()) {
            long amount = entry.amount();
            if (amount <= 0) {
                continue;
            }
            long inserted = storage.insert(entry.key(), amount, Actionable.MODULATE, source);
            if (inserted >= amount) {
                changed = true;
                settings.recordBufferFlush();
            } else {
                settings.bufferRemainder(entry.key(), amount - inserted);
            }
        }
        return changed;
    }

    private static void logRunMetrics(MePassiveCraftingSettings settings) {
        MekEnergistics.LOGGER.debug(
                "Passive crafting run: scanned={}, rejected={}, restored={}, submitted={}, bufferFlushes={}",
                settings.patternsScanned(), settings.patternsRejected(), settings.patternsRestored(),
                settings.patternsSubmitted(), settings.bufferFlushes());
    }

    private static KeyCounter[] plan(IPatternDetails pattern, long copies, Level level,
            MEStorage storage, IActionSource source) {
        if (copies < 1) return null;
        Map<AEKey, Long> reserved = new HashMap<>();
        IPatternDetails.IInput[] patternInputs = pattern.getInputs();
        KeyCounter[] result = new KeyCounter[patternInputs.length];
        for (int index = 0; index < patternInputs.length; index++) {
            result[index] = new KeyCounter();
            IPatternDetails.IInput input = patternInputs[index];
            if (input == null) continue;
            long required;
            try { required = Math.multiplyExact(input.getMultiplier(), copies); }
            catch (ArithmeticException ignored) { return null; }
            for (GenericStack possible : input.getPossibleInputs()) {
                if (possible == null || possible.what() == null || !input.isValid(possible.what(), level)) continue;
                AEKey key = possible.what();
                long available = storage.extract(key, Long.MAX_VALUE, Actionable.SIMULATE, source);
                long amount = Math.min(required, Math.max(0, available - reserved.getOrDefault(key, 0L)));
                if (amount > 0) {
                    result[index].add(key, amount);
                    reserved.merge(key, amount, Long::sum);
                    required -= amount;
                }
                if (required == 0) break;
            }
            if (required != 0) return null;
        }
        return result;
    }

    private static KeyCounter extract(KeyCounter[] inputs, MEStorage storage, IActionSource source,
            MePassiveCraftingSettings settings) {
        KeyCounter extracted = new KeyCounter();
        for (KeyCounter input : inputs) for (var entry : input) {
            long amount = storage.extract(entry.getKey(), entry.getLongValue(), Actionable.MODULATE, source);
            if (amount > 0) extracted.add(entry.getKey(), amount);
            if (amount != entry.getLongValue()) {
                restoreRemainder(extracted, storage, source, settings);
                return null;
            }
        }
        return extracted;
    }
}
