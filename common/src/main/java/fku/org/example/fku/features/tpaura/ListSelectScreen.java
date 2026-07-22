package fku.org.example.fku.features.tpaura;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class ListSelectScreen
extends Screen {
    private static final int PANEL_W = 320;
    private static final int PANEL_H = 400;
    private static final int ITEM_HEIGHT = 13;
    private static final int LIST_X = 20;
    private static final int LIST_W = 280;
    private static final int LIST_TOP = 75;
    private static final int LIST_BOTTOM_OFFSET = 60;
    private final String screenTitle;
    private final List<String> allItems;
    private final Set<String> selected;
    private final Consumer<Set<String>> callback;
    private EditBox searchBox;
    private List<String> filteredItems;
    private int scrollOffset = 0;
    private int maxVisibleItems;
    private String filterText = "";

    public ListSelectScreen(String title, Collection<String> allItems, Collection<String> currentSelected, Consumer<Set<String>> callback) {
        super(Component.literal(title));
        this.screenTitle = title;
        this.allItems = new ArrayList<String>(new TreeSet<String>(allItems));
        this.selected = new HashSet<String>(currentSelected);
        this.callback = callback;
        this.filteredItems = new ArrayList<String>(this.allItems);
    }

    protected void init() {
        super.init();
        int cx = (this.width - 320) / 2;
        int cy = (this.height - 400) / 2;
        this.searchBox = new EditBox(this.font, cx + 20, cy + 35, 280, 16, Component.literal("\u641c\u7d22."));
        this.searchBox.setMaxLength(50);
        this.searchBox.setFilter(s -> true);
        this.searchBox.setResponder(this::onSearchChanged);
        this.addRenderableWidget(this.searchBox);
        this.addRenderableWidget(Button.builder(Component.literal("\u5168\u9009"), btn -> this.selectAll()).bounds(cx + 20, cy + 55, 60, 16).build());
        this.addRenderableWidget(Button.builder(Component.literal("\u53d6\u6d88\u5168\u9009"), btn -> this.deselectAll()).bounds(cx + 85, cy + 55, 70, 16).build());
        this.addRenderableWidget(Button.builder(Component.literal("\u53cd\u9009"), btn -> this.invertSelection()).bounds(cx + 160, cy + 55, 50, 16).build());
        this.addRenderableWidget(Button.builder(Component.literal(("\u603b\u6570:" + this.selected.size())), btn -> {}).bounds(cx + 220, cy + 55, 70, 16).build());
        this.addRenderableWidget(Button.builder(Component.literal("\u4fdd\u5b58"), btn -> this.saveAndClose()).bounds(cx + 80, cy + 400 - 35, 60, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("\u53d6\u6d88"), btn -> this.onClose()).bounds(cx + 180, cy + 400 - 35, 60, 20).build());
        int listAreaHeight = 265;
        this.maxVisibleItems = Math.max(1, listAreaHeight / 13);
    }

    private void onSearchChanged(String text) {
        this.filterText = text.toLowerCase().trim();
        this.filteredItems = this.allItems.stream().filter(s -> this.filterText.isEmpty() || s.toLowerCase().contains(this.filterText)).collect(Collectors.toList());
        this.scrollOffset = 0;
    }

    private void selectAll() {
        this.selected.addAll(this.filteredItems);
    }

    private void deselectAll() {
        this.selected.removeAll(this.filteredItems);
    }

    private void invertSelection() {
        for (String item : this.filteredItems) {
            if (this.selected.contains(item)) {
                this.selected.remove(item);
                continue;
            }
            this.selected.add(item);
        }
    }

    private void toggleItem(String item) {
        if (this.selected.contains(item)) {
            this.selected.remove(item);
        } else {
            this.selected.add(item);
        }
    }

    private void saveAndClose() {
        if (this.callback != null) {
            this.callback.accept(new HashSet<String>(this.selected));
        }
        this.onClose();
    }

    public void render(@NotNull GuiGraphics g, int mx, int my, float pt) {
        int y;
        this.renderBackground(g);
        int cx = (this.width - 320) / 2;
        int cy = (this.height - 400) / 2;
        g.fill(cx, cy, cx + 320, cy + 400, -870178270);
        g.fill(cx, cy, cx + 320, cy + 1, -12303292);
        g.fill(cx, cy + 400 - 1, cx + 320, cy + 400, -12303292);
        g.fill(cx, cy, cx + 1, cy + 400, -12303292);
        g.fill(cx + 320 - 1, cy, cx + 320, cy + 400, -12303292);
        g.drawString(this.font, this.screenTitle, cx + 20, cy + 12, 0xFFFFFF);
        g.drawString(this.font, "\u00a77\u5df2\u9009: " + this.selected.size() + " / " + this.filteredItems.size() + " \u9879", cx + 20, cy + 400 - 52, 0xAAAAAA);
        int listY = cy + 75;
        int listEndY = cy + 400 - 60;
        int visibleArea = listEndY - listY;
        int maxScroll = Math.max(0, this.filteredItems.size() - this.maxVisibleItems);
        if (this.scrollOffset > maxScroll) {
            this.scrollOffset = maxScroll;
        }
        if (this.scrollOffset < 0) {
            this.scrollOffset = 0;
        }
        this.enableScissor(cx + 20, listY, cx + 20 + 280, listEndY);
        for (int i = this.scrollOffset; i < this.filteredItems.size() && (y = listY + (i - this.scrollOffset) * 13) + 13 <= listEndY; ++i) {
            boolean isHovered;
            String item = this.filteredItems.get(i);
            boolean isSelected = this.selected.contains(item);
            boolean bl = isHovered = mx >= cx + 20 && mx < cx + 20 + 280 && my >= y && my < y + 13;
            if (isHovered) {
                g.fill(cx + 20, y, cx + 20 + 280, y + 13, 0x33FFFFFF);
            }
            String marker = isSelected ? "\u00a7a[\u2713]" : "\u00a77[ ]";
            g.drawString(this.font, marker + " " + item, cx + 20 + 4, y + 2, isSelected ? 0x55FF55 : 0xCCCCCC);
        }
        this.disableScissor();
        if (this.filteredItems.size() > this.maxVisibleItems) {
            int barY = listY + (this.scrollOffset / this.filteredItems.size() * visibleArea);
            int barH = Math.max(10, (this.maxVisibleItems / this.filteredItems.size() * visibleArea));
            g.fill(cx + 20 + 280 - 4, Math.max(barY, listY), cx + 20 + 280 - 1, Math.min(barY + barH, listEndY), -2002081110);
        }
        super.render(g, mx, my, pt);
    }

    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0) {
            int y;
            int cx = (this.width - 320) / 2;
            int cy = (this.height - 400) / 2;
            int listY = cy + 75;
            for (int i = this.scrollOffset; i < this.filteredItems.size() && (y = listY + (i - this.scrollOffset) * 13) + 13 <= cy + 400 - 60; ++i) {
                if (!(mx >= (cx + 20)) || !(mx < (cx + 20 + 280)) || !(my >= y) || !(my < (y + 13))) continue;
                this.toggleItem(this.filteredItems.get(i));
                return true;
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    public boolean mouseScrolled(double mx, double my, double delta) {
        if (delta < 0.0) {
            this.scrollOffset = Math.min(this.scrollOffset + 3, Math.max(0, this.filteredItems.size() - this.maxVisibleItems));
        } else if (delta > 0.0) {
            this.scrollOffset = Math.max(this.scrollOffset - 3, 0);
        }
        return true;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.searchBox.isFocused() && this.searchBox.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (keyCode == 256) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public boolean charTyped(char codePoint, int modifiers) {
        if (this.searchBox.isFocused() && this.searchBox.charTyped(codePoint, modifiers)) {
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    public boolean isPauseScreen() {
        return false;
    }

    public void onClose() {
        this.minecraft.setScreen(null);
    }

    private void enableScissor(int x1, int y1, int x2, int y2) {
        int scale = (int)Minecraft.getInstance().getWindow().getGuiScale();
        int sx1 = x1 * scale;
        int sy1 = Minecraft.getInstance().getWindow().getScreenHeight() - y2 * scale;
        int sx2 = x2 * scale;
        int sy2 = Minecraft.getInstance().getWindow().getScreenHeight() - y1 * scale;
        RenderSystem.enableScissor(sx1, sy1, (sx2 - sx1), (sy2 - sy1));
    }

    private void disableScissor() {
        RenderSystem.disableScissor();
    }
}

