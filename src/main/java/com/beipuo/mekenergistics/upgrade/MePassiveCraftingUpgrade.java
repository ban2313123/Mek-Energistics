package com.beipuo.mekenergistics.upgrade;

import mekanism.api.Upgrade;
import com.beipuo.mekenergistics.MekEnergistics;
import com.beipuo.mekenergistics.compat.OptionalCompatClasses;
import com.beipuo.mekenergistics.registry.ModItems;
import java.lang.reflect.InvocationTargetException;
import net.minecraft.core.Holder;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

public final class MePassiveCraftingUpgrade {
    public static final String INTERNAL_NAME = "ME_PASSIVE_CRAFTING";
    public static final String SERIALIZED_NAME = "me_passive_crafting";

    private static Upgrade standaloneUpgrade;

    private MePassiveCraftingUpgrade() {
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
        throw new IllegalStateException("ME passive crafting upgrade was not added to Mekanism's Upgrade enum");
    }

    public static void setStandaloneUpgrade(Upgrade upgrade) {
        if (standaloneUpgrade != null) {
            throw new IllegalStateException("ME passive crafting upgrade was initialized twice");
        }
        standaloneUpgrade = upgrade;
    }

    public static void commonSetup(FMLCommonSetupEvent event) {
        if (OptionalCompatClasses.hasMekanismEmpoweredCore()) event.enqueueWork(() -> {
            registerEmpoweredSupportedUpgrade();
            registerEmpoweredItemMapping();
        });
    }

    public static void registerEmpoweredSupportedUpgrade() {
        if (!OptionalCompatClasses.hasMekanismEmpoweredCore()) return;
        invoke("registerSupportedUpgrade");
    }

    private static void registerEmpoweredItemMapping() {
        invoke("registerItem", Holder.class, ModItems.ME_PASSIVE_CRAFTING_UPGRADE);
    }

    private static void invoke(String method, Class<?> parameter, Object value) {
        try {
            Class<?> provider = Class.forName("com.beipuo.mekenergistics.upgrade.empowered.EmpoweredMePassiveCraftingUpgradeProvider");
            provider.getMethod(method, parameter).invoke(null, value);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            MekEnergistics.LOGGER.error("Failed to register ME passive crafting upgrade", e);
        }
    }

    private static void invoke(String method) {
        try {
            Class<?> provider = Class.forName("com.beipuo.mekenergistics.upgrade.empowered.EmpoweredMePassiveCraftingUpgradeProvider");
            provider.getMethod(method).invoke(null);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            MekEnergistics.LOGGER.error("Failed to register ME passive crafting upgrade", e);
        }
    }
}
