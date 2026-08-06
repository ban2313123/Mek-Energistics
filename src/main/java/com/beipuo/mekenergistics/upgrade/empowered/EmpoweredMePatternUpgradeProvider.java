package com.beipuo.mekenergistics.upgrade.empowered;

import com.beipuo.mekenergistics.upgrade.MePatternProviderUpgrade;
import com.beipuo.mekenergistics.upgrade.MePatternUpgradeLang;
import dev.lapis256.mekanism_empowered.core.api.upgrade.AdditionalUpgrade;
import dev.lapis256.mekanism_empowered.core.api.upgrade.AdditionalUpgradeDelegate;
import dev.lapis256.mekanism_empowered.core.api.upgrade.IAdditionalUpgrades;
import dev.lapis256.mekanism_empowered.core.common.upgrade.UpgradeItemRegistry;
import mekanism.api.text.EnumColor;
import mekanism.common.registries.MekanismBlockTypes;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import dev.lapis256.mekanism_empowered.core.common.util.AdditionalUpgradeUtil;
import com.beipuo.mekenergistics.blockentity.api.MeUpgradeableMachine;
import com.beipuo.mekenergistics.blockentity.api.MeAeMachine;
import java.lang.reflect.Proxy;
import java.util.Set;
import mekanism.api.Upgrade;

public final class EmpoweredMePatternUpgradeProvider implements IAdditionalUpgrades {
    public static final AdditionalUpgradeDelegate ME_PATTERN_PROVIDER = AdditionalUpgrade.register(
            MePatternProviderUpgrade.INTERNAL_NAME,
            MePatternProviderUpgrade.SERIALIZED_NAME,
            MePatternUpgradeLang.NAME,
            MePatternUpgradeLang.DESCRIPTION,
            1,
            EnumColor.AQUA);

    public static void registerItem(Holder<Item> item) {
        UpgradeItemRegistry.register(ME_PATTERN_PROVIDER, item);
    }

    public static void registerSupportedUpgrade() {
        Upgrade upgrade = findUpgrade();
        AdditionalUpgradeUtil.addSupported(MekanismBlockTypes.ENRICHMENT_CHAMBER, upgrade);
        registerFallbackProvider(upgrade);
    }

    private static void registerFallbackProvider(Upgrade upgrade) {
        try {
            Class<?> function = Class.forName("kotlin.jvm.functions.Function1");
            Object provider = Proxy.newProxyInstance(function.getClassLoader(), new Class<?>[]{function},
                    (proxy, method, args) -> {
                        if (method.getName().equals("invoke") && args != null && args.length == 1) {
                            Object tile = args[0];
                            return tile instanceof MeAeMachine machine
                                    && (!(machine instanceof MeUpgradeableMachine upgradeable)
                                            || upgradeable.isMeUpgradeTarget())
                                    ? Set.of(upgrade)
                                    : Set.of();
                        }
                        return switch (method.getName()) {
                            case "hashCode" -> System.identityHashCode(proxy);
                            case "equals" -> args != null && args.length == 1 && proxy == args[0];
                            case "toString" -> "MekEnergisticsMeUpgradeFallback";
                            default -> null;
                        };
                    });
            Class<?> registry = Class.forName(
                    "dev.lapis256.mekanism_empowered.core.common.util.TileUpgradeSupportFallbackRegistry");
            Object instance = registry.getField("INSTANCE").get(null);
            registry.getMethod("registerSupportedUpgradeProvider", function).invoke(instance, provider);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to register Empowered fallback upgrade support", e);
        }
    }

    private static Upgrade findUpgrade() {
        for (Upgrade upgrade : Upgrade.values()) {
            if (MePatternProviderUpgrade.SERIALIZED_NAME.equals(upgrade.getSerializedName())) {
                return upgrade;
            }
        }
        throw new IllegalStateException("ME pattern provider upgrade was not registered");
    }
}
