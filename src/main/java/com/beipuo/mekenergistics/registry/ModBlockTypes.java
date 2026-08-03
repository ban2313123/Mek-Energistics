package com.beipuo.mekenergistics.registry;

import com.beipuo.mekenergistics.common.machine.MeMekanismMachine;
import com.beipuo.mekenergistics.compat.catalog.CompatMachineCatalog;
import com.beipuo.mekenergistics.compat.provider.CompatMachineProviders;
import java.util.LinkedHashMap;
import java.util.Map;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.tile.base.TileEntityMekanism;

public final class ModBlockTypes {
    private static final Map<MeMekanismMachine, BlockTypeTile<? extends TileEntityMekanism>> MACHINES =
            new LinkedHashMap<>();

    static {
        CompatMachineCatalog.available().forEach(spec -> MACHINES.put(
                spec.machine(),
                CompatMachineProviders.get(spec.provider()).createBlockType(
                        spec, ModBlockEntities.getMachineBlockEntity(spec.machine()))));
    }

    private ModBlockTypes() {
    }

    public static BlockTypeTile<? extends TileEntityMekanism> getMachineBlockType(MeMekanismMachine machine) {
        return MACHINES.get(machine);
    }
}
