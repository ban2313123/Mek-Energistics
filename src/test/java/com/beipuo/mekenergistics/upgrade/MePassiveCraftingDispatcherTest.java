package com.beipuo.mekenergistics.upgrade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import com.beipuo.mekenergistics.testfixture.FakeKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

class MePassiveCraftingDispatcherTest {
    private static final IActionSource SOURCE = IActionSource.empty();

    @Test
    void restoreFailurePersistsRemainderAndNothingIsLost() {
        FakeKey iron = new FakeKey("dispatch_iron");
        FakeStorage storage = new FakeStorage(Map.of(iron, 10L), 0);
        MePassiveCraftingSettings settings = new MePassiveCraftingSettings();

        boolean submitted = MePassiveCraftingDispatcher.submitAvailable(
                List.of(pattern(iron, 1)), 4, null, storage, SOURCE, rejectedSubmitter(), settings);

        assertFalse(submitted);
        assertEquals(6, storage.amount(iron), "extracted inputs stay in the network except the stuck part");
        List<MePassiveCraftingSettings.RecoveryEntry> buffered = settings.drainRecoveryBuffer();
        assertEquals(1, buffered.size());
        assertEquals(4, buffered.get(0).amount());
        assertEquals(10L, storage.amount(iron) + buffered.get(0).amount(), "network + buffer must conserve the total");
    }

    @Test
    void recoveryBufferFlushesWhenCapacityReturns() {
        FakeKey iron = new FakeKey("dispatch_iron");
        FakeStorage storage = new FakeStorage(Map.of(iron, 10L), 0);
        MePassiveCraftingSettings settings = new MePassiveCraftingSettings();
        MePassiveCraftingDispatcher.submitAvailable(
                List.of(pattern(iron, 1)), 4, null, storage, SOURCE, rejectedSubmitter(), settings);
        assertEquals(1, settings.recoveryBufferSize());
        assertTrue(settings.hasRecoverableRemainders());

        storage.setInsertCapacity(100);
        boolean changed = MePassiveCraftingDispatcher.submitAvailable(
                List.of(), 1, null, storage, SOURCE, rejectedSubmitter(), settings);

        assertTrue(changed, "returning a buffered remainder counts as machine work");
        assertEquals(10, storage.amount(iron));
        assertFalse(settings.hasRecoverableRemainders());
    }

    @Test
    void partialExtractIsRestoredIntoTheRecoveryBufferNotLost() {
        FakeKey iron = new FakeKey("dispatch_iron");
        FakeStorage storage = new FakeStorage(Map.of(iron, 10L), 0, 2);
        MePassiveCraftingSettings settings = new MePassiveCraftingSettings();

        boolean submitted = MePassiveCraftingDispatcher.submitAvailable(
                List.of(pattern(iron, 1)), 4, null, storage, SOURCE, rejectedSubmitter(), settings);

        assertFalse(submitted);
        assertEquals(8, storage.amount(iron));
        List<MePassiveCraftingSettings.RecoveryEntry> buffered = settings.drainRecoveryBuffer();
        assertEquals(1, buffered.size());
        assertEquals(2, buffered.get(0).amount());
        assertEquals(10L, storage.amount(iron) + buffered.get(0).amount(), "network + buffer must conserve the total");
    }

    @Test
    void perTickScanBudgetCapsPatternsAndCursorAmortizesTheScan() {
        Map<AEKey, Long> contents = new HashMap<>();
        List<IPatternDetails> patterns = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            FakeKey key = new FakeKey("scan_input_" + i);
            contents.put(key, 100L);
            patterns.add(pattern(key, 1));
        }
        FakeStorage storage = new FakeStorage(contents, 100);
        MePassiveCraftingSettings settings = new MePassiveCraftingSettings();
        RecordingSubmitter submitter = acceptingSubmitter();

        MePassiveCraftingDispatcher.submitAvailable(patterns, 1, null, storage, SOURCE, submitter, settings);

        assertEquals(MePassiveCraftingSettings.MAX_PATTERN_SCAN_PER_TICK, settings.patternsScanned(),
                "one tick must not rescan the whole pattern list");
        assertEquals(MePassiveCraftingSettings.MAX_PATTERN_SCAN_PER_TICK, settings.patternsSubmitted());
        assertEquals(8, settings.patternScanCursor());

        MePassiveCraftingDispatcher.submitAvailable(patterns, 1, null, storage, SOURCE, submitter, settings);

        assertEquals(16, settings.patternScanCursor(), "cursor must continue where it stopped");
        assertEquals(16, settings.patternScanCursor());
        assertEquals(16, submitter.submittedAmounts().size());
    }

    @Test
    void cursorPersistsThroughSaveLoad() {
        MePassiveCraftingSettings settings = new MePassiveCraftingSettings();
        settings.setPatternScanCursor(17);
        CompoundTag tag = new CompoundTag();
        settings.save(tag, RegistryAccess.EMPTY);
        MePassiveCraftingSettings loaded = new MePassiveCraftingSettings();
        loaded.load(tag, RegistryAccess.EMPTY);
        assertEquals(17, loaded.patternScanCursor());
    }

    @Test
    void rejectedPatternsAreCountedAndDoNotExhaustTheBudget() {
        Map<AEKey, Long> contents = new HashMap<>();
        List<IPatternDetails> patterns = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            FakeKey key = new FakeKey("missing_input_" + i);
            patterns.add(pattern(key, 1));
        }
        FakeStorage storage = new FakeStorage(contents, 100);
        MePassiveCraftingSettings settings = new MePassiveCraftingSettings();

        MePassiveCraftingDispatcher.submitAvailable(patterns, 1, null, storage, SOURCE, rejectedSubmitter(), settings);

        assertEquals(MePassiveCraftingSettings.MAX_PATTERN_SCAN_PER_TICK, settings.patternsScanned());
        assertEquals(MePassiveCraftingSettings.MAX_PATTERN_SCAN_PER_TICK, settings.patternsRejected());
    }

    @Test
    void copiesCapIsAppliedPerSubmission() {
        FakeKey iron = new FakeKey("dispatch_iron");
        FakeStorage storage = new FakeStorage(Map.of(iron, 1_000L), 1_000);
        MePassiveCraftingSettings settings = new MePassiveCraftingSettings();
        RecordingSubmitter submitter = acceptingSubmitter();

        boolean submitted = MePassiveCraftingDispatcher.submitAvailable(
                List.of(pattern(iron, 1)), 10_000, null, storage, SOURCE, submitter, settings);

        assertTrue(submitted);
        assertEquals(List.of(MePassiveCraftingSettings.MAX_COPIES_PER_TICK), submitter.submittedAmounts());
    }

    private static TestPattern pattern(FakeKey input, long amount) {
        return new TestPattern(input, amount);
    }

    private static RecordingSubmitter acceptingSubmitter() {
        return new RecordingSubmitter(true);
    }

    private static RecordingSubmitter rejectedSubmitter() {
        return new RecordingSubmitter(false);
    }

    private static final class RecordingSubmitter implements Predicate<KeyCounter[]> {
        private final boolean accept;
        private final List<Long> submittedAmounts = new ArrayList<>();

        private RecordingSubmitter(boolean accept) {
            this.accept = accept;
        }

        @Override
        public boolean test(KeyCounter[] inputs) {
            long total = 0;
            for (KeyCounter counter : inputs) {
                for (var entry : counter) {
                    total += entry.getLongValue();
                }
            }
            this.submittedAmounts.add(total);
            return this.accept;
        }

        private List<Long> submittedAmounts() {
            return this.submittedAmounts;
        }
    }

    private static final class TestPattern implements IPatternDetails {
        private final FakeKey input;
        private final long amount;

        private TestPattern(FakeKey input, long amount) {
            this.input = input;
            this.amount = amount;
        }

        @Override
        public AEItemKey getDefinition() {
            return null;
        }

        @Override
        public IInput[] getInputs() {
            return new IInput[] {new TestInput(this.input, this.amount)};
        }

        @Override
        public List<GenericStack> getOutputs() {
            return List.of();
        }
    }

    private record TestInput(FakeKey key, long amount) implements IPatternDetails.IInput {
        @Override
        public GenericStack[] getPossibleInputs() {
            return new GenericStack[] {new GenericStack(this.key, this.amount)};
        }

        @Override
        public long getMultiplier() {
            return 1;
        }

        @Override
        public boolean isValid(AEKey input, Level level) {
            return this.key.equals(input);
        }

        @Override
        public AEKey getRemainingKey(AEKey template) {
            return null;
        }
    }

    private static final class FakeStorage implements MEStorage {
        private final Map<AEKey, Long> contents = new HashMap<>();
        private long insertCapacity;
        private final long extractModulateLimit;

        private FakeStorage(Map<AEKey, Long> contents, long insertCapacity) {
            this(contents, insertCapacity, Long.MAX_VALUE);
        }

        private FakeStorage(Map<AEKey, Long> contents, long insertCapacity, long extractModulateLimit) {
            this.contents.putAll(contents);
            this.insertCapacity = insertCapacity;
            this.extractModulateLimit = extractModulateLimit;
        }

        private void setInsertCapacity(long insertCapacity) {
            this.insertCapacity = insertCapacity;
        }

        private long amount(AEKey key) {
            return this.contents.getOrDefault(key, 0L);
        }

        @Override
        public boolean isPreferredStorageFor(AEKey key, IActionSource source) {
            return true;
        }

        @Override
        public long insert(AEKey key, long amount, Actionable mode, IActionSource source) {
            if (amount <= 0) {
                return 0;
            }
            long accepted = Math.min(amount, Math.max(0, this.insertCapacity));
            if (mode == Actionable.MODULATE && accepted > 0) {
                this.contents.merge(key, accepted, Long::sum);
            }
            return accepted;
        }

        @Override
        public long extract(AEKey key, long amount, Actionable mode, IActionSource source) {
            long available = this.contents.getOrDefault(key, 0L);
            long taken = Math.min(amount, available);
            if (mode == Actionable.MODULATE) {
                taken = Math.min(taken, this.extractModulateLimit);
                if (taken > 0) {
                    long left = available - taken;
                    if (left > 0) {
                        this.contents.put(key, left);
                    } else {
                        this.contents.remove(key);
                    }
                }
            }
            return taken;
        }

        @Override
        public void getAvailableStacks(KeyCounter out) {
            this.contents.forEach(out::add);
        }

        @Override
        public Component getDescription() {
            return Component.literal("fake storage");
        }
    }
}
