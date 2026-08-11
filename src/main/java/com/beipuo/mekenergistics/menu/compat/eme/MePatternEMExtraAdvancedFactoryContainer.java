package com.beipuo.mekenergistics.menu.compat.eme;

import com.beipuo.mekenergistics.menu.MePatternContainerQuickMove;
import com.beipuo.mekenergistics.menu.MePatternQuickMoveContainer;
import io.github.masyumero.emextras.common.integration.mekaf.inventory.container.tile.EMExtraAdvancedFactoryContainer;
import io.github.masyumero.emextras.common.integration.mekaf.tile.factory.base.TileEntityEMExtraAdvancedFactoryBase;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class MePatternEMExtraAdvancedFactoryContainer extends EMExtraAdvancedFactoryContainer implements MePatternQuickMoveContainer {
    public MePatternEMExtraAdvancedFactoryContainer(int id, Inventory inv, TileEntityEMExtraAdvancedFactoryBase<?> tile) {
        super(id, inv, tile);
    }

    @NotNull
    @Override
    public ItemStack quickMoveStack(@NotNull Player player, int slotID) {
        return MePatternContainerQuickMove.quickMoveStack(this.slots, this, this.tile,
                this::transferSuccess, super::quickMoveStack, player, slotID);
    }
}
