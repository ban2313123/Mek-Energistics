package com.beipuo.mekenergistics.blockentity.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

class AbstractMeAeSupportSchemaTest {
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
