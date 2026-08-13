package com.beipuo.mekenergistics.blockentity.support.io;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;

/** Immutable physical input layout shared by direct pushes and smart batching. */
public record MeInputLayout(List<? extends MeInputPort> ports,
        List<? extends List<? extends MeInputPort>> lanes) {
    private static final MeInputLayout EMPTY = new MeInputLayout(List.of(), List.of());

    public MeInputLayout {
        ports = List.copyOf(ports);
        lanes = lanes.stream().map(List::copyOf).toList();
        if (!ports.isEmpty() && !lanes.isEmpty()) {
            throw new IllegalArgumentException("An input layout cannot be unordered and lane-based at once");
        }
    }

    public static MeInputLayout empty() {
        return EMPTY;
    }

    public static MeInputLayout unordered(List<? extends MeInputPort> ports) {
        return ports == null || ports.isEmpty() ? EMPTY : new MeInputLayout(ports, List.of());
    }

    public static MeInputLayout lanes(List<? extends List<? extends MeInputPort>> lanes) {
        return lanes == null || lanes.isEmpty() ? EMPTY : new MeInputLayout(List.of(), lanes);
    }

    public boolean isEmpty() {
        return this.ports.isEmpty() && this.lanes.isEmpty();
    }

    public boolean route(KeyCounter[] inputs) {
        if (isEmpty()) {
            return false;
        }
        return this.lanes.isEmpty()
                ? MePatternInputRouter.route(inputs, this.ports)
                : MePatternInputRouter.routeLanes(inputs, this.lanes);
    }

    public long maxAcceptedCopies(KeyCounter[] oneCraftInputs) {
        if (isEmpty()) {
            return 0;
        }
        return this.lanes.isEmpty()
                ? MePatternInputRouter.maxAcceptedCopies(oneCraftInputs, this.ports)
                : MePatternInputRouter.maxAcceptedLaneCopies(oneCraftInputs, this.lanes);
    }

    /**
     * Returns how much of one interface resource can enter any compatible physical input. Interface
     * stocking is not a positional pattern, so lane groupings are deliberately flattened here.
     */
    public long maxAcceptedInterfaceAmount(AEKey key, long requested) {
        if (key == null || requested <= 0 || isEmpty()) {
            return 0;
        }
        KeyCounter counter = new KeyCounter();
        counter.add(key, 1);
        return Math.min(requested,
                MePatternInputRouter.maxAcceptedCopies(new KeyCounter[]{counter}, interfacePorts()));
    }

    /** Routes one stocked interface resource into any compatible physical input. */
    public boolean routeInterface(AEKey key, long amount) {
        if (key == null || amount <= 0 || isEmpty()) {
            return false;
        }
        KeyCounter counter = new KeyCounter();
        counter.add(key, amount);
        return MePatternInputRouter.route(new KeyCounter[]{counter}, interfacePorts());
    }

    private List<? extends MeInputPort> interfacePorts() {
        if (!this.ports.isEmpty()) {
            return this.ports;
        }
        List<MeInputPort> flattened = new ArrayList<>();
        IdentityHashMap<MeInputPort, Boolean> seen = new IdentityHashMap<>();
        for (List<? extends MeInputPort> lane : this.lanes) {
            for (MeInputPort port : lane) {
                if (seen.put(port, Boolean.TRUE) == null) {
                    flattened.add(port);
                }
            }
        }
        return flattened;
    }
}
