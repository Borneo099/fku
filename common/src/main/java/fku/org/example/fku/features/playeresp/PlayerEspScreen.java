package fku.org.example.fku.features.playeresp; /* water */

import fku.org.example.fku.client.gui.ClickGuiScreen;
import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.client.gui.components.ColorWheelPicker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * 玩家ESP配置界面 — 支持颜色轮盘自定义方框/连线/六面颜色
 * 参考 AttackIndicatorConfigScreen 的颜色选择器实现
 * 该配置界面由赛博教员实现
 */
public class PlayerEspScreen extends Screen {

    private static final int W = 280, H = 240;
    private int bx, by;

    // 颜色轮盘
    private ColorWheelPicker colorWheelPicker;
    private boolean colorPickerOpen = false;
    private String pendingColorField = null;

    // 颜色按钮引用和色块绘制坐标
    private final Map<String, Button[]> colorButtons = new HashMap<>();
    private final Map<String, int[]> colorButtonPositions = new HashMap<>();

    // 颜色字段对应的中文标签名
    private static final Map<String, String> COLOR_LABELS = new HashMap<>();
    static {
        COLOR_LABELS.put("boxColor", "方框颜色");
        COLOR_LABELS.put("linesColor", "连线颜色");
        COLOR_LABELS.put("sidesColor", "六面颜色");
    }

    public PlayerEspScreen() {
        super(Component.literal("玩家ESP配置"));
        // 初始化颜色轮盘，回调中更新配置 + 实时刷新按钮文本
        this.colorWheelPicker = new ColorWheelPicker("FF0000", hex -> {
            if (pendingColorField == null) return;
            PlayerEspConfig cfg = PlayerEspConfig.getInstance();
            // 将6位hex与原始alpha合并为ARGB int
            int rgb = Integer.parseInt(hex, 16);
            switch (pendingColorField) {
                case "boxColor" -> cfg.setBoxColor((cfg.boxColor & 0xFF000000) | rgb);
                case "linesColor" -> cfg.setLinesColor((cfg.linesColor & 0xFF000000) | rgb);
                case "sidesColor" -> cfg.setSidesColor((cfg.sidesColor & 0xFF000000) | rgb);
            }
            // 实时更新按钮文本
            updateColorButtonText(pendingColorField);
        });
    }

    @Override
    protected void init() {
        super.init();
        bx = (width - W) / 2;
        by = (height - H) / 2;
        rebuildWidgets();
    }

    protected void rebuildWidgets() {
        clearWidgets();
        colorButtons.clear();
        colorButtonPositions.clear();
        PlayerEspConfig cfg = PlayerEspConfig.getInstance();

        int cx = bx + 10, cy = by + 30, sp = 22;

        // 显示模式
        addRenderableWidget(Button.builder(Component.literal("显示模式: §b" + modeName(cfg.mode)),
            btn -> {
                cfg.setMode(nextMode(cfg.mode));
                btn.setMessage(Component.literal("显示模式: §b" + modeName(cfg.mode)));
            }).bounds(cx, cy, 260, 18).build());
        cy += sp;

        // 颜色选择按钮（3个：方框/连线/六面）
        addColorInput(cx, cy, "boxColor");
        addColorInput(cx + 134, cy, "linesColor");
        cy += sp;

        addColorInput(cx, cy, "sidesColor");
        // 队伍颜色开关
        addRenderableWidget(Button.builder(Component.literal(cfg.forceTeamColor ? "§a队伍颜色: 开" : "§7队伍颜色: 关"),
            btn -> {
                cfg.setForceTeamColor(!cfg.forceTeamColor);
                btn.setMessage(Component.literal(cfg.forceTeamColor ? "§a队伍颜色: 开" : "§7队伍颜色: 关"));
            }).bounds(cx + 134, cy, 126, 18).build());
        cy += sp;

        // 最大距离
        addRenderableWidget(Button.builder(Component.literal("最大距离: §b" + cfg.maxDistance),
            btn -> {
                int[] levels = {16, 32, 64, 128, 256, 512, 1024};
                int idx = 0;
                for (int i = 0; i < levels.length; i++) {
                    if (cfg.maxDistance == levels[i]) { idx = i; break; }
                }
                idx = (idx + 1) % levels.length;
                cfg.setMaxDistance(levels[idx]);
                btn.setMessage(Component.literal("最大距离: §b" + cfg.maxDistance));
            }).bounds(cx, cy, 260, 18).build());
        cy += sp;

        // 模式说明
        addRenderableWidget(Button.builder(Component.literal("§7模式说明: 方框+连线+六面 | 可单独组合"), b -> {}).bounds(cx, cy, 260, 14).build());

        // 返回按钮
        addRenderableWidget(Button.builder(Component.literal("§a← 返回"),
            btn -> minecraft.setScreen(new ClickGuiScreen()))
            .bounds(bx + W / 2 - 30, by + H - 28, 60, 18).build());
    }

    /**
     * 创建颜色选择按钮（含色块 + 标签）
     */
    private void addColorInput(int x, int y, String fieldName) {
        PlayerEspConfig cfg = PlayerEspConfig.getInstance();
        String hex = colorHex(getFieldValue(fieldName));
        String label = COLOR_LABELS.getOrDefault(fieldName, "颜色");

        // 色块按钮：显示十六进制值
        Button colorBtn = Button.builder(Component.literal("#" + hex),
            btn -> openColorPicker(fieldName)).bounds(x, y, 54, 16).build();
        Button labelBtn = Button.builder(Component.literal("§7" + label),
            btn -> openColorPicker(fieldName)).bounds(x + 56, y, 70, 16).build();

        // 存储色块绘制坐标
        colorButtonPositions.put(fieldName, new int[]{x + 2, y + 2, 14, 12});

        addRenderableWidget(colorBtn);
        addRenderableWidget(labelBtn);
        colorButtons.put(fieldName, new Button[]{colorBtn, labelBtn});
    }

    /** 实时更新颜色按钮文本 */
    private void updateColorButtonText(String fieldName) {
        Button[] btns = colorButtons.get(fieldName);
        if (btns == null) return;
        PlayerEspConfig cfg = PlayerEspConfig.getInstance();
        String hex = colorHex(getFieldValue(fieldName));
        btns[0].setMessage(Component.literal("#" + hex));
    }

    /** 从配置对象获取当前字段的颜色值（ARGB int） */
    private int getFieldValue(String fieldName) {
        PlayerEspConfig cfg = PlayerEspConfig.getInstance();
        return switch (fieldName) {
            case "boxColor" -> cfg.boxColor;
            case "linesColor" -> cfg.linesColor;
            case "sidesColor" -> cfg.sidesColor;
            default -> 0xB4FF0000;
        };
    }

    /** 打开颜色轮盘 */
    private void openColorPicker(String fieldName) {
        this.pendingColorField = fieldName;
        // 转换为6位hex（去掉alpha）
        int argb = getFieldValue(fieldName);
        this.colorWheelPicker.setColor(colorHex(argb));
        this.colorWheelPicker.open(width / 2, height / 2);
        this.colorPickerOpen = true;
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        GuiRenderHelper.drawRoundedRect(g, bx - 10, by - 8, W + 20, H + 16, 0xAA2D2D2D, 8);
        g.drawString(font, "§l玩家ESP 配置", bx + 10, by + 10, 0xFFFFFF);
        super.render(g, mx, my, pt);

        // 绘制所有颜色字段的色块
        for (Map.Entry<String, int[]> entry : colorButtonPositions.entrySet()) {
            String field = entry.getKey();
            int[] pos = entry.getValue();
            int argb = getFieldValue(field);
            // 使用完整ARGB颜色绘制色块（保留alpha透明度）
            g.fill(pos[0], pos[1], pos[0] + pos[2], pos[1] + pos[3], argb);
            // 色块边框
            g.fill(pos[0], pos[1], pos[0] + pos[2], pos[1] + 1, 0xFF888888);
            g.fill(pos[0], pos[1] + pos[3] - 1, pos[0] + pos[2], pos[1] + pos[3], 0xFF888888);
            g.fill(pos[0], pos[1], pos[0] + 1, pos[1] + pos[3], 0xFF888888);
            g.fill(pos[0] + pos[2] - 1, pos[1], pos[0] + pos[2], pos[1] + pos[3], 0xFF888888);
        }

        // 颜色轮盘在最后绘制（覆盖在最上层）
        if (colorPickerOpen && colorWheelPicker.isOpen()) {
            colorWheelPicker.render(g, mx, my);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (colorPickerOpen && colorWheelPicker.isOpen()) {
            if (colorWheelPicker.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
            colorWheelPicker.close();
            colorPickerOpen = false;
            pendingColorField = null;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (colorPickerOpen && colorWheelPicker.isOpen()) {
            colorWheelPicker.mouseClicked(mouseX, mouseY, button);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (colorPickerOpen && colorWheelPicker.isOpen()) {
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void onClose() {
        PlayerEspConfig.save();
        minecraft.setScreen(new ClickGuiScreen());
    }

    // ═══════ 工具方法 ═══════

    private static String modeName(String mode) {
        return switch (mode) {
            case "BOX_ONLY" -> "仅方框";
            case "LINES_ONLY" -> "仅连线";
            case "SIDES_ONLY" -> "仅六面";
            case "BOX_LINES" -> "方框+连线";
            case "BOX_SIDES" -> "方框+六面";
            case "LINES_SIDES" -> "连线+六面";
            default -> "全部";
        };
    }

    private static String nextMode(String current) {
        return switch (current) {
            case "BOX_ONLY" -> "LINES_ONLY";
            case "LINES_ONLY" -> "SIDES_ONLY";
            case "SIDES_ONLY" -> "BOX_LINES";
            case "BOX_LINES" -> "BOX_SIDES";
            case "BOX_SIDES" -> "LINES_SIDES";
            case "LINES_SIDES" -> "ALL";
            default -> "BOX_ONLY";
        };
    }

    /** 将ARGB int转换为6位十六进制颜色字符串（不含alpha） */
    private static String colorHex(int argb) {
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
        return String.format("%02X%02X%02X", r, g, b);
    }
}