package com.beipuo.mekenergistics.client.overlay;

import com.beipuo.mekenergistics.MekEnergistics;
import com.beipuo.mekenergistics.network.packet.RequestUpgradeStatePacket;
import com.beipuo.mekenergistics.network.packet.UninstallMeUpgradePacket;
import com.beipuo.mekenergistics.network.packet.UpgradeStateSyncPacket;
import com.beipuo.mekenergistics.registry.ModItems;
import com.beipuo.mekenergistics.upgrade.MeUpgradeStateOwner;
import com.beipuo.mekenergistics.upgrade.MeUpgradeType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import mekanism.client.SpecialColors;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.window.GuiWindow;
import mekanism.client.render.IFancyFontRenderer.TextAlignment;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.inventory.container.SelectedWindowData;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

/**
 * Client overlay for the self-owned ME upgrade window. A tab on the machine GUI opens a window
 * listing every installed ME upgrade with an uninstall button; uninstalling goes through
 * {@link UninstallMeUpgradePacket} so the server revalidates menu, distance, permissions and
 * machine. The window intentionally replaces Mekanism's {@code GuiUpgradeWindow}.
 */
@EventBusSubscriber(modid = MekEnergistics.MODID, value = Dist.CLIENT)
public final class MeUpgradeWindowOverlay {
    private static final Component UPGRADE_BUTTON_TOOLTIP = Component.translatable("gui.mekenergistics.me_upgrades.button");
    private static final Component UPGRADE_WINDOW_TITLE = Component.translatable("gui.mekenergistics.me_upgrades.title");
    private static final Component UNINSTALL_TOOLTIP = Component.translatable("gui.mekenergistics.me_upgrades.uninstall");
    private static final Component EMPTY_TEXT = Component.translatable("gui.mekenergistics.me_upgrades.empty");
    private static final int TAB_X = 0;
    private static final int TAB_Y = 88;
    private static final int TAB_SIZE = 26;
    private static final int INNER_SIZE = 18;
    private static final int INNER_X_OFFSET = 3;
    private static final int INNER_Y_OFFSET = 4;
    private static final int WINDOW_WIDTH = 178;
    private static final int WINDOW_HEIGHT = 118;
    private static final int ROW_HEIGHT = 18;
    private static final int ROW_START_Y = 20;
    private static final int ICON_X = 8;
    private static final int TEXT_X = 30;
    private static final int UNINSTALL_WIDTH = 26;
    private static final int UNINSTALL_HEIGHT = 14;
    private static final ResourceLocation HOLDER_RIGHT = MekanismUtils.getResource(ResourceType.GUI, "holder_right.png");
    private static final ResourceLocation BUTTON = MekanismUtils.getResource(ResourceType.GUI, "button.png");
    private static final ResourceLocation UPGRADE_BUTTON_ICON = ResourceLocation.fromNamespaceAndPath(
            MekEnergistics.MODID, "textures/item/upgrade_me_pattern_provider.png");

    /** Client-side cache of the last synced upgrade state per machine position. */
    private static final Map<BlockPos, List<MeUpgradeType>> KNOWN_STATE = new HashMap<>();
    /** Positions already asked for state while their GUI is open. */
    private static final Set<BlockPos> REQUESTED = new HashSet<>();

    private MeUpgradeWindowOverlay() {
    }

    /** Applies a server upgrade-state snapshot to the cache and any matching open window. */
    public static void handleStateSync(UpgradeStateSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            KNOWN_STATE.put(packet.pos(), packet.installed());
            Minecraft minecraft = Minecraft.getInstance();
            if (!(minecraft.screen instanceof GuiMekanism<?> gui)) {
                return;
            }
            for (GuiWindow window : new ArrayList<>(gui.getWindows())) {
                if (window instanceof MeUpgradeWindow upgradeWindow && upgradeWindow.matches(packet.pos())) {
                    if (packet.installed().isEmpty()) {
                        upgradeWindow.close();
                    } else {
                        upgradeWindow.applyData(packet.installed());
                    }
                    return;
                }
            }
        });
    }

    @SubscribeEvent
    public static void render(ScreenEvent.Render.Post event) {
        Target target = findTarget(event.getScreen());
        if (target == null) {
            if (event.getScreen() instanceof GuiMekanism<?> gui) {
                closeOpenUpgradeWindows(gui);
            }
            return;
        }
        BlockPos pos = target.pos();
        if (!KNOWN_STATE.containsKey(pos) && REQUESTED.add(pos)) {
            PacketDistributor.sendToServer(new RequestUpgradeStatePacket(pos));
        }
        List<MeUpgradeType> data = KNOWN_STATE.get(pos);
        if (data == null || data.isEmpty()) {
            return;
        }
        ButtonBounds bounds = buttonBounds(target.gui());
        GuiGraphics graphics = event.getGuiGraphics();
        boolean windowOpen = hasUpgradeWindow(target.gui());
        MekanismRenderer.color(graphics, SpecialColors.TAB_UPGRADE);
        GuiUtils.blitNineSlicedSized(graphics, HOLDER_RIGHT, bounds.tabX(), bounds.tabY(), TAB_SIZE, TAB_SIZE, 4, 26, 9, 0, 0, 26, 9);
        MekanismRenderer.resetColor(graphics);
        int buttonTextureY = windowOpen || bounds.contains(event.getMouseX(), event.getMouseY()) ? 40 : 20;
        GuiUtils.blitNineSlicedSized(graphics, BUTTON, bounds.buttonX(), bounds.buttonY(), INNER_SIZE, INNER_SIZE, 20, 4, 200, 20, 0, buttonTextureY, 200, 60);
        graphics.blit(UPGRADE_BUTTON_ICON, bounds.buttonX() + 1, bounds.buttonY() + 1, 0, 0, 16, 16, 16, 16);
        if (bounds.contains(event.getMouseX(), event.getMouseY())) {
            graphics.renderTooltip(target.gui().font(), UPGRADE_BUTTON_TOOLTIP, event.getMouseX(), event.getMouseY());
        }
    }

    @SubscribeEvent
    public static void mouseClicked(ScreenEvent.MouseButtonPressed.Pre event) {
        if (event.getButton() != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return;
        }
        Target target = findTarget(event.getScreen());
        if (target == null) {
            return;
        }
        List<MeUpgradeType> data = KNOWN_STATE.get(target.pos());
        if (data == null || data.isEmpty() || !buttonBounds(target.gui()).contains(event.getMouseX(), event.getMouseY())) {
            return;
        }
        openUpgradeWindow(target.gui(), target);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void screenClosing(ScreenEvent.Closing event) {
        if (event.getScreen() instanceof GuiMekanism<?>) {
            KNOWN_STATE.clear();
            REQUESTED.clear();
        }
    }

    public static boolean shouldShowTab(GuiMekanism<?> gui) {
        Target target = findTarget(gui);
        if (target == null) {
            return false;
        }
        List<MeUpgradeType> data = KNOWN_STATE.get(target.pos());
        return data != null && !data.isEmpty();
    }

    public static Rect2i jeiButtonArea(GuiMekanism<?> gui) {
        ButtonBounds bounds = buttonBounds(gui);
        return new Rect2i(bounds.tabX(), bounds.tabY(), TAB_SIZE, TAB_SIZE);
    }

    private static Target findTarget(Screen screen) {
        if (!(screen instanceof GuiMekanism<?> gui) || !(gui.getMenu() instanceof MekanismTileContainer<?> container)) {
            return null;
        }
        if (!(container.getTileEntity() instanceof MeUpgradeStateOwner)) {
            return null;
        }
        return new Target(gui, container.getTileEntity().getBlockPos());
    }

    private static void openUpgradeWindow(GuiMekanism<?> gui, Target target) {
        if (hasUpgradeWindow(gui)) {
            return;
        }
        gui.addWindow(new MeUpgradeWindow(gui, (gui.getXSize() - WINDOW_WIDTH) / 2, 8, target.pos(), KNOWN_STATE.get(target.pos())));
    }

    private static boolean hasUpgradeWindow(GuiMekanism<?> gui) {
        for (GuiWindow window : gui.getWindows()) {
            if (window instanceof MeUpgradeWindow) {
                return true;
            }
        }
        return false;
    }

    private static void closeOpenUpgradeWindows(GuiMekanism<?> gui) {
        for (GuiWindow window : new ArrayList<>(gui.getWindows())) {
            if (window instanceof MeUpgradeWindow upgradeWindow) {
                upgradeWindow.close();
            }
        }
    }

    private static ButtonBounds buttonBounds(GuiMekanism<?> gui) {
        return new ButtonBounds(gui.getGuiLeft() + gui.getXSize() + TAB_X, gui.getGuiTop() + TAB_Y);
    }

    private record Target(GuiMekanism<?> gui, BlockPos pos) {
    }

    private record ButtonBounds(int x, int y) {
        private int tabX() {
            return this.x;
        }

        private int tabY() {
            return this.y;
        }

        private int buttonX() {
            return this.x + INNER_X_OFFSET;
        }

        private int buttonY() {
            return this.y + INNER_Y_OFFSET;
        }

        private boolean contains(double mouseX, double mouseY) {
            return mouseX >= buttonX() && mouseX < buttonX() + INNER_SIZE && mouseY >= buttonY() && mouseY < buttonY() + INNER_SIZE;
        }
    }

    private static final class MeUpgradeWindow extends GuiWindow {
        private final BlockPos pos;
        private List<MeUpgradeType> installed;

        private MeUpgradeWindow(IGuiWrapper gui, int x, int y, BlockPos pos, @Nullable List<MeUpgradeType> installed) {
            super(gui, x, y, WINDOW_WIDTH, WINDOW_HEIGHT, SelectedWindowData.UNSPECIFIED);
            this.pos = pos;
            this.installed = installed == null ? List.of() : installed;
        }

        private boolean matches(BlockPos other) {
            return this.pos.equals(other);
        }

        private void applyData(List<MeUpgradeType> installed) {
            this.installed = installed == null ? List.of() : installed;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                int row = rowAt(mouseY, this.installed.size());
                if (row >= 0 && uninstallBounds(row).contains((int) mouseX, (int) mouseY)) {
                    PacketDistributor.sendToServer(new UninstallMeUpgradePacket(this.pos, this.installed.get(row)));
                    return true;
                }
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
            super.renderForeground(guiGraphics, mouseX, mouseY);
            int titleWidth = font().width(UPGRADE_WINDOW_TITLE);
            drawScrollingString(guiGraphics, UPGRADE_WINDOW_TITLE, (width - titleWidth) / 2, 6, TextAlignment.LEFT,
                    titleTextColor(), titleWidth, 0, false);
            if (this.installed.isEmpty()) {
                drawScrollingString(guiGraphics, EMPTY_TEXT, relativeX + ICON_X, relativeY + ROW_START_Y + 10,
                        TextAlignment.LEFT, titleTextColor(), width - 16, 0, false);
                return;
            }
            for (int row = 0; row < this.installed.size(); row++) {
                MeUpgradeType type = this.installed.get(row);
                ItemStack stack = upgradeStack(type);
                int rowY = relativeY + ROW_START_Y + row * ROW_HEIGHT;
                if (!stack.isEmpty()) {
                    guiGraphics.renderItem(stack, relativeX + ICON_X, rowY);
                }
                Rect2i uninstall = uninstallBounds(row);
                int nameWidth = uninstall.getX() - 8 - (relativeX + TEXT_X) - 4;
                drawScrollingString(guiGraphics, Component.translatable(type.getItemLangKey()),
                        relativeX + TEXT_X, rowY + 3, TextAlignment.LEFT, titleTextColor(),
                        Math.max(16, nameWidth), 0, false);
                int textureY = uninstall.contains(mouseX, mouseY) ? 40 : 20;
                GuiUtils.blitNineSlicedSized(guiGraphics, BUTTON, uninstall.getX(), uninstall.getY(), uninstall.getWidth(),
                        uninstall.getHeight(), 20, 4, 200, 20, 0, textureY, 200, 60);
                if (uninstall.contains(mouseX, mouseY)) {
                    guiGraphics.renderTooltip(font(), UNINSTALL_TOOLTIP, mouseX, mouseY);
                }
            }
        }

        private int rowAt(double mouseY, int rowCount) {
            int localY = (int) mouseY - (relativeY + ROW_START_Y);
            if (localY < 0 || rowCount <= 0 || localY >= rowCount * ROW_HEIGHT) {
                return -1;
            }
            return localY / ROW_HEIGHT;
        }

        private Rect2i uninstallBounds(int row) {
            return new Rect2i(relativeX + WINDOW_WIDTH - UNINSTALL_WIDTH - 8,
                    relativeY + ROW_START_Y + row * ROW_HEIGHT + 2, UNINSTALL_WIDTH, UNINSTALL_HEIGHT);
        }

        private static ItemStack upgradeStack(MeUpgradeType type) {
            return switch (type) {
                case PATTERN_PROVIDER -> ModItems.ME_PATTERN_PROVIDER_UPGRADE.toStack();
                case PASSIVE_CRAFTING -> ModItems.ME_PASSIVE_CRAFTING_UPGRADE.toStack();
                case OUTPUT_INTERFACE -> ModItems.ME_OUTPUT_INTERFACE_UPGRADE.toStack();
            };
        }
    }
}
