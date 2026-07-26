package com.beipuo.mekenergistics.blockentity.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

class MeLargeMachineFootprintTest {
    private static final BlockPos CONTROLLER = BlockPos.ZERO;

    /** The shape MekMM calls FULL_JAVA_ENTITY: a solid 3x3x3 sitting on the controller's layer. */
    private static Set<BlockPos> solid3x3x3() {
        return box(-1, 1, 0, 2, -1, 1);
    }

    /** The shape shared by the large electrolytic separator: a solid 3x3 footprint two blocks tall. */
    private static Set<BlockPos> solid3x2x3() {
        return box(-1, 1, 0, 1, -1, 1);
    }

    private static Set<BlockPos> box(int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
        Set<BlockPos> positions = new HashSet<>();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    positions.add(new BlockPos(x, y, z));
                }
            }
        }
        return positions;
    }

    @Test
    void everyBlockOfTheShellCanTakeACableButTheEnclosedCoreCannot() {
        MeLargeMachineFootprint footprint = MeLargeMachineFootprint.ofOccupied(solid3x3x3());

        // 27 blocks, of which only the middle one is walled in on all six sides.
        assertEquals(26, footprint.exposedPositionCount());
        assertEquals(Set.of(), footprint.exposedFaces(new BlockPos(0, 1, 0)));
    }

    @Test
    void facesPointingIntoTheMachineAreNotExposed() {
        MeLargeMachineFootprint footprint = MeLargeMachineFootprint.ofOccupied(solid3x3x3());

        // The centre of the north face: only north and down leave the machine.
        Set<Direction> northFaceCentre = footprint.exposedFaces(new BlockPos(0, 0, -1));
        assertEquals(Set.of(Direction.NORTH, Direction.DOWN), northFaceCentre);

        // The controller sits at the centre of the bottom layer, walled in on every side but below.
        assertEquals(Set.of(Direction.DOWN), footprint.exposedFaces(CONTROLLER));
    }

    @Test
    void neighbouringNodesOfTheSameMachineNeverFaceEachOther() {
        MeLargeMachineFootprint footprint = MeLargeMachineFootprint.ofOccupied(solid3x3x3());

        // This is what keeps AE2's in-world scan from duplicating the direct connections we make.
        for (BlockPos position : solid3x3x3()) {
            for (Direction direction : footprint.exposedFaces(position)) {
                assertFalse(solid3x3x3().contains(position.relative(direction)),
                        "exposed " + direction + " at " + position + " points back into the machine");
            }
        }
    }

    @Test
    void aShorterMachineExposesItsWholeTopLayer() {
        MeLargeMachineFootprint footprint = MeLargeMachineFootprint.ofOccupied(solid3x2x3());

        assertEquals(18, footprint.exposedPositionCount());
        assertTrue(footprint.exposedFaces(new BlockPos(0, 1, 0)).contains(Direction.UP));
        assertFalse(footprint.exposedFaces(new BlockPos(0, 1, 0)).contains(Direction.DOWN));
    }

    @Test
    void positionsOutsideTheMachineExposeNothing() {
        MeLargeMachineFootprint footprint = MeLargeMachineFootprint.ofOccupied(solid3x3x3());

        assertEquals(Set.of(), footprint.exposedFaces(new BlockPos(5, 5, 5)));
    }
}
