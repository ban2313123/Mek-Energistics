package com.beipuo.mekenergistics.network.packet;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class ServerPacketTargetTest {
    @Test
    void acceptsDistanceAtAndBelowEightBlocks() {
        assertTrue(ServerPacketTarget.isInRange(new Vec3(0.5D, 0.5D, 8.5D), BlockPos.ZERO));
        assertTrue(ServerPacketTarget.isInRange(new Vec3(0.5D, 0.5D, 8.49D), BlockPos.ZERO));
    }

    @Test
    void rejectsDistanceBeyondEightBlocks() {
        assertFalse(ServerPacketTarget.isInRange(new Vec3(0.5D, 0.5D, 8.51D), BlockPos.ZERO));
    }

    @Test
    void rejectsTerminalNamesLongerThanSixtyFourCharacters() {
        assertTrue(ServerPacketTarget.isValidTerminalName("a".repeat(64)));
        assertFalse(ServerPacketTarget.isValidTerminalName("a".repeat(65)));
    }
}
