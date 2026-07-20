package com.beipuo.mekenergistics.blockentity.support.io;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import me.ramidzkh.mekae2.ae2.MekanismKey;
import java.util.ArrayList;
import java.util.List;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.inventory.IInventorySlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

public final class MeMachineIoAdapter {
    private MeMachineIoAdapter() {
    }

    public static MeInputPort itemInput(IInventorySlot slot) {
        return new MeInputPort() {
            @Override
            public boolean supports(AEKey key) {
                return key instanceof AEItemKey;
            }

            @Override
            public long insert(AEKey key, long amount, Action action) {
                if (!(key instanceof AEItemKey itemKey) || amount <= 0 || amount > Integer.MAX_VALUE) {
                    return 0;
                }
                ItemStack offered = itemKey.toStack((int) amount);
                ItemStack remainder = slot.insertItem(offered, action, AutomationType.INTERNAL);
                return offered.getCount() - remainder.getCount();
            }

            @Override
            public Object snapshot() {
                return slot.getStack().copy();
            }

            @Override
            public void restore(Object snapshot) {
                slot.setStack(((ItemStack) snapshot).copy());
            }
        };
    }

    /**
     * Treats the parallel item inputs of a Mekanism factory as one physical
     * input. Mekanism performs its recipe-aware redistribution before the next
     * processing pass; duplicating that logic here would incorrectly split
     * recipes whose per-process input is greater than one.
     */
    public static MeInputPort autoSortedFactoryItemInput(List<? extends IInventorySlot> slots) {
        return groupedInput(slots.stream().map(MeMachineIoAdapter::itemInput).toList());
    }

    static MeInputPort groupedInput(List<? extends MeInputPort> ports) {
        List<? extends MeInputPort> inputPorts = List.copyOf(ports);
        return new MeInputPort() {
            @Override
            public boolean supports(AEKey key) {
                return inputPorts.stream().anyMatch(port -> port.supports(key));
            }

            @Override
            public long insert(AEKey key, long amount, Action action) {
                if (key == null || amount <= 0 || inputPorts.isEmpty()) {
                    return 0;
                }
                long inserted = 0;
                for (MeInputPort inputPort : inputPorts) {
                    long offeredAmount = amount - inserted;
                    if (offeredAmount <= 0) {
                        break;
                    }
                    inserted += inputPort.insert(key, offeredAmount, action);
                }
                return inserted;
            }

            @Override
            public Object snapshot() {
                List<Object> snapshot = new ArrayList<>(inputPorts.size());
                for (MeInputPort inputPort : inputPorts) {
                    snapshot.add(inputPort.snapshot());
                }
                return snapshot;
            }

            @Override
            @SuppressWarnings("unchecked")
            public void restore(Object snapshot) {
                List<Object> states = (List<Object>) snapshot;
                for (int i = 0; i < inputPorts.size(); i++) {
                    inputPorts.get(i).restore(states.get(i));
                }
            }
        };
    }

    public static MeInputPort chemicalInput(IChemicalTank tank) {
        return new MeInputPort() {
            @Override
            public boolean supports(AEKey key) {
                return key instanceof MekanismKey;
            }

            @Override
            public long insert(AEKey key, long amount, Action action) {
                if (!(key instanceof MekanismKey chemicalKey) || amount <= 0) {
                    return 0;
                }
                ChemicalStack offered = chemicalKey.getStack().copyWithAmount(amount);
                ChemicalStack remainder = tank.insert(offered, action, AutomationType.INTERNAL);
                return offered.getAmount() - remainder.getAmount();
            }

            @Override
            public Object snapshot() {
                return tank.getStack().copy();
            }

            @Override
            public void restore(Object snapshot) {
                tank.setStack(((ChemicalStack) snapshot).copy());
            }
        };
    }

    public static MeInputPort fluidInput(IExtendedFluidTank tank) {
        return new MeInputPort() {
            @Override
            public boolean supports(AEKey key) {
                return key instanceof AEFluidKey;
            }

            @Override
            public long insert(AEKey key, long amount, Action action) {
                if (!(key instanceof AEFluidKey fluidKey) || amount <= 0 || amount > Integer.MAX_VALUE) {
                    return 0;
                }
                FluidStack offered = fluidKey.toStack((int) amount);
                FluidStack remainder = tank.insert(offered, action, AutomationType.INTERNAL);
                return offered.getAmount() - remainder.getAmount();
            }

            @Override
            public Object snapshot() {
                return tank.getFluid().copy();
            }

            @Override
            public void restore(Object snapshot) {
                tank.setStack(((FluidStack) snapshot).copy());
            }
        };
    }

    public static MeOutputPort itemOutput(IInventorySlot slot) {
        return output(new OutputAccess() {
            @Override public AEKey key() { return slot.getStack().isEmpty() ? null : AEItemKey.of(slot.getStack()); }
            @Override public long amount() { return slot.getCount(); }
            @Override public Object snapshot() { return slot.getStack().copy(); }
            @Override public void restore(Object state) { slot.setStack(((ItemStack) state).copy()); }
            @Override public void shrink(long amount) {
                ItemStack stack = slot.getStack().copy();
                stack.shrink((int) amount);
                slot.setStack(stack.isEmpty() ? ItemStack.EMPTY : stack);
            }
        });
    }

    public static MeOutputPort chemicalOutput(IChemicalTank tank) {
        return output(new OutputAccess() {
            @Override public AEKey key() { return tank.isEmpty() ? null : MekanismKey.of(tank.getStack()); }
            @Override public long amount() { return tank.getStored(); }
            @Override public Object snapshot() { return tank.getStack().copy(); }
            @Override public void restore(Object state) { tank.setStack(((ChemicalStack) state).copy()); }
            @Override public void shrink(long amount) { tank.shrinkStack(amount, Action.EXECUTE); }
        });
    }

    public static MeOutputPort fluidOutput(IExtendedFluidTank tank) {
        return output(new OutputAccess() {
            @Override public AEKey key() { return tank.isEmpty() ? null : AEFluidKey.of(tank.getFluid()); }
            @Override public long amount() { return tank.getFluidAmount(); }
            @Override public Object snapshot() { return tank.getFluid().copy(); }
            @Override public void restore(Object state) { tank.setStack(((FluidStack) state).copy()); }
            @Override public void shrink(long amount) { tank.shrinkStack((int) amount, Action.EXECUTE); }
        });
    }

    private static MeOutputPort output(OutputAccess access) {
        return new MeOutputPort() {
            @Override public AEKey key() { return access.key(); }
            @Override public long amount() { return access.amount(); }
            @Override public long extract(long amount, Action action) {
                long extracted = Math.min(Math.max(0, amount), access.amount());
                if (extracted > 0 && action.execute()) {
                    access.shrink(extracted);
                }
                return extracted;
            }
            @Override public Object snapshot() { return access.snapshot(); }
            @Override public void restore(Object snapshot) { access.restore(snapshot); }
        };
    }

    private interface OutputAccess {
        AEKey key();
        long amount();
        Object snapshot();
        void restore(Object state);
        void shrink(long amount);
    }
}
