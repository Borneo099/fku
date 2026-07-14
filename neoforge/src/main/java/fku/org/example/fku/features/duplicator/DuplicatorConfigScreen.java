package fku.org.example.fku.features.duplicator;

import fku.org.example.fku.client.gui.GuiRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

/**
 * 三叉戟复制配置 — 开关初始显示真实状态
 */
public class DuplicatorConfigScreen extends Screen {

    private static final int W = 280, H = 240;
    private int cx, cy;
    private EditBox dupeDelayInput, holdDurationInput;

    public DuplicatorConfigScreen() {
        super(Component.literal("三叉戟复制配置"));
    }

    @Override
    protected void init() {
        super.init();
        cx = (width - W) / 2;
        cy = (height - H) / 2;
        var cfg = DuplicatorConfig.getInstance();

        dupeDelayInput = mkEdit(cx + 130, cy + 30, 50, String.valueOf(cfg.dupeDelay));
        holdDurationInput = mkEdit(cx + 130, cy + 52, 50, String.valueOf(cfg.holdDuration));

        // 开关 — 初始文字从 config 读取
        toggle(cfg, "自动丢弃",   cy + 76, c -> c.dropTridents, (c, v) -> c.setDropTridents(v));
        toggle(cfg, "绕过GrimV3", cy + 96, c -> c.bypassGrim, (c, v) -> c.setBypassGrim(v));
        toggle(cfg, "受伤自动关闭", cy + 116, c -> c.autoCloseOnDamage, (c, v) -> c.setAutoCloseOnDamage(v));
        toggle(cfg, "自动清理背包", cy + 136, c -> c.autoCleanInventory, (c, v) -> c.setAutoCleanInventory(v));
        toggle(cfg, "耐久管理",   cy + 156, c -> c.durabilityManagement, (c, v) -> c.setDurabilityManagement(v));

        addRenderableWidget(Button.builder(Component.literal("§a保存并返回"), b -> {
            saveInputs(); Minecraft.getInstance().setScreen(null);
        }).bounds(cx + W / 2 - 40, cy + H - 22, 80, 16).build());
    }

    private void toggle(DuplicatorConfig cfg, String label, int y,
                        java.util.function.Function<DuplicatorConfig, Boolean> getter,
                        DupeSetter setter) {
        boolean cur = getter.apply(cfg);
        addRenderableWidget(Button.builder(
                Component.literal(label + (cur ? " §a开" : " §7关")),
                btn -> {
                    var c = DuplicatorConfig.getInstance();
                    boolean now = !getter.apply(c);
                    setter.accept(c, now);
                    btn.setMessage(Component.literal(label + (now ? " §a开" : " §7关")));
                }
        ).bounds(cx + 10, y, 150, 14).build());
    }

    @FunctionalInterface
    private interface DupeSetter { void accept(DuplicatorConfig cfg, boolean v); }

    private void saveInputs() {
        var cfg = DuplicatorConfig.getInstance();
        try { cfg.setDupeDelay(Integer.parseInt(dupeDelayInput.getValue())); } catch (Exception ignored) {}
        try { cfg.setHoldDuration(Integer.parseInt(holdDurationInput.getValue())); } catch (Exception ignored) {}
    }

    private EditBox mkEdit(int x, int y, int w, String val) {
        var b = new EditBox(font, x, y, w, 14, Component.literal(""));
        b.setValue(val); b.setMaxLength(8); b.setFilter(s -> s.matches("\\d*"));
        addWidget(b); return b;
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g, mx, my, pt);
        GuiRenderHelper.drawPanelBackground(g, cx, cy, W, H, false);
        g.drawString(font, "§l§b三叉戟复制配置", cx + 10, cy + 8, 0xFFFFFF);
        g.drawString(font, "§7冷却延迟(tick):", cx + 12, cy + 33, 0xAAAAAA);
        g.drawString(font, "§7蓄力时长(tick):", cx + 12, cy + 55, 0xAAAAAA);
        dupeDelayInput.render(g, mx, my, pt);
        holdDurationInput.render(g, mx, my, pt);
        super.render(g, mx, my, pt);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        dupeDelayInput.mouseClicked(mx, my, button);
        holdDurationInput.mouseClicked(mx, my, button);
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { saveInputs(); Minecraft.getInstance().setScreen(null); return true; }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    @Override public void onClose() { saveInputs(); super.onClose(); }
    @Override public boolean isPauseScreen() { return false; }
}
