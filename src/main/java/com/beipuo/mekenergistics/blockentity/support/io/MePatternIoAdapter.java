package com.beipuo.mekenergistics.blockentity.support.io;

import com.beipuo.mekenergistics.blockentity.api.MePatternIoOwner;
import java.util.List;

/** Immutable view of an owner's externally supplied pattern I/O. */
public record MePatternIoAdapter(List<? extends MeInputPort> inputPorts,
        List<? extends MeOutputPort> outputPorts, boolean busy) {
    public MePatternIoAdapter {
        inputPorts = List.copyOf(inputPorts);
        outputPorts = List.copyOf(outputPorts);
    }

    public static MePatternIoAdapter of(MePatternIoOwner owner) {
        return new MePatternIoAdapter(owner.getPatternInputPorts(), owner.getPatternOutputPorts(), owner.isPatternBusy());
    }
}
