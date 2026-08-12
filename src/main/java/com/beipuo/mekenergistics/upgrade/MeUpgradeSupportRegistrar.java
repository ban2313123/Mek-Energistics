package com.beipuo.mekenergistics.upgrade;

import com.beipuo.mekenergistics.compat.catalog.CompatMachineCatalog;
import com.beipuo.mekenergistics.registry.ModBlocks;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import mekanism.api.Upgrade;
import mekanism.common.block.attribute.AttributeUpgradeSupport;
import mekanism.common.block.interfaces.ITypeBlock;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

/** Adds the ME cards to every catalog-backed machine's native Mekanism upgrade component. */
public final class MeUpgradeSupportRegistrar {
    private MeUpgradeSupportRegistrar() {
    }

    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(MeUpgradeSupportRegistrar::registerSupportedBlocks);
    }

    static void registerSupportedBlocks() {
        List<Block> targets = new ArrayList<>();
        CompatMachineCatalog.available().forEach(spec -> {
            Block source = BuiltInRegistries.BLOCK.get(spec.sourceBlockId());
            if (BuiltInRegistries.BLOCK.getKey(source).equals(spec.sourceBlockId())) {
                targets.add(source);
            }
            var meBlock = ModBlocks.getMachineBlock(spec.machine());
            if (meBlock != null && meBlock.isBound()) {
                targets.add(meBlock.get());
            }
        });
        targets.forEach(MeUpgradeSupportRegistrar::addMeUpgrades);
    }

    private static void addMeUpgrades(Block block) {
        if (!(block instanceof ITypeBlock typeBlock)) {
            return;
        }
        AttributeUpgradeSupport current = typeBlock.getType().get(AttributeUpgradeSupport.class);
        Set<Upgrade> supported = new LinkedHashSet<>();
        if (current != null) {
            supported.addAll(current.supportedUpgrades());
        }
        supported.add(MeMekanismUpgrades.patternProvider());
        supported.add(MeMekanismUpgrades.passiveCrafting());
        supported.add(MeMekanismUpgrades.outputInterface());
        typeBlock.getType().add(AttributeUpgradeSupport.create(supported.toArray(Upgrade[]::new)));
    }
}
