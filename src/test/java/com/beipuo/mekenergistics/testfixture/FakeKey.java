package com.beipuo.mekenergistics.testfixture;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * An {@link AEKey} identified only by a name, for tests that need distinguishable keys without a
 * registry behind them. Equality is by name, which is what the routing and batching code keys on.
 *
 * <p>{@link #getType()} throws: no test should reach a code path that needs a real
 * {@link AEKeyType}, and failing loudly is better than returning a lie.
 */
public final class FakeKey extends AEKey {
    private final String id;

    public FakeKey(String id) {
        this.id = id;
    }

    @Override
    public AEKeyType getType() {
        throw new UnsupportedOperationException("Tests do not serialize fake keys");
    }

    @Override
    public AEKey dropSecondary() {
        return this;
    }

    @Override
    public CompoundTag toTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", this.id);
        return tag;
    }

    @Override
    public Object getPrimaryKey() {
        return this.id;
    }

    @Override
    public ResourceLocation getId() {
        return ResourceLocation.fromNamespaceAndPath("mekenergistics_test", this.id);
    }

    @Override
    public void writeToPacket(RegistryFriendlyByteBuf data) {
        data.writeUtf(this.id);
    }

    @Override
    protected Component computeDisplayName() {
        return Component.literal(this.id);
    }

    @Override
    public void addDrops(long amount, List<ItemStack> drops, Level level, BlockPos pos) {
    }

    @Override
    public boolean hasComponents() {
        return false;
    }

    @Override
    public boolean equals(Object object) {
        return this == object || object instanceof FakeKey other && this.id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public String toString() {
        return this.id;
    }
}
