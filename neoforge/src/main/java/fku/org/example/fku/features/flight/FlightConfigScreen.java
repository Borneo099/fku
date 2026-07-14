package fku.org.example.fku.features.flight; /* water */

import fku.org.example.fku.client.gui.GuiRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

/**
 * 飞行配置界面
 */
public class FlightConfigScreen extends Screen {

    private static final int W = 280, H = 230;
    private EditBox speedInput, vertInput, tapInput;
    private Button collisionBtn, onlyCreativeBtn, hungerBtn, sprintBtn, particleBtn, soundBtn, antiKickBtn;

    public FlightConfigScreen() {
        super(Component.literal("飞行配置"));
    }

    @Override
    protected void init() {
        FlightConfig cfg = FlightConfig.getInstance();
        int cx = (width - W) / 2, cy = (height - H) / 2;

        // 第1行：速度
        addRenderableWidget(Button.builder(Component.literal("水平速度:"), b -> {}).bounds(cx + 5, cy + 8, 70, 18).build());
        speedInput = new EditBox(font, cx + 80, cy + 8, 50, 16, Component.literal(""));
        speedInput.setValue(String.format("%.2f", cfg.flySpeed)); speedInput.setFilter(s -> s.matches("\\d*\\.?\\d*")); addWidget(speedInput);

        addRenderableWidget(Button.builder(Component.literal("垂直速度:"), b -> {}).bounds(cx + 145, cy + 8, 70, 18).build());
        vertInput = new EditBox(font, cx + 220, cy + 8, 50, 16, Component.literal(""));
        vertInput.setValue(String.format("%.2f", cfg.verticalSpeed)); vertInput.setFilter(s -> s.matches("\\d*\\.?\\d*")); addWidget(vertInput);

        // 第2行：双击窗口
        addRenderableWidget(Button.builder(Component.literal("双击窗口:"), b -> {}).bounds(cx + 5, cy + 33, 70, 18).build());
        tapInput = new EditBox(font, cx + 80, cy + 33, 50, 16, Component.literal(""));
        tapInput.setValue(String.valueOf(cfg.doubleTapWindow)); tapInput.setFilter(s -> s.matches("\\d*")); addWidget(tapInput);

        // 开关：两列布局
        int ly = cy + 60, sp = 20, lx = cx + 10, rx = cx + 145;
        collisionBtn   = toggle(lx, ly, "禁用碰撞", cfg.disableCollision, v -> cfg.setDisableCollision(v));
        onlyCreativeBtn = toggle(rx, ly, "仅创造", cfg.onlyInCreative, v -> cfg.setOnlyInCreative(v));
        hungerBtn       = toggle(lx, ly + sp, "饥饿消耗", cfg.consumeHunger, v -> cfg.setConsumeHunger(v));
        sprintBtn       = toggle(rx, ly + sp, "允许疾跑", cfg.allowSprint, v -> cfg.setAllowSprint(v));
        particleBtn     = toggle(lx, ly + sp * 2, "粒子效果", cfg.particleEffect, v -> cfg.setParticleEffect(v));
        soundBtn        = toggle(rx, ly + sp * 2, "音效反馈", cfg.soundFeedback, v -> cfg.setSoundFeedback(v));
        antiKickBtn     = toggle(lx, ly + sp * 3, "防踢", cfg.antiKick, v -> cfg.setAntiKick(v));

        // 返回
        addRenderableWidget(Button.builder(Component.literal("§a返回"), b -> saveAndClose())
            .bounds(cx + W / 2 - 30, cy + H - 28, 60, 18).build());
    }

    private Button toggle(int x, int y, String label, boolean cur, java.util.function.Consumer<Boolean> setter) {
        return addRenderableWidget(Button.builder(
            Component.literal(label + ": " + (cur ? "§a开" : "§c关")),
            b -> { setter.accept(!cur); b.setMessage(Component.literal(label + ": " + (!cur ? "§a开" : "§c关"))); }
        ).bounds(x, y, 125, 16).build());
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g, mx, my, pt);
        int cx = (width - W) / 2, cy = (height - H) / 2;
        GuiRenderHelper.drawPanelBackground(g, cx, cy, W, H, false);
        super.render(g, mx, my, pt);
        if (speedInput != null) speedInput.render(g, mx, my, pt);
        if (vertInput != null) vertInput.render(g, mx, my, pt);
        if (tapInput != null) { tapInput.render(g, mx, my, pt); g.drawString(font, "ms", cx + 134, cy + 34, 0x666666); }
    }

    @Override public boolean mouseClicked(double mx, double my, int btn) {
        if (speedInput != null) speedInput.mouseClicked(mx, my, btn);
        if (vertInput != null) vertInput.mouseClicked(mx, my, btn);
        if (tapInput != null) tapInput.mouseClicked(mx, my, btn);
        return super.mouseClicked(mx, my, btn);
    }
    @Override public boolean keyPressed(int k, int s, int m) {
        if (speedInput != null && speedInput.isFocused()) return speedInput.keyPressed(k, s, m);
        if (vertInput != null && vertInput.isFocused()) return vertInput.keyPressed(k, s, m);
        if (tapInput != null && tapInput.isFocused()) return tapInput.keyPressed(k, s, m);
        if (k == 256) { saveAndClose(); return true; }
        return super.keyPressed(k, s, m);
    }
    @Override public boolean charTyped(char c, int m) {
        if (speedInput != null && speedInput.isFocused()) return speedInput.charTyped(c, m);
        if (vertInput != null && vertInput.isFocused()) return vertInput.charTyped(c, m);
        if (tapInput != null && tapInput.isFocused()) return tapInput.charTyped(c, m);
        return super.charTyped(c, m);
    }
    @Override public void onClose() { saveAndClose(); }
    @Override public boolean isPauseScreen() { return false; }

    private void saveAndClose() {
        FlightConfig cfg = FlightConfig.getInstance();
        try { cfg.setFlySpeed(Double.parseDouble(speedInput.getValue().trim())); } catch (NumberFormatException e) {}
        try { cfg.setVerticalSpeed(Double.parseDouble(vertInput.getValue().trim())); } catch (NumberFormatException e) {}
        try { cfg.setDoubleTapWindow(Integer.parseInt(tapInput.getValue().trim())); } catch (NumberFormatException e) {}
        this.minecraft.setScreen(null);
    }
}
