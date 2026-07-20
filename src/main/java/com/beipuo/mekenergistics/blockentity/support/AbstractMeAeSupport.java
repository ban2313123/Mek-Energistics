package com.beipuo.mekenergistics.blockentity.support;

import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.networking.GridFlags;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IManagedGridNode;
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
import com.beipuo.mekenergistics.blockentity.api.MeAeSupportOwner;
import com.beipuo.mekenergistics.blockentity.api.MePatternIoOwner;
import com.beipuo.mekenergistics.blockentity.slot.MePatternInventorySlot;
import com.beipuo.mekenergistics.blockentity.support.io.MeInputPort;
import com.beipuo.mekenergistics.blockentity.support.io.MeOutputPort;
import com.beipuo.mekenergistics.blockentity.support.io.MePatternIoAdapter;
import com.beipuo.mekenergistics.blockentity.support.io.MePatternInputRouter;
import com.beipuo.mekenergistics.config.MekEnergisticsConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;

public abstract class AbstractMeAeSupport<O extends MeAeSupportOwner> {
    public static final int AE_PATTERN_SCHEMA = 2;
    private static final String TAG_PATTERN_SCHEMA = "AePatternSchema";
    private static final String TAG_PATTERN_TERMINAL_NAME = "PatternTerminalName";
    private static final String TAG_NODE = "node";

    protected final O owner;
    protected final TileEntityMekanism ownerTile;
    protected final IActionSource actionSource;
    protected final List<BasicInventorySlot> patternSlots;
    protected final List<IPatternDetails> patterns = new ArrayList<>();
    protected final Map<AEKey, IPatternDetails> patternsByDefinition = new HashMap<>();
    protected final MeSmartPatternMultiplication smartPatternMultiplication = new MeSmartPatternMultiplication();

    protected IManagedGridNode mainNode;
    protected int patternPriority;
    protected String patternTerminalName = "";

    private NodeState nodeState = NodeState.NEW;
    private CompoundTag retainedNodeData;

    protected AbstractMeAeSupport(O owner) {
        this.owner = owner;
        this.ownerTile = owner.getAeOwnerTile();
        this.actionSource = IActionSource.ofMachine(owner);
        this.mainNode = createManagedNode();
        List<BasicInventorySlot> externalSlots = owner instanceof MePatternIoOwner io ? io.getExternalPatternSlots() : List.of();
        if (externalSlots != null && !externalSlots.isEmpty()) {
            this.patternSlots = externalSlots;
        } else {
            this.patternSlots = new ArrayList<>(MekEnergisticsConfig.patternSlots());
            for (int i = 0; i < MekEnergisticsConfig.patternSlots(); i++) {
                this.patternSlots.add(MePatternInventorySlot.create(PatternDetailsHelper::isEncodedPattern, this::updatePatterns));
            }
        }
    }

    private IManagedGridNode createManagedNode() {
        IManagedGridNode node = GridHelper.createManagedNode(this.owner, NodeListener.INSTANCE)
                .setInWorldNode(true)
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

    public final List<BasicInventorySlot> getPatternSlots() {
        return Collections.unmodifiableList(this.patternSlots);
    }

    public final MePatternIoAdapter getPatternIoAdapter() {
        return this.owner instanceof MePatternIoOwner io ? io.getPatternIoAdapter()
                : new MePatternIoAdapter(List.of(), List.of(), false);
    }

    public final boolean isPatternBusy() {
        return getPatternIoAdapter().busy();
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

    public final void createOnFirstTick() {
        GridHelper.onFirstTick(this.ownerTile, tile -> create());
    }

    private void create() {
        if (this.ownerTile.isRemoved() || this.ownerTile.getLevel() == null || this.ownerTile.getLevel().isClientSide()) {
            return;
        }
        create(this.ownerTile.getLevel(), this.ownerTile.getBlockPos());
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
            this.mainNode.create(level, pos);
            this.nodeState = NodeState.ACTIVE;
            updatePatterns();
        }
    }

    public final void destroyNode() {
        if (this.nodeState == NodeState.DESTROYED) {
            return;
        }
        retainNodeData();
        this.mainNode.destroy();
        this.nodeState = NodeState.DESTROYED;
    }

    public final IGrid getGrid() {
        IGridNode node = this.mainNode.getNode();
        return node == null || !node.isActive() ? null : node.getGrid();
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

    public final boolean enqueueSmartPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
        if (!this.smartPatternMultiplication.enqueue(patternDetails, inputHolder)) {
            return false;
        }
        this.owner.saveChanges();
        alertAeTicker();
        return true;
    }

    public final boolean pushPatternInputs(KeyCounter[] inputHolder, List<? extends MeInputPort> inputPorts) {
        boolean changed = MePatternInputRouter.route(inputHolder, inputPorts);
        if (changed) {
            this.owner.saveChanges();
        }
        return changed;
    }

    public final boolean drainOutputPorts(com.beipuo.mekenergistics.blockentity.api.AeOutputMode mode,
            List<? extends MeOutputPort> outputPorts) {
        boolean changed = false;
        for (MeOutputPort output : outputPorts) {
            AEKey key = output.key();
            if (key == null || output.amount() <= 0
                    || key instanceof AEItemKey && !mode.items()
                    || key instanceof AEFluidKey && !mode.fluids()
                    || !(key instanceof AEItemKey) && !(key instanceof AEFluidKey) && !mode.chemicals()) {
                continue;
            }
            long inserted = insertIntoNetwork(key, output.amount());
            if (inserted > 0) {
                output.extract(inserted, mekanism.api.Action.EXECUTE);
                changed = true;
            }
        }
        if (changed) {
            this.owner.saveChanges();
        }
        return changed;
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

    protected final boolean processSmartPatternViaOwner() {
        boolean wasEnabled = this.smartPatternMultiplication.isEnabled();
        this.smartPatternMultiplication.setEnabled(false);
        try {
            return processSmartPatternWithPattern(this.owner::pushPattern);
        } finally {
            this.smartPatternMultiplication.setEnabled(wasEnabled);
        }
    }

    private boolean processSmartPatternWithPattern(MeSmartPatternMultiplication.PatternFeeder feeder) {
        boolean changed = this.smartPatternMultiplication.processNext(this.patternsByDefinition, feeder);
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
        this.patterns.clear();
        this.patternsByDefinition.clear();
        for (BasicInventorySlot patternSlot : this.patternSlots) {
            ItemStack stack = patternSlot.getStack();
            if (!stack.isEmpty()) {
                IPatternDetails pattern = MePatternDecodeHelper.safeDecode(stack, this.ownerTile.getLevel(),
                        this.ownerTile.getBlockPos(), patternOwnerName());
                if (pattern != null) {
                    this.patterns.add(pattern);
                    this.patternsByDefinition.putIfAbsent(pattern.getDefinition(), pattern);
                }
            }
        }
        requestCraftingUpdate();
        this.owner.saveChanges();
    }

    protected abstract String patternOwnerName();

    protected abstract boolean hasAeOutputWork();

    protected abstract boolean processAeOutputWork();

    private void requestCraftingUpdate() {
        IGridNode node = this.mainNode.getNode();
        if (node != null && node.isActive()) {
            ICraftingProvider.requestUpdate(this.mainNode);
        }
    }

    protected final void saveCommon(CompoundTag tag, HolderLookup.Provider registries) {
        save(tag);
        saveSlots(tag, registries);
    }

    public final void save(CompoundTag tag) {
        tag.putInt("PatternPriority", this.patternPriority);
        this.smartPatternMultiplication.saveConfig(tag);
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
        updatePatterns();
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

    final NodeState nodeStateForTesting() {
        return this.nodeState;
    }

    enum NodeState {
        NEW,
        ACTIVE,
        DESTROYED
    }

    private enum NodeListener implements IGridNodeListener<MeAeSupportOwner> {
        INSTANCE;

        @Override
        public void onSaveChanges(MeAeSupportOwner nodeOwner, IGridNode node) {
            nodeOwner.saveChanges();
        }

        @Override
        public void onStateChanged(MeAeSupportOwner nodeOwner, IGridNode node, State state) {
            if (node.isActive()) {
                node.getGrid().getTickManager().alertDevice(node);
            }
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
