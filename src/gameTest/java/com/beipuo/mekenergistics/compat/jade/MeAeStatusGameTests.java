package com.beipuo.mekenergistics.compat.jade;

import appeng.core.definitions.AEBlocks;
import com.beipuo.mekenergistics.MekEnergistics;
import com.beipuo.mekenergistics.blockentity.api.MeAeMachine;
import com.beipuo.mekenergistics.blockentity.api.MeUpgradeableMachine;
import com.beipuo.mekenergistics.blockentity.machine.process.MeElectricMachineBlockEntity;
import com.beipuo.mekenergistics.menu.MePatternMachineContainer;
import com.beipuo.mekenergistics.registry.ModMenuTypes;
import com.beipuo.mekenergistics.registry.ModItems;
import java.lang.reflect.Proxy;
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

    private MeAeStatusGameTests() {
    }

    @GameTest(template = "empty_3x3x3", timeoutTicks = 4 * SharedConstants.TICKS_PER_SECOND)
    public static void jadeReportsOnlineForUpgradeAndMeFactory(GameTestHelper helper) {
        helper.setBlock(ENERGY_CELL, AEBlocks.CREATIVE_ENERGY_CELL.block());
        helper.setBlock(UPGRADED_MACHINE, requiredBlock("mekanism:enrichment_chamber"));
        helper.setBlock(ME_FACTORY, requiredBlock("mekenergistics:me_basic_enriching_factory"));
        helper.setBlock(ME_BASE_MACHINE, requiredBlock("mekenergistics:me_enrichment_chamber"));

        helper.startSequence()
                .thenExecuteAfter(1, () -> requiredTile(helper, UPGRADED_MACHINE).getComponent().getUpgradeSlot()
                        .setStack(ModItems.ME_PATTERN_PROVIDER_UPGRADE.toStack()))
                .thenExecuteAfter(2 * SharedConstants.TICKS_PER_SECOND, () -> {
                    TileEntityMekanism upgradedTile = requiredTile(helper, UPGRADED_MACHINE);
                    helper.assertTrue(upgradedTile instanceof MeUpgradeableMachine upgradeable
                                    && upgradeable.isMeUpgradeActive(),
                            "pattern provider upgrade did not activate before the Jade check");
                    assertJadeOnline(helper, upgradedTile, "upgraded Mekanism machine");
                    assertJadeOnline(helper, requiredTile(helper, ME_FACTORY), "ME factory");
                    assertBaseMachinePatternSlots(helper, requiredTile(helper, ME_BASE_MACHINE));
                })
                .thenSucceed();
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
}
