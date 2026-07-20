package com.beipuo.mekenergistics.blockentity.support.io;

import com.beipuo.mekenergistics.blockentity.api.MePatternIoOwner;
import java.util.List;

/** Immutable view of an owner's externally supplied pattern I/O. */
public record MePatternIoAdapter(MeInputLayout inputLayout,
        List<? extends MeOutputPort> outputPorts, boolean busy) {
    public MePatternIoAdapter {
        inputLayout = inputLayout == null ? MeInputLayout.empty() : inputLayout;
        outputPorts = List.copyOf(outputPorts);
    }

    public static MePatternIoAdapter of(MePatternIoOwner owner) {
        return new MePatternIoAdapter(owner.getPatternInputLayout(), owner.getPatternOutputPorts(), owner.isPatternBusy());
    }
}
