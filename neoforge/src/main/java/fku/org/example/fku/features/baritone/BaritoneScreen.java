package fku.org.example.fku.features.baritone;

import fku.org.example.fku.client.gui.GuiRenderHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Baritone 配置界面 — 规整排版，避免拥挤/重叠
 */
public class BaritoneScreen extends Screen {

    private static final int W = 300, H = 320;
    private int cx, cy;

    public BaritoneScreen() {
        super(Component.literal("Baritone 设置"));
    }

    @Override
    protected void init() {
        super.init();
        cx = (width - W) / 2;
        cy = (height - H) / 2;
        var cfg = BaritoneConfig.getInstance();

        int y = cy + 28, sp = 16;

        // ════════ 跑酷模式 ════════
        toggle("  破坏方块",     y, c -> c.allowBreak,     (c, v) -> c.allowBreak = v); y += sp;
        toggle("  放置方块",     y, c -> c.allowPlace,     (c, v) -> c.allowPlace = v); y += sp;
        toggle("  疾跑",         y, c -> c.allowSprint,    (c, v) -> c.allowSprint = v); y += sp;
        toggle("  跑酷跳跃",     y, c -> c.allowParkour,   (c, v) -> c.allowParkour = v); y += sp;
        toggle("  跑酷放置",     y, c -> c.allowParkourPlace, (c, v) -> c.allowParkourPlace = v); y += sp;
        toggle("  背包操作",     y, c -> c.allowInventory, (c, v) -> c.allowInventory = v); y += sp;

        // ── 跑酷总开关（单独一行，用不同颜色） ──
        toggleParkour(cfg, y); y += sp + 4;

        // ════════ 加速 ════════
        y += 4;
        int speedToggleY = y;
        toggle("启用加速",      y, c -> c.speedEnabled,   (c, v) -> { c.speedEnabled = v; BaritoneSpeedFeature.setEnabled(v); }); y += sp;

        // 速度倍率行
        int btnY = y;
        speedBtn("-0.5", cx + 68, btnY, c -> c.speedMultiplier = Math.max(1.0, c.speedMultiplier - 0.5));
        speedBtn("-0.1", cx + 108, btnY, c -> c.speedMultiplier = Math.max(1.0, c.speedMultiplier - 0.1));
        speedBtn("+0.1", cx + 148, btnY, c -> c.speedMultiplier = Math.min(32.0, c.speedMultiplier + 0.1));
        speedBtn("+0.5", cx + 188, btnY, c -> c.speedMultiplier = Math.min(32.0, c.speedMultiplier + 0.5));
        y += 18;

        toggle("  仅地面加速",  y, c -> c.groundOnly,    (c, v) -> c.groundOnly = v); y += sp + 6;

        // ════════ 鞘翅 ════════
        y += 4;
        int elytraY = y;
        toggle("启用鞘翅任意维度", y, c -> c.elytraEnabled, (c, v) -> { c.elytraEnabled = v; ElytraAnywhereFeature.setEnabled(v); });

        // ── 保存区 ──
        y += sp + 6;
        addRenderableWidget(Button.builder(
                Component.literal("§a保存并返回"),
                b -> onClose()
        ).bounds(cx + W / 2 - 45, y, 90, 18).build());

        // 记录各区域标题 Y 供 render 使用
        yParkourLabelY = cy + 26;
        ySpeedLabelY = speedToggleY - 2;
        yElytraLabelY = elytraY - 2;
    }

    private int yParkourLabelY, ySpeedLabelY, yElytraLabelY;

    /** 切换按钮 — 初始文字从 Config 读取，点击实时读取 */
    private void toggle(String label, int y,
                        java.util.function.Function<BaritoneConfig, Boolean> getter,
                        BaritoneSetter setter) {
        var cfg = BaritoneConfig.getInstance();
        boolean cur = getter.apply(cfg);
        addRenderableWidget(Button.builder(
                Component.literal(label + (cur ? " §a[ON]" : " §7[OFF]")),
                btn -> {
                    var c = BaritoneConfig.getInstance();
                    boolean now = !getter.apply(c);
                    setter.accept(c, now);
                    c.save();
                    btn.setMessage(Component.literal(label + (now ? " §a[ON]" : " §7[OFF]")));
                }
        ).bounds(cx + 20, y, W - 50, 14).build());
    }

    /** 跑酷总开关（宽按钮 + 高亮） */
    private void toggleParkour(BaritoneConfig cfg, int y) {
        boolean cur = cfg.parkourEnabled;
        addRenderableWidget(Button.builder(
                Component.literal("§6§l★ 跑酷模式 §7" + (cur ? "§a[ON]" : "§c[OFF]")),
                btn -> {
                    var c = BaritoneConfig.getInstance();
                    boolean now = !c.parkourEnabled;
                    c.parkourEnabled = now;
                    BaritoneParkourFeature.setEnabled(now);
                    c.save();
                    btn.setMessage(Component.literal("§6§l★ 跑酷模式 §7" + (now ? "§a[ON]" : "§c[OFF]")));
                }
        ).bounds(cx + 10, y, W - 20, 15).build());
    }

    @FunctionalInterface
    private interface BaritoneSetter { void accept(BaritoneConfig cfg, boolean v); }

    private void speedBtn(String text, int x, int y, Consumer<BaritoneConfig> modifier) {
        addRenderableWidget(Button.builder(Component.literal(text), b -> {
            modifier.accept(BaritoneConfig.getInstance());
            BaritoneConfig.save();
        }).bounds(x, y, 34, 15).build());
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g, mx, my, pt);
        GuiRenderHelper.drawPanelBackground(g, cx, cy, W, H, false);
        g.drawString(font, "§l§dBaritone 设置", cx + 10, cy + 8, 0xFFFFFF);

        // 区域标题
        g.drawString(font, "§b≡ 跑酷模式", cx + 12, yParkourLabelY, 0x55FFFF);
        g.drawString(font, "§e≡ 加速", cx + 12, ySpeedLabelY, 0xFFFF55);
        g.drawString(font, "§a≡ 鞘翅任意维度", cx + 12, yElytraLabelY, 0x55FF55);

        // 实时速度倍率
        double spd = BaritoneConfig.getInstance().speedMultiplier;
        g.drawString(font, "§7速度: §b" + String.format("%.1f", spd) + "x", cx + 16, ySpeedLabelY + 32, 0xCCCCCC);

        g.drawString(font, "§7§o需安装 Baritone", cx + 10, cy + H - 14, 0x666666);
        super.render(g, mx, my, pt);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { onClose(); return true; }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override public boolean isPauseScreen() { return false; }
}
