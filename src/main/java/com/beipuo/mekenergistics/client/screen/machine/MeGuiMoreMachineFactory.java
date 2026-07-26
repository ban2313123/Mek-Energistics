package com.beipuo.mekenergistics.client.screen.machine;

import com.beipuo.mekenergistics.client.screen.MeGuiConfigurableTile;
import com.jerry.mekmm.Mekmm;
import com.jerry.mekmm.client.gui.element.tab.MoreMachineGuiSortingTab;
import com.jerry.mekmm.common.tile.factory.TileEntityMoreMachineFactory;
import com.jerry.mekmm.common.tile.factory.TileEntityPlantingFactory;
import com.jerry.mekmm.common.tile.factory.TileEntityReplicatingFactory;

import mekanism.api.recipes.cache.CachedRecipe.OperationTracker.RecipeError;
import mekanism.client.gui.element.GuiDumpButton;
import mekanism.client.gui.element.bar.GuiChemicalBar;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.inventory.warning.WarningTracker;
import mekanism.common.tier.FactoryTier;
import mekanism.common.tile.interfaces.IHasDumpButton;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MeGuiMoreMachineFactory extends MeGuiConfigurableTile<TileEntityMoreMachineFactory<?>, MekanismTileContainer<TileEntityMoreMachineFactory<?>>> {

    @Nullable
    private GuiDumpButton<?> dumpButton;

    public MeGuiMoreMachineFactory(MekanismTileContainer<TileEntityMoreMachineFactory<?>> container, Inventory inv, Component title) {
        super(container, inv, title);
        if (tile.hasSecondaryResourceBar()) {
            imageHeight += 11;
            inventoryLabelY = 85;
            if (tile instanceof TileEntityPlantingFactory) {
                imageHeight += 20;
                inventoryLabelY = 105;
            }
        } else {
            inventoryLabelY = 75;
        }

        if (tile.tier == FactoryTier.ULTIMATE) {
            imageWidth += 34;
            inventoryLabelX = 26;
        }
        // 想尝试使用Emek的gui布局，但似乎有点麻烦，还是采用原始布局吧
        if (isEMLoadAndTierOrdinalAboveOverLocked()) {
            int index = evolvedTierIndex();
            imageWidth += MeFactoryGuiLayout.imageWidthDelta(index);
            inventoryLabelX = MeFactoryGuiLayout.inventoryLabelX(index);
        }
        titleLabelY = 4;
        dynamicSlots = true;
    }

    private boolean isEMLoadAndTierOrdinalAboveOverLocked() {
        if (Mekmm.hooks.evolvedMekanism.isLoaded()) {
            return tile.tier.ordinal() >= MeFactoryGuiLayout.MEKANISM_TIER_OFFSET;
        }
        return false;
    }

    private int evolvedTierIndex() {
        return tile.tier.ordinal() - MeFactoryGuiLayout.MEKANISM_TIER_OFFSET;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new MoreMachineGuiSortingTab(this, tile));
        addRenderableWidget(new GuiVerticalPowerBar(this, tile.getEnergyContainer(), imageWidth - 12, 16, tile instanceof TileEntityPlantingFactory ? 73 : 52))
                .warning(WarningTracker.WarningType.NOT_ENOUGH_ENERGY, tile.getWarningCheck(RecipeError.NOT_ENOUGH_ENERGY, 0));

        addRenderableWidget(new GuiEnergyTab(this, tile.getEnergyContainer(), tile::getLastUsage));
        if (tile.hasSecondaryResourceBar()) {
            if (tile instanceof TileEntityPlantingFactory factory) {
                addRenderableWidget(new GuiChemicalBar(this, GuiChemicalBar.getProvider(factory.getChemicalTank(), tile.getChemicalTanks(null)), 7, 96,
                        getBarWidth(), 4, true))
                        .warning(WarningTracker.WarningType.NO_MATCHING_RECIPE, tile.getWarningCheck(RecipeError.NOT_ENOUGH_SECONDARY_INPUT, 0));
                dumpButton = addRenderableWidget(new GuiDumpButton<>(this, (TileEntityMoreMachineFactory<?> & IHasDumpButton) tile, getButtonX(), 96));
            }
            if (tile instanceof TileEntityReplicatingFactory factory) {
                addRenderableWidget(new GuiChemicalBar(this, GuiChemicalBar.getProvider(factory.getChemicalTank(), tile.getChemicalTanks(null)), 7, 76,
                        getBarWidth(), 4, true))
                        .warning(WarningTracker.WarningType.NO_MATCHING_RECIPE, tile.getWarningCheck(RecipeError.NOT_ENOUGH_SECONDARY_INPUT, 0));
                dumpButton = addRenderableWidget(new GuiDumpButton<>(this, (TileEntityMoreMachineFactory<?> & IHasDumpButton) tile, getButtonX(), 76));
            }
        }

        int baseX = tile.tier == FactoryTier.BASIC ? 55 : tile.tier == FactoryTier.ADVANCED ? 35 : tile.tier == FactoryTier.ELITE ? 29 : 27;
        int baseXMult = tile.tier == FactoryTier.BASIC ? 38 : tile.tier == FactoryTier.ADVANCED ? 26 : 19;
        for (int i = 0; i < tile.tier.processes; i++) {
            int cacheIndex = i;
            addRenderableWidget(new GuiProgress(() -> tile.getScaledProgress(1, cacheIndex), ProgressType.DOWN, this, 4 + baseX + (i * baseXMult), 33))
                    .recipeViewerCategory(tile)
                    // Only can happen if recipes change because inputs are sanitized in the factory based on the output
                    .warning(WarningTracker.WarningType.INPUT_DOESNT_PRODUCE_OUTPUT, tile.getWarningCheck(RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT, cacheIndex));
        }
    }

    private int getBarWidth() {
        if (isEMLoadAndTierOrdinalAboveOverLocked()) {
            return MeFactoryGuiLayout.barWidth(evolvedTierIndex());
        }
        return tile.tier == FactoryTier.ULTIMATE ? 172 : 138;
    }

    private int getButtonX() {
        if (isEMLoadAndTierOrdinalAboveOverLocked()) {
            return MeFactoryGuiLayout.buttonX(evolvedTierIndex());
        }
        return tile.tier == FactoryTier.ULTIMATE ? 182 : 148;
    }

    @Override
    protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderTitleText(guiGraphics);
        renderInventoryText(guiGraphics, dumpButton == null ? getXSize() : dumpButton.getRelativeX());
        drawMekanismForegroundText(guiGraphics, mouseX, mouseY);
    }
}
