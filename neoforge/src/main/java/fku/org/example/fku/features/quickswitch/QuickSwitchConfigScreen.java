package fku.org.example.fku.features.quickswitch;

import fku.org.example.fku.client.gui.GuiRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

/**
 * QuickSwitch 配置界面 — 退出时统一保存，避免每字符读写文件
 */
public class QuickSwitchConfigScreen extends Screen {

    private static final int W = 300, H = 220;
    private int cx, cy;

    private EditBox customItemsInput;
    private Button modeBtn, visualBtn, saveBtn;

    public QuickSwitchConfigScreen() {
        super(Component.literal("鬼手秒切配置"));
    }

    @Override
    protected void init() {
        super.init();
        cx = (width - W) / 2;
        cy = (height - H) / 2;
        var cfg = QuickSwitchConfig.getInstance();

        // ── 模式选择 ──
        modeBtn = Button.builder(Component.literal("模式: " + modeLabel(cfg.mode)), b -> {
            cfg.mode = cycleMode(cfg.mode);
            cfg.save();
            b.setMessage(Component.literal("模式: " + modeLabel(cfg.mode)));
        }).bounds(cx + 10, cy + 30, 130, 18).build();
        addRenderableWidget(modeBtn);

        // ── 视觉反馈 ──
        visualBtn = Button.builder(Component.literal("视觉反馈: " + (cfg.visualFeedback ? "开" : "关")), b -> {
            cfg.visualFeedback = !cfg.visualFeedback;
            cfg.save();
            b.setMessage(Component.literal("视觉反馈: " + (cfg.visualFeedback ? "开" : "关")));
        }).bounds(cx + 150, cy + 30, 130, 18).build();
        addRenderableWidget(visualBtn);

        // ── 自定义物品列表（保存只在退出时触发，避免连续 IO） ──
        customItemsInput = new EditBox(font, cx + 10, cy + 75, W - 20, 16, Component.literal("物品列表"));
        customItemsInput.setValue(cfg.customItems);
        customItemsInput.setMaxLength(100000);
        addRenderableWidget(customItemsInput);

        // ── RTT延迟 ──
        addRenderableWidget(Button.builder(Component.literal("延迟: " + cfg.rttDelay + "ms"), b -> {
            int[] opts = {40, 60, 80, 100, 120, 150, 200};
            int next = 0;
            for (int i = 0; i < opts.length; i++) {
                if (opts[i] == cfg.rttDelay) { next = (i + 1) % opts.length; break; }
            }
            cfg.rttDelay = opts[next];
            cfg.save();
            b.setMessage(Component.literal("延迟: " + cfg.rttDelay + "ms"));
        }).bounds(cx + 10, cy + 110, 100, 16).build());

        // ── 优先级槽位（文字提示，不提供编辑） ──
        addRenderableWidget(Button.builder(Component.literal("优先级槽位: " + intArrStr(cfg.prioritySlots)), b -> {}).bounds(cx + 120, cy + 110, 160, 16).build());

        // ── 保存并返回 ──
        saveBtn = Button.builder(Component.literal("§a保存并返回"), b -> {
            saveAndClose();
        }).bounds(cx + W / 2 - 40, cy + H - 24, 80, 16).build();
        addRenderableWidget(saveBtn);
    }

    private void saveAndClose() {
        var cfg = QuickSwitchConfig.getInstance();
        cfg.customItems = customItemsInput.getValue();
        cfg.save();
        this.minecraft.setScreen(null);
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mx, int my, float pt) {
        var cfg = QuickSwitchConfig.getInstance();

        GuiRenderHelper.drawPanelBackground(g, cx, cy, W, H, false);
        g.drawString(font, "§l§bQuickSwitch 鬼手秒切", cx + 10, cy + 10, 0xFFFFFFFF);

        String modeDesc = switch (cfg.mode) {
            case "SMART" -> "智能: 附魔评分最高武器";
            case "CUSTOM" -> "自定义: 按列表顺序切换";
            default -> "关闭: 功能未启用";
        };
        g.drawString(font, "§7" + modeDesc, cx + 10, cy + 54, 0xFF888888);
        g.drawString(font, "§7物品列表(逗号分隔):", cx + 10, cy + 98, 0xFFAAAAAA);
        g.drawString(font, "§7§o点击「保存并返回」或按 ESC 退出并保存", cx + 10, cy + H - 14, 0xFF666666);

        super.render(g, mx, my, pt);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { saveAndClose(); return true; }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() { saveAndClose(); super.onClose(); }

    @Override
    public boolean isPauseScreen() { return false; }

    private static String modeLabel(String m) {
        return switch (m) { case "SMART" -> "智能"; case "CUSTOM" -> "自定义"; default -> "关闭"; };
    }
    private static String cycleMode(String m) {
        return switch (m) { case "OFF" -> "SMART"; case "SMART" -> "CUSTOM"; default -> "OFF"; };
    }
    private static String intArrStr(int[] arr) {
        if (arr == null || arr.length == 0) return "无";
        var sb = new StringBuilder();
        for (int v : arr) sb.append(v).append(",");
        return sb.substring(0, sb.length() - 1);
    }
}
