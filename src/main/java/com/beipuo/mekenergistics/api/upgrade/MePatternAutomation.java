package com.beipuo.mekenergistics.api.upgrade;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * Registration entry for third-party blocks that host {@link IMePatternAutomationHost} tiles.
 *
 * <p>Call {@link #registerBlock(ResourceLocation)} from your {@code @Mod} constructor, before
 * capability events. {@link #registerBlock(Block)} only records the block after it exists in
 * {@link BuiltInRegistries#BLOCK}; a constructor-time deferred holder is ignored. Common setup is
 * too late for AE cable discovery.</p>
 *
 * <p>Mek Energistics applies ME cards and {@code IN_WORLD_GRID_NODE_HOST} to the union of these
 * blocks and any built-in discovered hosts (such as Mekanism Magic).</p>
 */
public final class MePatternAutomation {
    private static final Set<ResourceLocation> REGISTERED_BLOCKS = ConcurrentHashMap.newKeySet();

    private MePatternAutomation() {
    }

    public static void registerBlock(Block block) {
        if (block == null || block == Blocks.AIR) {
            return;
        }
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
        if (id != null && BuiltInRegistries.BLOCK.get(id) == block) {
            REGISTERED_BLOCKS.add(id);
        }
    }

    public static void registerBlock(ResourceLocation id) {
        if (id != null) {
            REGISTERED_BLOCKS.add(id);
        }
    }

    public static Set<ResourceLocation> registeredBlockIds() {
        return Set.copyOf(REGISTERED_BLOCKS);
    }

    public static List<Block> resolveRegisteredBlocks() {
        List<Block> blocks = new ArrayList<>();
        LinkedHashSet<ResourceLocation> seen = new LinkedHashSet<>(REGISTERED_BLOCKS);
        for (ResourceLocation id : seen) {
            Block block = BuiltInRegistries.BLOCK.get(id);
            if (block != null && block != Blocks.AIR && BuiltInRegistries.BLOCK.getKey(block).equals(id)) {
                blocks.add(block);
            }
        }
        return List.copyOf(blocks);
    }
}
