package com.beipuo.mekenergistics.blockentity.support.io;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.KeyCounter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import mekanism.api.Action;
import mekanism.api.chemical.ChemicalStack;
import me.ramidzkh.mekae2.ae2.MekanismKey;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

public final class MePatternInputRouter {
    private MePatternInputRouter() {
    }

    /** Normalized single-key input used by machines with position-sensitive ports. */
    public record PatternInput(ItemStack item, ChemicalStack chemical, FluidStack fluid) {
        public boolean isItem() {
            return !this.item.isEmpty() && this.chemical.isEmpty() && this.fluid.isEmpty();
        }

        public boolean isChemical() {
            return this.item.isEmpty() && !this.chemical.isEmpty() && this.fluid.isEmpty();
        }

        public boolean isFluid() {
            return this.item.isEmpty() && this.chemical.isEmpty() && !this.fluid.isEmpty();
        }

        @Nullable
        public static PatternInput single(KeyCounter counter) {
            if (counter == null || counter.isEmpty()) {
                return null;
            }
            PatternInput input = null;
            for (var entry : counter) {
                AEKey key = entry.getKey();
                long amount = entry.getLongValue();
                PatternInput next;
                if (key instanceof AEItemKey itemKey && amount > 0 && amount <= Integer.MAX_VALUE) {
                    next = new PatternInput(itemKey.toStack((int) amount), ChemicalStack.EMPTY, FluidStack.EMPTY);
                } else if (key instanceof MekanismKey chemicalKey && amount > 0) {
                    next = new PatternInput(ItemStack.EMPTY, chemicalKey.getStack().copyWithAmount(amount), FluidStack.EMPTY);
                } else if (key instanceof AEFluidKey fluidKey && amount > 0 && amount <= Integer.MAX_VALUE) {
                    next = new PatternInput(ItemStack.EMPTY, ChemicalStack.EMPTY, fluidKey.toStack((int) amount));
                } else {
                    return null;
                }
                if (input != null) {
                    return null;
                }
                input = next;
            }
            return input;
        }

        public static ItemStack singleItem(KeyCounter counter) {
            PatternInput input = single(counter);
            return input != null && input.isItem() ? input.item() : ItemStack.EMPTY;
        }

        @Nullable
        public static PatternInput separate(KeyCounter[] counters) {
            if (counters == null || counters.length == 0) {
                return null;
            }
            ItemStack item = ItemStack.EMPTY;
            ChemicalStack chemical = ChemicalStack.EMPTY;
            FluidStack fluid = FluidStack.EMPTY;
            for (KeyCounter counter : counters) {
                PatternInput input = single(counter);
                if (input == null) {
                    return null;
                }
                if (input.isItem()) {
                    if (!item.isEmpty()) return null;
                    item = input.item();
                } else if (input.isChemical()) {
                    if (!chemical.isEmpty()) return null;
                    chemical = input.chemical();
                } else if (input.isFluid()) {
                    if (!fluid.isEmpty()) return null;
                    fluid = input.fluid();
                } else {
                    return null;
                }
            }
            return new PatternInput(item, chemical, fluid);
        }
    }

    public static boolean route(KeyCounter[] inputHolders, List<? extends MeInputPort> ports) {
        Map<AEKey, Long> requests = normalize(inputHolders);
        if (requests == null || requests.isEmpty() || ports.isEmpty()) {
            return false;
        }

        List<Map.Entry<AEKey, Long>> requestList = new ArrayList<>(requests.entrySet());
        List<Insertion> plan = new ArrayList<>();
        Map<MeInputPort, Object> snapshots = new java.util.IdentityHashMap<>();
        ports.forEach(port -> snapshots.put(port, port.snapshot()));
        if (!assign(requestList, 0, requestList.get(0).getValue(),
                ports, plan, new java.util.IdentityHashMap<>(), new java.util.IdentityHashMap<>())) {
            restoreInputs(snapshots);
            return false;
        }
        restoreInputs(snapshots);
        for (Insertion insertion : plan) {
            if (insertion.port().insert(insertion.key(), insertion.amount(), Action.EXECUTE) != insertion.amount()) {
                restoreInputs(snapshots);
                return false;
            }
        }
        return true;
    }

    private static boolean assign(List<Map.Entry<AEKey, Long>> requests, int index, long remaining,
            List<? extends MeInputPort> ports, List<Insertion> plan,
            Map<MeInputPort, AEKey> reservedKeys, Map<MeInputPort, Long> reservedAmounts) {
        if (index >= requests.size()) {
            return true;
        }
        Map.Entry<AEKey, Long> request = requests.get(index);
        if (remaining <= 0) {
            return assign(requests, index + 1,
                    index + 1 < requests.size() ? requests.get(index + 1).getValue() : 0,
                    ports, plan, reservedKeys, reservedAmounts);
        }
        for (MeInputPort port : ports) {
            AEKey reservedKey = reservedKeys.get(port);
            if (!port.supports(request.getKey()) || reservedKey != null && !reservedKey.equals(request.getKey())) {
                continue;
            }
            long alreadyReserved = reservedAmounts.getOrDefault(port, 0L);
            long available = Math.max(0L, port.insert(request.getKey(), remaining, Action.SIMULATE) - alreadyReserved);
            if (available <= 0) {
                continue;
            }
            long accepted = Math.min(remaining, available);
            int planSize = plan.size();
            plan.add(new Insertion(port, request.getKey(), accepted));
            reservedKeys.put(port, request.getKey());
            reservedAmounts.put(port, alreadyReserved + accepted);
            if (assign(requests, index, remaining - accepted, ports, plan, reservedKeys, reservedAmounts)) {
                return true;
            }
            while (plan.size() > planSize) {
                plan.remove(plan.size() - 1);
            }
            if (alreadyReserved == 0) {
                reservedKeys.remove(port);
                reservedAmounts.remove(port);
            } else {
                reservedAmounts.put(port, alreadyReserved);
            }
        }
        return false;
    }

    private static Map<AEKey, Long> normalize(KeyCounter[] holders) {
        if (holders == null || holders.length == 0) {
            return null;
        }
        Map<AEKey, Long> result = new LinkedHashMap<>();
        try {
            for (KeyCounter holder : holders) {
                if (holder == null || holder.isEmpty()) {
                    return null;
                }
                for (var entry : holder) {
                    if (entry.getKey() == null || entry.getLongValue() <= 0) {
                        return null;
                    }
                    result.merge(entry.getKey(), entry.getLongValue(), Math::addExact);
                }
            }
        } catch (ArithmeticException ignored) {
            return null;
        }
        return result;
    }

    private static void restoreInputs(Map<MeInputPort, Object> snapshots) {
        snapshots.forEach(MeInputPort::restore);
    }

    private record Insertion(MeInputPort port, AEKey key, long amount) {
    }
}
