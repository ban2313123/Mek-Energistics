package com.beipuo.mekenergistics.client.overlay;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class MeInterfaceWindowOverlayContractTest {
    private static final Path OVERLAY = Path.of(
            "src/main/java/com/beipuo/mekenergistics/client/overlay/MeInterfaceWindowOverlay.java");
    private static final Path CONFIG = Path.of(
            "src/main/java/com/beipuo/mekenergistics/upgrade/MeInterfaceConfig.java");
    private static final Path JEI = Path.of(
            "src/main/java/com/beipuo/mekenergistics/client/jei/MekEnergisticsJeiPlugin.java");

    @Test
    void interfaceUsesNineColumnsAndThreeFixedHeightRows() throws IOException {
        String overlay = Files.readString(OVERLAY);
        String config = Files.readString(CONFIG);

        assertTrue(config.contains("public static final int SLOT_COUNT = 9;"));
        assertTrue(overlay.contains("private static final int ROW_Y = 18;"));
        assertTrue(overlay.contains("private static final int CONFIG_ROW_Y = ROW_Y + 18;"));
        assertTrue(overlay.contains("private static final int INVENTORY_ROW_Y = CONFIG_ROW_Y + 18;"));
        assertTrue(overlay.contains("drawSlot(guiGraphics, minecraft, slotX, relativeY + CONFIG_ROW_Y"));
        assertTrue(overlay.contains("drawSlot(guiGraphics, minecraft, slotX, relativeY + INVENTORY_ROW_Y"));
    }

    @Test
    void onlyTheMarkRowChangesConfigurationAndHitTestingUsesScreenCoordinates() throws IOException {
        String overlay = Files.readString(OVERLAY);

        assertTrue(overlay.contains("int configSlot = configSlotAt(mouseX, mouseY);"));
        assertTrue(overlay.contains("int amountSlot = amountButtonAt(mouseX, mouseY);"));
        assertTrue(overlay.contains("int inventorySlot = columnAt(mouseX, mouseY, INVENTORY_ROW_Y, 18);"));
        assertTrue(overlay.contains("int slotX = (int) mouseX - (getX() + 8);"));
        assertTrue(overlay.contains("int slotY = (int) mouseY - (getY() + rowY);"));
        assertFalse(overlay.contains("slotAt(mouseX, mouseY)"));
        assertFalse(overlay.contains("GLFW.GLFW_MOUSE_BUTTON_MIDDLE"));
        assertTrue(overlay.contains("amountColumn >= 0 || configSlot >= 0 || inventorySlot >= 0"));
    }

    @Test
    void interfaceButtonIsExcludedFromJei() throws IOException {
        String jei = Files.readString(JEI);

        assertTrue(jei.contains("MeInterfaceWindowOverlay.isInterfaceMachine(gui)"));
        assertTrue(jei.contains("areas.add(MeInterfaceWindowOverlay.jeiButtonArea(gui));"));
        assertTrue(jei.contains("ModItems.ME_OUTPUT_INTERFACE_UPGRADE.get().getDefaultInstance()"));
    }
}
