package com.beipuo.mekenergistics.compat.provider;

import com.beipuo.mekenergistics.MekEnergistics;
import com.beipuo.mekenergistics.compat.catalog.CompatMachineCatalog;
import com.beipuo.mekenergistics.compat.catalog.CompatMod;
import java.lang.reflect.InvocationTargetException;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.world.item.ItemStack;

public final class CompatMachineProviders {
    private static final Map<CompatMod, String> PROVIDER_CLASSES = Map.of(
            CompatMod.MEKANISM, "com.beipuo.mekenergistics.compat.provider.MekanismMachineProvider",
            CompatMod.MEKMM, "com.beipuo.mekenergistics.compat.provider.MekmmMachineProvider",
            CompatMod.MEKE, "com.beipuo.mekenergistics.compat.provider.MekeMachineProvider",
            CompatMod.EMEK, "com.beipuo.mekenergistics.compat.provider.EmekMachineProvider",
            CompatMod.EMEKE, "com.beipuo.mekenergistics.compat.provider.EmekeMachineProvider");
    private static final Map<CompatMod, CompatMachineProvider> PROVIDERS = new EnumMap<>(CompatMod.class);

    private CompatMachineProviders() {
    }

    public static CompatMachineProvider get(CompatMod mod) {
        synchronized (PROVIDERS) {
            return PROVIDERS.computeIfAbsent(mod, CompatMachineProviders::load);
        }
    }

    public static Stream<CompatMachineProvider> available() {
        return CompatMachineCatalog.available()
                .map(spec -> spec.provider())
                .distinct()
                .map(CompatMachineProviders::get);
    }

    public static boolean isInstaller(ItemStack stack) {
        return available().anyMatch(provider -> provider.isInstaller(stack));
    }

    private static CompatMachineProvider load(CompatMod mod) {
        String className = PROVIDER_CLASSES.get(mod);
        try {
            return (CompatMachineProvider) Class.forName(
                            className, true, CompatMachineProviders.class.getClassLoader())
                    .getDeclaredConstructor()
                    .newInstance();
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException
                | InvocationTargetException | LinkageError exception) {
            MekEnergistics.LOGGER.error("Unable to load machine compatibility provider {} for {}", className, mod, exception);
            throw new IllegalStateException("Unable to load machine compatibility provider for " + mod, exception);
        }
    }
}
