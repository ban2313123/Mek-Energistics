package com.beipuo.mekenergistics.blockentity.support.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import com.beipuo.mekenergistics.testfixture.FakeInputPort;
import com.beipuo.mekenergistics.testfixture.FakeKey;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Routing a pattern whose recipe needs more than one ingredient — every two-input machine, from the
 * Combiner and Metallurgic Infuser to the Chemixer and Pressurized Reaction Chamber.
 *
 * <p>With one ingredient the router takes a fast path that simply sums the matching ports. With two
 * it has to decide which port serves which ingredient, backtracking when an early choice starves a
 * later one. The invariant that matters is that the estimate and the transaction agree: if
 * {@code maxAcceptedCopies} promises N and {@code route} then refuses N, the smart-multiplication
 * scheduler spends its whole per-tick budget halving the batch down, every tick, without ever
 * reporting an error.
 */
class MeMultiKeyRoutingTest {
    private static final AEKey IRON = new FakeKey("iron");
    private static final AEKey COAL = new FakeKey("coal");

    /**
     * One counter per ingredient holder, which is how a pattern presents a recipe: each holder
     * resolves to exactly one key, and mixing two keys into one holder is rejected outright.
     */
    private static KeyCounter[] recipe(long ironPerCraft, long coalPerCraft, long copies) {
        KeyCounter iron = new KeyCounter();
        iron.add(IRON, ironPerCraft * copies);
        KeyCounter coal = new KeyCounter();
        coal.add(COAL, coalPerCraft * copies);
        return new KeyCounter[] {iron, coal};
    }

    @Test
    void twoIngredientsAreRoutedToTheirOwnDedicatedPorts() {
        FakeInputPort ironPort = new FakeInputPort(IRON, 64);
        FakeInputPort coalPort = new FakeInputPort(COAL, 64);

        assertTrue(MePatternInputRouter.route(recipe(2, 3, 1),
                List.of(ironPort, coalPort)));

        assertEquals(2, ironPort.amount());
        assertEquals(3, coalPort.amount());
    }

    @Test
    void onePortMayNotBeCountedTowardsTwoDifferentIngredients() {
        // A single port accepting only iron cannot also satisfy the coal half of the recipe.
        FakeInputPort ironOnly = new FakeInputPort(IRON, 64);

        assertFalse(MePatternInputRouter.route(recipe(1, 1, 1), List.of(ironOnly)));
        assertEquals(0, ironOnly.amount(), "a rejected route must leave the machine untouched");
    }

    @Test
    void anEarlyChoiceThatStarvesTheSecondIngredientIsBackedOut() {
        // The shared port is the only one that takes coal, so iron has to fall back to its own port
        // even though the shared port could have taken the iron first.
        FakeInputPort shared = new FakeInputPort(COAL, 4);
        FakeInputPort ironOnly = new FakeInputPort(IRON, 4);

        assertTrue(MePatternInputRouter.route(recipe(4, 4, 1),
                List.of(shared, ironOnly)));

        assertEquals(4, shared.amount());
        assertEquals(4, ironOnly.amount());
    }

    @Test
    void theSameRouteIsFoundWhicheverOrderThePortsAreDeclaredIn() {
        FakeInputPort shared = new FakeInputPort(COAL, 4);
        FakeInputPort ironOnly = new FakeInputPort(IRON, 4);

        assertTrue(MePatternInputRouter.route(recipe(4, 4, 1),
                List.of(ironOnly, shared)), "port declaration order must not decide feasibility");
    }

    @Test
    void aRejectedMultiKeyRouteRollsBackTheIngredientItHadAlreadyPlaced() {
        FakeInputPort ironPort = new FakeInputPort(IRON, 64);
        FakeInputPort coalPort = new FakeInputPort(COAL, 1);

        assertFalse(MePatternInputRouter.route(recipe(2, 8, 1),
                List.of(ironPort, coalPort)));

        assertEquals(0, ironPort.amount(), "iron went in before coal failed and must come back out");
        assertEquals(0, coalPort.amount());
    }

    @Test
    void theEstimateIsLimitedByWhicheverIngredientRunsOutFirst() {
        FakeInputPort ironPort = new FakeInputPort(IRON, 100);
        FakeInputPort coalPort = new FakeInputPort(COAL, 9);

        // Three coal per craft, so nine coal is three crafts even though iron would allow fifty.
        assertEquals(3, MePatternInputRouter.maxAcceptedCopies(
                recipe(2, 3, 1), List.of(ironPort, coalPort)));
    }

    @Test
    void theEstimateIsNotReachedByWritingIntoTheMachine() {
        FakeInputPort ironPort = new FakeInputPort(IRON, 100);
        FakeInputPort coalPort = new FakeInputPort(COAL, 9);

        MePatternInputRouter.maxAcceptedCopies(
                recipe(2, 3, 1), List.of(ironPort, coalPort));

        assertEquals(0, ironPort.amount(), "probing must not load the machine");
        assertEquals(0, coalPort.amount());
    }

    @Test
    void aBatchOfExactlyTheEstimatedSizeIsAlwaysAccepted() {
        // If these two ever disagree, feedBestBatch burns its retry budget halving down every tick.
        for (long ironCapacity : List.of(7L, 64L, 100L, 513L)) {
            for (long coalCapacity : List.of(3L, 40L, 128L)) {
                FakeInputPort ironPort = new FakeInputPort(IRON, ironCapacity);
                FakeInputPort coalPort = new FakeInputPort(COAL, coalCapacity);
                List<FakeInputPort> ports = List.of(ironPort, coalPort);
                long estimate = MePatternInputRouter.maxAcceptedCopies(recipe(2, 3, 1), ports);
                if (estimate <= 0) {
                    continue;
                }

                assertTrue(MePatternInputRouter.route(recipe(2, 3, estimate), ports),
                        () -> "estimated " + estimate + " copies for iron=" + ironCapacity
                                + " coal=" + coalCapacity + " but the transaction refused them");
            }
        }
    }

    @Test
    void aBatchOneLargerThanTheEstimateIsRefused() {
        FakeInputPort ironPort = new FakeInputPort(IRON, 100);
        FakeInputPort coalPort = new FakeInputPort(COAL, 9);
        List<FakeInputPort> ports = List.of(ironPort, coalPort);
        long estimate = MePatternInputRouter.maxAcceptedCopies(recipe(2, 3, 1), ports);

        assertFalse(MePatternInputRouter.route(recipe(2, 3, estimate + 1), ports),
                "the estimate must be the true maximum, not a lower bound");
    }

    @Test
    void anIngredientNoPortAcceptsMakesTheWholeRecipeUnroutable() {
        FakeInputPort ironPort = new FakeInputPort(IRON, 64);

        assertEquals(0, MePatternInputRouter.maxAcceptedCopies(
                recipe(1, 1, 1), List.of(ironPort)));
    }
}
