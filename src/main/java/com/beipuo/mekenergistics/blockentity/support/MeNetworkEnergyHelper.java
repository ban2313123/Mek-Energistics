package com.beipuo.mekenergistics.blockentity.support;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.PowerUnit;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionSource;
import com.beipuo.mekenergistics.config.MekEnergisticsConfig;
import com.beipuo.mekenergistics.compat.OptionalCompatClasses;
import java.util.function.LongConsumer;
import java.util.function.LongUnaryOperator;
import java.util.function.Supplier;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

public final class MeNetworkEnergyHelper {
    private MeNetworkEnergyHelper() {
    }

    public interface LocalEnergyBuffer {
        long getLocalEnergy();

        long extractLocal(long amount, Action action, AutomationType automationType);
    }

    public static long extractWithLocalBuffer(MachineEnergyContainer<?> localEnergy, IGrid grid, IActionSource source, long amount,
            Action action, AutomationType automationType) {
        if (amount <= 0) {
            return 0;
        }
        if (automationType != AutomationType.INTERNAL || grid == null) {
            return extractLocal(localEnergy, amount, action, automationType);
        }
        if (!MekEnergisticsConfig.preferNetworkEnergy()) {
            long localExtracted = extractLocal(localEnergy, amount, action, automationType);
            return localExtracted + extractNetworkFe(grid, source, amount - localExtracted, action);
        }
        long networkExtracted = extractNetworkFe(grid, source, amount, action);
        return networkExtracted + extractLocal(localEnergy, amount - networkExtracted, action, automationType);
    }

    private static long extractLocal(MachineEnergyContainer<?> localEnergy, long amount, Action action, AutomationType automationType) {
        if (amount <= 0) {
            return 0;
        }
        return localEnergy instanceof LocalEnergyBuffer buffer ? buffer.extractLocal(amount, action, automationType)
                : localEnergy.extract(amount, action, automationType);
    }

    /**
     * Per-grid, per-tick cache of the network-available FE. The recipe engine asks for the same
     * grid-wide value from every machine on that grid, many times per tick; answering each with a
     * live AE energy-service SIMULATE is the dominant cost that drops TPS. The value is stable
     * within a tick because SIMULATE never mutates the grid, so one query per grid per tick suffices.
     */
    private static final java.util.Map<IGrid, long[]> gridAvailableCache = new java.util.WeakHashMap<>();

    /** Returns the current server tick, or -1 when no server is available (e.g. client side). */
    public static long currentGameTick() {
        net.minecraft.server.MinecraftServer server = net.neoforged.fml.common.ServerLifecycleHooks.getCurrentServer();
        return server == null ? -1 : server.getTickCount();
    }

    /**
     * Network-available FE for {@code grid}, cached for the current game tick. The amount is a fixed
     * large query (independent of any single machine's buffer) so the cached grid-wide value is the
     * same no matter which machine populated it; otherwise a machine with a huge local buffer would
     * ask for less and poison the cache for every other machine on the grid.
     */
    private static final long NETWORK_QUERY_AMOUNT = Long.MAX_VALUE / 2;

    public static long networkAvailableFe(IGrid grid, IActionSource source, long local, long gameTick) {
        if (grid == null) {
            return 0;
        }
        if (gameTick >= 0) {
            long[] cached = gridAvailableCache.get(grid);
            if (cached != null && cached[0] == gameTick) {
                return cached[1];
            }
        }
        long available = extractNetworkFe(grid, source, NETWORK_QUERY_AMOUNT, Action.SIMULATE);
        if (gameTick >= 0) {
            gridAvailableCache.put(grid, new long[]{gameTick, available});
        }
        return available;
    }

    public static long availableWithLocalBuffer(MachineEnergyContainer<?> localEnergy, IGrid grid, IActionSource source) {
        long local = localEnergy instanceof LocalEnergyBuffer buffer ? buffer.getLocalEnergy() : localEnergy.getEnergy();
        if (grid == null) {
            return local;
        }
        long net = networkAvailableFe(grid, source, local, currentGameTick());
        return totalAvailableEnergy(local, net);
    }

    static long totalAvailableEnergy(long local, long network) {
        if (local <= 0) {
            return Math.max(0, network);
        }
        if (network <= 0) {
            return local;
        }
        return local > Long.MAX_VALUE - network ? Long.MAX_VALUE : local + network;
    }

    public static long extractNetworkFe(IGrid grid, IActionSource source, long requestedFe, Action action) {
        if (requestedFe <= 0 || grid == null) {
            return 0;
        }
        Actionable actionable = action.execute() ? Actionable.MODULATE : Actionable.SIMULATE;
        long extracted = 0;
        boolean hasAppliedFlux = OptionalCompatClasses.hasAppliedFlux();
        boolean appliedFluxFirst = hasAppliedFlux && MekEnergisticsConfig.preferAppliedFluxNetworkFe();
        if (appliedFluxFirst) {
            extracted = AppliedFluxEnergyBridge.extractFe(grid, requestedFe, actionable, source);
            requestedFe -= extracted;
        }
        long aeExtracted = extractAeEnergyAsFe(grid, requestedFe, actionable);
        extracted += aeExtracted;
        requestedFe -= aeExtracted;
        if (hasAppliedFlux && !appliedFluxFirst) {
            extracted += AppliedFluxEnergyBridge.extractFe(grid, requestedFe, actionable, source);
        }
        return extracted;
    }

    public static long refillLocalEnergy(IEnergyContainer localEnergy, IGrid grid, IActionSource source) {
        if (localEnergy == null || grid == null) {
            return 0;
        }
        return refillEnergyBuffer(localEnergy.getEnergy(), localEnergy.getMaxEnergy(),
                requested -> extractNetworkFe(grid, source, requested, Action.EXECUTE), localEnergy::setEnergy);
    }

    static long refillEnergyBuffer(long currentEnergy, long maxEnergy, LongUnaryOperator networkExtraction,
            LongConsumer energySetter) {
        if (currentEnergy >= maxEnergy || maxEnergy <= 0) {
            return 0;
        }
        long normalizedCurrent = Math.max(0, currentEnergy);
        long remainingCapacity = maxEnergy - normalizedCurrent;
        long extracted = networkExtraction.applyAsLong(remainingCapacity);
        long injected = Math.min(remainingCapacity, Math.max(0, extracted));
        if (injected > 0) {
            energySetter.accept(normalizedCurrent + injected);
        }
        return injected;
    }

    public static IEnergyContainer recipeEnergyView(MachineEnergyContainer<?> energyContainer, Supplier<IGrid> gridSupplier, IActionSource source) {
        return new NetworkRecipeEnergyView(energyContainer, gridSupplier, source);
    }

    public static class NetworkBackedEnergyContainer<TILE extends TileEntityMekanism> extends MachineEnergyContainer<TILE>
            implements LocalEnergyBuffer {
        private final Supplier<IGrid> gridSupplier;
        private final Supplier<IActionSource> actionSourceSupplier;

        public NetworkBackedEnergyContainer(TILE owner, IContentsListener listener, Supplier<IGrid> gridSupplier, IActionSource actionSource) {
            this(owner, listener, gridSupplier, () -> actionSource);
        }

        public NetworkBackedEnergyContainer(TILE owner, IContentsListener listener, Supplier<IGrid> gridSupplier, Supplier<IActionSource> actionSourceSupplier) {
            super(MachineEnergyContainer.validateBlock(owner).getStorage(), MachineEnergyContainer.validateBlock(owner).getUsage(),
                    BasicEnergyContainer.notExternal, ConstantPredicates.alwaysTrue(), owner, listener);
            this.gridSupplier = gridSupplier;
            this.actionSourceSupplier = actionSourceSupplier;
        }

        @Override
        public long extract(long amount, Action action, AutomationType automationType) {
            IGrid grid = this.gridSupplier == null ? null : this.gridSupplier.get();
            IActionSource actionSource = this.actionSourceSupplier == null ? null : this.actionSourceSupplier.get();
            return extractWithLocalBuffer(this, grid, actionSource, amount, action, automationType);
        }

        @Override
        public long getLocalEnergy() {
            return super.getEnergy();
        }

        @Override
        public long extractLocal(long amount, Action action, AutomationType automationType) {
            return super.extract(amount, action, automationType);
        }
    }

    private static long extractAeEnergyAsFe(IGrid grid, long requestedFe, Actionable action) {
        if (requestedFe <= 0) {
            return 0;
        }
        double requestedAe = PowerUnit.FE.convertTo(PowerUnit.AE, requestedFe);
        double extractedAe = grid.getEnergyService().extractAEPower(requestedAe, action, PowerMultiplier.ONE);
        return Math.min(requestedFe, (long) Math.floor(PowerUnit.AE.convertTo(PowerUnit.FE, extractedAe)));
    }

    private static final class NetworkRecipeEnergyView implements IEnergyContainer {
        private final MachineEnergyContainer<?> energyContainer;
        private final Supplier<IGrid> gridSupplier;
        private final IActionSource actionSource;

        private NetworkRecipeEnergyView(MachineEnergyContainer<?> energyContainer, Supplier<IGrid> gridSupplier, IActionSource actionSource) {
            this.energyContainer = energyContainer;
            this.gridSupplier = gridSupplier;
            this.actionSource = actionSource;
        }

        @Override
        public long getEnergy() {
            return availableWithLocalBuffer(this.energyContainer, this.gridSupplier.get(), this.actionSource);
        }

        @Override
        public void setEnergy(long energy) {
            this.energyContainer.setEnergy(energy);
        }

        @Override
        public long extract(long amount, Action action, AutomationType automationType) {
            return extractWithLocalBuffer(this.energyContainer, this.gridSupplier.get(), this.actionSource, amount, action, automationType);
        }

        @Override
        public long getMaxEnergy() {
            return this.energyContainer.getMaxEnergy();
        }

        @Override
        public void onContentsChanged() {
            this.energyContainer.onContentsChanged();
        }

        @Override
        public CompoundTag serializeNBT(HolderLookup.Provider provider) {
            return this.energyContainer.serializeNBT(provider);
        }

        @Override
        public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
            this.energyContainer.deserializeNBT(provider, nbt);
        }
    }
}
