package com.beipuo.mekenergistics.blockentity.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

class AbstractMeAeSupportSchemaTest {
    @Test
    void loadingDefersPatternDecodeUntilTheManagedNodeIsCreated() throws IOException {
        String source = Files.readString(Path.of(
                "src/main/java/com/beipuo/mekenergistics/blockentity/support/AbstractMeAeSupport.java"));
        int loadSlots = source.indexOf("public final void loadSlots(");
        int nextMethod = source.indexOf("private boolean hasPatternSlotTags", loadSlots);
        String loadBody = source.substring(loadSlots, nextMethod);

        assertTrue(loadBody.contains("craftingUpdateState.markPending();"));
        assertFalse(loadBody.contains("updatePatterns();"));
        assertFalse(loadBody.contains("owner.saveChanges();"));
    }

    @Test
    void restoredPatternsArePublishedWhenTheNodeBecomesActive() {
        AbstractMeAeSupport.CraftingUpdateState state = new AbstractMeAeSupport.CraftingUpdateState();
        int[] updates = {0};

        state.markPending();
        state.request(false, () -> updates[0]++);

        assertTrue(state.isPending());
        assertEquals(0, updates[0]);

        state.flush(() -> updates[0]++);

        assertFalse(state.isPending());
        assertEquals(1, updates[0]);
    }

    @Test
    void activePatternChangesRefreshImmediatelyWithoutLeavingPendingWork() {
        AbstractMeAeSupport.CraftingUpdateState state = new AbstractMeAeSupport.CraftingUpdateState();
        int[] updates = {0};

        state.request(true, () -> updates[0]++);
        state.flush(() -> updates[0]++);

        assertFalse(state.isPending());
        assertEquals(1, updates[0]);
    }

    @Test
    void writingPatternSlotsAddsSchemaAndRetainsExistingKeys() {
        CompoundTag tag = new CompoundTag();

        AbstractMeAeSupport.savePatternSlots(List.of(), tag, null);

        assertEquals(2, tag.getInt("AePatternSchema"));
    }

    @Test
    void oldInventoryIsMigratedToPatternSlotKeys() {
        CompoundTag tag = new CompoundTag();
        net.minecraft.nbt.ListTag inventory = new net.minecraft.nbt.ListTag();
        tag.put("Inventory", inventory);

        AbstractMeAeSupport.loadLegacyInventory(List.of(), tag, null, 0);
        AbstractMeAeSupport.savePatternSlots(List.of(), tag, null);

        assertEquals(2, tag.getInt("AePatternSchema"));
    }
}
