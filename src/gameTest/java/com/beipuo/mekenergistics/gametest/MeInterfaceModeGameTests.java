package com.beipuo.mekenergistics.gametest;

import appeng.api.config.Actionable;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.blockentity.storage.MEChestBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import com.beipuo.mekenergistics.MekEnergistics;
import com.beipuo.mekenergistics.blockentity.api.MeAeMachine;
import com.beipuo.mekenergistics.blockentity.support.AbstractMeAeSupport;
import com.beipuo.mekenergistics.upgrade.MeUpgradeStateOwner;
import com.beipuo.mekenergistics.upgrade.MeUpgradeType;
import mekanism.api.energy.IEnergyContainer;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(MekEnergistics.MODID)
@PrefixGameTestTemplate(false)
public final class MeInterfaceModeGameTests {
    private static final BlockPos MACHINE = new BlockPos(1, 1, 1);
    private static final BlockPos POWER = new BlockPos(1, 1, 0);
    private static final BlockPos STORAGE = new BlockPos(0, 1, 1);

    private MeInterfaceModeGameTests() {
    }

    @GameTest(template = "empty_3x3x3", timeoutTicks = 8 * SharedConstants.TICKS_PER_SECOND)
    public static void interfaceSupplyRespectsMachineCapacityAndRefundsRemainder(GameTestHelper helper) {
        setupInterfaceMachine(helper);
        helper.startSequence()
                .thenExecuteAfter(1, () -> installInterfaceUpgrade(helper))
                .thenExecuteAfter(2 * SharedConstants.TICKS_PER_SECOND, () -> {
                    seedNetwork(helper, 100);
                    MeAeMachine machine = machine(helper);
                    inputSlot(helper).setStack(new ItemStack(Items.IRON_ORE, 54));
                    machine.getRecipeAeSupport().getInterfaceConfig()
                            .setStack(0, new GenericStack(AEItemKey.of(Items.IRON_ORE), 64));
                })
                .thenExecuteAfter(3 * SharedConstants.TICKS_PER_SECOND, () -> {
                    helper.assertTrue(networkCount(helper, Items.IRON_ORE) == 90,
                            "AE did not decrease by exactly 10 when the machine could only accept 10: "
                                    + networkCount(helper, Items.IRON_ORE));
                    helper.assertTrue(inputSlot(helper).getCount() == 64,
                            "machine input was not filled to 64: " + inputSlot(helper).getCount());
                })
                .thenSucceed();
    }

    @GameTest(template = "empty_3x3x3", timeoutTicks = 8 * SharedConstants.TICKS_PER_SECOND)
    public static void interfaceDoesNotExtractWhenMachineCannotReceive(GameTestHelper helper) {
        setupInterfaceMachine(helper);
        helper.startSequence()
                .thenExecuteAfter(1, () -> installInterfaceUpgrade(helper))
                .thenExecuteAfter(2 * SharedConstants.TICKS_PER_SECOND, () -> {
                    seedNetwork(helper, 100);
                    inputSlot(helper).setStack(new ItemStack(Items.IRON_ORE, 64));
                    machine(helper).getRecipeAeSupport().getInterfaceConfig()
                            .setStack(0, new GenericStack(AEItemKey.of(Items.IRON_ORE), 64));
                })
                .thenExecuteAfter(3 * SharedConstants.TICKS_PER_SECOND, () -> {
                    helper.assertTrue(networkCount(helper, Items.IRON_ORE) == 100,
                            "AE changed while the machine could not receive anything: "
                                    + networkCount(helper, Items.IRON_ORE));
                    helper.assertTrue(inputSlot(helper).getCount() == 64,
                            "machine input changed while full: " + inputSlot(helper).getCount());
                })
                .thenSucceed();
    }

    @GameTest(template = "empty_3x3x3", timeoutTicks = 8 * SharedConstants.TICKS_PER_SECOND)
    public static void interfacePartialStockInsertsOnlyActual(GameTestHelper helper) {
        setupInterfaceMachine(helper);
        helper.startSequence()
                .thenExecuteAfter(1, () -> installInterfaceUpgrade(helper))
                .thenExecuteAfter(2 * SharedConstants.TICKS_PER_SECOND, () -> {
                    seedNetwork(helper, 5);
                    machine(helper).getRecipeAeSupport().getInterfaceConfig()
                            .setStack(0, new GenericStack(AEItemKey.of(Items.IRON_ORE), 64));
                })
                .thenExecuteAfter(3 * SharedConstants.TICKS_PER_SECOND, () -> {
                    helper.assertTrue(inputSlot(helper).getCount() == 5,
                            "machine input did not receive exactly the available 5: " + inputSlot(helper).getCount());
                    helper.assertTrue(networkCount(helper, Items.IRON_ORE) == 0,
                            "AE was not drained to zero: " + networkCount(helper, Items.IRON_ORE));
                })
                .thenSucceed();
    }

    @GameTest(template = "empty_3x3x3", timeoutTicks = 8 * SharedConstants.TICKS_PER_SECOND)
    public static void interfaceDuplicateSlotsAccumulateBatches(GameTestHelper helper) {
        setupInterfaceMachine(helper);
        helper.startSequence()
                .thenExecuteAfter(1, () -> installInterfaceUpgrade(helper))
                .thenExecuteAfter(2 * SharedConstants.TICKS_PER_SECOND, () -> {
                    seedNetwork(helper, 200);
                    var config = machine(helper).getRecipeAeSupport().getInterfaceConfig();
                    config.setStack(0, new GenericStack(AEItemKey.of(Items.IRON_ORE), 64));
                    config.setStack(1, new GenericStack(AEItemKey.of(Items.IRON_ORE), 64));
                })
                .thenExecuteAfter(3 * SharedConstants.TICKS_PER_SECOND, () -> {
                    helper.assertTrue(inputSlot(helper).getCount() == 64,
                            "machine input did not reach one full batch: " + inputSlot(helper).getCount());
                    helper.assertTrue(networkCount(helper, Items.IRON_ORE) == 136,
                            "AE loss was not exactly the accepted batch (64) with the rest refunded: "
                                    + networkCount(helper, Items.IRON_ORE));
                })
                .thenSucceed();
    }

    @GameTest(template = "empty_3x3x3", timeoutTicks = 8 * SharedConstants.TICKS_PER_SECOND)
    public static void interfaceModeIsolatesPatternServices(GameTestHelper helper) {
        setupInterfaceMachine(helper);
        helper.startSequence()
                .thenExecuteAfter(1, () -> installInterfaceUpgrade(helper))
                .thenExecuteAfter(2 * SharedConstants.TICKS_PER_SECOND, () -> {
                    MeAeMachine machine = machine(helper);
                    helper.assertTrue(machine.getMainNode().getNode() != null
                                    && machine.getMainNode().getNode().isActive(),
                            "interface mode dropped off the AE network");
                    helper.assertTrue(MeAeMachine.modeOf(machine).isOutputInterface(),
                            "machine is not running interface mode");
                    helper.assertTrue(machine.getAvailablePatterns().isEmpty(),
                            "interface mode exposed patterns");
                    var pattern = PatternDetailsHelper.encodeProcessingPattern(
                            java.util.List.of(new GenericStack(AEItemKey.of(Items.IRON_ORE), 1)),
                            java.util.List.of(new GenericStack(AEItemKey.of(Items.IRON_INGOT), 1)));
                    machine.getPatternSlots().getFirst().setStack(pattern);
                    helper.assertTrue(machine.getAvailablePatterns().isEmpty(),
                            "interface mode accepted a pattern into its published list");
                    KeyCounter[] inputs = new KeyCounter[]{new KeyCounter()};
                    inputs[0].add(AEItemKey.of(Items.IRON_ORE), 1);
                    helper.assertTrue(!machine.pushPattern(pattern, inputs),
                            "interface mode accepted a pattern push");
                    helper.assertTrue(machine.maxAcceptedPatternCopies(inputs) == 0,
                            "interface mode accepted pattern copies");
                    helper.assertTrue(machine.isBusy(),
                            "interface mode did not report busy");
                    helper.assertTrue(!machine.isVisibleInTerminal(),
                            "interface mode stayed visible in the Pattern Access Terminal");
                    helper.assertTrue(!machine.hasPassiveCraftingUpgrade(),
                            "interface mode kept passive crafting active");
                    helper.assertTrue(!machine.isSmartPatternMultiplicationEnabled(),
                            "interface mode kept smart pattern multiplication active");
                })
                .thenSucceed();
    }

    @GameTest(template = "empty_3x3x3", timeoutTicks = 8 * SharedConstants.TICKS_PER_SECOND)
    public static void interfaceOutputReturnsToAeByActualReceived(GameTestHelper helper) {
        setupInterfaceMachine(helper);
        helper.startSequence()
                .thenExecuteAfter(1, () -> installInterfaceUpgrade(helper))
                .thenExecuteAfter(2 * SharedConstants.TICKS_PER_SECOND, () -> {
                    outputSlot(helper).setStack(new ItemStack(Items.IRON_INGOT, 10));
                })
                .thenExecuteAfter(3 * SharedConstants.TICKS_PER_SECOND, () -> {
                    helper.assertTrue(networkCount(helper, Items.IRON_INGOT) == 10,
                            "AE did not receive exactly the 10 returned items: "
                                    + networkCount(helper, Items.IRON_INGOT));
                    helper.assertTrue(outputSlot(helper).isEmpty(),
                            "machine output was not cleared after AE accepted it");
                })
                .thenSucceed();
    }

    @GameTest(template = "empty_3x3x3", timeoutTicks = 8 * SharedConstants.TICKS_PER_SECOND)
    public static void interfaceOutputStaysWhenNetworkDrops(GameTestHelper helper) {
        setupInterfaceMachine(helper);
        helper.startSequence()
                .thenExecuteAfter(1, () -> installInterfaceUpgrade(helper))
                .thenExecuteAfter(2 * SharedConstants.TICKS_PER_SECOND, () -> {
                    outputSlot(helper).setStack(new ItemStack(Items.IRON_INGOT, 10));
                    helper.setBlock(POWER, Blocks.AIR);
                })
                .thenExecuteAfter(3 * SharedConstants.TICKS_PER_SECOND, () -> {
                    helper.assertTrue(outputSlot(helper).getCount() == 10,
                            "machine output was lost while the AE network was down");
                    helper.assertTrue(helper.getLevel().getEntitiesOfClass(
                                    net.minecraft.world.entity.item.ItemEntity.class,
                                    new net.minecraft.world.phys.AABB(helper.absolutePos(MACHINE)).inflate(4.0D))
                                    .isEmpty(),
                            "output items were dropped instead of preserved");
                })
                .thenSucceed();
    }

    @GameTest(template = "empty_3x3x3", timeoutTicks = 8 * SharedConstants.TICKS_PER_SECOND)
    public static void interfaceConfigSurvivesBlockEntityReload(GameTestHelper helper) {
        setupInterfaceMachine(helper);
        helper.startSequence()
                .thenExecuteAfter(1, () -> installInterfaceUpgrade(helper))
                .thenExecuteAfter(2 * SharedConstants.TICKS_PER_SECOND, () -> {
                    MeAeMachine machine = machine(helper);
                    machine.getRecipeAeSupport().getInterfaceConfig()
                            .setStack(0, new GenericStack(AEItemKey.of(Items.IRON_ORE), 64));
                    CompoundTag saved = new CompoundTag();
                    machine.getRecipeAeSupport().saveSlots(saved, helper.getLevel().registryAccess());
                    machine.getRecipeAeSupport().loadSlots(saved, helper.getLevel().registryAccess());
                    var stack = machine.getRecipeAeSupport().getInterfaceConfig().getStack(0);
                    helper.assertTrue(stack != null && stack.amount() == 64,
                            "interface config did not survive a slot save/load cycle");
                    MeUpgradeStateOwner owner = (MeUpgradeStateOwner) helper.getBlockEntity(MACHINE);
                    helper.assertTrue(owner.getMeUpgradeContainer().isInstalled(MeUpgradeType.OUTPUT_INTERFACE),
                            "interface upgrade did not survive a slot save/load cycle");
                })
                .thenSucceed();
    }

    private static void setupInterfaceMachine(GameTestHelper helper) {
        helper.setBlock(MACHINE, requiredBlock("mekanism:enrichment_chamber"));
        helper.setBlock(POWER, AEBlocks.CREATIVE_ENERGY_CELL.block());
        helper.setBlock(STORAGE, AEBlocks.ME_CHEST.block());
    }

    private static void installInterfaceUpgrade(GameTestHelper helper) {
        MEChestBlockEntity chest = (MEChestBlockEntity) helper.getBlockEntity(STORAGE);
        chest.setCell(AEItems.ITEM_CELL_1K.stack());
        TileEntityMekanism tile = requiredTile(helper, MACHINE);
        drainLocalEnergy(helper, tile, "enrichment chamber before interface test");
        MeUpgradeStateOwner owner = (MeUpgradeStateOwner) tile;
        helper.assertTrue(owner.getMeUpgradeContainer().install(MeUpgradeType.OUTPUT_INTERFACE).successful(),
                "could not install the ME output interface upgrade");
    }

    private static MeAeMachine machine(GameTestHelper helper) {
        TileEntityMekanism tile = requiredTile(helper, MACHINE);
        if (!(tile instanceof MeAeMachine machine)) {
            throw new GameTestAssertException("machine does not implement MeAeMachine");
        }
        return machine;
    }

    private static void seedNetwork(GameTestHelper helper, int amount) {
        MeAeMachine machine = machine(helper);
        IGrid grid = machine.getGrid();
        if (grid == null) {
            throw new GameTestAssertException("machine has no AE grid to seed");
        }
        MEStorage storage = grid.getStorageService().getInventory();
        IActionSource source = IActionSource.ofMachine((IActionHost) machine);
        long inserted = storage.insert(AEItemKey.of(Items.IRON_ORE), amount, Actionable.MODULATE, source);
        if (inserted != amount) {
            throw new GameTestAssertException("seeded " + inserted + " of " + amount + " iron ore");
        }
    }

    private static long networkCount(GameTestHelper helper, net.minecraft.world.level.ItemLike item) {
        MeAeMachine machine = machine(helper);
        IGrid grid = machine.getGrid();
        if (grid == null) {
            return -1;
        }
        MEStorage storage = grid.getStorageService().getInventory();
        return storage.extract(AEItemKey.of(item), Long.MAX_VALUE, Actionable.SIMULATE,
                IActionSource.ofMachine((IActionHost) machine));
    }

    private static mekanism.api.inventory.IInventorySlot inputSlot(GameTestHelper helper) {
        return requiredTile(helper, MACHINE).getInputSlots(null).getFirst();
    }

    private static mekanism.api.inventory.IInventorySlot outputSlot(GameTestHelper helper) {
        return requiredTile(helper, MACHINE).getOutputSlots(null).getFirst();
    }

    private static void drainLocalEnergy(GameTestHelper helper, TileEntityMekanism tile, String description) {
        var containers = tile.getEnergyContainers(null);
        helper.assertTrue(!containers.isEmpty(), description + " has no local energy container");
        for (IEnergyContainer container : containers) {
            container.setEnergy(0);
        }
    }

    private static TileEntityMekanism requiredTile(GameTestHelper helper, BlockPos position) {
        BlockEntity blockEntity = helper.getBlockEntity(position);
        if (blockEntity instanceof TileEntityMekanism tile) {
            return tile;
        }
        throw new GameTestAssertException("position " + position + " did not create a Mekanism block entity");
    }

    private static Block requiredBlock(String id) {
        Block block = net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(ResourceLocation.parse(id));
        if (block == Blocks.AIR) {
            throw new IllegalStateException("Missing required block " + id);
        }
        return block;
    }
}
