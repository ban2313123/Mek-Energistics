package com.beipuo.mekenergistics.blockentity.support;

import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;

/**
 * Marker for inventory holders that already include ME pattern slots. Constructor-time SPI wrapping
 * must leave these alone so factory mixins keep their recipe-cache unpause listener.
 */
public interface MePatternSlotInventoryHolder extends IInventorySlotHolder {
}
