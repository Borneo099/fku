package fku.org.example.fku.features.fastjoin; /* water */

import fku.org.example.fku.client.gui.GuiRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

/**
 * 快速加载配置界面
 */
public class FastJoinConfigScreen extends Screen {

    private static final int W = 280, H = 240;
    private static final String[] MODES = {"EXTREME", "SMOOTH", "COMPAT"};
    private static final String[] MODE_LABELS = {"极速模式", "平滑模式", "兼容模式"};

    private int modeIndex = 1;
    private EditBox targetInput, speedInput;
    private Button modeBtn, progressBtn, timeoutBtn;

    public FastJoinConfigScreen() {
        super(Component.literal("快速加载配置"));
    }

    @Override
    protected void init() {
        FastJoinConfig cfg = FastJoinConfig.getInstance();
        int cx = (width - W) / 2, cy = (height - H) / 2;

        // 同步模式索引
        for (int i = 0; i < MODES.length; i++) {
            if (MODES[i].equals(cfg.mode)) { modeIndex = i; break; }
        }

        // ── 分组1：模式选择 ──

        modeBtn = addRenderableWidget(Button.builder(
            Component.literal(MODE_LABELS[modeIndex]), b -> {
                modeIndex = (modeIndex + 1) % MODES.length;
                cfg.setMode(MODES[modeIndex]);
                b.setMessage(Component.literal(MODE_LABELS[modeIndex]));
            }
        ).bounds(cx + 12, cy + 20, 90, 18).build());

        // 小问号按钮（悬浮显示提示）
        addRenderableWidget(Button.builder(
            Component.literal("§e?"), b -> {}
        ).bounds(cx + 106, cy + 20, 16, 18).build());

        // ── 分组2：参数 ──

        addRenderableWidget(Button.builder(Component.literal("目标视距:"), b -> {}).bounds(cx + 12, cy + 65, 70, 18).build());
        targetInput = new EditBox(font, cx + 86, cy + 65, 40, 16, Component.literal(""));
        targetInput.setValue(String.valueOf(cfg.targetRenderDistance));
        targetInput.setFilter(s -> s.matches("\\d*"));
        targetInput.setMaxLength(2);
        addWidget(targetInput);

        addRenderableWidget(Button.builder(Component.literal("区块 (2~32)"), b -> {}).bounds(cx + 130, cy + 65, 80, 18).build());

        addRenderableWidget(Button.builder(Component.literal("恢复速度:"), b -> {}).bounds(cx + 12, cy + 88, 70, 18).build());
        speedInput = new EditBox(font, cx + 86, cy + 88, 40, 16, Component.literal(""));
        speedInput.setValue(String.valueOf(cfg.recoverSpeed));
        speedInput.setFilter(s -> s.matches("[1-4]"));
        speedInput.setMaxLength(1);
        addWidget(speedInput);
        addRenderableWidget(Button.builder(Component.literal("(1~4)"), b -> {}).bounds(cx + 130, cy + 88, 50, 18).build());

        // ── 分组3：选项 ──

        progressBtn = addToggle(cx + 12, cy + 130, "显示进度", cfg.showLoadingProgress, v -> cfg.setShowLoadingProgress(v));
        timeoutBtn = addToggle(cx + 12, cy + 153, "超时回退", cfg.onTimeoutFallback, v -> cfg.setOnTimeoutFallback(v));

        // ── 返回 ──
        addRenderableWidget(Button.builder(Component.literal("§a返回"), b -> saveAndClose())
            .bounds(cx + W / 2 - 30, cy + H - 28, 60, 18).build());
    }

    private Button addToggle(int x, int y, String label, boolean cur, java.util.function.Consumer<Boolean> setter) {
        return addRenderableWidget(Button.builder(
            Component.literal(label + ": " + (cur ? "§a开" : "§c关")),
            b -> { setter.accept(!cur); b.setMessage(Component.literal(label + ": " + (!cur ? "§a开" : "§c关"))); }
        ).bounds(x, y, 130, 16).build());
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        int cx = (width - W) / 2, cy = (height - H) / 2;
        GuiRenderHelper.drawPanelBackground(g, cx, cy, W, H, false);
        super.render(g, mx, my, pt);

        if (targetInput != null) targetInput.render(g, mx, my, pt);
        if (speedInput != null) speedInput.render(g, mx, my, pt);

        // 分组标签
        g.drawString(font, "§7- - - 模式 - - -", cx + 10, cy + 5, 0x666666);
        g.drawString(font, "§7- - - 参数 - - -", cx + 10, cy + 50, 0x666666);
        g.drawString(font, "§7- - - 选项 - - -", cx + 10, cy + 115, 0x666666);
        g.drawString(font, "§7连接时自动联动禁连超时", cx + 10, cy + 180, 0x666666);

        // ★ 悬浮提示：当鼠标在小问号上时显示
        if (mx >= cx + 106 && mx <= cx + 122 && my >= cy + 20 && my <= cy + 38) {
            String tip = FastJoinConfig.getModeTooltip(MODES[modeIndex]);
            if (!tip.isEmpty()) {
                int tw = Math.max(180, font.width(tip));
                g.fill(mx + 10, my + 10, mx + 10 + tw, my + 30, 0xE0333333);
                g.drawString(font, tip, mx + 12, my + 14, 0xFFFFFF);
            }
        }
    }

    @Override public boolean mouseClicked(double mx, double my, int btn) {
        if (targetInput != null) targetInput.mouseClicked(mx, my, btn);
        if (speedInput != null) speedInput.mouseClicked(mx, my, btn);
        return super.mouseClicked(mx, my, btn);
    }
    @Override public boolean keyPressed(int k, int s, int m) {
        if (targetInput != null && targetInput.isFocused()) return targetInput.keyPressed(k, s, m);
        if (speedInput != null && speedInput.isFocused()) return speedInput.keyPressed(k, s, m);
        if (k == 256) { saveAndClose(); return true; }
        return super.keyPressed(k, s, m);
    }
    @Override public boolean charTyped(char c, int m) {
        if (targetInput != null && targetInput.isFocused()) return targetInput.charTyped(c, m);
        if (speedInput != null && speedInput.isFocused()) return speedInput.charTyped(c, m);
        return super.charTyped(c, m);
    }
    @Override public void onClose() { saveAndClose(); }
    @Override public boolean isPauseScreen() { return false; }

    private void saveAndClose() {
        FastJoinConfig cfg = FastJoinConfig.getInstance();
        try { cfg.setTargetRenderDistance(Integer.parseInt(targetInput.getValue().trim())); } catch (Exception e) {}
        try { cfg.setRecoverSpeed(Integer.parseInt(speedInput.getValue().trim())); } catch (Exception e) {}
        this.minecraft.setScreen(null);
    }
}
