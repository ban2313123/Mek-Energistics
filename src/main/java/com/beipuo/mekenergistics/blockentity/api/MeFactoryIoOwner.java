package com.beipuo.mekenergistics.blockentity.api;

import java.util.List;
import mekanism.api.inventory.IInventorySlot;

/** Shared physical I/O contract for factory owners backed by the common AE support. */
public interface MeFactoryIoOwner extends MeFactoryAeMachine {
    List<IInventorySlot> meInputSlots();

    List<IInventorySlot> meOutputSlots();

    void unpauseRecipeMonitors();
}
