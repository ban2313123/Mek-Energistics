package com.beipuo.mekenergistics.mixin;

import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.security.IActionHost;
import com.beipuo.mekenergistics.blockentity.api.AeOutputMode;
import com.beipuo.mekenergistics.blockentity.api.MeUpgradeableMachine;
import com.beipuo.mekenergistics.blockentity.support.AbstractMeAeSupport;
import com.beipuo.mekenergistics.blockentity.support.MeRecipeMachineAeSupport;
import com.beipuo.mekenergistics.blockentity.support.io.MeInputLayout;
import com.beipuo.mekenergistics.blockentity.support.io.MeMachineIoAdapter;
import com.beipuo.mekenergistics.blockentity.support.io.MeOutputPort;
import com.beipuo.mekenergistics.upgrade.MePatternProviderUpgrade;
import com.beipuo.mekenergistics.upgrade.MeUpgradeMachineProfile;
import com.beipuo.mekenergistics.upgrade.MeUpgradeRuntimeState;
import com.beipuo.mekenergistics.upgrade.MekanismFactoryUpgradeProfiles;
import java.util.ArrayList;
import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.content.blocktype.FactoryType;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.recipe.lookup.monitor.FactoryRecipeCacheLookupMonitor;
import mekanism.common.tile.factory.TileEntityFactory;
import mekanism.common.tile.factory.TileEntityItemStackChemicalToItemStackFactory;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.extensions.IBlockEntityExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TileEntityFactory.class, remap = false)
public abstract class TileEntityFactoryMeUpgradeMixin implements MeUpgradeableMachine,
        MekanismFactoryUpgradeProfiles.FactoryIoAccess, ICraftingProvider, IActionHost, IBlockEntityExtension {
    @Shadow protected List<IInventorySlot> inputSlots;
    @Shadow protected List<IInventorySlot> outputSlots;
    @Shadow protected FactoryRecipeCacheLookupMonitor<?>[] recipeCacheLookupMonitors;
    @Shadow protected abstract IInventorySlot getExtraSlot();

    @Unique private MeRecipeMachineAeSupport<?> mekenergistics$factoryAeSupport;
    @Unique private MeUpgradeRuntimeState mekenergistics$factoryRuntimeState;
    @Unique private AeOutputMode mekenergistics$factoryOutputMode = AeOutputMode.BOTH;

    @Unique
    private TileEntityFactory<?> mekenergistics$factory() {
        return (TileEntityFactory<?>) (Object) this;
    }

    @Unique
    private MeUpgradeRuntimeState mekenergistics$factoryRuntimeState() {
        if (this.mekenergistics$factoryRuntimeState == null) {
            this.mekenergistics$factoryRuntimeState = new MeUpgradeRuntimeState();
        }
        return this.mekenergistics$factoryRuntimeState;
    }

    @Unique
    @SuppressWarnings({"rawtypes", "unchecked"})
    private MeRecipeMachineAeSupport<?> mekenergistics$factorySupport() {
        if (this.mekenergistics$factoryAeSupport == null) {
            this.mekenergistics$factoryAeSupport = new MeRecipeMachineAeSupport(mekenergistics$factory());
        }
        return this.mekenergistics$factoryAeSupport;
    }

    @Unique
    private MeUpgradeMachineProfile<TileEntityFactory<?>> mekenergistics$factoryProfile() {
        return MekanismFactoryUpgradeProfiles.forTile(mekenergistics$factory());
    }

    @Override
    public MeUpgradeMachineProfile<?> getMeUpgradeProfile() {
        return mekenergistics$factoryProfile();
    }

    @Override
    public boolean isMeUpgradeTarget() {
        MeUpgradeMachineProfile<TileEntityFactory<?>> profile = mekenergistics$factoryProfile();
        return profile != null && profile.matches(mekenergistics$factory());
    }

    @Unique
    private boolean mekenergistics$factoryUpgradeInstalled() {
        TileEntityFactory<?> tile = mekenergistics$factory();
        return isMeUpgradeTarget() && tile.getComponent() != null
                && tile.getComponent().isUpgradeInstalled(MePatternProviderUpgrade.get());
    }

    @Override
    public boolean isMeUpgradeActive() {
        TileEntityFactory<?> tile = mekenergistics$factory();
        return mekenergistics$factoryRuntimeState().activeFor(
                tile.getLevel(), isMeUpgradeTarget(), mekenergistics$factoryUpgradeInstalled());
    }

    @Override
    public AbstractMeAeSupport<?> getRecipeAeSupport() {
        return mekenergistics$factorySupport();
    }

    @Override
    public MeInputLayout getPatternInputLayout() {
        return mekenergistics$getFactoryInputLayout();
    }

    @Override
    public List<? extends MeOutputPort> getPatternOutputPorts() {
        return mekenergistics$getFactoryOutputPorts();
    }

    @Override
    public MeInputLayout mekenergistics$getFactoryInputLayout() {
        List<com.beipuo.mekenergistics.blockentity.support.io.MeInputPort> inputs = new ArrayList<>();
        TileEntityFactory<?> tile = mekenergistics$factory();
        FactoryType type = tile.getFactoryType();
        if (type == FactoryType.INFUSING && getAeOutputMode().chemicals()) {
            inputs.add(MeMachineIoAdapter.manualItemInput(getExtraSlot()));
        } else {
            inputs.add(MeMachineIoAdapter.autoSortedFactoryItemInput(this.inputSlots));
            if (tile instanceof TileEntityItemStackChemicalToItemStackFactory chemicalFactory) {
                inputs.add(MeMachineIoAdapter.chemicalInput(chemicalFactory.getChemicalTank()));
                if (type != FactoryType.INFUSING) {
                    inputs.add(MeMachineIoAdapter.itemInput(getExtraSlot()));
                }
            } else if (type == FactoryType.COMBINING) {
                inputs.add(MeMachineIoAdapter.itemInput(getExtraSlot()));
            }
        }
        return MeInputLayout.unordered(inputs);
    }

    @Override
    public List<? extends MeOutputPort> mekenergistics$getFactoryOutputPorts() {
        List<MeOutputPort> outputs = new ArrayList<>();
        this.outputSlots.stream().map(MeMachineIoAdapter::itemOutput).forEach(outputs::add);
        TileEntityFactory<?> tile = mekenergistics$factory();
        if (tile.getFactoryType() == FactoryType.INFUSING
                && tile instanceof TileEntityItemStackChemicalToItemStackFactory chemicalFactory) {
            outputs.add(MeMachineIoAdapter.chemicalOutput(chemicalFactory.getChemicalTank()));
        }
        return List.copyOf(outputs);
    }

    @Override
    public AeOutputMode getAeOutputMode() {
        return this.mekenergistics$factoryOutputMode;
    }

    @Override
    public void cycleAeOutputMode() {
        this.mekenergistics$factoryOutputMode = this.mekenergistics$factoryOutputMode.next();
        mekenergistics$factorySupport().invalidatePatternIoCache();
        saveChanges();
    }

    @Inject(method = "getInitialInventory", at = @At("RETURN"), cancellable = true)
    private void mekenergistics$addFactoryPatternSlots(IContentsListener listener,
            CallbackInfoReturnable<IInventorySlotHolder> cir) {
        mekenergistics$factoryRuntimeState().setRecipeCacheListener(this::mekenergistics$unpauseFactoryRecipes);
        if (isMeUpgradeTarget()) {
            cir.setReturnValue(mekenergistics$factorySupport().withPatternSlots(cir.getReturnValue()));
        }
    }

    @Unique
    private void mekenergistics$unpauseFactoryRecipes() {
        if (this.recipeCacheLookupMonitors != null) {
            for (FactoryRecipeCacheLookupMonitor<?> monitor : this.recipeCacheLookupMonitors) {
                if (monitor != null) {
                    monitor.unpause();
                }
            }
        }
    }

    @Inject(method = "onUpdateServer", at = @At("RETURN"), cancellable = true)
    private void mekenergistics$processFactoryPatternIo(CallbackInfoReturnable<Boolean> cir) {
        boolean active = isMeUpgradeActive();
        switch (mekenergistics$factoryRuntimeState().transitionTo(active)) {
            case ACTIVATE -> {
                TileEntityFactory<?> tile = mekenergistics$factory();
                mekenergistics$syncFactoryOwner(tile);
                mekenergistics$factorySupport().create(tile.getLevel(), tile.getBlockPos());
                mekenergistics$invalidateFactoryCapabilities();
                mekenergistics$factoryRuntimeState().refreshRecipeCache();
            }
            case DEACTIVATE -> {
                mekenergistics$factorySupport().destroyNode();
                mekenergistics$invalidateFactoryCapabilities();
                mekenergistics$factoryRuntimeState().refreshRecipeCache();
            }
            case NONE -> {
            }
        }
        if (active) {
            cir.setReturnValue(mekenergistics$factorySupport().processPatternIo(
                    this.mekenergistics$factoryOutputMode, cir.getReturnValue()));
        }
    }

    @Unique
    private void mekenergistics$invalidateFactoryCapabilities() {
        TileEntityFactory<?> tile = mekenergistics$factory();
        if (tile.getLevel() != null) {
            tile.getLevel().invalidateCapabilities(tile.getBlockPos());
        }
    }

    @Unique
    private void mekenergistics$syncFactoryOwner(TileEntityFactory<?> tile) {
        if (!(tile.getLevel() instanceof ServerLevel level) || tile.getOwnerUUID() == null) {
            return;
        }
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(tile.getOwnerUUID());
        if (player != null) {
            mekenergistics$factorySupport().setOwningPlayer(player);
        }
    }

    @Override
    public void createMeNodeIfActive() {
        if (isMeUpgradeActive()) {
            mekenergistics$syncFactoryOwner(mekenergistics$factory());
            mekenergistics$factoryRuntimeState().markActive();
            mekenergistics$factorySupport().createOnFirstTick();
        }
    }

    @Override
    public void destroyMeNode() {
        if (this.mekenergistics$factoryAeSupport != null) {
            this.mekenergistics$factoryAeSupport.destroyNode();
        }
        mekenergistics$factoryRuntimeState().markInactive();
    }

    @Override
    public void onChunkUnloaded() {
        destroyMeNode();
    }

    @Override
    public void addMeTrackers(MekanismContainer container) {
        if (isMeUpgradeTarget()) {
            container.track(mekanism.common.inventory.container.sync.SyncableBoolean.create(
                    this::mekenergistics$factoryUpgradeInstalled,
                    mekenergistics$factoryRuntimeState()::acceptClientActive));
            mekenergistics$factorySupport().addAeTrackers(container, this::getAeOutputMode,
                    mode -> this.mekenergistics$factoryOutputMode = mode, true);
        }
    }

    @Override
    public void saveMeState(CompoundTag tag, HolderLookup.Provider registries) {
        if (this.mekenergistics$factoryAeSupport != null) {
            this.mekenergistics$factoryAeSupport.saveAeState(
                    tag, registries, this.mekenergistics$factoryOutputMode);
        }
    }

    @Override
    public void loadMeState(CompoundTag tag, HolderLookup.Provider registries) {
        if (isMeUpgradeTarget()) {
            this.mekenergistics$factoryOutputMode =
                    mekenergistics$factorySupport().loadAeState(tag, registries);
        }
    }
}
