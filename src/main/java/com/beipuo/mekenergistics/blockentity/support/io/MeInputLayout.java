package com.beipuo.mekenergistics.blockentity.support.io;

import appeng.api.stacks.KeyCounter;
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
}
