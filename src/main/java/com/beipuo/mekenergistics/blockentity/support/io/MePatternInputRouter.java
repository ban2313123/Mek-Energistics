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
    /** Keeps smart-pattern capacity probing bounded; later ticks continue the pending batch. */
    private static final long MAX_CAPACITY_PROBE_COPIES = 1_048_576L;
    /** A malformed port must never be able to monopolize the server thread. */
    private static final int MAX_ASSIGNMENT_STEPS = 256;

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
        boolean assigned = requestList.size() == 1
                ? assignSingle(requestList.get(0), ports, plan,
                        new java.util.IdentityHashMap<>(), new java.util.IdentityHashMap<>())
                : assign(requestList, 0, requestList.get(0).getValue(),
                        ports, plan, 0, new java.util.IdentityHashMap<>(), new java.util.IdentityHashMap<>());
        if (!assigned) {
            return false;
        }
        return executePlan(plan, snapshots);
    }

    /**
     * Routes one AE counter per physical lane as one transaction. A port can
     * appear in more than one lane, but its simulated reservations are shared
     * across the complete assignment and all lanes roll back together.
     */
    public static boolean routeLanes(KeyCounter[] laneInputs,
            List<? extends List<? extends MeInputPort>> lanePorts) {
        if (laneInputs == null || lanePorts == null || laneInputs.length == 0
                || laneInputs.length != lanePorts.size()) {
            return false;
        }

        List<Insertion> plan = new ArrayList<>();
        Map<MeInputPort, Object> snapshots = new java.util.IdentityHashMap<>();
        for (List<? extends MeInputPort> ports : lanePorts) {
            if (ports == null || ports.isEmpty()) {
                return false;
            }
            for (MeInputPort port : ports) {
                if (port == null) {
                    return false;
                }
                snapshots.putIfAbsent(port, port.snapshot());
            }
        }

        Map<MeInputPort, AEKey> reservedKeys = new java.util.IdentityHashMap<>();
        Map<MeInputPort, Long> reservedAmounts = new java.util.IdentityHashMap<>();
        for (int lane = 0; lane < laneInputs.length; lane++) {
            Map<AEKey, Long> requests = normalize(new KeyCounter[] {laneInputs[lane]});
            if (requests == null || requests.isEmpty()) {
                return false;
            }
            List<Map.Entry<AEKey, Long>> requestList = new ArrayList<>(requests.entrySet());
            int planSize = plan.size();
            boolean assigned = requestList.size() == 1
                    ? assignSingle(requestList.get(0), lanePorts.get(lane), plan,
                            reservedKeys, reservedAmounts)
                    : assign(requestList, 0, requestList.get(0).getValue(), lanePorts.get(lane), plan, planSize,
                            reservedKeys, reservedAmounts);
            if (!assigned) {
                while (plan.size() > planSize) {
                    plan.remove(plan.size() - 1);
                }
                return false;
            }
        }
        return executePlan(plan, snapshots);
    }

    /** Returns the largest whole-copy batch that can be simulated by the ports. */
    public static long maxAcceptedCopies(KeyCounter[] oneCraftInputs,
            List<? extends MeInputPort> ports) {
        if (oneCraftInputs == null || ports == null || ports.isEmpty()) {
            return 0;
        }
        Map<AEKey, Long> requests = normalize(oneCraftInputs);
        if (requests == null || requests.isEmpty()) {
            return 0;
        }
        if (requests.size() == 1) {
            Map.Entry<AEKey, Long> request = requests.entrySet().iterator().next();
            long capacity = 0;
            long probeAmount = request.getKey() instanceof MekanismKey ? Long.MAX_VALUE : Integer.MAX_VALUE;
            for (MeInputPort port : ports) {
                if (port.supports(request.getKey())) {
                    long accepted = Math.max(0, port.insert(request.getKey(), probeAmount, Action.SIMULATE));
                    capacity = accepted > Long.MAX_VALUE - capacity ? Long.MAX_VALUE : capacity + accepted;
                }
            }
            return Math.min(MAX_CAPACITY_PROBE_COPIES, capacity / request.getValue());
        }
        if (!canRoute(oneCraftInputs, ports)) {
            return 0;
        }
        long low = 1;
        long high = 2;
        high = Math.min(high, MAX_CAPACITY_PROBE_COPIES);
        while (high > low && high < Long.MAX_VALUE
                && canRoute(scale(oneCraftInputs, high), ports)) {
            low = high;
            high = high > MAX_CAPACITY_PROBE_COPIES / 2
                    ? MAX_CAPACITY_PROBE_COPIES : high * 2;
        }
        if (high == MAX_CAPACITY_PROBE_COPIES && canRoute(scale(oneCraftInputs, high), ports)) {
            return high;
        }
        while (low + 1 < high) {
            long middle = low + (high - low) / 2;
            if (canRoute(scale(oneCraftInputs, middle), ports)) {
                low = middle;
            } else {
                high = middle;
            }
        }
        return low;
    }

    /** Returns the largest batch accepted by position-sensitive lanes. */
    public static long maxAcceptedLaneCopies(KeyCounter[] oneCraftInputs,
            List<? extends List<? extends MeInputPort>> lanePorts) {
        if (oneCraftInputs == null || lanePorts == null || lanePorts.isEmpty()
                || !canRouteLanes(oneCraftInputs, lanePorts)) {
            return 0;
        }
        long low = 1;
        long high = 2;
        high = Math.min(high, MAX_CAPACITY_PROBE_COPIES);
        while (high > low && high < Long.MAX_VALUE
                && canRouteLanes(scale(oneCraftInputs, high), lanePorts)) {
            low = high;
            high = high > MAX_CAPACITY_PROBE_COPIES / 2
                    ? MAX_CAPACITY_PROBE_COPIES : high * 2;
        }
        if (high == MAX_CAPACITY_PROBE_COPIES && canRouteLanes(scale(oneCraftInputs, high), lanePorts)) {
            return high;
        }
        while (low + 1 < high) {
            long middle = low + (high - low) / 2;
            if (canRouteLanes(scale(oneCraftInputs, middle), lanePorts)) {
                low = middle;
            } else {
                high = middle;
            }
        }
        return low;
    }

    private static boolean assign(List<Map.Entry<AEKey, Long>> requests, int index, long remaining,
            List<? extends MeInputPort> ports, List<Insertion> plan,
            int requestPlanStart,
            Map<MeInputPort, AEKey> reservedKeys, Map<MeInputPort, Long> reservedAmounts) {
        if (plan.size() >= MAX_ASSIGNMENT_STEPS) {
            return false;
        }
        if (index >= requests.size()) {
            return true;
        }
        Map.Entry<AEKey, Long> request = requests.get(index);
        if (remaining <= 0) {
            return assign(requests, index + 1,
                    index + 1 < requests.size() ? requests.get(index + 1).getValue() : 0,
                    ports, plan, plan.size(), reservedKeys, reservedAmounts);
        }
        for (MeInputPort port : ports) {
            boolean usedInRequest = false;
            for (int i = requestPlanStart; i < plan.size(); i++) {
                if (plan.get(i).port() == port) {
                    usedInRequest = true;
                    break;
                }
            }
            if (usedInRequest) {
                continue;
            }
            AEKey reservedKey = reservedKeys.get(port);
            if (!port.supports(request.getKey()) || reservedKey != null && !reservedKey.equals(request.getKey())) {
                continue;
            }
            long alreadyReserved = reservedAmounts.getOrDefault(port, 0L);
            long available = simulateAdditionalCapacity(
                    port, request.getKey(), remaining, alreadyReserved);
            if (available <= 0) {
                continue;
            }
            long accepted = Math.min(remaining, available);
            if (accepted <= 0 || alreadyReserved > Long.MAX_VALUE - accepted) {
                continue;
            }
            int planSize = plan.size();
            plan.add(new Insertion(port, request.getKey(), accepted));
            reservedKeys.put(port, request.getKey());
            reservedAmounts.put(port, alreadyReserved + accepted);
            if (assign(requests, index, remaining - accepted, ports, plan, requestPlanStart,
                    reservedKeys, reservedAmounts)) {
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

    /** Fast path for the overwhelmingly common single-key request, including large factories. */
    private static boolean assignSingle(Map.Entry<AEKey, Long> request,
            List<? extends MeInputPort> ports, List<Insertion> plan,
            Map<MeInputPort, AEKey> reservedKeys, Map<MeInputPort, Long> reservedAmounts) {
        long remaining = request.getValue();
        for (MeInputPort port : ports) {
            AEKey reservedKey = reservedKeys.get(port);
            if (!port.supports(request.getKey()) || reservedKey != null && !reservedKey.equals(request.getKey())) {
                continue;
            }
            long alreadyReserved = reservedAmounts.getOrDefault(port, 0L);
            long available = simulateAdditionalCapacity(
                    port, request.getKey(), remaining, alreadyReserved);
            long accepted = Math.min(remaining, available);
            if (accepted <= 0 || alreadyReserved > Long.MAX_VALUE - accepted) {
                continue;
            }
            plan.add(new Insertion(port, request.getKey(), accepted));
            reservedKeys.put(port, request.getKey());
            reservedAmounts.put(port, alreadyReserved + accepted);
            remaining -= accepted;
            if (remaining <= 0) {
                return true;
            }
        }
        return false;
    }

    private static long simulateAdditionalCapacity(MeInputPort port, AEKey key,
            long requested, long alreadyReserved) {
        long numericLimit = key instanceof AEItemKey || key instanceof AEFluidKey
                ? Integer.MAX_VALUE : Long.MAX_VALUE;
        if (requested <= 0 || alreadyReserved < 0 || alreadyReserved >= numericLimit) {
            return 0;
        }
        long offered = requested > numericLimit - alreadyReserved
                ? numericLimit : requested + alreadyReserved;
        long acceptedIncludingReservations = port.insert(key, offered, Action.SIMULATE);
        return Math.max(0L, acceptedIncludingReservations - alreadyReserved);
    }

    private static Map<AEKey, Long> normalize(KeyCounter[] holders) {
        if (holders == null || holders.length == 0) {
            return null;
        }
        Map<AEKey, Long> result = new LinkedHashMap<>();
        try {
            for (KeyCounter holder : holders) {
                // One physical lane must resolve to one AE key. Alternatives belong in
                // separate lanes/ports and must not be merged into one transaction request.
                if (holder == null || holder.isEmpty() || holder.size() != 1) {
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

    private static boolean canRoute(KeyCounter[] inputs, List<? extends MeInputPort> ports) {
        Map<AEKey, Long> requests = normalize(inputs);
        if (requests == null || requests.isEmpty()) {
            return false;
        }
        List<Insertion> plan = new ArrayList<>();
        List<Map.Entry<AEKey, Long>> requestList = new ArrayList<>(requests.entrySet());
        boolean accepted = requestList.size() == 1
                ? assignSingle(requestList.get(0), ports, plan,
                        new java.util.IdentityHashMap<>(), new java.util.IdentityHashMap<>())
                : assign(requestList, 0, requestList.get(0).getValue(), ports, plan, 0,
                        new java.util.IdentityHashMap<>(), new java.util.IdentityHashMap<>());
        return accepted;
    }

    private static boolean canRouteLanes(KeyCounter[] inputs,
            List<? extends List<? extends MeInputPort>> lanePorts) {
        if (inputs.length != lanePorts.size()) {
            return false;
        }
        List<Insertion> plan = new ArrayList<>();
        Map<MeInputPort, AEKey> reservedKeys = new java.util.IdentityHashMap<>();
        Map<MeInputPort, Long> reservedAmounts = new java.util.IdentityHashMap<>();
        for (int lane = 0; lane < inputs.length; lane++) {
            Map<AEKey, Long> requests = normalize(new KeyCounter[] {inputs[lane]});
            if (requests == null || requests.isEmpty()) {
                return false;
            }
            List<Map.Entry<AEKey, Long>> requestList = new ArrayList<>(requests.entrySet());
            boolean assigned = requestList.size() == 1
                    ? assignSingle(requestList.get(0), lanePorts.get(lane), plan,
                            reservedKeys, reservedAmounts)
                    : assign(requestList, 0, requestList.get(0).getValue(), lanePorts.get(lane), plan, plan.size(),
                            reservedKeys, reservedAmounts);
            if (!assigned) {
                return false;
            }
        }
        return true;
    }

    private static KeyCounter[] scale(KeyCounter[] inputs, long copies) {
        KeyCounter[] scaled = new KeyCounter[inputs.length];
        try {
            for (int i = 0; i < inputs.length; i++) {
                KeyCounter counter = new KeyCounter();
                if (inputs[i] == null) {
                    return new KeyCounter[inputs.length];
                }
                for (var entry : inputs[i]) {
                    counter.add(entry.getKey(), Math.multiplyExact(entry.getLongValue(), copies));
                }
                scaled[i] = counter;
            }
            return scaled;
        } catch (ArithmeticException ex) {
            return new KeyCounter[inputs.length];
        }
    }

    private static void restoreInputs(Map<MeInputPort, Object> snapshots) {
        snapshots.forEach(MeInputPort::restore);
    }

    private static boolean executePlan(List<Insertion> plan, Map<MeInputPort, Object> snapshots) {
        Map<MeInputPort, Object> touched = new java.util.IdentityHashMap<>();
        for (Insertion insertion : plan) {
            touched.putIfAbsent(insertion.port(), snapshots.get(insertion.port()));
            if (insertion.port().insert(insertion.key(), insertion.amount(), Action.EXECUTE) != insertion.amount()) {
                restoreInputs(touched);
                return false;
            }
        }
        return true;
    }

    private record Insertion(MeInputPort port, AEKey key, long amount) {
    }
}
