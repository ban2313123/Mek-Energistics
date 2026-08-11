package com.beipuo.mekenergistics.registry;

import com.beipuo.mekenergistics.MekEnergistics;
import com.beipuo.mekenergistics.upgrade.MeUpgradeData;
import com.beipuo.mekenergistics.upgrade.MeUpgradePersistence;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Data components registered by Mek-Energistics. */
public final class ModDataComponents {
    private static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, MekEnergistics.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<MeUpgradeData>> ME_UPGRADES =
            DATA_COMPONENT_TYPES.register("me_upgrades",
                    () -> DataComponentType.<MeUpgradeData>builder()
                            .persistent(MeUpgradePersistence.CODEC)
                            .networkSynchronized(MeUpgradePersistence.STREAM_CODEC)
                            .build());

    private ModDataComponents() {
    }

    public static void register(IEventBus eventBus) {
        DATA_COMPONENT_TYPES.register(eventBus);
    }
}
