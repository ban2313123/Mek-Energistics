package com.beipuo.mekenergistics.compat.jade;

import com.beipuo.mekenergistics.MekEnergistics;
import com.beipuo.mekenergistics.block.MeMekanismMachineBlock;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IComponentProvider;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class MekEnergisticsJadePlugin implements IWailaPlugin {
    public static final ResourceLocation AE_STATUS = ResourceLocation.fromNamespaceAndPath(MekEnergistics.MODID, "ae_status");

    // Jade walks superclasses but not interfaces when resolving providers. Register the server
    // provider on the shared Mekanism tile base, and cover both original Mekanism blocks upgraded
    // in place and this mod's separately registered ME machine blocks on the client.
    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(MeAeStatusDataProvider.INSTANCE, TileEntityMekanism.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(
                (IComponentProvider<BlockAccessor>) MeAeStatusComponentProvider.INSTANCE, BlockTile.class);
        registration.registerBlockComponent(
                (IComponentProvider<BlockAccessor>) MeAeStatusComponentProvider.INSTANCE, MeMekanismMachineBlock.class);
    }
}
