package com.beipuo.mekenergistics.blockentity.api;

import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.security.IActionHost;
import com.beipuo.mekenergistics.common.machine.MeMekanismMachine;
import java.util.List;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public interface MeAeSupportOwner extends ICraftingProvider, IActionHost {
    TileEntityMekanism getAeOwnerTile();

    MeMekanismMachine getMachine();

    default BlockPos getGridNodePosition() {
        return getAeOwnerTile().getBlockPos();
    }

    default List<LargeMachineGridPort> getLargeMachineGridPorts() {
        if (!getMachine().isMekmmLargeMachine()) {
            return List.of();
        }
        return largeMachineGridPorts(getAeOwnerTile().getBlockPos());
    }

    static List<LargeMachineGridPort> largeMachineGridPorts(BlockPos controllerPos) {
        return List.of(
                new LargeMachineGridPort(controllerPos.north(), Direction.NORTH),
                new LargeMachineGridPort(controllerPos.south(), Direction.SOUTH),
                new LargeMachineGridPort(controllerPos.west(), Direction.WEST),
                new LargeMachineGridPort(controllerPos.east(), Direction.EAST));
    }

    void saveChanges();

    boolean isSmartPatternMultiplicationEnabled();

    record LargeMachineGridPort(BlockPos position, Direction outwardSide) {
    }
}
