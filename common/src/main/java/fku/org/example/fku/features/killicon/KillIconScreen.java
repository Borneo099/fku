package fku.org.example.fku.features.killicon; /* water */

import fku.org.example.fku.client.gui.GuiRenderHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class KillIconScreen extends Screen {

    private static final int W = 280, H = 240;
    private int cx, cy;
    private EditBox xIn, yIn, durIn, maxIn, opIn;
    private Button saveBtn, closeBtn;
    private final boolean[] toggles = new boolean[5]; // hs,bg,combo,dist,anim
    private static final String[] TOGGLE_LABELS = {"爆头图标", "背景", "连杀", "距离", "动画"};

    public KillIconScreen() { super(Component.literal("击杀图标配置")); }

    @Override
    protected void init() {
        super.init();
        cx = (width - W) / 2; cy = (height - H) / 2;
        var c = KillIconConfig.getInstance();
        toggles[0] = c.headshotEnabled; toggles[1] = c.showBackground;
        toggles[2] = c.showCombo; toggles[3] = c.showDistance; toggles[4] = c.enableAnimation;

        xIn = mkEdit(cx + 40, cy + 25, 50, String.valueOf(c.x), "-?\\d*");
        yIn = mkEdit(cx + 130, cy + 25, 50, String.valueOf(c.y), "-?\\d*");
        durIn = mkEdit(cx + 85, cy + 50, 55, String.valueOf(c.displayDuration), "\\d*");
        maxIn = mkEdit(cx + 205, cy + 50, 40, String.valueOf(c.maxEntries), "\\d*");
        opIn = mkEdit(cx + 100, cy + 110, 45, String.valueOf(c.bgOpacity), "\\d*");

        // ★ 不使用 toggle 按钮（rebuildWidgets 会导致按钮失效），直接用普通按钮+即时更新文字
        for (int i = 0; i < 5; i++) {
            final int idx = i;
            int bx = cx + 10 + (i % 3) * 90;
            int by = cy + 75 + (i / 3) * 20;
            int bw = (i % 3 == 2 && i < 3) ? 80 : 80;
            addRenderableWidget(Button.builder(
                Component.literal(toggleText(idx)),
                b -> { toggles[idx] = !toggles[idx]; b.setMessage(Component.literal(toggleText(idx))); }
            ).bounds(bx, by, bw, 16).build());
        }

        saveBtn = addRenderableWidget(Button.builder(Component.literal("§a保存"), b -> save()).bounds(cx + 30, cy + 200, 100, 20).build());
        closeBtn = addRenderableWidget(Button.builder(Component.literal("§c关闭"), b -> onClose()).bounds(cx + 150, cy + 200, 100, 20).build());
    }

    private String toggleText(int idx) {
        return (toggles[idx] ? "§a✔ " : "§7✘ ") + TOGGLE_LABELS[idx];
    }

    private EditBox mkEdit(int x, int y, int w, String val, String filter) {
        var b = new EditBox(font, x, y, w, 14, Component.literal(""));
        b.setValue(val); b.setMaxLength(6); b.setFilter(s -> s.matches(filter));
        addWidget(b); return b;
    }

    private void save() {
        var c = KillIconConfig.getInstance();
        try { c.x = Integer.parseInt(xIn.getValue()); } catch (NumberFormatException e) {}
        try { c.y = Integer.parseInt(yIn.getValue()); } catch (NumberFormatException e) {}
        try { c.displayDuration = Math.max(10, Math.min(600, Integer.parseInt(durIn.getValue()))); } catch (NumberFormatException e) {}
        try { c.maxEntries = Math.max(1, Math.min(20, Integer.parseInt(maxIn.getValue()))); } catch (NumberFormatException e) {}
        try { c.bgOpacity = Math.max(0, Math.min(255, Integer.parseInt(opIn.getValue()))); } catch (NumberFormatException e) {}
        c.headshotEnabled = toggles[0]; c.showBackground = toggles[1] && c.bgOpacity > 0;
        c.showCombo = toggles[2]; c.showDistance = toggles[3]; c.enableAnimation = toggles[4];
        KillIconConfig.save();
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        GuiRenderHelper.drawPanelBackground(g, cx, cy, W, H, false);
        g.drawString(font, "§l击杀图标配置", cx + 10, cy + 8, 0xFFFFFF);
        g.fill(cx + 10, cy + 20, cx + W - 10, cy + 21, 0xFF444444);

        g.drawString(font, "X:", cx + 10, cy + 26, 0xAAAAAA); xIn.render(g, mx, my, pt);
        g.drawString(font, "Y:", cx + 105, cy + 26, 0xAAAAAA); yIn.render(g, mx, my, pt);
        g.drawString(font, "显示时长(Tick):", cx + 10, cy + 51, 0xAAAAAA); durIn.render(g, mx, my, pt);
        g.drawString(font, "Max:", cx + 175, cy + 51, 0xAAAAAA); maxIn.render(g, mx, my, pt);

        // toggle 按钮由 super.render 渲染，无需额外渲染
        g.drawString(font, "背景透明度(0-255):", cx + 10, cy + 113, 0xAAAAAA);
        opIn.render(g, mx, my, pt);

        g.drawString(font, "§7位置=拖动UI区域设置", cx + 10, cy + 138, 0x666666);
        g.drawString(font, "§7预览:", cx + 10, cy + 155, 0x888888);
        String preview = "";
        if (toggles[2]) preview += "§6[2连杀] ";
        if (toggles[0]) preview += "§c☠ ";
        preview += "§c✦ §fSteve";
        if (toggles[3]) preview += " §7(15m)";
        g.drawString(font, preview, cx + 50, cy + 155, 0xFFFFFF);

        super.render(g, mx, my, pt);
    }

    @Override public boolean isPauseScreen() { return false; }
}
