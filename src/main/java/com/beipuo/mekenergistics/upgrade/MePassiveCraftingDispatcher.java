package com.beipuo.mekenergistics.upgrade;

import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.world.level.Level;

public final class MePassiveCraftingDispatcher {
    private MePassiveCraftingDispatcher() {
    }

    public static boolean submitAvailable(List<IPatternDetails> patterns, long copies, Level level,
            MEStorage storage, IActionSource source, Predicate<KeyCounter[]> submitter) {
        boolean submitted = false;
        for (IPatternDetails pattern : patterns) {
            KeyCounter[] inputs = plan(pattern, copies, level, storage, source);
            if (inputs == null) {
                continue;
            }
            KeyCounter extracted = extract(inputs, storage, source);
            if (extracted == null) {
                continue;
            }
            if (submitter.test(inputs)) {
                submitted = true;
                continue;
            }
            restore(extracted, storage, source);
        }
        return submitted;
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

    private static KeyCounter extract(KeyCounter[] inputs, MEStorage storage, IActionSource source) {
        KeyCounter extracted = new KeyCounter();
        for (KeyCounter input : inputs) for (var entry : input) {
            long amount = storage.extract(entry.getKey(), entry.getLongValue(), Actionable.MODULATE, source);
            if (amount > 0) extracted.add(entry.getKey(), amount);
            if (amount != entry.getLongValue()) { restore(extracted, storage, source); return null; }
        }
        return extracted;
    }

    private static void restore(KeyCounter extracted, MEStorage storage, IActionSource source) {
        for (var entry : extracted) storage.insert(entry.getKey(), entry.getLongValue(), Actionable.MODULATE, source);
    }
}
