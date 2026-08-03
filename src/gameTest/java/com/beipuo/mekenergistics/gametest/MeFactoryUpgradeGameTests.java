package com.beipuo.mekenergistics.gametest;

import com.beipuo.mekenergistics.MekEnergistics;
import com.beipuo.mekenergistics.blockentity.api.MeAeMachine;
import com.beipuo.mekenergistics.blockentity.api.MeFactoryIoOwner;
import com.beipuo.mekenergistics.blockentity.api.MeUpgradeableMachine;
import com.beipuo.mekenergistics.compat.catalog.CompatMachineCatalog;
import com.beipuo.mekenergistics.registry.ModItems;
import com.beipuo.mekenergistics.upgrade.MePatternProviderUpgrade;
import java.util.List;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(MekEnergistics.MODID)
@PrefixGameTestTemplate(false)
public final class MeFactoryUpgradeGameTests {
    private static final List<String> FACTORY_ROOTS = List.of(
            "mekanism:basic_enriching_factory",
            "mekanism_extras:absolute_enriching_factory",
            "mekmm:basic_recycling_factory",
            "mekanism_extras:absolute_recycling_factory",
            "mekmm:basic_oxidizing_factory",
            "mekanism_extras:absolute_oxidizing_factory",
            "emextras:absolute_overclocked_enriching_factory",
            "emextras:absolute_overclocked_recycling_factory",
            "emextras:absolute_overclocked_oxidizing_factory");
    private static final List<String> EMEKE_MORE_MACHINE_FACTORIES = List.of(
            "emextras:absolute_overclocked_recycling_factory",
            "emextras:absolute_overclocked_planting_factory",
            "emextras:absolute_overclocked_stamping_factory",
            "emextras:absolute_overclocked_lathing_factory",
            "emextras:absolute_overclocked_rolling_mill_factory",
            "emextras:absolute_overclocked_replicating_factory");

    private MeFactoryUpgradeGameTests() {
    }

    @GameTest(template = "empty_3x3x3", timeoutTicks = 4 * SharedConstants.TICKS_PER_SECOND)
    public static void factoryRootsActivateFromStandardSlot(GameTestHelper helper) {
        for (int index = 0; index < FACTORY_ROOTS.size(); index++) {
            helper.setBlock(position(index), requiredBlock(FACTORY_ROOTS.get(index)));
        }

        helper.startSequence()
                .thenExecuteAfter(1, () -> FACTORY_ROOTS.forEach(id -> installUpgrade(helper, FACTORY_ROOTS, id)))
                .thenExecuteAfter(SharedConstants.TICKS_PER_SECOND + 2,
                        () -> FACTORY_ROOTS.forEach(id -> assertActiveFactory(helper, FACTORY_ROOTS, id)))
                .thenExecute(() -> FACTORY_ROOTS.forEach(id -> removeUpgrade(helper, FACTORY_ROOTS, id)))
                .thenExecuteAfter(2,
                        () -> FACTORY_ROOTS.forEach(id -> assertInactiveFactory(helper, FACTORY_ROOTS, id)))
                .thenSucceed();
    }

    @GameTest(template = "empty_3x3x3", timeoutTicks = 4 * SharedConstants.TICKS_PER_SECOND)
    public static void combinedMoreMachineFactoriesActivateFromStandardSlot(GameTestHelper helper) {
        for (int index = 0; index < EMEKE_MORE_MACHINE_FACTORIES.size(); index++) {
            helper.setBlock(position(index), requiredBlock(EMEKE_MORE_MACHINE_FACTORIES.get(index)));
        }

        helper.startSequence()
                .thenExecuteAfter(1, () -> EMEKE_MORE_MACHINE_FACTORIES.forEach(
                        id -> installUpgrade(helper, EMEKE_MORE_MACHINE_FACTORIES, id)))
                .thenExecuteAfter(SharedConstants.TICKS_PER_SECOND + 2,
                        () -> EMEKE_MORE_MACHINE_FACTORIES.forEach(
                                id -> assertActiveFactory(helper, EMEKE_MORE_MACHINE_FACTORIES, id)))
                .thenSucceed();
    }

    @GameTest(template = "empty_3x3x3", timeoutTicks = 2 * SharedConstants.TICKS_PER_SECOND)
    public static void legacyMeFactoryRemainsAlwaysOn(GameTestHelper helper) {
        String id = "mekenergistics:me_basic_enriching_factory";
        BlockPos position = new BlockPos(1, 1, 1);
        helper.setBlock(position, requiredBlock(id));
        helper.startSequence()
                .thenExecuteAfter(2, () -> {
                    BlockEntity blockEntity = helper.getBlockEntity(position);
                    helper.assertTrue(blockEntity instanceof MeFactoryIoOwner,
                            id + " must retain its legacy ME factory contract");
                    MeFactoryIoOwner legacy = (MeFactoryIoOwner) blockEntity;
                    helper.assertTrue(!legacy.getPatternSlots().isEmpty(),
                            id + " must retain permanent pattern slots");
                    helper.assertTrue(legacy.getMainNode().getNode() != null,
                            id + " must create its permanent AE node without an upgrade");
                })
                .thenSucceed();
    }

    @GameTest(template = "empty_3x3x3", timeoutTicks = 4 * SharedConstants.TICKS_PER_SECOND)
    public static void machineUpgradePageOne(GameTestHelper helper) {
        runMachineUpgradePage(helper, 0);
    }

    @GameTest(template = "empty_3x3x3", timeoutTicks = 4 * SharedConstants.TICKS_PER_SECOND)
    public static void machineUpgradePageTwo(GameTestHelper helper) {
        runMachineUpgradePage(helper, 1);
    }

    @GameTest(template = "empty_3x3x3", timeoutTicks = 4 * SharedConstants.TICKS_PER_SECOND)
    public static void machineUpgradePageThree(GameTestHelper helper) {
        runMachineUpgradePage(helper, 2);
    }

    @GameTest(template = "empty_3x3x3", timeoutTicks = 4 * SharedConstants.TICKS_PER_SECOND)
    public static void machineUpgradePageFour(GameTestHelper helper) {
        runMachineUpgradePage(helper, 3);
    }

    @GameTest(template = "empty_3x3x3", timeoutTicks = 4 * SharedConstants.TICKS_PER_SECOND)
    public static void machineUpgradePageFive(GameTestHelper helper) {
        runMachineUpgradePage(helper, 4);
    }

    private static void runMachineUpgradePage(GameTestHelper helper, int page) {
        List<String> roots = CompatMachineCatalog.available()
                .filter(spec -> !spec.machine().isFactory())
                .map(spec -> spec.sourceBlockId().toString())
                .toList();
        int start = page * 9;
        if (start >= roots.size()) {
            helper.succeed();
            return;
        }
        List<String> pageRoots = roots.subList(start, Math.min(start + 9, roots.size()));
        for (int index = 0; index < pageRoots.size(); index++) {
            helper.setBlock(position(index), requiredBlock(pageRoots.get(index)));
        }
        helper.startSequence()
                .thenExecuteAfter(1, () -> pageRoots.forEach(id -> installUpgrade(helper, pageRoots, id)))
                .thenExecuteAfter(SharedConstants.TICKS_PER_SECOND + 2,
                        () -> pageRoots.forEach(id -> assertActiveFactory(helper, pageRoots, id)))
                .thenExecute(() -> pageRoots.forEach(id -> removeUpgrade(helper, pageRoots, id)))
                .thenExecuteAfter(2,
                        () -> pageRoots.forEach(id -> assertInactiveFactory(helper, pageRoots, id)))
                .thenSucceed();
    }

    private static void installUpgrade(GameTestHelper helper, List<String> roots, String id) {
        TileEntityMekanism tile = requiredTile(helper, roots, id);
        if (!(tile instanceof MeUpgradeableMachine machine)) {
            throw new GameTestAssertException(id + " is missing its ME upgrade adapter");
        }
        helper.assertTrue(machine.isMeUpgradeTarget(), id + " does not resolve to an upgrade profile");
        helper.assertTrue(!machine.isMeUpgradeActive(), id + " activated before the upgrade was installed");
        helper.assertTrue(tile.getComponent().supports(MePatternProviderUpgrade.get()),
                id + " does not expose the standard ME upgrade slot support");
        tile.getComponent().getUpgradeSlot().setStack(ModItems.ME_PATTERN_PROVIDER_UPGRADE.toStack());
    }

    private static void assertActiveFactory(GameTestHelper helper, List<String> roots, String id) {
        TileEntityMekanism tile = requiredTile(helper, roots, id);
        MeUpgradeableMachine machine = (MeUpgradeableMachine) tile;
        helper.assertTrue(tile.getComponent().isUpgradeInstalled(MePatternProviderUpgrade.get()),
                id + " did not install the ME upgrade from its standard slot");
        helper.assertTrue(machine.isMeUpgradeActive(), id + " did not activate after upgrade installation");
        helper.assertTrue(!machine.getPatternSlots().isEmpty(), id + " has no pattern slots");
        helper.assertTrue(machine.getPatternInputLayout() != null, id + " has no pattern input layout");
        helper.assertTrue(!machine.getPatternOutputPorts().isEmpty(), id + " has no pattern output ports");
        helper.assertTrue(((MeAeMachine) machine).getMainNode().getNode() != null,
                id + " did not create its AE node");
    }

    private static void removeUpgrade(GameTestHelper helper, List<String> roots, String id) {
        requiredTile(helper, roots, id).getComponent().removeUpgrade(MePatternProviderUpgrade.get(), true);
    }

    private static void assertInactiveFactory(GameTestHelper helper, List<String> roots, String id) {
        MeUpgradeableMachine machine = (MeUpgradeableMachine) requiredTile(helper, roots, id);
        helper.assertTrue(!machine.isMeUpgradeActive(), id + " remained active after upgrade removal");
        helper.assertTrue(machine.getMainNode().getNode() == null,
                id + " retained its AE node after upgrade removal");
    }

    private static TileEntityMekanism requiredTile(GameTestHelper helper, List<String> roots, String id) {
        int index = roots.indexOf(id);
        BlockEntity blockEntity = helper.getBlockEntity(position(index));
        if (blockEntity instanceof TileEntityMekanism tile) {
            return tile;
        }
        throw new GameTestAssertException(id + " did not create a Mekanism block entity");
    }

    private static BlockPos position(int index) {
        return new BlockPos(index % 3, 1, index / 3);
    }

    private static Block requiredBlock(String id) {
        ResourceLocation key = ResourceLocation.parse(id);
        Block block = BuiltInRegistries.BLOCK.get(key);
        if (block == Blocks.AIR) {
            throw new IllegalStateException("Missing required factory block " + id);
        }
        return block;
    }
}
