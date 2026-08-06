package com.beipuo.mekenergistics.blockentity.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.beipuo.mekenergistics.blockentity.api.AeOutputMode;
import com.beipuo.mekenergistics.blockentity.api.MeAeMachine;
import java.lang.reflect.Proxy;
import java.util.Map;
import org.junit.jupiter.api.Test;

class MeMemoryCardSettingsTest {

    @Test
    void exportsAllMeMachineSettingsForEveryOutputCombination() {
        for (AeOutputMode mode : AeOutputMode.values()) {
            MachineState source = new MachineState(mode, "Line 4", false);

            Map<String, String> settings = MeMemoryCardSettings.exportMeSettings(source.proxy());

            assertEquals(mode.name(), settings.get(MeMemoryCardSettings.OUTPUT_MODE));
            assertEquals("Line 4", settings.get(MeMemoryCardSettings.TERMINAL_NAME));
            assertEquals("false", settings.get(MeMemoryCardSettings.TERMINAL_VISIBLE));
        }
    }

    @Test
    void importsOutputModeTerminalNameAndNetworkVisibility() {
        MachineState target = new MachineState(AeOutputMode.NONE, "Old", true);

        MeMemoryCardSettings.importMeSettings(target.proxy(), Map.of(
                MeMemoryCardSettings.OUTPUT_MODE, AeOutputMode.CHEMICALS_FLUIDS.name(),
                MeMemoryCardSettings.TERMINAL_NAME, "Processing Hall",
                MeMemoryCardSettings.TERMINAL_VISIBLE, "false"));

        assertSame(AeOutputMode.CHEMICALS_FLUIDS, target.outputMode);
        assertEquals("Processing Hall", target.terminalName);
        assertFalse(target.visible);
    }

    @Test
    void ignoresMissingOrCorruptSettingsWithoutResettingTheMachine() {
        MachineState target = new MachineState(AeOutputMode.ITEMS_FLUIDS, "Keep", false);

        MeMemoryCardSettings.importMeSettings(target.proxy(), Map.of(
                MeMemoryCardSettings.OUTPUT_MODE, "BROKEN",
                MeMemoryCardSettings.TERMINAL_VISIBLE, "not-a-boolean"));

        assertSame(AeOutputMode.ITEMS_FLUIDS, target.outputMode);
        assertEquals("Keep", target.terminalName);
        assertFalse(target.visible);
        assertNull(MeMemoryCardSettings.parseOutputMode(null));
        assertNull(MeMemoryCardSettings.parseBoolean(null));
    }

    private static final class MachineState {
        private AeOutputMode outputMode;
        private String terminalName;
        private boolean visible;

        private MachineState(AeOutputMode outputMode, String terminalName, boolean visible) {
            this.outputMode = outputMode;
            this.terminalName = terminalName;
            this.visible = visible;
        }

        private MeAeMachine proxy() {
            return (MeAeMachine) Proxy.newProxyInstance(
                    MeAeMachine.class.getClassLoader(),
                    new Class<?>[] {MeAeMachine.class},
                    (proxy, method, args) -> switch (method.getName()) {
                        case "getAeOutputMode" -> this.outputMode;
                        case "setAeOutputMode" -> {
                            this.outputMode = (AeOutputMode) args[0];
                            yield null;
                        }
                        case "getCustomPatternTerminalName" -> this.terminalName;
                        case "setCustomPatternTerminalName" -> {
                            this.terminalName = (String) args[0];
                            yield null;
                        }
                        case "isVisibleInTerminal" -> this.visible;
                        case "setVisibleInPatternAccessTerminal" -> {
                            this.visible = (boolean) args[0];
                            yield null;
                        }
                        case "toString" -> "TestMeAeMachine";
                        default -> throw new UnsupportedOperationException(method.toString());
                    });
        }
    }
}
