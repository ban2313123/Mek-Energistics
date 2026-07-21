package com.beipuo.mekenergistics.blockentity.api;

import com.beipuo.mekenergistics.blockentity.support.io.MeInputLayout;
import com.beipuo.mekenergistics.blockentity.support.io.MeOutputPort;
import com.beipuo.mekenergistics.blockentity.support.io.MePatternIoAdapter;
import java.util.List;
import mekanism.common.inventory.slot.BasicInventorySlot;

/** Required physical pattern I/O contract for owners backed by the common AE support. */
public interface MePatternIoOwner extends MeAeSupportOwner {
    default List<BasicInventorySlot> getExternalPatternSlots() {
        return List.of();
    }

    MeInputLayout getPatternInputLayout();

    List<? extends MeOutputPort> getPatternOutputPorts();

    default boolean isPatternBusy() {
        return false;
    }

    default int getLegacyPatternSlotOffset() {
        return 0;
    }

    default MePatternIoAdapter getPatternIoAdapter() {
        return MePatternIoAdapter.of(this);
    }
}
