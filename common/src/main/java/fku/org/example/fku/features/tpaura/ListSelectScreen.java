package fku.org.example.fku.features.tpaura;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.systems.RenderSystem;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 通用实体选择列表界面
 *
 * 职责：提供可搜索、多选、全选/反选的实体类型列表，选择和保存配置。
 * 用于 TpAuraScreen 中的"目标实体类型"和"白名单"两个配置项。
 *
 * 用法：
 *   Minecraft.getInstance().setScreen(new ListSelectScreen(
 *       "选择目标实体", allItems, currentSelected, newSelection -> {
 *           cfg.setEntityTypes(String.join(",", newSelection));
 *       }
 *   ));
 */
public class ListSelectScreen extends Screen {

    private static final int PANEL_W = 320;
    private static final int PANEL_H = 400;
    private static final int ITEM_HEIGHT = 13;
    private static final int LIST_X = 20;
    private static final int LIST_W = 280;
    private static final int LIST_TOP = 75;
    private static final int LIST_BOTTOM_OFFSET = 60; // 底部按钮区域高度

    private final String screenTitle;
    private final List<String> allItems;          // 全部条目（已排序）
    private final Set<String> selected;           // 当前选中（可变）
    private final Consumer<Set<String>> callback; // 保存回调

    // ════════ 控件 ════════
    private EditBox searchBox;
    private List<String> filteredItems;           // 搜索过滤后的条目
    private int scrollOffset = 0;
    private int maxVisibleItems;

    public ListSelectScreen(String title, Collection<String> allItems,
                            Collection<String> currentSelected,
                            Consumer<Set<String>> callback) {
        super(Component.literal(title));
        this.screenTitle = title;
        this.allItems = new ArrayList<>(new TreeSet<>(allItems)); // 去重+排序
        this.selected = new HashSet<>(currentSelected);
        this.callback = callback;
        this.filteredItems = new ArrayList<>(this.allItems);
    }

    @Override
    protected void init() {
        super.init();
        int cx = (width - PANEL_W) / 2;
        int cy = (height - PANEL_H) / 2;

        // ── 搜索框 ──
        searchBox = new EditBox(font, cx + 20, cy + 35, 280, 16, Component.literal("搜索..."));
        searchBox.setMaxLength(50);
        searchBox.setFilter(s -> true);
        searchBox.setResponder(this::onSearchChanged);
        addRenderableWidget(searchBox);

        // ── 顶部按钮行 ──
        addRenderableWidget(Button.builder(
                Component.literal("全选"),
                btn -> selectAll()
        ).bounds(cx + 20, cy + 55, 60, 16).build());

        addRenderableWidget(Button.builder(
                Component.literal("取消全选"),
                btn -> deselectAll()
        ).bounds(cx + 85, cy + 55, 70, 16).build());

        addRenderableWidget(Button.builder(
                Component.literal("反选"),
                btn -> invertSelection()
        ).bounds(cx + 160, cy + 55, 50, 16).build());

        addRenderableWidget(Button.builder(
                Component.literal("总数:" + selected.size()),
                btn -> {} // 仅显示，无操作
        ).bounds(cx + 220, cy + 55, 70, 16).build());

        // ── 底部按钮 ──
        addRenderableWidget(Button.builder(
                Component.literal("保存"),
                btn -> saveAndClose()
        ).bounds(cx + 80, cy + PANEL_H - 35, 60, 20).build());

        addRenderableWidget(Button.builder(
                Component.literal("取消"),
                btn -> onClose()
        ).bounds(cx + 180, cy + PANEL_H - 35, 60, 20).build());

        // 计算可见条目数
        int listAreaHeight = PANEL_H - LIST_TOP - LIST_BOTTOM_OFFSET;
        maxVisibleItems = Math.max(1, listAreaHeight / ITEM_HEIGHT);
    }

    // ══════════════════════════════════════════════
    //  搜索过滤
    // ══════════════════════════════════════════════

    private void onSearchChanged(String text) {
        filterText = text.toLowerCase().trim();
        filteredItems = allItems.stream()
                .filter(s -> filterText.isEmpty() || s.toLowerCase().contains(filterText))
                .collect(Collectors.toList());
        scrollOffset = 0;
    }

    private String filterText = "";

    // ══════════════════════════════════════════════
    //  选择操作
    // ══════════════════════════════════════════════

    private void selectAll() {
        selected.addAll(filteredItems);
    }

    private void deselectAll() {
        selected.removeAll(filteredItems);
    }

    private void invertSelection() {
        for (String item : filteredItems) {
            if (selected.contains(item)) {
                selected.remove(item);
            } else {
                selected.add(item);
            }
        }
    }

    private void toggleItem(String item) {
        if (selected.contains(item)) {
            selected.remove(item);
        } else {
            selected.add(item);
        }
    }

    private void saveAndClose() {
        if (callback != null) {
            callback.accept(new HashSet<>(selected));
        }
        onClose();
    }

    // ══════════════════════════════════════════════
    //  渲染
    // ══════════════════════════════════════════════

    @Override
    public void render(@NotNull GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        int cx = (width - PANEL_W) / 2;
        int cy = (height - PANEL_H) / 2;

        // ── 面板背景 ──
        g.fill(cx, cy, cx + PANEL_W, cy + PANEL_H, 0xCC222222);
        // 边框
        g.fill(cx, cy, cx + PANEL_W, cy + 1, 0xFF444444);
        g.fill(cx, cy + PANEL_H - 1, cx + PANEL_W, cy + PANEL_H, 0xFF444444);
        g.fill(cx, cy, cx + 1, cy + PANEL_H, 0xFF444444);
        g.fill(cx + PANEL_W - 1, cy, cx + PANEL_W, cy + PANEL_H, 0xFF444444);

        // ── 标题 ──
        g.drawString(font, screenTitle, cx + 20, cy + 12, 0xFFFFFF);

        // ── 已选计数 ──
        g.drawString(font, "§7已选: " + selected.size() + " / " + filteredItems.size() + " 项", cx + 20, cy + PANEL_H - 52, 0xAAAAAA);

        // ── 列表渲染 ──
        int listY = cy + LIST_TOP;
        int listEndY = cy + PANEL_H - LIST_BOTTOM_OFFSET;
        int visibleArea = listEndY - listY;

        // 限制滚动范围
        int maxScroll = Math.max(0, filteredItems.size() - maxVisibleItems);
        if (scrollOffset > maxScroll) scrollOffset = maxScroll;
        if (scrollOffset < 0) scrollOffset = 0;

        // 裁剪区域
        enableScissor(cx + LIST_X, listY, cx + LIST_X + LIST_W, listEndY);

        for (int i = scrollOffset; i < filteredItems.size(); i++) {
            int y = listY + (i - scrollOffset) * ITEM_HEIGHT;
            if (y + ITEM_HEIGHT > listEndY) break;

            String item = filteredItems.get(i);
            boolean isSelected = selected.contains(item);
            boolean isHovered = mx >= cx + LIST_X && mx < cx + LIST_X + LIST_W
                    && my >= y && my < y + ITEM_HEIGHT;

            // 悬停高亮
            if (isHovered) {
                g.fill(cx + LIST_X, y, cx + LIST_X + LIST_W, y + ITEM_HEIGHT, 0x33FFFFFF);
            }

            // 选择标记 [ ] 或 [x]
            String marker = isSelected ? "§a[✓]" : "§7[ ]";
            g.drawString(font, marker + " " + item, cx + LIST_X + 4, y + 2, isSelected ? 0x55FF55 : 0xCCCCCC);
        }

        disableScissor();

        // ── 滚动条 ──
        if (filteredItems.size() > maxVisibleItems) {
            int barY = listY + (int) ((float) scrollOffset / filteredItems.size() * visibleArea);
            int barH = Math.max(10, (int) ((float) maxVisibleItems / filteredItems.size() * visibleArea));
            g.fill(cx + LIST_X + LIST_W - 4, Math.max(barY, listY),
                    cx + LIST_X + LIST_W - 1, Math.min(barY + barH, listEndY), 0x88AAAAAA);
        }

        super.render(g, mx, my, pt);
    }

    // ══════════════════════════════════════════════
    //  输入处理
    // ══════════════════════════════════════════════

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0) {
            int cx = (width - PANEL_W) / 2;
            int cy = (height - PANEL_H) / 2;
            int listY = cy + LIST_TOP;

            // 检查列表点击
            for (int i = scrollOffset; i < filteredItems.size(); i++) {
                int y = listY + (i - scrollOffset) * ITEM_HEIGHT;
                if (y + ITEM_HEIGHT > cy + PANEL_H - LIST_BOTTOM_OFFSET) break;

                if (mx >= cx + LIST_X && mx < cx + LIST_X + LIST_W
                        && my >= y && my < y + ITEM_HEIGHT) {
                    toggleItem(filteredItems.get(i));
                    return true;
                }
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double delta) {
        if (delta < 0) {
            scrollOffset = Math.min(scrollOffset + 3, Math.max(0, filteredItems.size() - maxVisibleItems));
        } else if (delta > 0) {
            scrollOffset = Math.max(scrollOffset - 3, 0);
        }
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (searchBox.isFocused() && searchBox.keyPressed(keyCode, scanCode, modifiers)) return true;
        if (keyCode == 256) { onClose(); return true; }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (searchBox.isFocused() && searchBox.charTyped(codePoint, modifiers)) return true;
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(null);
    }

    // ══════════════════════════════════════════════
    //  裁剪辅助
    // ══════════════════════════════════════════════

    private void enableScissor(int x1, int y1, int x2, int y2) {
        int scale = (int) Minecraft.getInstance().getWindow().getGuiScale();
        int sx1 = x1 * scale;
        int sy1 = (int) (Minecraft.getInstance().getWindow().getScreenHeight() - y2 * scale);
        int sx2 = x2 * scale;
        int sy2 = (int) (Minecraft.getInstance().getWindow().getScreenHeight() - y1 * scale);
        RenderSystem.enableScissor(sx1, sy1, sx2 - sx1, sy2 - sy1);
    }

    private void disableScissor() {
        RenderSystem.disableScissor();
    }
}