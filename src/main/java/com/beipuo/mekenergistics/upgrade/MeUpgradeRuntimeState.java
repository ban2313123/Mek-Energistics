package com.beipuo.mekenergistics.upgrade;

import mekanism.api.IContentsListener;
import net.minecraft.world.level.Level;

/** Shared client-sync and recipe-cache state for machines attached by an ME upgrade. */
public final class MeUpgradeRuntimeState {
    private boolean wasActive;
    private boolean clientActive;
    private boolean clientSyncReceived;
    private IContentsListener recipeCacheListener;

    public boolean activeFor(Level level, boolean target, boolean componentActive) {
        return level != null && level.isClientSide && this.clientSyncReceived
                ? target && this.clientActive
                : componentActive;
    }

    public void acceptClientActive(boolean active) {
        this.clientActive = active;
        this.clientSyncReceived = true;
    }

    public void setRecipeCacheListener(IContentsListener listener) {
        this.recipeCacheListener = listener;
    }

    public MeUpgradeActivationTransition transitionTo(boolean active) {
        MeUpgradeActivationTransition transition = MeUpgradeActivationTransition.between(this.wasActive, active);
        this.wasActive = active;
        return transition;
    }

    public void markActive() {
        this.wasActive = true;
    }

    public void markInactive() {
        this.wasActive = false;
    }

    public void refreshRecipeCache() {
        if (this.recipeCacheListener != null) {
            this.recipeCacheListener.onContentsChanged();
        }
    }
}
