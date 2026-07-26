package com.beipuo.mekenergistics.blockentity.support;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeHasBounding;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Every block position a bounding-block machine occupies, paired with the faces of that position which
 * touch the outside world.
 *
 * <p>AE2 only links two in-world nodes when both are exposed on the faces they share, so the exposed
 * faces decide where a cable may attach to a large machine. Deriving them from the machine's own
 * {@link AttributeHasBounding} keeps the attachment points in step with the declared shape, including
 * the shapes whose upper layer depends on which way the machine is facing.
 */
public final class MeLargeMachineFootprint {
    private final Map<BlockPos, Set<Direction>> exposedFaces;

    private MeLargeMachineFootprint(Map<BlockPos, Set<Direction>> exposedFaces) {
        this.exposedFaces = exposedFaces;
    }

    public static MeLargeMachineFootprint of(Level level, BlockPos controllerPos, BlockState state) {
        Set<BlockPos> occupied = new HashSet<>();
        occupied.add(controllerPos.immutable());
        AttributeHasBounding bounding = Attribute.get(state, AttributeHasBounding.class);
        if (bounding != null) {
            // The handler hands out a shared mutable position, so each one has to be copied.
            bounding.handle(level, controllerPos, state, occupied, (ignored, pos, positions) -> {
                positions.add(pos.immutable());
                return true;
            });
        }
        return ofOccupied(occupied);
    }

    static MeLargeMachineFootprint ofOccupied(Set<BlockPos> occupied) {
        Map<BlockPos, Set<Direction>> exposedFaces = new HashMap<>();
        for (BlockPos position : occupied) {
            Set<Direction> exposed = EnumSet.noneOf(Direction.class);
            for (Direction direction : Direction.values()) {
                if (!occupied.contains(position.relative(direction))) {
                    exposed.add(direction);
                }
            }
            if (!exposed.isEmpty()) {
                exposedFaces.put(position, Collections.unmodifiableSet(exposed));
            }
        }
        return new MeLargeMachineFootprint(Collections.unmodifiableMap(exposedFaces));
    }

    public Set<Direction> exposedFaces(BlockPos position) {
        return this.exposedFaces.getOrDefault(position, Set.of());
    }

    public void forEachExposedPosition(BiConsumer<BlockPos, Set<Direction>> consumer) {
        this.exposedFaces.forEach(consumer);
    }

    public int exposedPositionCount() {
        return this.exposedFaces.size();
    }
}
