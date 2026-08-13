package com.beipuo.mekenergistics.upgrade;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import mekanism.common.tile.component.TileComponentUpgrade;
import org.jetbrains.annotations.NotNull;

/**
 * Shared {@link MeUpgradeStateOwner} implementation for tiles that hold a container directly
 * (native ME machines and the two direct upgrade Mixins). The runtime-based adapter path uses its
 * own owner inside {@link MeUpgradeRecipeMachineRuntime}.
 */
public final class MeUpgradeStateOwnerSupport implements MeUpgradeStateOwner {
    private final MeUpgradeContainer container;
    private final BooleanSupplier nativePatternProvider;
    private final Supplier<Boolean> patternInventoryEmpty;
    private final Supplier<Boolean> interfaceInventoryEmpty;
    private final Runnable onChange;

    public MeUpgradeStateOwnerSupport(@NotNull BooleanSupplier nativePatternProvider,
            @NotNull Supplier<Boolean> patternInventoryEmpty, @NotNull Runnable onChange) {
        this(nativePatternProvider, patternInventoryEmpty, () -> true, onChange, () -> null);
    }

    public MeUpgradeStateOwnerSupport(@NotNull BooleanSupplier nativePatternProvider,
            @NotNull Supplier<Boolean> patternInventoryEmpty, @NotNull Runnable onChange,
            @NotNull Supplier<TileComponentUpgrade> nativeComponent) {
        this(nativePatternProvider, patternInventoryEmpty, () -> true, onChange, nativeComponent);
    }

    public MeUpgradeStateOwnerSupport(@NotNull BooleanSupplier nativePatternProvider,
            @NotNull Supplier<Boolean> patternInventoryEmpty,
            @NotNull Supplier<Boolean> interfaceInventoryEmpty, @NotNull Runnable onChange,
            @NotNull Supplier<TileComponentUpgrade> nativeComponent) {
        this.nativePatternProvider = Objects.requireNonNull(nativePatternProvider, "nativePatternProvider");
        this.patternInventoryEmpty = Objects.requireNonNull(patternInventoryEmpty, "patternInventoryEmpty");
        this.interfaceInventoryEmpty = Objects.requireNonNull(interfaceInventoryEmpty, "interfaceInventoryEmpty");
        this.onChange = Objects.requireNonNull(onChange, "onChange");
        this.container = new MeUpgradeContainer(this, () -> {
        }, nativeComponent);
    }

    @Override
    public MeUpgradeContainer getMeUpgradeContainer() {
        return this.container;
    }

    @Override
    public boolean supportsNativePatternProvider() {
        return this.nativePatternProvider.getAsBoolean();
    }

    @Override
    public boolean isPatternInventoryEmpty() {
        return Boolean.TRUE.equals(this.patternInventoryEmpty.get());
    }

    @Override
    public boolean isInterfaceInventoryEmpty() {
        return Boolean.TRUE.equals(this.interfaceInventoryEmpty.get());
    }

    @Override
    public void onMeUpgradeStateChanged() {
        this.onChange.run();
    }
}
