package com.beipuo.mekenergistics.item;

import com.beipuo.mekenergistics.upgrade.MeUpgradeType;
import java.util.List;
import java.util.Objects;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import org.jetbrains.annotations.NotNull;

/**
 * Self-owned ME upgrade item. Deliberately not a Mekanism {@code ItemUpgrade} and not an
 * {@code IUpgradeItem}; the installed state lives in the machine's {@code MeUpgradeContainer}.
 */
public final class MeUpgradeItem extends Item {
    private final MeUpgradeType type;

    public MeUpgradeItem(MeUpgradeType type, Properties properties) {
        super(properties);
        this.type = Objects.requireNonNull(type, "type");
    }

    public MeUpgradeType getType() {
        return this.type;
    }

    @NotNull
    @Override
    public InteractionResult useOn(UseOnContext context) {
        return MeUpgradeInteractionHandler.tryInstall(context);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context,
            @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable(this.type.getItemLangKey() + ".tooltip"));
    }
}
