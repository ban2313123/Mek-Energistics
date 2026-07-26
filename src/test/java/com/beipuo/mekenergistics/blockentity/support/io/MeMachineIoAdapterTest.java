package com.beipuo.mekenergistics.blockentity.support.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import com.beipuo.mekenergistics.testfixture.FakeInputPort;
import com.beipuo.mekenergistics.testfixture.FakeKey;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
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
        FakeInputPort first = new FakeInputPort(IRON, 3);
        FakeInputPort second = new FakeInputPort(IRON, 5);
        KeyCounter counter = new KeyCounter();
        counter.add(IRON, 8);

        assertTrue(MePatternInputRouter.route(new KeyCounter[] { counter }, List.of(first, second)));
        assertEquals(3, first.amount());
        assertEquals(5, second.amount());
        assertEquals(1, first.simulations());
        assertEquals(0, first.restores());
        assertEquals(0, second.restores());
    }

    @Test
    void routerRollsBackEarlierPortsWhenExecutionChanges() {
        FakeInputPort first = new FakeInputPort(IRON, 3);
        FakeInputPort second = new FakeInputPort(IRON, 5);
        second.failExecution();
        KeyCounter counter = new KeyCounter();
        counter.add(IRON, 8);

        assertFalse(MePatternInputRouter.route(new KeyCounter[] { counter }, List.of(first, second)));
        assertEquals(0, first.amount());
        assertEquals(0, second.amount());
        assertEquals(1, first.restores());
        assertEquals(1, second.restores());
    }

    @Test
    void laneRouterKeepsLaneOrderAndCommitsAsOneTransaction() {
        FakeInputPort main = new FakeInputPort(IRON, 8);
        FakeInputPort extra = new FakeInputPort(IRON, 8);
        KeyCounter first = new KeyCounter();
        first.add(IRON, 3);
        KeyCounter second = new KeyCounter();
        second.add(IRON, 5);

        assertTrue(MePatternInputRouter.routeLanes(
                new KeyCounter[] {first, second}, List.of(List.of(main), List.of(extra))));
        assertEquals(3, main.amount());
        assertEquals(5, extra.amount());
    }

    @Test
    void laneRouterRollsBackAllLanesWhenLaterLaneFails() {
        FakeInputPort main = new FakeInputPort(IRON, 8);
        FakeInputPort extra = new FakeInputPort(IRON, 8);
        extra.failExecution();
        KeyCounter first = new KeyCounter();
        first.add(IRON, 3);
        KeyCounter second = new KeyCounter();
        second.add(IRON, 5);

        assertFalse(MePatternInputRouter.routeLanes(
                new KeyCounter[] {first, second}, List.of(List.of(main), List.of(extra))));
        assertEquals(0, main.amount());
        assertEquals(0, extra.amount());
    }

    @Test
    void laneRouterCanBacktrackAcrossSharedPorts() {
        FakeInputPort shared = new FakeInputPort(IRON, 5);
        FakeInputPort fallback = new FakeInputPort(IRON, 5);
        KeyCounter first = new KeyCounter();
        first.add(IRON, 5);
        KeyCounter second = new KeyCounter();
        second.add(IRON, 5);

        assertTrue(MePatternInputRouter.routeLanes(
                new KeyCounter[] {first, second}, List.of(List.of(shared, fallback), List.of(fallback))));
        assertEquals(5, shared.amount());
        assertEquals(5, fallback.amount());
    }

    @Test
    void laneRouterBacktracksWhenFirstCandidateIsNeededByLaterLane() {
        FakeInputPort fallback = new FakeInputPort(IRON, 5);
        FakeInputPort shared = new FakeInputPort(IRON, 5);
        KeyCounter first = new KeyCounter();
        first.add(IRON, 5);
        KeyCounter second = new KeyCounter();
        second.add(IRON, 5);

        assertTrue(MePatternInputRouter.routeLanes(
                new KeyCounter[] {first, second}, List.of(List.of(fallback, shared), List.of(fallback))));
        assertEquals(5, shared.amount());
        assertEquals(5, fallback.amount());
    }

    @Test
    void laneRouterCanReduceEarlierReservationForLaterLane() {
        FakeInputPort shared = new FakeInputPort(IRON, 10);
        FakeInputPort fallback = new FakeInputPort(IRON, 10);
        KeyCounter first = new KeyCounter();
        first.add(IRON, 15);
        KeyCounter second = new KeyCounter();
        second.add(IRON, 5);

        assertTrue(MePatternInputRouter.routeLanes(
                new KeyCounter[] {first, second}, List.of(List.of(shared, fallback), List.of(shared))));
        assertEquals(10, shared.amount());
        assertEquals(10, fallback.amount());
    }

    @Test
    void laneRouterAccountsForReservationsWhenOnePortServesMultipleLanes() {
        FakeInputPort shared = new FakeInputPort(IRON, 8);
        KeyCounter first = new KeyCounter();
        first.add(IRON, 3);
        KeyCounter second = new KeyCounter();
        second.add(IRON, 3);

        assertTrue(MePatternInputRouter.routeLanes(
                new KeyCounter[] {first, second}, List.of(List.of(shared), List.of(shared))));
        assertEquals(6, shared.amount());
    }

    @Test
    void sharedLaneCapacityPredictionMatchesTransaction() {
        FakeInputPort shared = new FakeInputPort(IRON, 8);
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
        assertEquals(8, shared.amount());
    }

    @Test
    void capacityPredictionMatchesLaneTransaction() {
        FakeInputPort main = new FakeInputPort(IRON, 12);
        FakeInputPort extra = new FakeInputPort(IRON, 4);
        KeyCounter first = new KeyCounter();
        first.add(IRON, 3);
        KeyCounter second = new KeyCounter();
        second.add(IRON, 1);

        long copies = MePatternInputRouter.maxAcceptedLaneCopies(
                new KeyCounter[] {first, second}, List.of(List.of(main), List.of(extra)));
        assertEquals(4, copies);
        assertEquals(0, main.restores());
        assertEquals(0, extra.restores());
        assertTrue(MePatternInputRouter.routeLanes(new KeyCounter[] {
                scale(first, copies), scale(second, copies)
        }, List.of(List.of(main), List.of(extra))));
        assertEquals(12, main.amount());
        assertEquals(4, extra.amount());
    }

    @Test
    void capacityProbeAggregatesAutoSortedFactorySlotsWithoutWritingThem() {
        FakeInputPort first = new FakeInputPort(IRON, 4_096);
        FakeInputPort second = new FakeInputPort(IRON, 4_096);
        first.setAmount(3_840);
        second.setAmount(3_584);
        KeyCounter counter = new KeyCounter();
        counter.add(IRON, 1);

        assertEquals(768, MePatternInputRouter.maxAcceptedCopies(
                new KeyCounter[] {counter}, List.of(first, second)));
        assertEquals(3_840, first.amount());
        assertEquals(3_584, second.amount());
        assertEquals(0, first.restores());
        assertEquals(0, second.restores());
        assertEquals(1, first.simulations());
        assertEquals(1, second.simulations());
    }

    @Test
    void groupedFactoryInputUsesTheCapacityOfEveryProcessSlot() {
        FakeInputPort first = new FakeInputPort(IRON, 4_096);
        FakeInputPort second = new FakeInputPort(IRON, 4_096);
        MeInputPort factoryInput = MeMachineIoAdapter.groupedInput(List.of(first, second));
        KeyCounter counter = new KeyCounter();
        counter.add(IRON, 8_192);

        assertTrue(MePatternInputRouter.route(new KeyCounter[] {counter}, List.of(factoryInput)));
        assertEquals(4_096, first.amount());
        assertEquals(4_096, second.amount());
    }

    @Test
    void groupedFactoryInputRollsBackEveryProcessSlot() {
        FakeInputPort first = new FakeInputPort(IRON, 4);
        FakeInputPort second = new FakeInputPort(IRON, 4);
        second.failExecution();
        MeInputPort factoryInput = MeMachineIoAdapter.groupedInput(List.of(first, second));
        KeyCounter counter = new KeyCounter();
        counter.add(IRON, 8);

        assertFalse(MePatternInputRouter.route(new KeyCounter[] {counter}, List.of(factoryInput)));
        assertEquals(0, first.amount());
        assertEquals(0, second.amount());
        assertEquals(1, first.restores());
        assertEquals(1, second.restores());
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
                List.of(new FakeInputPort(IRON, 4))));
    }
}
