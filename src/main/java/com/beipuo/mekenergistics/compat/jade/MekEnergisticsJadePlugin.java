package com.beipuo.mekenergistics.compat.jade;

import com.beipuo.mekenergistics.MekEnergistics;
import com.beipuo.mekenergistics.block.MeMekanismMachineBlock;
import mekanism.common.block.prefab.BlockFactoryMachine;
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

    // Registered against this mod's own types rather than BlockEntity/Block. The universal
    // supertypes made Jade run these providers for every block entity in the game, only for the
    // provider to fail its instanceof check and bail.
    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(MeAeStatusDataProvider.INSTANCE, TileEntityMekanism.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(
                (IComponentProvider<BlockAccessor>) MeAeStatusComponentProvider.INSTANCE, BlockFactoryMachine.class);
        registration.registerBlockComponent(
                (IComponentProvider<BlockAccessor>) MeAeStatusComponentProvider.INSTANCE, MeMekanismMachineBlock.class);
    }
}
