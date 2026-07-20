package com.beipuo.mekenergistics.blockentity.api;

import com.beipuo.mekenergistics.blockentity.support.io.MeInputPort;
import com.beipuo.mekenergistics.blockentity.support.io.MeInputLayout;
import com.beipuo.mekenergistics.blockentity.support.io.MeOutputPort;
import com.beipuo.mekenergistics.blockentity.support.io.MePatternIoAdapter;
import java.util.List;
import mekanism.common.inventory.slot.BasicInventorySlot;

/** Optional contract for owners whose pattern inventory and I/O are supplied externally. */
public interface MePatternIoOwner extends MeAeSupportOwner {
    default List<BasicInventorySlot> getExternalPatternSlots() {
        return List.of();
    }

    default List<? extends MeInputPort> getPatternInputPorts() {
        return List.of();
    }

    default MeInputLayout getPatternInputLayout() {
        return MeInputLayout.unordered(getPatternInputPorts());
    }

    default List<? extends MeOutputPort> getPatternOutputPorts() {
        return List.of();
    }

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
