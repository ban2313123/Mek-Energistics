package com.beipuo.mekenergistics.upgrade;

import com.beipuo.mekenergistics.MekEnergistics;
import com.beipuo.mekenergistics.compat.OptionalCompatClasses;
import com.beipuo.mekenergistics.registry.ModItems;
import java.lang.reflect.InvocationTargetException;
import mekanism.api.Upgrade;
import net.minecraft.core.Holder;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

public final class MePatternProviderUpgrade {
    public static final String INTERNAL_NAME = "ME_PATTERN_PROVIDER";
    public static final String SERIALIZED_NAME = "me_pattern_provider";
    public static final String EMPOWERED_CORE_MOD_ID = "mekanism_empowered_core";

    private static Upgrade standaloneUpgrade;

    private MePatternProviderUpgrade() {
    }

    public static Upgrade get() {
        if (standaloneUpgrade != null) {
            return standaloneUpgrade;
        }
        for (Upgrade upgrade : Upgrade.values()) {
            if (SERIALIZED_NAME.equals(upgrade.getSerializedName())) {
                return upgrade;
            }
        }
        throw new IllegalStateException("ME pattern provider upgrade was not added to Mekanism's Upgrade enum");
    }

    public static void setStandaloneUpgrade(Upgrade upgrade) {
        if (standaloneUpgrade != null) {
            throw new IllegalStateException("ME pattern provider upgrade was initialized twice");
        }
        standaloneUpgrade = upgrade;
    }

    public static Backend backend(boolean empoweredCoreLoaded) {
        return empoweredCoreLoaded ? Backend.EMPOWERED_CORE : Backend.STANDALONE_MIXIN;
    }

    public static void commonSetup(FMLCommonSetupEvent event) {
        if (backend(OptionalCompatClasses.hasMekanismEmpoweredCore()) == Backend.EMPOWERED_CORE) {
            event.enqueueWork(MePatternProviderUpgrade::registerEmpoweredItemMapping);
        }
    }

    public static void registerEmpoweredSupportedUpgrade() {
        if (backend(OptionalCompatClasses.hasMekanismEmpoweredCore()) != Backend.EMPOWERED_CORE) {
            return;
        }
        try {
            Class<?> provider = Class.forName(
                    "com.beipuo.mekenergistics.upgrade.empowered.EmpoweredMePatternUpgradeProvider");
            provider.getMethod("registerSupportedUpgrade").invoke(null);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            MekEnergistics.LOGGER.error("Failed to register ME pattern provider block support early", e);
        }
    }

    private static void registerEmpoweredItemMapping() {
        try {
            Class<?> provider = Class.forName(
                    "com.beipuo.mekenergistics.upgrade.empowered.EmpoweredMePatternUpgradeProvider");
            provider.getMethod("registerItem", Holder.class)
                    .invoke(null, ModItems.ME_PATTERN_PROVIDER_UPGRADE);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            MekEnergistics.LOGGER.error("Failed to register the ME pattern provider upgrade item with Empowered Core", e);
        }
    }

    public enum Backend {
        STANDALONE_MIXIN,
        EMPOWERED_CORE
    }
}
