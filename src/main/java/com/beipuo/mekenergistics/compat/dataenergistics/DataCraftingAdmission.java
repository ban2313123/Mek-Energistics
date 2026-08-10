package com.beipuo.mekenergistics.compat.dataenergistics;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.KeyCounter;
import com.beipuo.mekenergistics.blockentity.support.AbstractMeAeSupport;
import com.fish_dan_.data_energistics.api.crafting.dispatch.CountedCraftingAdmission;
import org.jetbrains.annotations.Nullable;

public final class DataCraftingAdmission {
    private DataCraftingAdmission() {
    }

    @Nullable
    public static CountedCraftingAdmission prepare(AbstractMeAeSupport<?> support,
            IPatternDetails patternDetails, KeyCounter[] prototype, long requestedCount) {
        if (support == null || patternDetails == null || prototype == null || requestedCount <= 0
                || support.isPatternBusy() || !support.hasRegisteredPattern(patternDetails)) {
            return null;
        }
        long capacity = support.maxAcceptedCopies(prototype);
        long count = Math.min(requestedCount, capacity);
        return count <= 0 ? null : new Admission(support, prototype, count);
    }

    private static KeyCounter[] scale(KeyCounter[] prototype, long count) {
        KeyCounter[] scaled = new KeyCounter[prototype.length];
        for (int index = 0; index < prototype.length; index++) {
            KeyCounter target = new KeyCounter();
            for (var entry : prototype[index]) {
                long amount = entry.getLongValue();
                if (amount < 0 || amount > Long.MAX_VALUE / count) {
                    throw new IllegalArgumentException("Invalid counted input amount");
                }
                if (amount > 0) {
                    target.add(entry.getKey(), amount * count);
                }
            }
            scaled[index] = target;
        }
        return scaled;
    }

    private static final class Admission implements CountedCraftingAdmission {
        private final AbstractMeAeSupport<?> support;
        private final KeyCounter[] preparedPrototype;
        private final long count;
        private boolean attempted;
        private boolean transferred;

        private Admission(AbstractMeAeSupport<?> support, KeyCounter[] preparedPrototype, long count) {
            this.support = support;
            this.preparedPrototype = preparedPrototype;
            this.count = count;
        }

        @Override
        public long count() {
            return count;
        }

        @Override
        public boolean hasTransferredInputOwnership() {
            return transferred;
        }

        @Override
        public boolean commit(KeyCounter[] prototype) {
            if (prototype != preparedPrototype) {
                throw new IllegalArgumentException("Admission must use its prepared prototype");
            }
            if (attempted) {
                throw new IllegalStateException("Admission has already been committed");
            }
            attempted = true;
            KeyCounter[] scaled = scale(prototype, count);
            if (!support.routeDataPatternInputs(scaled)) {
                return false;
            }
            transferred = true;
            return true;
        }
    }
}
