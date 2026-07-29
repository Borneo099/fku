package fku.org.example.fku.features.freecam; /* water */

import fku.org.example.fku.client.gui.GuiRenderHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

/**
 * 灵魂出窍配置界面 — 速度/平滑度设置
 * 该配置界面由赛博教员实现
 */
public class FreecamScreen extends Screen {

    private static final int W = 200, H = 150;
    private AbstractWidget speedInput, smoothInput;

    public FreecamScreen() {
        super(Component.literal("灵魂出窍 配置"));
    }

    @Override
    protected void init() {
        clearWidgets();
        speedInput = null; smoothInput = null;

        var cfg = FreecamConfig.getInstance();
        int cx = (width - W) / 2, cy = (height - H) / 2;
        int ly = cy + 20;

        addLabel(cx + 10, ly, "最大速度(5~500):");
        speedInput = mkEdit(cx + 130, ly, 50, String.valueOf((int) cfg.maxSpeed));
        ly += 22;

        addLabel(cx + 10, ly, "平滑度(1~100):");
        smoothInput = mkEdit(cx + 130, ly, 50, String.valueOf((int) cfg.smoothness));
        ly += 22;

        // 显示提示开关
        addRenderableWidget(Button.builder(
            Component.literal("提示: " + (cfg.showOverlay ? "§a开" : "§7关")),
            b -> { cfg.setShowOverlay(!cfg.showOverlay); init(); })
            .bounds(cx + 10, ly, 80, 14).build());
        ly += 22;

        addRenderableWidget(Button.builder(Component.literal("§a保存并返回"),
            b -> { save(); this.minecraft.setScreen(null); })
            .bounds(cx + W / 2 - 40, cy + H - 30, 80, 16).build());
    }

    private void addLabel(int x, int y, String text) {
        addRenderableWidget(Button.builder(Component.literal("§7" + text), b -> {}).bounds(x, y, font.width(text) + 4, 14).build());
    }

    private AbstractWidget mkEdit(int x, int y, int w, String val) {
        var b = new EditBox(font, x, y, w, 14, Component.literal(""));
        b.setValue(val); b.setMaxLength(8); b.setFilter(s -> s.matches("\\d*"));
        addWidget(b); return b;
    }

    private void save() {
        var cfg = FreecamConfig.getInstance();
        try { if (speedInput instanceof EditBox e && !e.getValue().isEmpty()) cfg.setMaxSpeed(Integer.parseInt(e.getValue())); } catch (Exception ignored) {}
        try { if (smoothInput instanceof EditBox e && !e.getValue().isEmpty()) cfg.setSmoothness(Integer.parseInt(e.getValue())); } catch (Exception ignored) {}
        cfg.save();
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        int cx = (width - W) / 2, cy = (height - H) / 2;
        GuiRenderHelper.drawPanelBackground(g, cx, cy, W, H, false);
        g.drawString(font, "§l§6灵魂出窍 配置", cx + 8, cy + 8, 0xFFFFFF);
        if (speedInput instanceof EditBox e) e.render(g, mx, my, pt);
        if (smoothInput instanceof EditBox e) e.render(g, mx, my, pt);
        super.render(g, mx, my, pt);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (speedInput instanceof EditBox e) e.mouseClicked(mx, my, button);
        if (smoothInput instanceof EditBox e) e.mouseClicked(mx, my, button);
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean keyPressed(int k, int s, int m) {
        if (speedInput instanceof EditBox e && e.isFocused()) return e.keyPressed(k, s, m);
        if (smoothInput instanceof EditBox e && e.isFocused()) return e.keyPressed(k, s, m);
        if (k == 256) { save(); this.minecraft.setScreen(null); return true; }
        return super.keyPressed(k, s, m);
    }

    @Override public void onClose() { save(); super.onClose(); }
    @Override public boolean isPauseScreen() { return false; }
}