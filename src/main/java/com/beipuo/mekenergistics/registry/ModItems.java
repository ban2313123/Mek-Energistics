package com.beipuo.mekenergistics.registry;

import com.beipuo.mekenergistics.common.machine.MeMekanismMachine;
import com.beipuo.mekenergistics.item.MeMachineBlockItem;
import com.beipuo.mekenergistics.item.MeTierInstallerItem;
import com.beipuo.mekenergistics.item.MeUpgradeItem;
import com.beipuo.mekenergistics.upgrade.MeUpgradeType;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;

public final class ModItems {
    private static final MeItemDeferredRegister ITEMS = new MeItemDeferredRegister();
    public static final DeferredItem<MeTierInstallerItem> ME_FACTORY_INSTALLER =
            ITEMS.register("me_factory_installer", () -> new MeTierInstallerItem(new Item.Properties()));
    public static final DeferredItem<MeUpgradeItem> ME_PATTERN_PROVIDER_UPGRADE =
            registerUpgrade("upgrade_me_pattern_provider", MeUpgradeType.PATTERN_PROVIDER);
    public static final DeferredItem<MeUpgradeItem> ME_PASSIVE_CRAFTING_UPGRADE =
            registerUpgrade("upgrade_me_passive_crafting", MeUpgradeType.PASSIVE_CRAFTING);
    public static final DeferredItem<MeUpgradeItem> ME_OUTPUT_INTERFACE_UPGRADE =
            registerUpgrade("upgrade_me_output_interface", MeUpgradeType.OUTPUT_INTERFACE);

    public static final DeferredItem<MeMachineBlockItem> ME_METALLURGIC_INFUSER = getMachineItem(MeMekanismMachine.METALLURGIC_INFUSER);

    private ModItems() {
    }

    private static DeferredItem<MeUpgradeItem> registerUpgrade(String name, MeUpgradeType type) {
        return ITEMS.register(name, () -> new MeUpgradeItem(type, new Item.Properties()));
    }

    public static DeferredItem<MeMachineBlockItem> getMachineItem(MeMekanismMachine machine) {
        return ModBlocks.getMachineItem(machine);
    }

    public static Iterable<DeferredItem<MeMachineBlockItem>> getMachineItems() {
        return ModBlocks.getMachineItems();
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
