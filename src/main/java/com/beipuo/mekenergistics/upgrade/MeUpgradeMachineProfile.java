package com.beipuo.mekenergistics.upgrade;

import com.beipuo.mekenergistics.blockentity.support.io.MeInputLayout;
import com.beipuo.mekenergistics.blockentity.support.io.MeOutputPort;
import com.beipuo.mekenergistics.common.machine.MeMekanismMachine;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * Static description of the Mekanism machine surface exposed by an ME upgrade.
 *
 * <p>The upgrade runtime owns lifecycle, AE nodes, persistence, and energy routing. A profile only
 * describes the machine-specific target, physical I/O, and terminal identity so another Mekanism
 * machine can be added without copying that runtime.</p>
 */
public record MeUpgradeMachineProfile<TILE extends TileEntityMekanism>(
        Predicate<TILE> target,
        Function<TILE, MeInputLayout> inputLayout,
        Function<TILE, List<? extends MeOutputPort>> outputPorts,
        MeMekanismMachine machine,
        Function<TILE, ItemStack> terminalIcon,
        Function<TILE, Component> terminalName) {

    public MeUpgradeMachineProfile {
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(inputLayout, "inputLayout");
        Objects.requireNonNull(outputPorts, "outputPorts");
        Objects.requireNonNull(machine, "machine");
        Objects.requireNonNull(terminalIcon, "terminalIcon");
        Objects.requireNonNull(terminalName, "terminalName");
    }

    public boolean matches(TILE tile) {
        return this.target.test(tile);
    }

    public MeInputLayout inputLayoutFor(TILE tile) {
        return this.inputLayout.apply(tile);
    }

    public List<? extends MeOutputPort> outputPortsFor(TILE tile) {
        return this.outputPorts.apply(tile);
    }

    public ItemStack terminalIconFor(TILE tile) {
        return this.terminalIcon.apply(tile);
    }

    public Component terminalNameFor(TILE tile) {
        return this.terminalName.apply(tile);
    }
}
