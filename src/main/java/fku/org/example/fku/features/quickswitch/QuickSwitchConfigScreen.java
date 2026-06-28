package fku.org.example.fku.features.quickswitch; /* water */

import fku.org.example.fku.client.gui.ClickGuiScreen;
import fku.org.example.fku.client.gui.GuiRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

/**
 * QuickSwitch（鬼手秒切）配置界面（该方法由赛博教员实现）
 *
 * ★ 职责：
 *   提供完整的秒切配置修改界面，遵循 ClickGUI 风格（带面板边框）。
 *   所有修改即时保存到 QuickSwitchConfig 并持久化到 config/fku/quickswitch.json。
 *
 * ★ 版本变更 v2：
 *   - 新增 SILENT（静默秒切）模式选项
 *   - 自定义物品列表改为按钮→弹出 MultiLineEditBox 大编辑界面
 *     （参考 BedrockBreaker.HelperBlockListScreen）
 *   - 模式循环: OFF → SILENT → NINE_SLOT → CUSTOM → OFF
 *
 * ★ 界面由赛博教员实现
 */
public class QuickSwitchConfigScreen extends Screen {

    private static final int WIDTH = 330;
    private static final int HEIGHT = 250;

    // 行Y偏移（相对于面板顶部）
    private static final int ROW_TITLE = 10;
    private static final int ROW_MODE = 36;
    private static final int ROW_CUSTOM_ITEMS = 66;
    private static final int ROW_RESTORE = 96;
    private static final int ROW_BURST = 126;
    private static final int ROW_VISUAL = 156;
    private static final int ROW_BUTTONS = 196;

    private Button modeButton;
    private Button restoreButton;
    private EditBox burstDelayInput;
    private Button visualButton;

    private final QuickSwitchConfig cfg = QuickSwitchConfig.getInstance();

    public QuickSwitchConfigScreen() {
        super(Component.literal("鬼手秒切配置"));
    }

    @Override
    protected void init() {
        super.init();
        int cx = (width - WIDTH) / 2;

        // ── 行1：秒切模式 ──
        modeButton = Button.builder(
                Component.literal("模式: " + getModeLabel(cfg.mode)),
                btn -> {
                    String next = cycleMode(cfg.mode);
                    cfg.mode = next;
                    cfg.save();
                    btn.setMessage(Component.literal("模式: " + getModeLabel(next)));
                }
        ).bounds(cx + 10, cy(ROW_MODE), 160, 18).build();
        addRenderableWidget(modeButton);

        // ── 行2：自定义物品列表（按钮打开大编辑界面） ──
        addRenderableWidget(Button.builder(
                Component.literal("编辑自定义物品列表..."),
                btn -> Minecraft.getInstance().setScreen(new QuickSwitchCustomItemsScreen(this))
        ).bounds(cx + 10, cy(ROW_CUSTOM_ITEMS), 160, 18).build());

        // ── 行3：恢复原槽位 ──
        restoreButton = Button.builder(
                Component.literal("恢复原槽位: " + (cfg.restoreSlot ? "开" : "关")),
                btn -> {
                    cfg.restoreSlot = !cfg.restoreSlot;
                    cfg.save();
                    btn.setMessage(Component.literal("恢复原槽位: " + (cfg.restoreSlot ? "开" : "关")));
                }
        ).bounds(cx + 10, cy(ROW_RESTORE), 160, 18).build();
        addRenderableWidget(restoreButton);

        // ── 行4：爆裂延迟 ──
        burstDelayInput = new EditBox(font, cx + 120, cy(ROW_BURST + 14), 50, 14, Component.literal(""));
        burstDelayInput.setValue(String.valueOf(cfg.burstDelay));
        burstDelayInput.setMaxLength(2);
        burstDelayInput.setFilter(s -> s.matches("\\d*"));
        addRenderableWidget(burstDelayInput);

        // ── 行5：视觉反馈 ──
        visualButton = Button.builder(
                Component.literal("视觉反馈: " + (cfg.visualFeedback ? "开" : "关")),
                btn -> {
                    cfg.visualFeedback = !cfg.visualFeedback;
                    cfg.save();
                    btn.setMessage(Component.literal("视觉反馈: " + (cfg.visualFeedback ? "开" : "关")));
                }
        ).bounds(cx + 10, cy(ROW_VISUAL), 160, 18).build();
        addRenderableWidget(visualButton);

        // ── 底部按钮 ──
        addRenderableWidget(Button.builder(
                Component.literal("完成"),
                btn -> {
                    saveConfig();
                    Minecraft.getInstance().setScreen(new ClickGuiScreen());
                }
        ).bounds(cx + 120, cy(ROW_BUTTONS), 60, 18).build());
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        int cx = (width - WIDTH) / 2;

        // ── 面板背景 ──
        GuiRenderHelper.drawPanelBackground(g, cx, cy(0), WIDTH, HEIGHT, false);
        g.drawString(font, "QuickSwitch 鬼手秒切配置", cx + 10, cy(ROW_TITLE), 0xFFFFFF);

        // ── 模式说明 ──
        g.drawString(font, "秒切模式:", cx + 10, cy(ROW_MODE + 1), 0xAAAAAA);
        String modeDesc = switch (cfg.mode) {
            case "SILENT" -> "静默秒切: 攻击前切武器, 攻击后立即恢复";
            case "NINE_SLOT" -> "九切: 遍历9个热栏槽位发送攻击";
            case "CUSTOM" -> "自定义: 遍历自定义物品列表中物品发送攻击";
            default -> "关闭: 功能未启用";
        };
        g.drawString(font, "§7" + modeDesc, cx + 175, cy(ROW_MODE + 2), 0x666666);

        // ── 自定义物品 ──
        g.drawString(font, "自定义物品列表:", cx + 10, cy(ROW_CUSTOM_ITEMS + 17), 0xAAAAAA);
        g.drawString(font, "§7(点击按钮打开大编辑界面)", cx + 180, cy(ROW_CUSTOM_ITEMS + 17), 0x666666);

        // ── 恢复原槽位说明 ──
        g.drawString(font, "§7SILENT/九切/自定义模式攻击后是否切回原槽", cx + 175, cy(ROW_RESTORE + 2), 0x666666);

        // ── 爆裂延迟说明 ──
        g.drawString(font, "爆裂延迟:", cx + 10, cy(ROW_BURST + 1), 0xAAAAAA);
        g.drawString(font, "§7每组切换+攻击间的延迟Tick (0~2)", cx + 175, cy(ROW_BURST + 15), 0x666666);

        // ── 使用提示 ──
        g.drawString(font, "§7左键组件开关/右键打开配置  |  自定义物品列表对所有模式生效", cx + 10, cy(ROW_BUTTONS - 12), 0x666666);

        super.render(g, mx, my, pt);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { // Escape
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        // 爆裂延迟输入框会通过其自身的事件处理响应字符输入
        return super.charTyped(codePoint, modifiers);
    }

    // ════════════ 辅助方法 ════════════

    private String getModeLabel(String mode) {
        return switch (mode) {
            case "SILENT" -> "静默秒切";
            case "NINE_SLOT" -> "九切";
            case "CUSTOM" -> "自定义";
            default -> "关闭";
        };
    }

    private String cycleMode(String current) {
        return switch (current) {
            case "OFF" -> "SILENT";
            case "SILENT" -> "NINE_SLOT";
            case "NINE_SLOT" -> "CUSTOM";
            case "CUSTOM" -> "OFF";
            default -> "OFF";
        };
    }

    private void saveConfig() {
        // 保存爆裂延迟
        try {
            int d = Integer.parseInt(burstDelayInput.getValue());
            cfg.burstDelay = Math.max(0, Math.min(2, d));
        } catch (NumberFormatException ignored) {}

        cfg.save();
    }

    /** 计算相对于面板顶部的 Y 坐标 */
    private int cy(int rowOffset) {
        return (height - HEIGHT) / 2 + rowOffset;
    }

    @Override
    public void onClose() {
        saveConfig();
        Minecraft.getInstance().setScreen(new ClickGuiScreen());
    }
}