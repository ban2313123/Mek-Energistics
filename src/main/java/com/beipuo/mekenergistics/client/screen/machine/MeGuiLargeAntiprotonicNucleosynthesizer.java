package com.beipuo.mekenergistics.client.screen.machine;

import com.beipuo.mekenergistics.blockentity.compat.mekmm.machine.MeLargeAntiprotonicNucleosynthesizerBlockEntity;
import com.beipuo.mekenergistics.client.screen.MeGuiConfigurableTile;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Minimal ME screen; the inherited tile screen renders slots, gauges and AE controls. */
public class MeGuiLargeAntiprotonicNucleosynthesizer extends MeGuiConfigurableTile<
        MeLargeAntiprotonicNucleosynthesizerBlockEntity,
        MekanismTileContainer<MeLargeAntiprotonicNucleosynthesizerBlockEntity>> {
    public MeGuiLargeAntiprotonicNucleosynthesizer(
            MekanismTileContainer<MeLargeAntiprotonicNucleosynthesizerBlockEntity> container,
            Inventory inventory, Component title) {
        super(container, inventory, title);
        dynamicSlots = true;
        imageHeight += 27;
        imageWidth += 20;
        inventoryLabelY = imageHeight - 93;
    }
}
