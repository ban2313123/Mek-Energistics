package com.beipuo.mekenergistics.registry;

import com.beipuo.mekenergistics.block.MeMekanismMachineBlock;
import com.beipuo.mekenergistics.common.machine.MeMekanismMachine;
import com.beipuo.mekenergistics.compat.catalog.CompatMachineCatalog;
import com.beipuo.mekenergistics.item.MeMachineBlockItem;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import org.jetbrains.annotations.Nullable;

public final class ModBlocks {
    private static final MeBlockDeferredRegister BLOCKS = new MeBlockDeferredRegister();
    private static final Map<MeMekanismMachine, MeBlockRegistryObject<MeMekanismMachineBlock, MeMachineBlockItem>> MACHINES = new EnumMap<>(MeMekanismMachine.class);

    static {
        CompatMachineCatalog.available().forEach(spec ->
                MACHINES.put(spec.machine(), BLOCKS.registerMachine(spec.machine())));
    }

    public static final DeferredBlock<MeMekanismMachineBlock> ME_METALLURGIC_INFUSER = getMachineBlock(MeMekanismMachine.METALLURGIC_INFUSER);

    private ModBlocks() {
    }

    public static DeferredBlock<MeMekanismMachineBlock> getMachineBlock(MeMekanismMachine machine) {
        MeBlockRegistryObject<MeMekanismMachineBlock, MeMachineBlockItem> registryObject = MACHINES.get(machine);
        return registryObject == null ? null : registryObject.blockHolder();
    }

    public static DeferredItem<MeMachineBlockItem> getMachineItem(MeMekanismMachine machine) {
        MeBlockRegistryObject<MeMekanismMachineBlock, MeMachineBlockItem> registryObject = MACHINES.get(machine);
        return registryObject == null ? null : registryObject.itemHolder();
    }

    public static Iterable<DeferredBlock<MeMekanismMachineBlock>> getMachineBlocks() {
        return MACHINES.values().stream().map(MeBlockRegistryObject::blockHolder)::iterator;
    }

    public static Iterable<DeferredItem<MeMachineBlockItem>> getMachineItems() {
        return MACHINES.values().stream().map(MeBlockRegistryObject::itemHolder)::iterator;
    }

    public static Block[] getMachineBlockArray() {
        return MACHINES.values().stream().map(MeBlockRegistryObject::get).toArray(Block[]::new);
    }

    /**
     * Reverse index for {@link #getMachine(Block)}, which sits on block-interaction paths and would
     * otherwise scan every registered machine. Built lazily rather than in the static initializer
     * because the {@link DeferredBlock} holders cannot be dereferenced until registration has run.
     */
    private static volatile Map<Block, MeMekanismMachine> machinesByBlock;

    @Nullable
    public static MeMekanismMachine getMachine(Block block) {
        if (block == null) {
            return null;
        }
        return machinesByBlock().get(block);
    }

    private static Map<Block, MeMekanismMachine> machinesByBlock() {
        Map<Block, MeMekanismMachine> index = machinesByBlock;
        if (index == null) {
            synchronized (ModBlocks.class) {
                index = machinesByBlock;
                if (index == null) {
                    index = new IdentityHashMap<>(MACHINES.size());
                    for (Map.Entry<MeMekanismMachine, MeBlockRegistryObject<MeMekanismMachineBlock, MeMachineBlockItem>> entry : MACHINES.entrySet()) {
                        index.put(entry.getValue().get(), entry.getKey());
                    }
                    machinesByBlock = index;
                }
            }
        }
        return index;
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
