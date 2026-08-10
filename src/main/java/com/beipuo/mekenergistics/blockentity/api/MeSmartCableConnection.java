package com.beipuo.mekenergistics.blockentity.api;

import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.util.AECableType;
import net.minecraft.core.Direction;

public interface MeSmartCableConnection extends IInWorldGridNodeHost {
    /**
     * Deliberately abstract instead of default: Productive Bees Genesis' {@code IAe2OutputHost}
     * defaults the same method to {@code AECableType.SMART}, and a concrete tile implementing both
     * interfaces would fail to load with {@code IncompatibleClassChangeError}. Each concrete
     * implementor provides its own class-level method, which wins over any integration default.
     */
    @Override
    AECableType getCableConnectionType(Direction dir);
}
