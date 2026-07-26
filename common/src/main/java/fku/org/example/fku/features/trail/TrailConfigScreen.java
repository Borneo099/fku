package fku.org.example.fku.features.trail; /* water */

import fku.org.example.fku.client.gui.ClickGuiScreen;
import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.client.gui.components.ColorWheelPicker;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

/**
 * 拖尾特效配置界面 — 分组分类设计
 * 包含6个分组：通用设置、残影模式、粒子模式、流光模式、颜色外观、性能限制
 * 所有改动即时保存到 TrailConfig
 * 该配置界面由赛博教员实现
 */
public class TrailConfigScreen extends Screen {
    private static final int WIDTH = 300;
    private static final int VISIBLE_HEIGHT = 255;
    private static final int CONTENT_HEIGHT = 1000;

    private final TrailConfig cfg;
    private String activeCategory = "通用";
    private int scrollOffset = 0;
    private int contentMaxRow = 0;

    // 颜色选择器
    private ColorWheelPicker colorWheelPicker;
    private String pendingColorField = null;

    // 拖尾模式中文名映射（仅保留有效模式）
    private static final String[][] TRAIL_MODES = {
        {"GHOST", "残影拖尾"},
        {"PARTICLE", "粒子流"},
        {"LIGHT_STREAK", "流光轨迹"},
        {"ELEMENTAL_FOOTPRINT", "元素足迹"}
    };

    // 触发条件中文名映射
    private static final String[][] TRIGGER_MODES = {
        {"ALWAYS", "始终开启"},
        {"SPRINTING", "仅疾跑时"},
        {"FLYING", "仅飞行时"},
        {"JUMPING", "仅跳跃时"},
        {"COMBAT", "仅战斗时"}
    };

    // 粒子类型中文名映射
    private static final String[][] PARTICLE_TYPES = {
        {"FLAME", "火焰"},
        {"DRAGON_BREATH", "龙息"},
        {"END_ROD", "末地烛"},
        {"FIREWORK", "烟花"},
        {"PORTAL", "传送门"},
        {"SOUL", "灵魂"}
    };

    public TrailConfigScreen() {
        super(Component.literal("拖尾特效配置"));
        this.cfg = TrailConfig.getInstance();
        this.colorWheelPicker = new ColorWheelPicker(cfg.mainColor, hex -> {
            if (pendingColorField != null) {
                switch (pendingColorField) {
                    case "mainColor" -> cfg.mainColor = hex;
                    case "secondaryColor" -> cfg.secondaryColor = hex;
                    case "streakColorStart" -> cfg.streakColorStart = hex;
                    case "streakColorEnd" -> cfg.streakColorEnd = hex;
                }
                TrailConfig.save();
            }
        });
    }

    @Override
    protected void init() {
        super.init();
        rebuildWidgets();
    }

    protected void rebuildWidgets() {
        clearWidgets();
        int cx = (width - WIDTH) / 2;
        int cy = (height - VISIBLE_HEIGHT) / 2;
        int row = cy + 35 - scrollOffset;
        contentMaxRow = row;

        // ── 分类标签行 ──
        String[] categories = {"通用", "残影", "粒子", "流光", "颜色", "性能"};
        int tabX = cx + 5;
        for (String cat : categories) {
            final String fcat = cat;
            boolean isActive = cat.equals(activeCategory);
            addRenderableWidget(Button.builder(
                Component.literal((isActive ? "§l[" : " ") + cat + (isActive ? "]§r" : " ")),
                btn -> {
                    activeCategory = fcat;
                    rebuildWidgets();
                }
            ).bounds(tabX, cy + 5, 42, 16).build());
            tabX += 44;
        }

        // 根据分类渲染内容
        switch (activeCategory) {
            case "通用" -> buildGeneralSettings(cx, cy, row);
            case "残影" -> buildGhostSettings(cx, cy, row);
            case "粒子" -> buildParticleSettings(cx, cy, row);
            case "流光" -> buildStreakSettings(cx, cy, row);
            case "颜色" -> buildColorSettings(cx, cy, row);
            case "性能" -> buildPerformanceSettings(cx, cy, row);
        }

        // 返回按钮
        addRenderableWidget(Button.builder(Component.literal("§7← 返回"),
            btn -> Minecraft.getInstance().setScreen(new ClickGuiScreen()))
            .bounds(cx + 110, cy + VISIBLE_HEIGHT - 28, 80, 20).build());
    }

    // ════════════════════════════════════════════
    // ★ 通用设置
    // ════════════════════════════════════════════

    private void buildGeneralSettings(int cx, int cy, int row) {
        addToggle(row, "功能开关", () -> cfg.enabled, v -> cfg.enabled = v);
        row += 24;

        // 拖尾模式选择
        drawLabel(cx, row, "拖尾模式:");
        int modeX = cx + 100;
        int modeRow = row;
        for (String[] mode : TRAIL_MODES) {
            boolean active = mode[0].equals(cfg.trailMode);
            int btnW = Math.min(70, (WIDTH - 110) / TRAIL_MODES.length);
            if (modeX + btnW > cx + WIDTH - 10) { modeX = cx + 100; modeRow += 18; }
            addRenderableWidget(Button.builder(
                Component.literal(active ? "▶" + mode[1] : mode[1]),
                btn -> { cfg.trailMode = mode[0]; TrailConfig.save(); rebuildWidgets(); }
            ).bounds(modeX, modeRow, btnW, 16).build());
            modeX += btnW + 4;
        }
        row = modeRow + 24;

        // 触发条件选择
        drawLabel(cx, row, "触发条件:");
        int trigX = cx + 100;
        int trigRow = row;
        for (String[] trig : TRIGGER_MODES) {
            boolean active = trig[0].equals(cfg.triggerMode);
            int btnW = Math.min(52, (WIDTH - 110) / 3);
            if (trigX + btnW > cx + WIDTH - 10) { trigX = cx + 100; trigRow += 18; }
            addRenderableWidget(Button.builder(
                Component.literal(active ? "▶" + trig[1] : trig[1]),
                btn -> { cfg.triggerMode = trig[0]; TrailConfig.save(); rebuildWidgets(); }
            ).bounds(trigX, trigRow, btnW, 16).build());
            trigX += btnW + 4;
        }
        row = trigRow + 24;

        // 淡出Tick
        drawLabel(cx, row, "淡出(Tick):");
        addCycleButton(cx + 110, row, 50, () -> cfg.fadeOutTicks,
            new int[]{2, 5, 10, 15, 20, 30, 40},
            v -> cfg.fadeOutTicks = v);
    }

    // ════════════════════════════════════════════
    // ★ 残影模式参数
    // ════════════════════════════════════════════

    private void buildGhostSettings(int cx, int cy, int row) {
        drawLabel(cx, row, "生成间隔(Tick):");
        addCycleButton(cx + 140, row, 50, () -> cfg.ghostInterval,
            new int[]{1, 2, 3, 4, 5, 8, 10},
            v -> cfg.ghostInterval = v);
        row += 22;

        drawLabel(cx, row, "最大残影数:");
        addCycleButton(cx + 140, row, 50, () -> cfg.ghostMaxCount,
            new int[]{5, 10, 15, 20, 30, 40, 50},
            v -> cfg.ghostMaxCount = v);
        row += 22;

        drawLabel(cx, row, "初始透明度:");
        addCycleButton(cx + 140, row, 50, () -> cfg.ghostAlphaStart,
            new double[]{0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0},
            v -> cfg.ghostAlphaStart = v);
        row += 22;

        drawLabel(cx, row, "消失透明度:");
        addCycleButton(cx + 140, row, 50, () -> cfg.ghostAlphaEnd,
            new double[]{0.0, 0.1, 0.2, 0.3, 0.4, 0.5},
            v -> cfg.ghostAlphaEnd = v);
    }

    // ════════════════════════════════════════════
    // ★ 粒子模式参数
    // ════════════════════════════════════════════

    private void buildParticleSettings(int cx, int cy, int row) {
        drawLabel(cx, row, "粒子类型:");
        int px = cx + 100;
        for (String[] pt : PARTICLE_TYPES) {
            boolean active = pt[0].equals(cfg.particleType);
            addRenderableWidget(Button.builder(
                Component.literal(active ? "▶" + pt[1] : pt[1]),
                btn -> { cfg.particleType = pt[0]; TrailConfig.save(); rebuildWidgets(); }
            ).bounds(px, row, 48, 16).build());
            px += 50;
            if (px > cx + WIDTH - 20) { px = cx + 100; row += 18; }
        }
        row += 24;

        drawLabel(cx, row, "每Tick粒子数:");
        addCycleButton(cx + 140, row, 50, () -> cfg.particlesPerTick,
            new int[]{1, 2, 3, 4, 5, 8, 10, 15},
            v -> cfg.particlesPerTick = v);
        row += 22;

        drawLabel(cx, row, "粒子生命(Tick):");
        addCycleButton(cx + 140, row, 50, () -> cfg.particleLifetime,
            new int[]{5, 10, 15, 20, 30, 40, 60},
            v -> cfg.particleLifetime = v);
        row += 22;

        drawLabel(cx, row, "粒子速度:");
        addCycleButton(cx + 140, row, 50, () -> cfg.particleSpeed,
            new double[]{0.05, 0.1, 0.2, 0.3, 0.5, 0.8, 1.0},
            v -> cfg.particleSpeed = v);
        row += 22;

        drawLabel(cx, row, "扩散范围:");
        addCycleButton(cx + 140, row, 50, () -> cfg.particleSpread,
            new double[]{0.1, 0.2, 0.3, 0.5, 0.8, 1.0, 1.5, 2.0},
            v -> cfg.particleSpread = v);
    }

    // ════════════════════════════════════════════
    // ★ 流光模式参数
    // ════════════════════════════════════════════

    private void buildStreakSettings(int cx, int cy, int row) {
        drawLabel(cx, row, "最大路径点数:");
        addCycleButton(cx + 140, row, 50, () -> cfg.streakMaxPoints,
            new int[]{20, 30, 40, 50, 60, 80, 100, 150, 200},
            v -> cfg.streakMaxPoints = v);
        row += 22;

        drawLabel(cx, row, "光轨宽度:");
        addCycleButton(cx + 140, row, 50, () -> cfg.streakWidth,
            new double[]{0.02, 0.05, 0.08, 0.1, 0.15, 0.2, 0.3, 0.5},
            v -> cfg.streakWidth = v);
        row += 22;

        drawLabel(cx, row, "平滑步数:");
        addCycleButton(cx + 140, row, 50, () -> cfg.streakSmoothness,
            new int[]{2, 3, 4, 5, 6, 7, 8},
            v -> cfg.streakSmoothness = v);
        row += 22;

        // 颜色按钮
        drawLabel(cx, row, "起点颜色:");
        addColorButton(cx + 100, row, cfg.streakColorStart, "streakColorStart");
        row += 22;

        drawLabel(cx, row, "终点颜色:");
        addColorButton(cx + 100, row, cfg.streakColorEnd, "streakColorEnd");
    }

    // ════════════════════════════════════════════
    // ★ 颜色与外观设置
    // ════════════════════════════════════════════

    private void buildColorSettings(int cx, int cy, int row) {
        drawLabel(cx, row, "主色调:");
        addColorButton(cx + 100, row, cfg.mainColor, "mainColor");
        row += 24;

        addToggle(row, "颜色渐变", () -> cfg.colorGradient, v -> cfg.colorGradient = v);
        row += 24;

        if (cfg.colorGradient) {
            drawLabel(cx, row, "次要颜色:");
            addColorButton(cx + 100, row, cfg.secondaryColor, "secondaryColor");
            row += 24;
        }

        drawLabel(cx, row, "发光强度:");
        addCycleButton(cx + 140, row, 50, () -> cfg.glowIntensity,
            new double[]{0.1, 0.2, 0.3, 0.5, 0.8, 1.0, 1.2, 1.5, 2.0},
            v -> cfg.glowIntensity = v);
    }

    // ════════════════════════════════════════════
    // ★ 性能限制设置
    // ════════════════════════════════════════════

    private void buildPerformanceSettings(int cx, int cy, int row) {
        drawLabel(cx, row, "最大粒子数:");
        addCycleButton(cx + 140, row, 50, () -> cfg.maxParticles,
            new int[]{50, 100, 150, 200, 300, 500, 800, 1000},
            v -> cfg.maxParticles = v);
        row += 24;

        drawLabel(cx, row, "最大残影数:");
        addCycleButton(cx + 140, row, 50, () -> cfg.maxGhosts,
            new int[]{10, 15, 20, 30, 40, 50, 70, 100},
            v -> cfg.maxGhosts = v);
        row += 24;

        addToggle(row, "低帧率自动禁用", () -> cfg.disableInLowFps, v -> cfg.disableInLowFps = v);
        row += 24;

        drawLabel(cx, row, "LOD距离:");
        addCycleButton(cx + 140, row, 50, () -> (int)cfg.lodDistance,
            new int[]{8, 16, 24, 32, 48, 64, 96, 128},
            v -> cfg.lodDistance = v);
    }

    // ════════════════════════════════════════════
    // ★ 通用控件方法
    // ════════════════════════════════════════════

    /** 开关按钮 — 使用Supplier实时读取当前值，修复lambda捕获问题 */
    private void addToggle(int row, String label, Supplier<Boolean> getter, java.util.function.Consumer<Boolean> setter) {
        int cx = (width - WIDTH) / 2;
        drawLabel(cx, row, label + ":");
        addRenderableWidget(Button.builder(
            Component.literal(getter.get() ? "开" : "关"),
            btn -> {
                boolean newVal = !getter.get();
                setter.accept(newVal);
                TrailConfig.save();
                rebuildWidgets();
            }
        ).bounds(cx + 135, row, 40, 20).build());
    }

    /** 循环按钮（整数）— 使用Supplier实时读取当前值，修复lambda捕获问题 */
    private void addCycleButton(int x, int row, int w, Supplier<Integer> getter, int[] values, java.util.function.Consumer<Integer> setter) {
        addRenderableWidget(Button.builder(
            Component.literal(String.valueOf(getter.get())),
            btn -> {
                int currentVal = getter.get();
                int idx = 0;
                for (int i = 0; i < values.length; i++) {
                    if (values[i] == currentVal) { idx = i; break; }
                }
                int newVal = values[(idx + 1) % values.length];
                setter.accept(newVal);
                TrailConfig.save();
                btn.setMessage(Component.literal(String.valueOf(newVal)));
                rebuildWidgets(); // 确保UI刷新
            }
        ).bounds(x, row, w, 18).build());
    }

    /** 循环按钮（浮点数）— 使用Supplier实时读取当前值 */
    private void addCycleButton(int x, int row, int w, Supplier<Double> getter, double[] values, java.util.function.Consumer<Double> setter) {
        addRenderableWidget(Button.builder(
            Component.literal(formatDouble(getter.get())),
            btn -> {
                double currentVal = getter.get();
                int idx = 0;
                for (int i = 0; i < values.length; i++) {
                    if (Math.abs(values[i] - currentVal) < 0.001) { idx = i; break; }
                }
                double newVal = values[(idx + 1) % values.length];
                setter.accept(newVal);
                TrailConfig.save();
                btn.setMessage(Component.literal(formatDouble(newVal)));
                rebuildWidgets(); // 确保UI刷新
            }
        ).bounds(x, row, w, 18).build());
    }

    private static String formatDouble(double v) {
        return v == (int)v ? String.valueOf((int)v) : String.format("%.2f", v);
    }

    /** 颜色按钮 */
    private void addColorButton(int x, int row, String hex, String fieldName) {
        addRenderableWidget(Button.builder(
            Component.literal("#" + hex),
            btn -> {
                pendingColorField = fieldName;
                int cx = (width - WIDTH) / 2;
                int cy = (height - VISIBLE_HEIGHT) / 2;
                colorWheelPicker.open(cx + WIDTH / 2, cy + VISIBLE_HEIGHT / 2);
            }
        ).bounds(x, row, 65, 18).build());
    }

    private void drawLabel(int cx, int row, String text) {
        // 标签在render中统一绘制
    }

    // ════════════════════════════════════════════
    // ★ 渲染
    // ════════════════════════════════════════════

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int cx = (width - WIDTH) / 2, cy = (height - VISIBLE_HEIGHT) / 2;
        if (mouseX >= cx && mouseX <= cx + WIDTH && mouseY >= cy && mouseY <= cy + VISIBLE_HEIGHT) {
            int newScroll = scrollOffset - (int)(delta * 16);
            int maxScroll = Math.max(0, contentMaxRow - (cy + VISIBLE_HEIGHT - 60));
            scrollOffset = Math.max(0, Math.min(newScroll, maxScroll));
            rebuildWidgets();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);

        int cx = (width - WIDTH) / 2;
        int cy = (height - VISIBLE_HEIGHT) / 2;

        // 绘制主面板背景
        GuiRenderHelper.drawPanelBackground(guiGraphics, cx, cy, WIDTH, VISIBLE_HEIGHT, false);

        // 绘制标题
        guiGraphics.drawString(font, "拖尾特效配置 - " + activeCategory, cx + 10, cy + 25, 0xFFFFFF);

        // 启用裁剪
        guiGraphics.enableScissor(cx + 2, cy + 35, cx + WIDTH - 2, cy + VISIBLE_HEIGHT - 32);

        // 绘制当前分类的标签
        int row = cy + 35 - scrollOffset;
        String[][] labelDefs = switch (activeCategory) {
            case "通用" -> new String[][]{
                {"功能开关:", "35"}, {"拖尾模式:", "59"}, {"触发条件:", "95"}, {"淡出(Tick):", "131"}
            };
            case "残影" -> new String[][]{
                {"生成间隔(Tick):", "35"}, {"最大残影数:", "57"}, {"初始透明度:", "79"}, {"消失透明度:", "101"}
            };
            case "粒子" -> new String[][]{
                {"粒子类型:", "35"}, {"每Tick粒子数:", "71"}, {"粒子生命(Tick):", "93"}, {"粒子速度:", "115"}, {"扩散范围:", "137"}
            };
            case "流光" -> new String[][]{
                {"最大路径点数:", "35"}, {"光轨宽度:", "57"}, {"平滑步数:", "79"}, {"起点颜色:", "101"}, {"终点颜色:", "123"}
            };
            case "颜色" -> new String[][]{
                {"主色调:", "35"}, {"颜色渐变:", "59"}, {"次要颜色:", "83"}, {"发光强度:", "107"}
            };
            case "性能" -> new String[][]{
                {"最大粒子数:", "35"}, {"最大残影数:", "59"}, {"低帧率自动禁用:", "83"}, {"LOD距离:", "107"}
            };
            default -> new String[][]{};
        };

        for (String[] pair : labelDefs) {
            int yRow = cy + Integer.parseInt(pair[1]) - scrollOffset;
            if (yRow + 4 >= cy + 35 && yRow < cy + VISIBLE_HEIGHT - 35) {
                guiGraphics.drawString(font, pair[0], cx + 10, yRow + 4, 0xAAAAAA);
            }
        }

        guiGraphics.disableScissor();
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        colorWheelPicker.render(guiGraphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (colorWheelPicker.isOpen()) {
            if (colorWheelPicker.mouseClicked(mouseX, mouseY, button)) return true;
            rebuildWidgets();
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void onClose() {
        TrailConfig.save();
        this.minecraft.setScreen(new ClickGuiScreen());
    }
}