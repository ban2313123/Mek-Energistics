package com.beipuo.mekenergistics.blockentity.support;

import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.networking.GridFlags;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IManagedGridNode;
import appeng.api.config.Actionable;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageService;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageHelper;
import com.beipuo.mekenergistics.MekEnergistics;
import com.beipuo.mekenergistics.blockentity.api.MePatternIoOwner;
import com.beipuo.mekenergistics.blockentity.slot.MePatternInventorySlot;
import com.beipuo.mekenergistics.blockentity.support.io.MeInputLayout;
import com.beipuo.mekenergistics.blockentity.support.io.MeOutputPort;
import com.beipuo.mekenergistics.blockentity.support.io.MePatternIoAdapter;
import com.beipuo.mekenergistics.config.MekEnergisticsConfig;
import com.beipuo.mekenergistics.upgrade.MePassiveCraftingDispatcher;
import com.beipuo.mekenergistics.upgrade.MePassiveCraftingSettings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import mekanism.api.energy.IEnergyContainer;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;

public abstract class AbstractMeAeSupport<O extends MePatternIoOwner> {
    public static final int AE_PATTERN_SCHEMA = 2;
    private static final String TAG_PATTERN_SCHEMA = "AePatternSchema";
    private static final String TAG_PATTERN_TERMINAL_NAME = "PatternTerminalName";
    private static final String TAG_TERMINAL_VISIBLE = "PatternTerminalVisible";
    private static final String TAG_NODE = "node";

    protected final O owner;
    protected final TileEntityMekanism ownerTile;
    protected final IActionSource actionSource;
    protected final List<BasicInventorySlot> patternSlots;
    private final List<BasicInventorySlot> patternSlotsView;
    protected final List<IPatternDetails> patterns = new ArrayList<>();
    protected final MeSmartPatternMultiplication smartPatternMultiplication = new MeSmartPatternMultiplication();
    private final MePassiveCraftingSettings passiveCraftingSettings = new MePassiveCraftingSettings();
    private Boolean clientPassiveCraftingEnabled;

    protected IManagedGridNode mainNode;
    protected int patternPriority;
    protected String patternTerminalName = "";
    protected boolean visibleInPatternAccessTerminal = true;

    private NodeState nodeState = NodeState.NEW;
    private CompoundTag retainedNodeData;
    private final CraftingUpdateState craftingUpdateState = new CraftingUpdateState();
    private final Map<BlockPos, IManagedGridNode> largeMachinePortNodes = new HashMap<>();

    protected AbstractMeAeSupport(O owner) {
        this.owner = owner;
        this.ownerTile = owner.getAeOwnerTile();
        this.actionSource = IActionSource.ofMachine(owner);
        this.mainNode = createManagedNode();
        List<BasicInventorySlot> externalSlots = owner.getExternalPatternSlots();
        if (!externalSlots.isEmpty()) {
            this.patternSlots = externalSlots;
        } else {
            int slotCount = MekEnergisticsConfig.patternSlots();
            this.patternSlots = new ArrayList<>(slotCount);
            for (int i = 0; i < slotCount; i++) {
                this.patternSlots.add(MePatternInventorySlot.create(PatternDetailsHelper::isEncodedPattern, this::updatePatterns));
            }
        }
        this.patternSlotsView = Collections.unmodifiableList(this.patternSlots);
    }

    private IManagedGridNode createManagedNode() {
        IManagedGridNode node = GridHelper.createManagedNode(this.owner, new NodeListener())
                .setTagName(TAG_NODE)
                .setFlags(GridFlags.REQUIRE_CHANNEL)
                .addService(ICraftingProvider.class, this.owner)
                .addService(IGridTickable.class, new AeTicker());
        if (this.retainedNodeData != null) {
            node.loadFromNBT(this.retainedNodeData);
        }
        return node;
    }

    public final IManagedGridNode getMainNode() {
        return this.mainNode;
    }

    /**
     * @param side ignored — AE2 rejects any node that is not exposed on the side it asked for, and the
     *             node already carries the machine's real exposed faces.
     */
    public final IGridNode getLargeMachineGridNode(BlockPos position, Direction side) {
        IManagedGridNode port = this.largeMachinePortNodes.get(position);
        return port == null ? null : port.getNode();
    }

    /** Wrapped once: the backing list is final, and the inventory queries this on every call. */
    public final List<BasicInventorySlot> getPatternSlots() {
        return this.patternSlotsView;
    }

    /**
     * Physical pattern I/O is structural: every port is a stateless wrapper holding a final
     * reference to a slot or tank that the owner creates once and never replaces. Rebuilding it
     * allocates a port per slot/tank plus the enclosing lists, and the tick path queries it about
     * nine times per machine, so it is built once and reused.
     *
     * <p>Only the structure is cached. {@link MePatternIoOwner#isPatternBusy()} is per-tick state
     * and is always read live.
     */
    private MeInputLayout cachedInputLayout;
    private List<? extends MeOutputPort> cachedOutputPorts;

    private MeInputLayout patternInputLayout() {
        if (this.cachedInputLayout == null) {
            this.cachedInputLayout = this.owner.getPatternInputLayout();
        }
        return this.cachedInputLayout;
    }

    private List<? extends MeOutputPort> patternOutputPorts() {
        if (this.cachedOutputPorts == null) {
            this.cachedOutputPorts = List.copyOf(this.owner.getPatternOutputPorts());
        }
        return this.cachedOutputPorts;
    }

    /** Drops the cached I/O structure; call if an owner ever swaps a slot or tank instance. */
    public final void invalidatePatternIoCache() {
        this.cachedInputLayout = null;
        this.cachedOutputPorts = null;
    }

    public final MePatternIoAdapter getPatternIoAdapter() {
        return new MePatternIoAdapter(patternInputLayout(), patternOutputPorts(), this.owner.isPatternBusy());
    }

    public final boolean isPatternBusy() {
        return this.smartPatternMultiplication.hasPendingWork() || this.owner.isPatternBusy();
    }

    /** Returns whether the exact pattern or a same-definition registered pattern is available. */
    public final boolean hasRegisteredPattern(IPatternDetails patternDetails) {
        return patternDetails != null && (this.patterns.contains(patternDetails)
                || hasMatchingPatternDefinition(this.patterns, patternDetails));
    }

    /** Returns the physical input capacity available for a counted CPU submission. */
    public final long maxAcceptedCopies(KeyCounter[] oneCraftInputs) {
        if (oneCraftInputs == null || !this.mainNode.isActive() || isPatternBusy()) {
            return 0;
        }
        return patternInputLayout().maxAcceptedCopies(oneCraftInputs);
    }

    /** Routes pre-scaled counted inputs without invoking Mek-Energistics smart multiplication. */
    public final boolean routeDataPatternInputs(KeyCounter[] scaledInputs) {
        if (scaledInputs == null || !this.mainNode.isActive() || isPatternBusy()) {
            return false;
        }
        return routePatternInputs(scaledInputs);
    }

    public final boolean pushPatternWithAdapter(IPatternDetails patternDetails, KeyCounter[] inputs) {
        if (!this.mainNode.isActive() || patternDetails == null || inputs == null
                || this.owner.isPatternBusy()) {
            return false;
        }
        boolean exactPattern = this.patterns.contains(patternDetails);
        boolean registeredPattern = exactPattern || hasMatchingPatternDefinition(this.patterns, patternDetails);
        return dispatchWithSmartPatternFallback(
                exactPattern,
                registeredPattern,
                this.smartPatternMultiplication,
                patternDetails,
                inputs,
                () -> {
                    MekEnergistics.LOGGER.warn(
                            "Disabling smart pattern multiplication for {} after an incompatible crafting CPU batch",
                            this.owner.getGridNodePosition());
                    setSmartPatternMultiplicationEnabled(false);
                },
                () -> routePatternInputs(inputs));
    }

    /**
     * Compatibility fallback for CPU paths without the DataEnergistics counted-provider contract:
     * some addon CPUs pre-batch pattern inputs, but their multiplier cannot be coordinated with our
     * smart queue. If their input shape cannot be represented, disable this machine's multiplier
     * and accept the CPU-provided batch directly. Counted CPU integrations use their dedicated
     * admission contracts instead of this fallback.
     */
    static boolean dispatchWithSmartPatternFallback(boolean exactPattern, boolean registeredPattern,
            MeSmartPatternMultiplication multiplication, IPatternDetails patternDetails, KeyCounter[] inputs,
            Runnable disableMultiplication, BooleanSupplier directDispatch) {
        if (!registeredPattern) {
            return false;
        }
        if (multiplication.isEnabled() && exactPattern) {
            if (multiplication.enqueue(patternDetails, inputs)) {
                return true;
            }
        }
        if (multiplication.isEnabled()) {
            disableMultiplication.run();
        }
        return directDispatch.getAsBoolean();
    }

    private static boolean hasMatchingPatternDefinition(List<IPatternDetails> registeredPatterns,
            IPatternDetails candidate) {
        AEItemKey definition = candidate.getDefinition();
        if (definition == null) {
            return false;
        }
        for (IPatternDetails registered : registeredPatterns) {
            if (definition.equals(registered.getDefinition())) {
                return true;
            }
        }
        return false;
    }

    private boolean routePatternInputs(KeyCounter[] inputs) {
        boolean changed = patternInputLayout().route(inputs);
        if (changed) {
            this.owner.saveChanges();
        }
        return changed;
    }

    protected final boolean processSmartPatternViaAdapter() {
        MeInputLayout layout = patternInputLayout();
        if (layout.isEmpty() || this.owner.isPatternBusy()) {
            return false;
        }
        return processSmartPattern(new MeSmartPatternMultiplication.CapacityAwareFeeder() {
            @Override
            public boolean feed(KeyCounter[] oneCraftInputs) {
                return layout.route(oneCraftInputs);
            }

            @Override
            public long maxAcceptedCopies(KeyCounter[] oneCraftInputs) {
                return layout.maxAcceptedCopies(oneCraftInputs);
            }
        });
    }

    public final boolean hasPatternOutputBacklog(
            com.beipuo.mekenergistics.blockentity.api.AeOutputMode mode) {
        for (MeOutputPort output : patternOutputPorts()) {
            AEKey key = output.key();
            if (key != null && output.amount() > 0 && outputModeAllows(mode, key)) {
                return true;
            }
        }
        return false;
    }

    public final boolean drainPatternOutputs(
            com.beipuo.mekenergistics.blockentity.api.AeOutputMode mode) {
        return drainOutputPorts(mode, patternOutputPorts());
    }

    public final IInventorySlotHolder withPatternSlots(IInventorySlotHolder original) {
        return side -> {
            List<mekanism.api.inventory.IInventorySlot> slots = new ArrayList<>(original.getInventorySlots(side));
            slots.addAll(this.patternSlots);
            return slots;
        };
    }

    public final List<IPatternDetails> getAvailablePatterns() {
        return Collections.unmodifiableList(this.patterns);
    }

    public final int getPatternPriority() {
        return this.patternPriority;
    }

    public final String getPatternTerminalName() {
        return MePatternTerminalNames.get(this.ownerTile, this.patternTerminalName);
    }

    public final void setPatternTerminalName(String name) {
        if (!MePatternTerminalNames.set(this.ownerTile, name, this.patternTerminalName)) {
            return;
        }
        this.patternTerminalName = com.beipuo.mekenergistics.blockentity.api.MeAeMachine.sanitizePatternTerminalName(name);
        requestCraftingUpdate();
    }

    public final boolean isVisibleInPatternAccessTerminal() {
        return this.visibleInPatternAccessTerminal;
    }

    public final void setVisibleInPatternAccessTerminal(boolean visible) {
        if (this.visibleInPatternAccessTerminal == visible) {
            return;
        }
        this.visibleInPatternAccessTerminal = visible;
        requestCraftingUpdate();
        this.owner.saveChanges();
    }

    public final void createOnFirstTick() {
        GridHelper.onFirstTick(this.ownerTile, tile -> create());
    }

    public final void refreshAfterWorldMutation() {
        GridHelper.onFirstTick(this.ownerTile, tile -> {
            create();
            rebuildPatternCache(false);
            net.minecraft.world.level.Level level = this.ownerTile.getLevel();
            if (level == null || level.isClientSide()) {
                return;
            }
            BlockPos pos = this.ownerTile.getBlockPos();
            level.invalidateCapabilities(pos);
            for (Direction direction : Direction.values()) {
                level.invalidateCapabilities(pos.relative(direction));
            }
            level.updateNeighborsAt(pos, this.ownerTile.getBlockState().getBlock());
        });
    }

    private void create() {
        if (this.ownerTile.isRemoved() || this.ownerTile.getLevel() == null || this.ownerTile.getLevel().isClientSide()) {
            return;
        }
        create(this.ownerTile.getLevel(), this.owner.getGridNodePosition());
    }

    public final void create(net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos) {
        if (level == null || level.isClientSide() || this.ownerTile.isRemoved()) {
            return;
        }
        if (this.nodeState == NodeState.DESTROYED) {
            this.mainNode = createManagedNode();
            this.nodeState = NodeState.NEW;
        }
        if (this.nodeState == NodeState.NEW) {
            this.mainNode.setInWorldNode(true);
            if (this.owner.getMachine().isMekmmLargeMachine()) {
                createLargeMachineNodes(level, pos);
            } else {
                this.mainNode.create(level, pos);
            }
            this.nodeState = NodeState.ACTIVE;
            rebuildPatternCache(false);
        }
    }

    /**
     * Places a node on every block the machine occupies so a cable can attach anywhere along its
     * surface. The controller keeps the main node — which carries the crafting and ticking services —
     * while the bounding blocks get bare nodes wired back to it.
     *
     * <p>Each node is exposed only on the faces that lead out of the machine, so two nodes of the same
     * machine never face each other and AE2's in-world scan cannot duplicate the direct connections
     * made here.
     */
    private void createLargeMachineNodes(net.minecraft.world.level.Level level, BlockPos controllerPos) {
        this.largeMachinePortNodes.clear();
        MeLargeMachineFootprint footprint =
                MeLargeMachineFootprint.of(level, controllerPos, this.ownerTile.getBlockState());
        this.mainNode.setExposedOnSides(footprint.exposedFaces(controllerPos));
        this.mainNode.create(level, controllerPos);
        IGridNode mainGridNode = this.mainNode.getNode();
        if (mainGridNode == null) {
            return;
        }
        int owningPlayerId = mainGridNode.getOwningPlayerId();
        footprint.forEachExposedPosition((position, exposed) -> {
            if (position.equals(controllerPos)) {
                return;
            }
            IManagedGridNode portNode = GridHelper.createManagedNode(this.owner, new PortNodeListener())
                    .setInWorldNode(true)
                    .setExposedOnSides(exposed)
                    .setIdlePowerUsage(0);
            portNode.setOwningPlayerId(owningPlayerId);
            portNode.create(level, position);
            GridHelper.createConnection(mainGridNode, portNode.getNode());
            this.largeMachinePortNodes.put(position, portNode);
            // The bounding block had no grid node when its capability was last resolved.
            level.invalidateCapabilities(position);
        });
    }

    /** Keeps every node of the machine attributed to the same player for AE2's security checks. */
    public final void setOwningPlayer(net.minecraft.server.level.ServerPlayer player) {
        this.mainNode.setOwningPlayer(player);
        for (IManagedGridNode port : this.largeMachinePortNodes.values()) {
            port.setOwningPlayer(player);
        }
    }

    public final void destroyNode() {
        if (this.nodeState == NodeState.DESTROYED) {
            return;
        }
        retainNodeData();
        this.craftingUpdateState.markPending();
        net.minecraft.world.level.Level level = this.ownerTile.getLevel();
        for (Map.Entry<BlockPos, IManagedGridNode> port : this.largeMachinePortNodes.entrySet()) {
            port.getValue().destroy();
            if (level != null) {
                level.invalidateCapabilities(port.getKey());
            }
        }
        this.largeMachinePortNodes.clear();
        this.mainNode.destroy();
        this.nodeState = NodeState.DESTROYED;
    }

    public final IGrid getGrid() {
        IGridNode node = this.mainNode.getNode();
        return node == null || !node.isActive() ? null : node.getGrid();
    }

    /** Refills every local FE buffer before Mekanism evaluates machine work for this tick. */
    public final void refillLocalEnergyBuffers() {
        IGrid grid = getGrid();
        if (grid == null) {
            return;
        }
        for (IEnergyContainer energyContainer : this.ownerTile.getEnergyContainers(null)) {
            MeNetworkEnergyHelper.refillLocalEnergy(energyContainer, grid, this.actionSource);
        }
    }

    public final boolean isSmartPatternMultiplicationEnabled() {
        return this.smartPatternMultiplication.isEnabled();
    }

    public final void setSmartPatternMultiplicationEnabled(boolean enabled) {
        if (this.smartPatternMultiplication.isEnabled() == enabled) {
            return;
        }
        this.smartPatternMultiplication.setEnabled(enabled);
        this.owner.saveChanges();
        alertAeTicker();
    }

    public final MePassiveCraftingSettings getPassiveCraftingSettings() {
        return passiveCraftingSettings;
    }

    public final Boolean getClientPassiveCraftingEnabled() {
        return clientPassiveCraftingEnabled;
    }

    public final void setClientPassiveCraftingEnabled(boolean enabled) {
        clientPassiveCraftingEnabled = enabled;
    }

    public final void setPassiveCraftingSettings(int intervalTicks, long multiplier) {
        passiveCraftingSettings.set(intervalTicks, multiplier);
        this.owner.saveChanges();
    }

    protected final boolean processPassiveCrafting(boolean enabled) {
        if (!enabled) {
            return false;
        }
        if (this.smartPatternMultiplication.isEnabled()) {
            setSmartPatternMultiplicationEnabled(false);
        }
        if (!passiveCraftingSettings.tick() || this.ownerTile.getLevel() == null
                || this.ownerTile.getLevel().isClientSide || isPatternBusy()) {
            return false;
        }
        IGrid grid = getGrid();
        MEStorage storage = getNetworkStorage(grid);
        if (storage == null) {
            return false;
        }
        boolean changed = MePassiveCraftingDispatcher.submitAvailable(
                this.patterns, passiveCraftingSettings.multiplier(), this.ownerTile.getLevel(), storage,
                this.actionSource, this::routeDataPatternInputs);
        if (changed) {
            this.owner.saveChanges();
        }
        return changed;
    }

    public final boolean enqueueSmartPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
        if (!this.smartPatternMultiplication.enqueue(patternDetails, inputHolder)) {
            return false;
        }
        this.owner.saveChanges();
        alertAeTicker();
        return true;
    }

    public final boolean drainOutputPorts(com.beipuo.mekenergistics.blockentity.api.AeOutputMode mode,
            List<? extends MeOutputPort> outputPorts) {
        boolean changed = drainOutputPorts(mode, outputPorts, this::insertIntoNetwork);
        if (changed) {
            this.owner.saveChanges();
        }
        return changed;
    }

    static boolean drainOutputPorts(com.beipuo.mekenergistics.blockentity.api.AeOutputMode mode,
            List<? extends MeOutputPort> outputPorts, BiFunction<AEKey, Long, Long> networkInserter) {
        boolean changed = false;
        for (MeOutputPort output : outputPorts) {
            AEKey key = output.key();
            long available = output.amount();
            if (key == null || available <= 0 || !outputModeAllows(mode, key)) {
                continue;
            }
            long inserted = Math.min(available, Math.max(0, networkInserter.apply(key, available)));
            if (inserted > 0) {
                changed |= output.extract(inserted, mekanism.api.Action.EXECUTE) > 0;
            }
        }
        return changed;
    }

    private static boolean outputModeAllows(
            com.beipuo.mekenergistics.blockentity.api.AeOutputMode mode, AEKey key) {
        return key instanceof AEItemKey ? mode.items()
                : key instanceof AEFluidKey ? mode.fluids() : mode.chemicals();
    }

    public boolean processSmartPattern(MeSmartPatternMultiplication.Feeder feeder) {
        boolean changed = this.smartPatternMultiplication.processNext(feeder);
        if (changed) {
            this.owner.saveChanges();
            if (this.smartPatternMultiplication.hasPendingWork()) {
                alertAeTicker();
            }
        }
        return changed;
    }

    protected final void alertAeTicker() {
        this.mainNode.ifPresent((grid, node) -> grid.getTickManager().alertDevice(node));
    }

    protected final long insertIntoNetwork(AEKey key, long amount) {
        IGrid grid = getGrid();
        MEStorage storage = getNetworkStorage(grid);
        return storage == null || key == null || amount <= 0 ? 0
                : StorageHelper.poweredInsert(grid.getEnergyService(), storage, key, amount, this.actionSource);
    }

    protected final MEStorage getNetworkStorage(IGrid grid) {
        IStorageService storageService = grid == null ? null : grid.getService(IStorageService.class);
        return storageService == null ? null : storageService.getInventory();
    }

    public final void updatePatterns() {
        rebuildPatternCache(true);
    }

    private void rebuildPatternCache(boolean saveChanges) {
        this.patterns.clear();
        for (BasicInventorySlot patternSlot : this.patternSlots) {
            ItemStack stack = patternSlot.getStack();
            if (!stack.isEmpty()) {
                IPatternDetails pattern = MePatternDecodeHelper.safeDecode(stack, this.ownerTile.getLevel(),
                        this.ownerTile.getBlockPos(), patternOwnerName());
                if (pattern != null) {
                    this.patterns.add(pattern);
                }
            }
        }
        requestCraftingUpdate();
        if (saveChanges) {
            this.owner.saveChanges();
        }
    }

    protected abstract String patternOwnerName();

    protected abstract boolean hasAeOutputWork();

    protected abstract boolean processAeOutputWork();

    private void requestCraftingUpdate() {
        IGridNode node = this.mainNode.getNode();
        this.craftingUpdateState.request(node != null && node.isActive(),
                () -> ICraftingProvider.requestUpdate(this.mainNode));
    }

    protected final void saveCommon(CompoundTag tag, HolderLookup.Provider registries) {
        save(tag);
        saveSlots(tag, registries);
    }

    public final void save(CompoundTag tag) {
        tag.putInt("PatternPriority", this.patternPriority);
        this.smartPatternMultiplication.saveConfig(tag);
        this.passiveCraftingSettings.save(tag);
        tag.putBoolean(TAG_TERMINAL_VISIBLE, this.visibleInPatternAccessTerminal);
        tag.remove(TAG_PATTERN_TERMINAL_NAME);
        saveNode(tag);
    }

    public final void saveSlots(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt(TAG_PATTERN_SCHEMA, AE_PATTERN_SCHEMA);
        for (int i = 0; i < this.patternSlots.size(); i++) {
            tag.put("MePatternSlot" + i, this.patternSlots.get(i).serializeNBT(registries));
        }
        this.smartPatternMultiplication.savePending(tag, registries);
    }

    protected final void loadCommon(CompoundTag tag, HolderLookup.Provider registries) {
        load(tag);
        loadSlots(tag, registries);
    }

    public final void load(CompoundTag tag) {
        this.patternPriority = tag.getInt("PatternPriority");
        this.smartPatternMultiplication.loadConfig(tag);
        this.passiveCraftingSettings.load(tag);
        this.visibleInPatternAccessTerminal = !tag.contains(TAG_TERMINAL_VISIBLE) || tag.getBoolean(TAG_TERMINAL_VISIBLE);
        this.patternTerminalName = MePatternTerminalNames.migrateLegacy(this.ownerTile, tag.getString(TAG_PATTERN_TERMINAL_NAME));
        loadNode(tag);
    }

    public final void loadSlots(CompoundTag tag, HolderLookup.Provider registries) {
        boolean migrated = tag.getInt(TAG_PATTERN_SCHEMA) < AE_PATTERN_SCHEMA;
        for (int i = 0; i < this.patternSlots.size(); i++) {
            if (tag.contains("MePatternSlot" + i)) {
                this.patternSlots.get(i).deserializeNBT(registries, tag.getCompound("MePatternSlot" + i));
            }
        }
        if (migrated && !hasPatternSlotTags(tag)) {
            int offset = this.owner instanceof MePatternIoOwner io ? Math.max(0, io.getLegacyPatternSlotOffset()) : 0;
            loadLegacyInventory(this.patternSlots, tag, registries, offset);
        }
        this.smartPatternMultiplication.loadPending(tag, registries);
        // Block entities can be deserialized before they have a level. Decode the
        // restored patterns after the managed node is created on the first tick.
        this.patterns.clear();
        this.craftingUpdateState.markPending();
    }

    private boolean hasPatternSlotTags(CompoundTag tag) {
        for (int i = 0; i < this.patternSlots.size(); i++) {
            if (tag.contains("MePatternSlot" + i)) {
                return true;
            }
        }
        return false;
    }

    static void loadLegacyInventory(List<BasicInventorySlot> slots, CompoundTag tag, HolderLookup.Provider registries, int offset) {
        if (!tag.contains("Inventory", CompoundTag.TAG_LIST)) {
            return;
        }
        ListTag inventory = tag.getList("Inventory", CompoundTag.TAG_COMPOUND);
        for (int i = 0; i < slots.size() && i + offset < inventory.size(); i++) {
            slots.get(i).deserializeNBT(registries, inventory.getCompound(i + offset));
        }
    }

    static void savePatternSlots(List<BasicInventorySlot> slots, CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt(TAG_PATTERN_SCHEMA, AE_PATTERN_SCHEMA);
        for (int i = 0; i < slots.size(); i++) {
            tag.put("MePatternSlot" + i, slots.get(i).serializeNBT(registries));
        }
    }

    protected final void saveNode(CompoundTag tag) {
        if (this.nodeState == NodeState.DESTROYED && this.retainedNodeData != null) {
            if (this.retainedNodeData.contains(TAG_NODE)) {
                tag.put(TAG_NODE, this.retainedNodeData.getCompound(TAG_NODE).copy());
            }
            return;
        }
        this.mainNode.saveToNBT(tag);
    }

    protected final void loadNode(CompoundTag tag) {
        this.retainedNodeData = copyNodeData(tag);
        if (this.nodeState == NodeState.DESTROYED) {
            this.mainNode = createManagedNode();
            this.nodeState = NodeState.NEW;
        } else {
            this.mainNode.loadFromNBT(tag);
        }
    }

    private void retainNodeData() {
        CompoundTag tag = new CompoundTag();
        this.mainNode.saveToNBT(tag);
        this.retainedNodeData = copyNodeData(tag);
    }

    private static CompoundTag copyNodeData(CompoundTag source) {
        CompoundTag result = new CompoundTag();
        if (source.contains(TAG_NODE)) {
            result.put(TAG_NODE, source.getCompound(TAG_NODE).copy());
        }
        return result;
    }

    enum NodeState {
        NEW,
        ACTIVE,
        DESTROYED
    }

    private final class PortNodeListener implements IGridNodeListener<O> {
        @Override
        public void onSaveChanges(O nodeOwner, IGridNode node) {
            // Port nodes contain no persistent services or state of their own.
        }
    }

    private final class NodeListener implements IGridNodeListener<O> {
        @Override
        public void onSaveChanges(O nodeOwner, IGridNode node) {
            nodeOwner.saveChanges();
        }

        @Override
        public void onStateChanged(O nodeOwner, IGridNode node, State state) {
            if (node.isActive()) {
                craftingUpdateState.flush(() -> ICraftingProvider.requestUpdate(mainNode));
                node.getGrid().getTickManager().alertDevice(node);
            }
        }
    }

    static final class CraftingUpdateState {
        private boolean pending;

        void request(boolean nodeActive, Runnable update) {
            if (nodeActive) {
                update.run();
                this.pending = false;
            } else {
                this.pending = true;
            }
        }

        void markPending() {
            this.pending = true;
        }

        void flush(Runnable update) {
            if (this.pending) {
                update.run();
                this.pending = false;
            }
        }

        boolean isPending() {
            return this.pending;
        }
    }

    private final class AeTicker implements IGridTickable {
        @Override
        public TickingRequest getTickingRequest(IGridNode node) {
            return new TickingRequest(1, 1, !hasAeOutputWork());
        }

        @Override
        public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
            if (!mainNode.isActive()) {
                return TickRateModulation.SLEEP;
            }
            boolean hadWork = hasAeOutputWork();
            boolean finished = processAeOutputWork();
            if (!hasAeOutputWork()) {
                return TickRateModulation.SLEEP;
            }
            return finished || hadWork ? TickRateModulation.URGENT : TickRateModulation.SLOWER;
        }
    }
}
