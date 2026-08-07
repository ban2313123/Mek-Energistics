package com.beipuo.mekenergistics.network;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class AeOutputPacketContractTest {
    private static final Path OVERLAY = Path.of(
            "src/main/java/com/beipuo/mekenergistics/client/overlay/MePatternWindowOverlay.java");
    private static final Path NETWORK = Path.of(
            "src/main/java/com/beipuo/mekenergistics/network/ModNetwork.java");
    private static final Path SET_PACKET = Path.of(
            "src/main/java/com/beipuo/mekenergistics/network/packet/SetAeOutputModePacket.java");

    @Test
    void outputButtonSendsTheCompleteClientSelectedMode() throws IOException {
        String overlay = Files.readString(OVERLAY);

        assertTrue(overlay.contains("get().toggle(type)"));
        assertTrue(overlay.contains("new SetAeOutputModePacket("));
        assertFalse(overlay.contains("CycleAeOutputTypePacket"));
    }

    @Test
    void serverRegistersAndAppliesTheExplicitModePacket() throws IOException {
        String network = Files.readString(NETWORK);
        String packet = Files.readString(SET_PACKET);

        assertTrue(network.contains("SetAeOutputModePacket.TYPE"));
        assertFalse(network.contains("CycleAeOutputTypePacket"));
        assertTrue(packet.contains("ServerPacketTarget.find(player, this.pos, MeAeMachine.class)"));
        assertTrue(packet.contains("machine.setAeOutputMode(this.mode)"));
    }
}
