package fku.org.example.fku.features.knockback; /* water */

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
 * 自由击退配置界面
 *
 * ★ 职责：
 *   提供完整的自由击退配置修改界面，遵循 ClickGUI 风格。
 *   所有修改即时保存到 KnockbackConfig 并持久化到 config/fku/knockback.json。
 *
 * ★ 参考：
 *   BedrockBreakerScreen 的布局模式（标签+输入框+按钮分层）
 *
 * 配置项：
 *   - 模式选择（推离/拉回/悬崖/自定义）
 *   - 自定义角度（CUSTOM 模式）
 *   - 悬崖搜索半径（CLIFF 模式）
 *   - 平滑旋转开关
 *   - 平滑步数
 *   - 旋转延迟（0~5 Tick）
 *   - 激进模式开关
 */
public class KnockbackConfigScreen extends Screen {

    private static final int WIDTH = 290;
    private static final int HEIGHT = 300;

    // 行Y偏移基准
    private static final int ROW_MODE_LABEL = 30;
    private static final int ROW_MODE_BTN = 44;
    private static final int ROW_CUSTOM = 72;       // CUSTOM 模式：标签72, 输入框86
    private static final int ROW_CLIFF = 106;       // CLIFF 模式：标签106, 输入框120
    private static final int ROW_SMOOTH_TOGGLE = 140;
    private static final int ROW_SMOOTH_STEPS = 168; // 标签168, 输入框182
    private static final int ROW_DELAY = 202;        // 标签202, 输入框216
    private static final int ROW_AGGRESSIVE = 236;
    private static final int ROW_BUTTON = 270;

    // 引用配置（直接读写，即时保存）
    private final KnockbackConfig cfg = KnockbackConfig.getInstance();

    // 模式按钮
    private Button modeButton;
    private final String[] modes = {"PUSHBACK", "PULLBACK", "CLIFF", "CUSTOM"};
    private final String[] modeLabels = {"推离", "拉回", "悬崖", "自定义"};

    // 输入框（条件显示）
    private EditBox customYawInput;
    private EditBox cliffRadiusInput;
    private EditBox smoothStepsInput;
    private EditBox delayInput;

    // 平滑开关按钮
    private Button smoothToggleButton;
    // 激进模式按钮
    private Button aggressiveButton;

    public KnockbackConfigScreen() {
        super(Component.literal("自由击退配置"));
    }

    @Override
    protected void init() {
        super.init();
        int cx = (width - WIDTH) / 2;

        // ── 行1：模式选择 ──
        String currentModeLabel = getModeLabel(cfg.mode);
        modeButton = Button.builder(
                Component.literal("模式: " + currentModeLabel),
                btn -> {
                    // 循环切换模式
                    int idx = 0;
                    for (int i = 0; i < modes.length; i++) {
                        if (modes[i].equals(cfg.mode)) { idx = (i + 1) % modes.length; break; }
                    }
                    cfg.setMode(modes[idx]);
                    btn.setMessage(Component.literal("模式: " + getModeLabel(modes[idx])));
                    // 刷新输入框可见性
                    rebuildWidgets();
                }
        ).bounds(cx + 10, cy(ROW_MODE_BTN), 120, 18).build();
        addRenderableWidget(modeButton);

        // ── 行2：自定义角度（CUSTOM 模式可见）──
        customYawInput = new EditBox(font, cx + 80, cy(ROW_CUSTOM + 14), 60, 14, Component.literal(""));
        customYawInput.setValue(String.format("%.0f", cfg.customYaw));
        customYawInput.setMaxLength(6);
        customYawInput.setFilter(s -> s.matches("-?\\d*\\.?\\d*"));
        customYawInput.setVisible("CUSTOM".equals(cfg.mode));
        addRenderableWidget(customYawInput);

        // ── 行3：悬崖搜索半径（CLIFF 模式可见）──
        cliffRadiusInput = new EditBox(font, cx + 80, cy(ROW_CLIFF + 14), 40, 14, Component.literal(""));
        cliffRadiusInput.setValue(String.valueOf(cfg.cliffSearchRadius));
        cliffRadiusInput.setMaxLength(2);
        cliffRadiusInput.setFilter(s -> s.matches("\\d*"));
        cliffRadiusInput.setVisible("CLIFF".equals(cfg.mode));
        addRenderableWidget(cliffRadiusInput);

        // ── 行4：平滑旋转开关 ──
        smoothToggleButton = Button.builder(
                Component.literal("平滑旋转: " + (cfg.smoothRotation ? "开" : "关")),
                btn -> {
                    cfg.setSmoothRotation(!cfg.smoothRotation);
                    btn.setMessage(Component.literal("平滑旋转: " + (cfg.smoothRotation ? "开" : "关")));
                    rebuildWidgets();
                }
        ).bounds(cx + 10, cy(ROW_SMOOTH_TOGGLE), 110, 18).build();
        addRenderableWidget(smoothToggleButton);

        // ── 行5：平滑步数（平滑旋转开启时可见）──
        smoothStepsInput = new EditBox(font, cx + 80, cy(ROW_SMOOTH_STEPS + 14), 40, 14, Component.literal(""));
        smoothStepsInput.setValue(String.valueOf(cfg.smoothSteps));
        smoothStepsInput.setMaxLength(2);
        smoothStepsInput.setFilter(s -> s.matches("\\d*"));
        smoothStepsInput.setVisible(cfg.smoothRotation);
        addRenderableWidget(smoothStepsInput);

        // ── 行6：旋转延迟 ──
        delayInput = new EditBox(font, cx + 80, cy(ROW_DELAY + 14), 40, 14, Component.literal(""));
        delayInput.setValue(String.valueOf(cfg.rotationDelay));
        delayInput.setMaxLength(1);
        delayInput.setFilter(s -> s.isEmpty() || s.matches("[0-5]"));
        addRenderableWidget(delayInput);

        // ── 行7：激进模式 ──
        aggressiveButton = Button.builder(
                Component.literal("激进模式: " + (cfg.aggressiveMode ? "开" : "关")),
                btn -> {
                    cfg.setAggressiveMode(!cfg.aggressiveMode);
                    btn.setMessage(Component.literal("激进模式: " + (cfg.aggressiveMode ? "开" : "关")));
                }
        ).bounds(cx + 10, cy(ROW_AGGRESSIVE), 110, 18).build();
        addRenderableWidget(aggressiveButton);

        // ── 行8：底部按钮 ──
        addRenderableWidget(Button.builder(
                Component.literal("保存"),
                btn -> saveConfig()
        ).bounds(cx + 70, cy(ROW_BUTTON), 60, 18).build());

        addRenderableWidget(Button.builder(
                Component.literal("完成"),
                btn -> {
                    saveConfig();
                    Minecraft.getInstance().setScreen(new ClickGuiScreen());
                }
        ).bounds(cx + 150, cy(ROW_BUTTON), 60, 18).build());
    }

    /** 获取模式的中文标签 */
    private String getModeLabel(String mode) {
        for (int i = 0; i < modes.length; i++) {
            if (modes[i].equals(mode)) return modeLabels[i];
        }
        return mode;
    }

    /** 从输入框读取值并保存到配置 */
    private void saveConfig() {
        // 自定义角度
        try {
            float yaw = Float.parseFloat(customYawInput.getValue());
            yaw = Math.max(-180, Math.min(180, yaw));
            cfg.setCustomYaw(yaw);
        } catch (Exception ignored) {}

        // 悬崖搜索半径
        try {
            int radius = Integer.parseInt(cliffRadiusInput.getValue());
            radius = Math.max(1, Math.min(20, radius));
            cfg.setCliffSearchRadius(radius);
        } catch (Exception ignored) {}

        // 平滑步数
        try {
            int steps = Integer.parseInt(smoothStepsInput.getValue());
            steps = Math.max(2, Math.min(10, steps));
            cfg.setSmoothSteps(steps);
        } catch (Exception ignored) {}

        // 旋转延迟
        try {
            int delay = Integer.parseInt(delayInput.getValue());
            delay = Math.max(0, Math.min(5, delay));
            cfg.setRotationDelay(delay);
        } catch (Exception ignored) {}

        // 强制保存
        KnockbackConfig.save();
    }

    /** 计算相对于面板顶部的 Y 坐标 */
    private int cy(int rowOffset) {
        return (height - HEIGHT) / 2 + rowOffset;
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        int cx = (width - WIDTH) / 2;

        // ── 面板背景 ──
        GuiRenderHelper.drawPanelBackground(g, cx, cy(0), WIDTH, HEIGHT, false);
        g.drawString(font, "自由击退配置", cx + 10, cy(8), 0xFFFFFF);

        boolean isCustom = "CUSTOM".equals(cfg.mode);
        boolean isCliff = "CLIFF".equals(cfg.mode);

        // ── 模式 ──
        g.drawString(font, "§7| 击退方向模式", cx + 135, cy(ROW_MODE_BTN + 2), 0x666666);

        // ── 自定义角度 ──
        if (isCustom) {
            g.drawString(font, "自定义角度:", cx + 10, cy(ROW_CUSTOM), 0xAAAAAA);
            g.drawString(font, "§7(-180~180°)", cx + 142, cy(ROW_CUSTOM + 14), 0x666666);
        }

        // ── 悬崖半径 ──
        if (isCliff) {
            g.drawString(font, "搜索半径:", cx + 10, cy(ROW_CLIFF), 0xAAAAAA);
            g.drawString(font, "§7(1~20 方块)", cx + 122, cy(ROW_CLIFF + 14), 0x666666);
        }

        // ── 平滑步数 ──
        if (cfg.smoothRotation) {
            g.drawString(font, "平滑步数:", cx + 10, cy(ROW_SMOOTH_STEPS), 0xAAAAAA);
            g.drawString(font, "§7(2~10)", cx + 122, cy(ROW_SMOOTH_STEPS + 14), 0x666666);
        }

        // ── 旋转延迟 ──
        g.drawString(font, "旋转延迟:", cx + 10, cy(ROW_DELAY), 0xAAAAAA);
        g.drawString(font, "§7(Tick, 0~5)", cx + 122, cy(ROW_DELAY + 14), 0x666666);
        g.drawString(font, "§7攻击后延迟多少 Tick 恢复原始旋转", cx + 10, cy(ROW_DELAY + 28), 0x666666);

        // ── 激进模式 ──
        g.drawString(font, "§7| 强制发送旋转包（无视旋转检测）", cx + 125, cy(ROW_AGGRESSIVE + 2), 0x666666);

        // ── 模式说明 ──
        String modeHint = switch (cfg.mode) {
            case "PUSHBACK" -> "§7将目标推离玩家";
            case "PULLBACK" -> "§7将目标拉向玩家";
            case "CLIFF" -> "§7将目标击退向最近的悬崖方向";
            case "CUSTOM" -> "§7基于玩家视角偏移指定角度击退（如90°=右侧击退）";
            default -> "";
        };
        g.drawString(font, modeHint, cx + 10, cy(ROW_MODE_LABEL), 0x888888);

        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // ★ 先让 EditBox 尝试处理点击（获得焦点）
        if (customYawInput.mouseClicked(mouseX, mouseY, button)) {
            setEditBoxFocus(customYawInput);
            return true;
        }
        if (cliffRadiusInput.mouseClicked(mouseX, mouseY, button)) {
            setEditBoxFocus(cliffRadiusInput);
            return true;
        }
        if (smoothStepsInput.mouseClicked(mouseX, mouseY, button)) {
            setEditBoxFocus(smoothStepsInput);
            return true;
        }
        if (delayInput.mouseClicked(mouseX, mouseY, button)) {
            setEditBoxFocus(delayInput);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    /** 设置单个输入框聚焦，其余失焦 */
    private void setEditBoxFocus(EditBox focused) {
        customYawInput.setFocused(false);
        cliffRadiusInput.setFocused(false);
        smoothStepsInput.setFocused(false);
        delayInput.setFocused(false);
        focused.setFocused(true);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // ★ 有焦点的输入框优先处理按键
        if (customYawInput.isFocused() && customYawInput.keyPressed(keyCode, scanCode, modifiers))
            return true;
        if (cliffRadiusInput.isFocused() && cliffRadiusInput.keyPressed(keyCode, scanCode, modifiers))
            return true;
        if (smoothStepsInput.isFocused() && smoothStepsInput.keyPressed(keyCode, scanCode, modifiers))
            return true;
        if (delayInput.isFocused() && delayInput.keyPressed(keyCode, scanCode, modifiers))
            return true;
        // ESC 关闭
        if (keyCode == 256) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        // ★ 字符输入委托给有焦点的输入框
        if (customYawInput.isFocused() && customYawInput.charTyped(codePoint, modifiers))
            return true;
        if (cliffRadiusInput.isFocused() && cliffRadiusInput.charTyped(codePoint, modifiers))
            return true;
        if (smoothStepsInput.isFocused() && smoothStepsInput.charTyped(codePoint, modifiers))
            return true;
        if (delayInput.isFocused() && delayInput.charTyped(codePoint, modifiers))
            return true;
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        saveConfig();
        this.minecraft.setScreen(new ClickGuiScreen());
    }
}