package com.beipuo.mekenergistics.blockentity.support.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.KeyCounter;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

class MeMachineIoAdapterTest {
    private static final FakeKey IRON = new FakeKey("iron");

    @Test
    void routerSimulatesThenCommitsAcrossPorts() {
        FakeInput first = new FakeInput(IRON, 3);
        FakeInput second = new FakeInput(IRON, 5);
        KeyCounter counter = new KeyCounter();
        counter.add(IRON, 8);

        assertTrue(MePatternInputRouter.route(new KeyCounter[] { counter }, List.of(first, second)));
        assertEquals(3, first.amount);
        assertEquals(5, second.amount);
        assertEquals(2, first.simulations);
    }

    @Test
    void routerRollsBackEarlierPortsWhenExecutionChanges() {
        FakeInput first = new FakeInput(IRON, 3);
        FakeInput second = new FakeInput(IRON, 5);
        second.failExecution = true;
        KeyCounter counter = new KeyCounter();
        counter.add(IRON, 8);

        assertFalse(MePatternInputRouter.route(new KeyCounter[] { counter }, List.of(first, second)));
        assertEquals(0, first.amount);
        assertEquals(0, second.amount);
    }

    @Test
    void outputCollectorRollsBackDestinationAndSource() {
        FakeOutput output = new FakeOutput(IRON, 4);
        FakeInput destination = new FakeInput(IRON, 100);
        destination.failExecution = true;

        assertFalse(MeOutputCollector.collectAll(List.of(output), destination));
        assertEquals(4, output.amount);
        assertEquals(0, destination.amount);
    }

    private static final class FakeInput implements MeInputPort {
        private final AEKey key;
        private final long capacity;
        private long amount;
        private int simulations;
        private boolean failExecution;

        private FakeInput(AEKey key, long capacity) {
            this.key = key;
            this.capacity = capacity;
        }

        @Override public boolean supports(AEKey key) { return this.key.equals(key); }
        @Override public long insert(AEKey key, long amount, mekanism.api.Action action) {
            if (!supports(key) || this.failExecution && action.execute()) return 0;
            if (!action.execute()) simulations++;
            long accepted = Math.min(amount, capacity - this.amount);
            if (action.execute()) this.amount += accepted;
            return Math.max(0, accepted);
        }
        @Override public Object snapshot() { return amount; }
        @Override public void restore(Object snapshot) { amount = (long) snapshot; }
    }

    private static final class FakeOutput implements MeOutputPort {
        private final AEKey key;
        private long amount;
        private FakeOutput(AEKey key, long amount) { this.key = key; this.amount = amount; }
        @Override public AEKey key() { return amount == 0 ? null : key; }
        @Override public long amount() { return amount; }
        @Override public long extract(long requested, mekanism.api.Action action) {
            long extracted = Math.min(requested, amount);
            if (action.execute()) amount -= extracted;
            return extracted;
        }
        @Override public Object snapshot() { return amount; }
        @Override public void restore(Object snapshot) { amount = (long) snapshot; }
    }

    private static final class FakeKey extends AEKey {
        private final String id;
        private FakeKey(String id) { this.id = id; }
        @Override public AEKeyType getType() { throw new UnsupportedOperationException(); }
        @Override public AEKey dropSecondary() { return this; }
        @Override public CompoundTag toTag(HolderLookup.Provider registries) { CompoundTag tag = new CompoundTag(); tag.putString("id", id); return tag; }
        @Override public Object getPrimaryKey() { return id; }
        @Override public ResourceLocation getId() { return ResourceLocation.fromNamespaceAndPath("mekenergistics_test", id); }
        @Override public void writeToPacket(RegistryFriendlyByteBuf data) { data.writeUtf(id); }
        @Override protected Component computeDisplayName() { return Component.literal(id); }
        @Override public void addDrops(long amount, List<ItemStack> drops, Level level, BlockPos pos) { }
        @Override public boolean hasComponents() { return false; }
        @Override public boolean equals(Object o) { return o == this || o instanceof FakeKey other && id.equals(other.id); }
        @Override public int hashCode() { return id.hashCode(); }
    }
}
