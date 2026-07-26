package fku.org.example.fku.features.attackindicator; /* water */

import fku.org.example.fku.client.gui.ClickGuiScreen;
import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.client.gui.components.ColorWheelPicker;
import fku.org.example.fku.features.attackindicator.AttackIndicatorConfig;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

/**
 * 攻击指示器详细配置界面 — 4页标签页布局（已移除屏幕覆盖页）
 * 包含通用、连接特效、目标标记、性能设置
 * 颜色选择：使用fill绘制的色块代替§x颜色代码，更可靠
 * 实时更新：点击调节按钮后立即刷新按钮文本
 * 该配置界面由赛博教员实现
 */
public class AttackIndicatorConfigScreen extends Screen {
    private static final int W = 280;
    private static final int H = 340;
    private int bx, by;
    private int currentPage = 0;
    private static final String[] PAGE_TITLES = {"§l通用设置", "§l连接特效", "§l目标标记", "§l性能设置"};

    private final AttackIndicatorConfig cfg = AttackIndicatorConfig.getInstance();

    // 颜色轮盘（参考 GuiStyleScreen 实现）
    private ColorWheelPicker colorWheelPicker;
    private boolean colorPickerOpen = false;
    private String pendingColorField = null;

    // ★ 存储颜色按钮引用，用于实时更新按钮文本（不重建界面）
    private final Map<String, Button[]> colorButtons = new HashMap<>();
    // ★ 存储颜色按钮的绘图坐标，用于render中绘制色块
    private final Map<String, int[]> colorButtonPositions = new HashMap<>();

    // 颜色字段对应的中文标签名
    private static final Map<String, String> COLOR_LABELS = new HashMap<>();
    static {
        COLOR_LABELS.put("beamColor", "颜色");
        COLOR_LABELS.put("waveColor", "颜色");
        COLOR_LABELS.put("swordWaveColor", "颜色");
        COLOR_LABELS.put("beamMarkerColor", "颜色");
        COLOR_LABELS.put("haloColor", "颜色");
    }

    public AttackIndicatorConfigScreen() {
        super(Component.literal("攻击指示器配置"));
        // 初始化颜色轮盘，回调中更新配置 + 实时刷新按钮文本
        this.colorWheelPicker = new ColorWheelPicker("FF4444", hex -> {
            if (pendingColorField == null) return;
            // 更新配置字段
            switch (pendingColorField) {
                case "beamColor" -> cfg.beamColor = hex;
                case "waveColor" -> cfg.waveColor = hex;
                case "beamMarkerColor" -> cfg.beamMarkerColor = hex;
                case "haloColor" -> cfg.haloColor = hex;
                case "swordWaveColor" -> cfg.swordWaveColor = hex;
            }
            AttackIndicatorConfig.save();
            // ★ 实时更新按钮文本（用户点击圆盘后立即看到颜色变化）
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
        int x = bx, y = by + 5;
        int btnW = 70;

        // 标签页按钮（4页）
        for (int i = 0; i < 4; i++) {
            int page = i;
            String label = (currentPage == i ? "§a" : "§7") + PAGE_TITLES[i].replace("§l", "");
            addRenderableWidget(Button.builder(Component.literal(label),
                btn -> { currentPage = page; rebuildWidgets(); })
                .bounds(x + i * (btnW + 4), y, btnW, 16).build());
        }

        y += 22;

        // 根据当前页面渲染内容
        switch (currentPage) {
            case 0 -> renderGeneralPage(x, y);
            case 1 -> renderConnectionPage(x, y);
            case 2 -> renderTargetMarkPage(x, y);
            case 3 -> renderPerformancePage(x, y);
        }

        // 返回按钮
        addRenderableWidget(Button.builder(Component.literal("§7← 返回"),
            btn -> Minecraft.getInstance().setScreen(new ClickGuiScreen()))
            .bounds(x + 140 - 40, by + H - 22, 80, 18).build());
    }

    private void renderGeneralPage(int x, int y) {
        int sp = 24;
        addToggle(x, y, W, "功能开关", cfg.enabled, v -> cfg.enabled = v);
        addRenderableWidget(Button.builder(Component.literal("触发模式: §b" + cfg.triggerMode),
            btn -> {
                cfg.triggerMode = switch (cfg.triggerMode) {
                    case "ON_ATTACK" -> "ON_TPAURA_LOCK";
                    case "ON_TPAURA_LOCK" -> "BOTH";
                    default -> "ON_ATTACK";
                };
                AttackIndicatorConfig.save();
                btn.setMessage(Component.literal("触发模式: §b" + cfg.triggerMode));
            }).bounds(x + 10, y += sp + 4, 260, 18).build());
        addToggle(x, y += sp, W, "平滑过渡", cfg.smoothTransition, v -> cfg.smoothTransition = v);
        // 触发模式说明
        y += sp + 8;
        addRenderableWidget(Button.builder(Component.literal("§7§l触发模式说明"), b -> {}).bounds(x + 10, y, 260, 14).build());
        addRenderableWidget(Button.builder(Component.literal("§7ON_ATTACK → 仅攻击时触发"), b -> {}).bounds(x + 10, y += 16, 260, 14).build());
        addRenderableWidget(Button.builder(Component.literal("§7ON_TPAURA_LOCK → 仅TpAura锁定"), b -> {}).bounds(x + 10, y += 14, 260, 14).build());
        addRenderableWidget(Button.builder(Component.literal("§7BOTH → 两者都触发"), b -> {}).bounds(x + 10, y += 14, 260, 14).build());
    }

    private void renderConnectionPage(int x, int y) {
        int sp = 24;
        // 能量光束
        addToggle(x, y, W, "能量光束", cfg.enableBeam, v -> cfg.enableBeam = v);
        y += sp;
        if (cfg.enableBeam) {
            addColorInput(x + 15, y, "颜色", "beamColor");
            addSliderDesc(x + 15, y += 20, "宽度: " + String.format("%.1f", cfg.beamWidth),
                b -> { cfg.beamWidth = cfg.beamWidth >= 5.0f ? 1.0f : cfg.beamWidth + 0.5f; AttackIndicatorConfig.save(); b.setMessage(Component.literal("§7  宽度: " + String.format("%.1f", cfg.beamWidth) + "  §8[点击调节]")); });
            addSliderDesc(x + 15, y += 20, "流速: " + String.format("%.1f", cfg.beamFlowSpeed),
                b -> { cfg.beamFlowSpeed = cfg.beamFlowSpeed >= 2.0f ? 0.1f : cfg.beamFlowSpeed + 0.2f; AttackIndicatorConfig.save(); b.setMessage(Component.literal("§7  流速: " + String.format("%.1f", cfg.beamFlowSpeed) + "  §8[点击调节]")); });
            y += 12;
        }
        // 脉冲波
        addToggle(x, y, W, "脉冲波", cfg.enablePulseWave, v -> cfg.enablePulseWave = v);
        y += sp;
        if (cfg.enablePulseWave) {
            addColorInput(x + 15, y, "颜色", "waveColor");
            addSliderDesc(x + 15, y += 20, "速度: " + String.format("%.1f", cfg.waveSpeed),
                b -> { cfg.waveSpeed = cfg.waveSpeed >= 3.0f ? 0.5f : cfg.waveSpeed + 0.5f; AttackIndicatorConfig.save(); b.setMessage(Component.literal("§7  速度: " + String.format("%.1f", cfg.waveSpeed) + "  §8[点击调节]")); });
            y += 12;
        }
        // 剑波（弯月剑气）
        addToggle(x, y, W, "§b剑波§r", cfg.enableSwordWave, v -> cfg.enableSwordWave = v);
        y += sp;
        if (cfg.enableSwordWave) {
            addColorInput(x + 15, y, "颜色", "swordWaveColor");
            addSliderDesc(x + 15, y += 20, "强度: " + String.format("%.1f", cfg.swordWaveIntensity),
                b -> { cfg.swordWaveIntensity = cfg.swordWaveIntensity >= 2.0f ? 0.5f : cfg.swordWaveIntensity + 0.5f; AttackIndicatorConfig.save(); b.setMessage(Component.literal("§7  强度: " + String.format("%.1f", cfg.swordWaveIntensity) + "  §8[点击调节]")); });
            addSliderDesc(x + 15, y += 20, "速度: " + String.format("%.1f", cfg.swordWaveSpeed),
                b -> { cfg.swordWaveSpeed = cfg.swordWaveSpeed >= 2.0f ? 0.5f : cfg.swordWaveSpeed + 0.5f; AttackIndicatorConfig.save(); b.setMessage(Component.literal("§7  速度: " + String.format("%.1f", cfg.swordWaveSpeed) + "  §8[点击调节]")); });
            y += 12;
        }
    }

    private void renderTargetMarkPage(int x, int y) {
        int sp = 24;
        // 标记光柱
        addToggle(x, y, W, "标记光柱", cfg.enableBeamMarker, v -> cfg.enableBeamMarker = v);
        y += sp;
        if (cfg.enableBeamMarker) {
            addColorInput(x + 15, y, "颜色", "beamMarkerColor");
            addSliderDesc(x + 15, y += 20, "高度: " + String.format("%.1f", cfg.beamMarkerHeight),
                b -> { cfg.beamMarkerHeight = cfg.beamMarkerHeight >= 16.0f ? 4.0f : cfg.beamMarkerHeight + 2.0f; AttackIndicatorConfig.save(); b.setMessage(Component.literal("§7  高度: " + String.format("%.1f", cfg.beamMarkerHeight) + "  §8[点击调节]")); });
            y += 12;
        }
        // 光环
        addToggle(x, y, W, "光环", cfg.enableHalo, v -> cfg.enableHalo = v);
        y += sp;
        if (cfg.enableHalo) {
            addColorInput(x + 15, y, "颜色", "haloColor");
            addSliderDesc(x + 15, y += 20, "半径: " + String.format("%.1f", cfg.haloRadius),
                b -> { cfg.haloRadius = cfg.haloRadius >= 3.0f ? 0.5f : cfg.haloRadius + 0.25f; AttackIndicatorConfig.save(); b.setMessage(Component.literal("§7  半径: " + String.format("%.1f", cfg.haloRadius) + "  §8[点击调节]")); });
            addSliderDesc(x + 15, y += 20, "旋转速度: " + String.format("%.1f", cfg.haloRotateSpeed),
                b -> { cfg.haloRotateSpeed = cfg.haloRotateSpeed >= 3.0f ? 0.5f : cfg.haloRotateSpeed + 0.5f; AttackIndicatorConfig.save(); b.setMessage(Component.literal("§7  旋转速度: " + String.format("%.1f", cfg.haloRotateSpeed) + "  §8[点击调节]")); });
            y += 12;
        }
    }

    private void renderPerformancePage(int x, int y) {
        int sp = 24;
        addToggle(x, y, W, "性能模式", cfg.enablePerformanceMode, v -> cfg.enablePerformanceMode = v);
        y += sp + 4;
        if (cfg.enablePerformanceMode) {
            addRenderableWidget(Button.builder(Component.literal("§7性能模式开启时："), b -> {}).bounds(x + 10, y, 260, 14).build());
            addRenderableWidget(Button.builder(Component.literal("§7- 限制粒子数量"), b -> {}).bounds(x + 10, y += 14, 260, 14).build());
            addRenderableWidget(Button.builder(Component.literal("§7- 禁用着色器特效"), b -> {}).bounds(x + 10, y += 14, 260, 14).build());
            addRenderableWidget(Button.builder(Component.literal("§7- 降低渲染频率"), b -> {}).bounds(x + 10, y += 14, 260, 14).build());
            y += 14;
        }
        addRenderableWidget(Button.builder(Component.literal("最大粒子数: §b" + cfg.maxParticles),
            btn -> {
                cfg.maxParticles = cfg.maxParticles >= 500 ? 10 : cfg.maxParticles * 2;
                AttackIndicatorConfig.save();
                btn.setMessage(Component.literal("最大粒子数: §b" + cfg.maxParticles));
            }).bounds(x + 10, y += 4, 260, 18).build());
        addRenderableWidget(Button.builder(Component.literal("LOD距离: §b" + String.format("%.0f", cfg.particleLODDistance)),
            btn -> {
                float[] levels = {8.0f, 16.0f, 24.0f, 32.0f, 48.0f, 64.0f, 96.0f, 128.0f};
                int idx = 0;
                for (int i = 0; i < levels.length; i++) {
                    if (Math.abs(cfg.particleLODDistance - levels[i]) < 0.1) { idx = i; break; }
                }
                idx = (idx + 1) % levels.length;
                cfg.particleLODDistance = levels[idx];
                AttackIndicatorConfig.save();
                btn.setMessage(Component.literal("LOD距离: §b" + String.format("%.0f", cfg.particleLODDistance)));
            }).bounds(x + 10, y += sp, 260, 18).build());
        addRenderableWidget(Button.builder(Component.literal("残留特效: §b" + cfg.despawnDelay + " ticks"),
            btn -> {
                cfg.despawnDelay = (cfg.despawnDelay + 1) % 11;
                AttackIndicatorConfig.save();
                btn.setMessage(Component.literal("残留特效: §b" + cfg.despawnDelay + " ticks"));
            }).bounds(x + 10, y += sp, 260, 18).build());
    }

    // ═══════ GUI辅助方法 ═══════

    /** ★ 更新滑块按钮文本（实时更新修复 — 改为调用rebuildWidgets确保刷新） */
    private void updateSliderText(Button btn, String label, String value) {
        btn.setMessage(Component.literal("§7  " + label + ": " + value + "  §8[点击调节]"));
    }

    private void addToggle(int x, int y, int w, String label, boolean current, Consumer<Boolean> setter) {
        addRenderableWidget(Button.builder(Component.literal(current ? "§a■ " + label : "§7□ " + label),
            btn -> {
                boolean newVal = !current;
                setter.accept(newVal);
                AttackIndicatorConfig.save();
                // 立即更新按钮文本，再rebuildWidgets以更新子选项的显隐
                btn.setMessage(Component.literal(newVal ? "§a■ " + label : "§7□ " + label));
                rebuildWidgets();
            })
            .bounds(x + 10, y, w - 20, 18).build());
    }

    /**
     * ★ 创建颜色选择按钮（修复：使用fill绘制的色块代替§x颜色代码）
     * 存储按钮引用和坐标，用于在render中绘制色块
     */
    private void addColorInput(int x, int y, String label, String fieldName) {
        String currentHex = getFieldValue(fieldName);

        // 色块按钮：显示十六进制值
        Button colorBtn = Button.builder(Component.literal("#" + currentHex),
            btn -> openColorPicker(fieldName)).bounds(x, y, 50, 16).build();
        Button labelBtn = Button.builder(Component.literal("§7" + label),
            btn -> openColorPicker(fieldName)).bounds(x + 52, y, 193, 16).build();

        // 存储色块按钮的色块绘制坐标
        colorButtonPositions.put(fieldName, new int[]{x + 2, y + 2, 14, 12});

        addRenderableWidget(colorBtn);
        addRenderableWidget(labelBtn);
        // 存储按钮引用，用于实时更新
        colorButtons.put(fieldName, new Button[]{colorBtn, labelBtn});
    }

    /** ★ 实时更新颜色按钮文本 */
    private void updateColorButtonText(String fieldName) {
        Button[] btns = colorButtons.get(fieldName);
        if (btns == null) return;
        String currentHex = getFieldValue(fieldName);
        btns[0].setMessage(Component.literal("#" + currentHex));
        // labelBtn 不需要更新，显示的是固定标签名
    }

    /** 从配置对象获取当前字段的十六进制颜色值 */
    private String getFieldValue(String fieldName) {
        return switch (fieldName) {
            case "beamColor" -> cfg.beamColor;
            case "waveColor" -> cfg.waveColor;
            case "beamMarkerColor" -> cfg.beamMarkerColor;
            case "haloColor" -> cfg.haloColor;
            case "swordWaveColor" -> cfg.swordWaveColor;
            default -> "FF4444";
        };
    }

    /** 打开颜色轮盘（参考 GuiStyleScreen 实现） */
    private void openColorPicker(String fieldName) {
        this.pendingColorField = fieldName;
        this.colorWheelPicker.setColor(getFieldValue(fieldName));
        this.colorWheelPicker.open(width / 2, height / 2);
        this.colorPickerOpen = true;
    }

    /**
     * ★ 添加滑块描述按钮 — 点击后执行action并立即重建UI确保数值实时更新
     * 使用rebuildWidgets()刷新所有控件的当前值，解决"需切界面再切回来才更新"的问题
     */
    private void addSliderDesc(int x, int y, String text, Consumer<Button> action) {
        addRenderableWidget(Button.builder(Component.literal("§7  " + text + "  §8[点击调节]"),
            b -> { action.accept(b); rebuildWidgets(); }).bounds(x, y, 245, 16).build());
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        GuiRenderHelper.drawRoundedRect(g, bx - 10, by - 8, 300, 356, 0xAA2D2D2D, 8);
        g.drawString(font, "§l攻击指示器配置", bx + 10, by - 4, 0xFFFFFF);
        super.render(g, mx, my, pt);

        // ★ 绘制所有颜色字段的色块（用fill绘制矩形，可靠不依赖§x解析）
        for (Map.Entry<String, int[]> entry : colorButtonPositions.entrySet()) {
            String field = entry.getKey();
            int[] pos = entry.getValue();
            String hex = getFieldValue(field);
            int color = hexToArgb(hex, 0xFF);
            // 绘制色块
            g.fill(pos[0], pos[1], pos[0] + pos[2], pos[1] + pos[3], color);
            // 绘制色块边框（确保可见）
            g.fill(pos[0], pos[1], pos[0] + pos[2], pos[1] + 1, 0xFF888888);
            g.fill(pos[0], pos[1] + pos[3] - 1, pos[0] + pos[2], pos[1] + pos[3], 0xFF888888);
            g.fill(pos[0], pos[1], pos[0] + 1, pos[1] + pos[3], 0xFF888888);
            g.fill(pos[0] + pos[2] - 1, pos[1], pos[0] + pos[2], pos[1] + pos[3], 0xFF888888);
        }

        // ★ 颜色轮盘在最后绘制（覆盖在最上层，参考 GuiStyleScreen 顺序）
        if (colorPickerOpen && colorWheelPicker.isOpen()) {
            colorWheelPicker.render(g, mx, my);
        }
    }

    /** 将6位十六进制颜色转换为ARGB整数 */
    private static int hexToArgb(String hex, int alpha) {
        try {
            if (hex == null || hex.length() < 6) return 0xFFFF4444;
            int r = Integer.parseInt(hex.substring(0, 2), 16);
            int g = Integer.parseInt(hex.substring(2, 4), 16);
            int b = Integer.parseInt(hex.substring(4, 6), 16);
            return (alpha << 24) | (r << 16) | (g << 8) | b;
        } catch (Exception e) {
            return 0xFFFF4444;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // ★ 颜色轮盘打开时优先处理（参考 GuiStyleScreen 做法）
        if (colorPickerOpen && colorWheelPicker.isOpen()) {
            if (colorWheelPicker.mouseClicked(mouseX, mouseY, button)) {
                // 颜色已更新（回调中已更新按钮文本并保存配置）
                return true;
            }
            // 点击到轮盘外部 → 关闭轮盘，阻止点击穿透
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
            // 拖动时也更新颜色
            colorWheelPicker.mouseClicked(mouseX, mouseY, button);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        // ★ 颜色轮盘打开时也阻止鼠标释放事件的穿透
        if (colorPickerOpen && colorWheelPicker.isOpen()) {
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void onClose() {
        AttackIndicatorConfig.save();
        minecraft.setScreen(new ClickGuiScreen());
    }
}