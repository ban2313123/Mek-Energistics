package com.beipuo.mekenergistics.compat.jade;

import appeng.core.definitions.AEBlocks;
import appeng.api.AECapabilities;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.IInWorldGridNodeHost;
import com.beipuo.mekenergistics.MekEnergistics;
import com.beipuo.mekenergistics.block.MeMekanismMachineBlock;
import com.beipuo.mekenergistics.blockentity.api.MeAeMachine;
import com.beipuo.mekenergistics.blockentity.api.MeUpgradeableMachine;
import com.beipuo.mekenergistics.blockentity.machine.process.MeElectricMachineBlockEntity;
import com.beipuo.mekenergistics.menu.MePatternMekanismTileContainer;
import com.beipuo.mekenergistics.menu.MePatternMachineContainer;
import com.beipuo.mekenergistics.registry.ModMenuTypes;
import com.beipuo.mekenergistics.registry.ModItems;
import java.lang.reflect.Proxy;
import java.util.List;
import mekanism.api.energy.IEnergyContainer;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeHasBounding;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import snownee.jade.api.BlockAccessor;

@GameTestHolder(MekEnergistics.MODID)
@PrefixGameTestTemplate(false)
public final class MeAeStatusGameTests {
    private static final BlockPos ENERGY_CELL = new BlockPos(1, 1, 1);
    private static final BlockPos UPGRADED_MACHINE = new BlockPos(1, 1, 2);
    private static final BlockPos ME_FACTORY = new BlockPos(2, 1, 1);
    private static final BlockPos ME_BASE_MACHINE = new BlockPos(2, 1, 2);
    private static final BlockPos LARGE_MACHINE = new BlockPos(1, 1, 1);
    private static final BlockPos LARGE_MACHINE_ENERGY_CELL = new BlockPos(1, 1, 3);
    private static final List<MachinePlacement> EMEK_MACHINES = List.of(
            new MachinePlacement(new BlockPos(1, 1, 0), "mekenergistics:me_alloyer"),
            new MachinePlacement(new BlockPos(0, 1, 1), "mekenergistics:me_chemixer"),
            new MachinePlacement(new BlockPos(2, 1, 1), "mekenergistics:me_solidification_chamber"),
            new MachinePlacement(new BlockPos(1, 1, 2), "mekenergistics:me_thermalizer"));

    private MeAeStatusGameTests() {
    }

    @GameTest(template = "empty_3x3x3", timeoutTicks = 4 * SharedConstants.TICKS_PER_SECOND)
    public static void jadeReportsOnlineForUpgradeAndMeFactory(GameTestHelper helper) {
        helper.setBlock(ENERGY_CELL, AEBlocks.CREATIVE_ENERGY_CELL.block());
        helper.setBlock(UPGRADED_MACHINE, requiredBlock("mekanism:enrichment_chamber"));
        helper.setBlock(ME_FACTORY, requiredBlock("mekenergistics:me_basic_enriching_factory"));
        helper.setBlock(ME_BASE_MACHINE, requiredBlock("mekenergistics:me_enrichment_chamber"));

        helper.startSequence()
                .thenExecuteAfter(1, () -> {
                    TileEntityMekanism tile = requiredTile(helper, UPGRADED_MACHINE);
                    drainLocalEnergy(helper, tile, "Mekanism machine before ME upgrade");
                    drainLocalEnergy(helper, requiredTile(helper, ME_FACTORY), "ME factory");
                    drainLocalEnergy(helper, requiredTile(helper, ME_BASE_MACHINE), "ME base machine");
                    CompoundTag data = new CompoundTag();
                    MeAeStatusDataProvider.INSTANCE.appendServerData(data, accessor(tile));
                    helper.assertTrue(!data.contains(MeAeStatusDataProvider.TAG_AE_STATE),
                            "Mekanism machine reported an AE status before the upgrade was installed");
                    tile.getComponent().getUpgradeSlot()
                            .setStack(ModItems.ME_PATTERN_PROVIDER_UPGRADE.toStack());
                })
                .thenExecuteAfter(2 * SharedConstants.TICKS_PER_SECOND, () -> {
                    TileEntityMekanism upgradedTile = requiredTile(helper, UPGRADED_MACHINE);
                    helper.assertTrue(upgradedTile instanceof MeUpgradeableMachine upgradeable
                                    && upgradeable.isMeUpgradeActive(),
                            "pattern provider upgrade did not activate before the Jade check");
                    assertJadeOnline(helper, upgradedTile, "upgraded Mekanism machine");
                    assertJadeOnline(helper, requiredTile(helper, ME_FACTORY), "ME factory");
                    assertBaseMachinePatternSlots(helper, requiredTile(helper, ME_BASE_MACHINE));
                    assertLocalEnergyFilled(helper, upgradedTile, "upgraded Mekanism machine");
                    assertLocalEnergyFilled(helper, requiredTile(helper, ME_FACTORY), "ME factory");
                    assertLocalEnergyFilled(helper, requiredTile(helper, ME_BASE_MACHINE), "ME base machine");
                })
                .thenSucceed();
    }

    @GameTest(template = "empty_3x3x3", timeoutTicks = 4 * SharedConstants.TICKS_PER_SECOND)
    public static void jadeReportsOnlineForEveryEvolvedMeMachine(GameTestHelper helper) {
        helper.setBlock(ENERGY_CELL, AEBlocks.CREATIVE_ENERGY_CELL.block());
        EMEK_MACHINES.forEach(machine -> helper.setBlock(machine.position(), requiredBlock(machine.blockId())));

        helper.startSequence()
                .thenExecuteAfter(2 * SharedConstants.TICKS_PER_SECOND, () -> {
                    for (MachinePlacement machine : EMEK_MACHINES) {
                        Block block = helper.getBlockState(machine.position()).getBlock();
                        helper.assertTrue(block instanceof MeMekanismMachineBlock,
                                machine.blockId() + " is not covered by Jade's ME machine block registration");
                        assertJadeOnline(helper, requiredTile(helper, machine.position()), machine.blockId());
                    }
                })
                .thenSucceed();
    }

    @GameTest(template = "empty_3x3x3", timeoutTicks = SharedConstants.TICKS_PER_SECOND)
    public static void everyEvolvedMeMachineCreatesCompatibleMenu(GameTestHelper helper) {
        EMEK_MACHINES.forEach(machine -> helper.setBlock(machine.position(), requiredBlock(machine.blockId())));

        helper.startSequence()
                .thenExecuteAfter(1, () -> {
                    Player player = helper.makeMockPlayer(GameType.SURVIVAL);
                    for (MachinePlacement machine : EMEK_MACHINES) {
                        TileEntityMekanism tile = requiredTile(helper, machine.position());
                        helper.assertTrue(tile instanceof MeAeMachine,
                                machine.blockId() + " does not implement the ME machine contract");
                        var menuProvider = ModMenuTypes.getMachineContainer(((MeAeMachine) tile).getMachine())
                                .getProvider(tile.getDisplayName(), tile, true);
                        helper.assertTrue(menuProvider != null,
                                machine.blockId() + " rejected its registered menu tile type");
                        var menu = menuProvider.createMenu(0, player.getInventory(), player);
                        helper.assertTrue(menu instanceof MePatternMekanismTileContainer,
                                machine.blockId() + " did not create the ME pattern container");
                    }
                })
                .thenSucceed();
    }

    @GameTest(template = "empty_3x3x3", timeoutTicks = 4 * SharedConstants.TICKS_PER_SECOND)
    public static void meLargeAntiprotonicRefillsLocalFeBeforeRecipeTick(GameTestHelper helper) {
        runLargeAntiprotonicRefillTest(helper,
                "mekenergistics:me_large_antiprotonic_nucleosynthesizer", false);
    }

    @GameTest(template = "empty_3x3x3", timeoutTicks = 5 * SharedConstants.TICKS_PER_SECOND)
    public static void upgradedLargeAntiprotonicRefillsLocalFeBeforeRecipeTick(GameTestHelper helper) {
        runLargeAntiprotonicRefillTest(helper,
                "mekmm:large_antiprotonic_nucleosynthesizer", true);
    }

    private static void runLargeAntiprotonicRefillTest(GameTestHelper helper, String blockId,
            boolean installUpgrade) {
        helper.setBlock(LARGE_MACHINE, requiredBlock(blockId));
        var largeState = helper.getBlockState(LARGE_MACHINE);
        AttributeHasBounding bounding = Attribute.get(largeState, AttributeHasBounding.class);
        helper.assertTrue(bounding != null, blockId + " has no large-machine bounding attribute");
        bounding.placeBoundingBlocks(helper.getLevel(), helper.absolutePos(LARGE_MACHINE), largeState);

        helper.startSequence()
                .thenExecuteAfter(1, () -> {
                    TileEntityMekanism tile = requiredTile(helper, LARGE_MACHINE);
                    // The controller fills a 3x3 footprint. Attach power after its port nodes exist.
                    helper.setBlock(LARGE_MACHINE_ENERGY_CELL, AEBlocks.CREATIVE_ENERGY_CELL.block());
                    drainLocalEnergy(helper, tile, blockId);
                    if (installUpgrade) {
                        helper.assertTrue(tile instanceof MeUpgradeableMachine upgradeable
                                        && upgradeable.isMeUpgradeTarget(),
                                blockId + " is missing its ME upgrade target");
                        tile.getComponent().getUpgradeSlot()
                                .setStack(ModItems.ME_PATTERN_PROVIDER_UPGRADE.toStack());
                    }
                })
                .thenExecuteAfter(2 * SharedConstants.TICKS_PER_SECOND, () -> {
                    TileEntityMekanism tile = requiredTile(helper, LARGE_MACHINE);
                    if (installUpgrade) {
                        helper.assertTrue(tile instanceof MeUpgradeableMachine upgradeable
                                        && upgradeable.isMeUpgradeActive(),
                                blockId + " did not activate its ME upgrade");
                    }
                    assertLargeMachinePortConnected(helper, tile, blockId);
                    assertJadeOnline(helper, tile, blockId);
                    drainLocalEnergy(helper, tile, blockId + " immediately before its recipe tick");
                })
                .thenExecuteAfter(1, () -> assertLocalEnergyFilled(helper,
                        requiredTile(helper, LARGE_MACHINE), blockId))
                .thenSucceed();
    }

    private static void assertLargeMachinePortConnected(GameTestHelper helper, TileEntityMekanism tile,
            String blockId) {
        helper.assertTrue(tile instanceof MeAeMachine, blockId + " does not implement MeAeMachine");
        MeAeMachine machine = (MeAeMachine) tile;
        BlockPos relativePort = LARGE_MACHINE.relative(net.minecraft.core.Direction.SOUTH);
        BlockPos absolutePort = helper.absolutePos(relativePort);
        var mainNode = machine.getMainNode().getNode();
        var portNode = machine.getRecipeAeSupport()
                .getLargeMachineGridNode(absolutePort, net.minecraft.core.Direction.SOUTH);
        IInWorldGridNodeHost host = helper.getLevel().getCapability(
                AECapabilities.IN_WORLD_GRID_NODE_HOST, absolutePort, null);
        helper.assertTrue(mainNode != null && mainNode.isActive(),
                blockId + " did not join the powered AE network: main=" + nodeState(mainNode)
                        + ", port=" + nodeState(portNode)
                        + ", host=" + (host != null)
                        + ", portBlock=" + BuiltInRegistries.BLOCK.getKey(helper.getBlockState(relativePort).getBlock())
                        + ", energyBlock=" + BuiltInRegistries.BLOCK.getKey(
                                helper.getBlockState(LARGE_MACHINE_ENERGY_CELL).getBlock()));
    }

    private static String nodeState(appeng.api.networking.IGridNode node) {
        return node == null ? "null" : node.isActive() ? "active" : "inactive";
    }

    private static void drainLocalEnergy(GameTestHelper helper, TileEntityMekanism tile, String description) {
        var containers = tile.getEnergyContainers(null);
        helper.assertTrue(!containers.isEmpty(), description + " has no local energy container");
        for (IEnergyContainer container : containers) {
            container.setEnergy(0);
        }
    }

    private static void assertLocalEnergyFilled(GameTestHelper helper, TileEntityMekanism tile, String description) {
        var containers = tile.getEnergyContainers(null);
        helper.assertTrue(!containers.isEmpty(), description + " has no local energy container");
        for (IEnergyContainer container : containers) {
            double simulatedAe = tile instanceof MeAeMachine machine && machine.getGrid() != null
                    ? machine.getGrid().getEnergyService().extractAEPower(100, Actionable.SIMULATE, PowerMultiplier.ONE)
                    : -1;
            helper.assertTrue(container.getMaxEnergy() > 0 && container.getEnergy() == container.getMaxEnergy(),
                    description + " did not refill local FE from the connected AE network: "
                            + container.getEnergy() + "/" + container.getMaxEnergy()
                            + ", simulated AE extraction=" + simulatedAe);
        }
    }

    private static void assertBaseMachinePatternSlots(GameTestHelper helper, TileEntityMekanism tile) {
        helper.assertTrue(tile instanceof MeAeMachine machine,
                "ME base machine does not implement the ME machine contract");
        if (!(tile instanceof MeAeMachine machine)) {
            return;
        }
        var patternSlots = machine.getPatternSlots();
        helper.assertTrue(!patternSlots.isEmpty(), "ME base machine has no AE pattern slots");
        var inventory = tile.getInventorySlots(null);
        for (var patternSlot : patternSlots) {
            helper.assertTrue(inventory.contains(patternSlot),
                    "ME base machine pattern slot is missing from its Mekanism inventory");
        }
        helper.assertTrue(tile instanceof MeElectricMachineBlockEntity,
                "basic enrichment chamber is not using the expected base machine class");
        if (tile instanceof MeElectricMachineBlockEntity electricMachine) {
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            var menu = new MePatternMachineContainer<>(ModMenuTypes.ME_ELECTRIC_MACHINE, 0,
                    player.getInventory(), electricMachine);
            for (var patternSlot : patternSlots) {
                helper.assertTrue(menu.getInventoryContainerSlots().stream()
                                .anyMatch(containerSlot -> containerSlot.getInventorySlot() == patternSlot),
                        "basic ME machine pattern slot is missing from its menu");
            }
        }
    }

    private static void assertJadeOnline(GameTestHelper helper, BlockEntity blockEntity, String description) {
        helper.assertTrue(blockEntity instanceof MeAeMachine machine
                        && machine.getMainNode().getNode() != null
                        && machine.getMainNode().getNode().isActive(),
                description + " did not join the powered AE network");
        CompoundTag data = new CompoundTag();
        MeAeStatusDataProvider.INSTANCE.appendServerData(data, accessor(blockEntity));
        helper.assertTrue(data.contains(MeAeStatusDataProvider.TAG_AE_STATE),
                description + " produced no Jade AE status");
        helper.assertTrue(data.getByte(MeAeStatusDataProvider.TAG_AE_STATE)
                        == MeAeStatusDataProvider.AeState.ONLINE.ordinal(),
                description + " did not report ONLINE to Jade");
    }

    private static BlockAccessor accessor(BlockEntity blockEntity) {
        return (BlockAccessor) Proxy.newProxyInstance(
                BlockAccessor.class.getClassLoader(),
                new Class<?>[] {BlockAccessor.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("getBlockEntity") || method.getName().equals("getTarget")) {
                        return blockEntity;
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
    }

    private static TileEntityMekanism requiredTile(GameTestHelper helper, BlockPos position) {
        BlockEntity blockEntity = helper.getBlockEntity(position);
        if (blockEntity instanceof TileEntityMekanism tile) {
            return tile;
        }
        throw new GameTestAssertException("position " + position + " did not create a Mekanism block entity");
    }

    private static Block requiredBlock(String id) {
        Block block = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(id));
        if (block == Blocks.AIR) {
            throw new IllegalStateException("Missing required block " + id);
        }
        return block;
    }

    private record MachinePlacement(BlockPos position, String blockId) {
    }
}
