package com.beipuo.mekenergistics.blockentity.support.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.KeyCounter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

class MeMachineIoAdapterTest {
    private static final FakeKey IRON = new FakeKey("iron");

    @Test
    void itemOfferCannotExceedTheSlotDeclaredRemainingCapacity() {
        assertEquals(16, MeMachineIoAdapter.boundedItemOffer(1_000, 48, 64));
        assertEquals(0, MeMachineIoAdapter.boundedItemOffer(1, 64, 64));
    }

    @Test
    void itemOfferKeepsUpgradedSlotLimitsAboveVanillaStackSize() {
        assertEquals(768, MeMachineIoAdapter.boundedItemOffer(1_000, 3_328, 4_096));
    }

    @Test
    void itemInputProbesTheSlotLimitWithOneItem() throws IOException {
        String source = Files.readString(Path.of(
                "src/main/java/com/beipuo/mekenergistics/blockentity/support/io/MeMachineIoAdapter.java"));

        assertTrue(source.contains("ItemStack probe = itemKey.toStack(1);"));
        assertTrue(source.contains("boundedItemOffer(amount, slot.getCount(), slot.getLimit(probe))"));
        assertFalse(source.contains("itemKey.toStack((int) amount)"));
    }

    @Test
    void routerSimulatesThenCommitsAcrossPorts() {
        FakeInput first = new FakeInput(IRON, 3);
        FakeInput second = new FakeInput(IRON, 5);
        KeyCounter counter = new KeyCounter();
        counter.add(IRON, 8);

        assertTrue(MePatternInputRouter.route(new KeyCounter[] { counter }, List.of(first, second)));
        assertEquals(3, first.amount);
        assertEquals(5, second.amount);
        assertEquals(1, first.simulations);
        assertEquals(0, first.restores);
        assertEquals(0, second.restores);
    }

    @Test
    void routerRollsBackEarlierPortsWhenExecutionChanges() {
        FakeInput first = new FakeInput(IRON, 3);
        FakeInput second = new FakeInput(IRON, 5);
        second.failExecution = true;
        KeyCounter counter = new KeyCounter();
        counter.add(IRON, 8);

        assertFalse(MePatternInputRouter.route(new KeyCounter[] { counter }, List.of(first, second)));
        assertEquals(0, first.amount);
        assertEquals(0, second.amount);
        assertEquals(1, first.restores);
        assertEquals(1, second.restores);
    }

    @Test
    void laneRouterKeepsLaneOrderAndCommitsAsOneTransaction() {
        FakeInput main = new FakeInput(IRON, 8);
        FakeInput extra = new FakeInput(IRON, 8);
        KeyCounter first = new KeyCounter();
        first.add(IRON, 3);
        KeyCounter second = new KeyCounter();
        second.add(IRON, 5);

        assertTrue(MePatternInputRouter.routeLanes(
                new KeyCounter[] {first, second}, List.of(List.of(main), List.of(extra))));
        assertEquals(3, main.amount);
        assertEquals(5, extra.amount);
    }

    @Test
    void laneRouterRollsBackAllLanesWhenLaterLaneFails() {
        FakeInput main = new FakeInput(IRON, 8);
        FakeInput extra = new FakeInput(IRON, 8);
        extra.failExecution = true;
        KeyCounter first = new KeyCounter();
        first.add(IRON, 3);
        KeyCounter second = new KeyCounter();
        second.add(IRON, 5);

        assertFalse(MePatternInputRouter.routeLanes(
                new KeyCounter[] {first, second}, List.of(List.of(main), List.of(extra))));
        assertEquals(0, main.amount);
        assertEquals(0, extra.amount);
    }

    @Test
    void laneRouterCanBacktrackAcrossSharedPorts() {
        FakeInput shared = new FakeInput(IRON, 5);
        FakeInput fallback = new FakeInput(IRON, 5);
        KeyCounter first = new KeyCounter();
        first.add(IRON, 5);
        KeyCounter second = new KeyCounter();
        second.add(IRON, 5);

        assertTrue(MePatternInputRouter.routeLanes(
                new KeyCounter[] {first, second}, List.of(List.of(shared, fallback), List.of(fallback))));
        assertEquals(5, shared.amount);
        assertEquals(5, fallback.amount);
    }

    @Test
    void laneRouterBacktracksWhenFirstCandidateIsNeededByLaterLane() {
        FakeInput fallback = new FakeInput(IRON, 5);
        FakeInput shared = new FakeInput(IRON, 5);
        KeyCounter first = new KeyCounter();
        first.add(IRON, 5);
        KeyCounter second = new KeyCounter();
        second.add(IRON, 5);

        assertTrue(MePatternInputRouter.routeLanes(
                new KeyCounter[] {first, second}, List.of(List.of(fallback, shared), List.of(fallback))));
        assertEquals(5, shared.amount);
        assertEquals(5, fallback.amount);
    }

    @Test
    void laneRouterCanReduceEarlierReservationForLaterLane() {
        FakeInput shared = new FakeInput(IRON, 10);
        FakeInput fallback = new FakeInput(IRON, 10);
        KeyCounter first = new KeyCounter();
        first.add(IRON, 15);
        KeyCounter second = new KeyCounter();
        second.add(IRON, 5);

        assertTrue(MePatternInputRouter.routeLanes(
                new KeyCounter[] {first, second}, List.of(List.of(shared, fallback), List.of(shared))));
        assertEquals(10, shared.amount);
        assertEquals(10, fallback.amount);
    }

    @Test
    void laneRouterAccountsForReservationsWhenOnePortServesMultipleLanes() {
        FakeInput shared = new FakeInput(IRON, 8);
        KeyCounter first = new KeyCounter();
        first.add(IRON, 3);
        KeyCounter second = new KeyCounter();
        second.add(IRON, 3);

        assertTrue(MePatternInputRouter.routeLanes(
                new KeyCounter[] {first, second}, List.of(List.of(shared), List.of(shared))));
        assertEquals(6, shared.amount);
    }

    @Test
    void sharedLaneCapacityPredictionMatchesTransaction() {
        FakeInput shared = new FakeInput(IRON, 8);
        KeyCounter first = new KeyCounter();
        first.add(IRON, 1);
        KeyCounter second = new KeyCounter();
        second.add(IRON, 1);

        long copies = MePatternInputRouter.maxAcceptedLaneCopies(
                new KeyCounter[] {first, second}, List.of(List.of(shared), List.of(shared)));
        assertEquals(4, copies);
        assertTrue(MePatternInputRouter.routeLanes(new KeyCounter[] {
                scale(first, copies), scale(second, copies)
        }, List.of(List.of(shared), List.of(shared))));
        assertEquals(8, shared.amount);
    }

    @Test
    void capacityPredictionMatchesLaneTransaction() {
        FakeInput main = new FakeInput(IRON, 12);
        FakeInput extra = new FakeInput(IRON, 4);
        KeyCounter first = new KeyCounter();
        first.add(IRON, 3);
        KeyCounter second = new KeyCounter();
        second.add(IRON, 1);

        long copies = MePatternInputRouter.maxAcceptedLaneCopies(
                new KeyCounter[] {first, second}, List.of(List.of(main), List.of(extra)));
        assertEquals(4, copies);
        assertEquals(0, main.restores);
        assertEquals(0, extra.restores);
        assertTrue(MePatternInputRouter.routeLanes(new KeyCounter[] {
                scale(first, copies), scale(second, copies)
        }, List.of(List.of(main), List.of(extra))));
        assertEquals(12, main.amount);
        assertEquals(4, extra.amount);
    }

    @Test
    void capacityProbeAggregatesAutoSortedFactorySlotsWithoutWritingThem() {
        FakeInput first = new FakeInput(IRON, 4_096);
        FakeInput second = new FakeInput(IRON, 4_096);
        first.amount = 3_840;
        second.amount = 3_584;
        KeyCounter counter = new KeyCounter();
        counter.add(IRON, 1);

        assertEquals(768, MePatternInputRouter.maxAcceptedCopies(
                new KeyCounter[] {counter}, List.of(first, second)));
        assertEquals(3_840, first.amount);
        assertEquals(3_584, second.amount);
        assertEquals(0, first.restores);
        assertEquals(0, second.restores);
        assertEquals(1, first.simulations);
        assertEquals(1, second.simulations);
    }

    @Test
    void groupedFactoryInputUsesTheCapacityOfEveryProcessSlot() {
        FakeInput first = new FakeInput(IRON, 4_096);
        FakeInput second = new FakeInput(IRON, 4_096);
        MeInputPort factoryInput = MeMachineIoAdapter.groupedInput(List.of(first, second));
        KeyCounter counter = new KeyCounter();
        counter.add(IRON, 8_192);

        assertTrue(MePatternInputRouter.route(new KeyCounter[] {counter}, List.of(factoryInput)));
        assertEquals(4_096, first.amount);
        assertEquals(4_096, second.amount);
    }

    @Test
    void groupedFactoryInputRollsBackEveryProcessSlot() {
        FakeInput first = new FakeInput(IRON, 4);
        FakeInput second = new FakeInput(IRON, 4);
        second.failExecution = true;
        MeInputPort factoryInput = MeMachineIoAdapter.groupedInput(List.of(first, second));
        KeyCounter counter = new KeyCounter();
        counter.add(IRON, 8);

        assertFalse(MePatternInputRouter.route(new KeyCounter[] {counter}, List.of(factoryInput)));
        assertEquals(0, first.amount);
        assertEquals(0, second.amount);
        assertEquals(1, first.restores);
        assertEquals(1, second.restores);
    }

    private static KeyCounter scale(KeyCounter source, long copies) {
        KeyCounter result = new KeyCounter();
        source.forEach(entry -> result.add(entry.getKey(), entry.getLongValue() * copies));
        return result;
    }

    @Test
    void routerRejectsMultipleKeysInOneLane() {
        KeyCounter counter = new KeyCounter();
        counter.add(IRON, 1);
        counter.add(new FakeKey("gold"), 1);
        assertFalse(MePatternInputRouter.route(new KeyCounter[] {counter},
                List.of(new FakeInput(IRON, 4))));
    }

    private static final class FakeInput implements MeInputPort {
        private final AEKey key;
        private final long capacity;
        private long amount;
        private int simulations;
        private int restores;
        private boolean failExecution;

        private FakeInput(AEKey key, long capacity) {
            this.key = key;
            this.capacity = capacity;
        }

        @Override public boolean supports(AEKey key) { return this.key.equals(key); }
        @Override public long insert(AEKey key, long amount, mekanism.api.Action action) {
            if (!supports(key) || this.failExecution && action.execute()) return 0;
            if (!action.execute()) simulations++;
            long accepted = Math.min(amount, capacity - this.amount);
            if (action.execute()) this.amount += accepted;
            return Math.max(0, accepted);
        }
        @Override public Object snapshot() { return amount; }
        @Override public void restore(Object snapshot) { restores++; amount = (long) snapshot; }
    }

    private static final class FakeKey extends AEKey {
        private final String id;
        private FakeKey(String id) { this.id = id; }
        @Override public AEKeyType getType() { throw new UnsupportedOperationException(); }
        @Override public AEKey dropSecondary() { return this; }
        @Override public CompoundTag toTag(HolderLookup.Provider registries) { CompoundTag tag = new CompoundTag(); tag.putString("id", id); return tag; }
        @Override public Object getPrimaryKey() { return id; }
        @Override public ResourceLocation getId() { return ResourceLocation.fromNamespaceAndPath("mekenergistics_test", id); }
        @Override public void writeToPacket(RegistryFriendlyByteBuf data) { data.writeUtf(id); }
        @Override protected Component computeDisplayName() { return Component.literal(id); }
        @Override public void addDrops(long amount, List<ItemStack> drops, Level level, BlockPos pos) { }
        @Override public boolean hasComponents() { return false; }
        @Override public boolean equals(Object o) { return o == this || o instanceof FakeKey other && id.equals(other.id); }
        @Override public int hashCode() { return id.hashCode(); }
    }
}
