package fku.org.example.fku.features.sprint; /* water */

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
 * 强制疾跑配置界面
 *
 * ★ 参考 BedrockBreakerScreen 的布局模式：
 *   - 所有标签在 render() 中用 guiGraphics.drawString() 直接绘制，不经过队列
 *   - 固定行距，标签在上、输入框在下，避免重叠
 *   - 底部固定位置放置「返回主菜单」和「重置为默认」按钮
 *
 * ★ 分类标签页：
 *   0=基础设置（模式、忽略条件）
 *   1=高级设置（Legit选项、平滑旋转、鞘翅修正）
 *
 * ★ 布局规范（Y行距 = 28px）：
 *   标签 → cy(rowOffset)
 *   按钮 → cy(rowOffset)
 *   输入框 → cy(rowOffset + 14)
 *   提示文字 → cy(rowOffset + 28)
 */
public class SprintConfigScreen extends Screen {

    private static final int WIDTH = 300;
    private static final int HEIGHT = 260;

    // 行Y偏移基准（间距28px）
    private static final int ROW_MODE = 30;
    private static final int ROW_IGNORE_TITLE = 55;
    private static final int ROW_IGNORE_BLINDNESS = 75;
    private static final int ROW_IGNORE_HUNGER = 100;
    private static final int ROW_IGNORE_COLLISION = 125;
    private static final int ROW_STOP_GROUND = 30;
    private static final int ROW_STOP_AIR = 55;
    private static final int ROW_ELYTRA = 80;
    private static final int ROW_SMOOTH_SWITCH = 105;
    private static final int ROW_BUTTON = 220;

    private static final int COL_LABEL = 10;
    private static final int COL_WIDGET = 105;

    private final SprintConfig cfg;
    private final SprintConfig.Mode[] modeValues = SprintConfig.Mode.values();
    private int modeIndex;

    /** 当前分类：0=基础设置，1=高级设置 */
    private int activeTab = 0;

    /** 当前tab名文字（不用存储，仅用于渲染） */
    private static final String[] TAB_NAMES = {"基础设置", "高级设置"};

    // 平滑旋转速度输入框（仅高级Tab、平滑开启时显示）
    private EditBox rotationSpeedInput;
    private String cachedSpeedText = "";

    public SprintConfigScreen() {
        super(Component.literal("强制疾跑配置"));
        this.cfg = SprintConfig.getInstance();
        for (int i = 0; i < modeValues.length; i++) {
            if (modeValues[i].name().equals(cfg.mode)) {
                modeIndex = i;
                break;
            }
        }
    }

    // ════════════════════════════════════════════════════════════
    // ★ 统一布局辅助：标签Y & 按钮Y
    // ════════════════════════════════════════════════════════════

    private int cy(int row) { return (height - HEIGHT) / 2 + row; }
    private int cx() { return (width - WIDTH) / 2; }

    @Override
    protected void init() {
        super.init();
        rebuildWidgets();
    }

    protected void rebuildWidgets() {
        clearWidgets();
        rotationSpeedInput = null;

        int cx = cx();
        int cy = cy(0);

        // ── 分类标签 ──
        int tabX = cx + 10;
        for (int i = 0; i < TAB_NAMES.length; i++) {
            final int fi = i;
            boolean isActive = i == activeTab;
            int tw = Minecraft.getInstance().font.width(TAB_NAMES[i]) + 12;
            addRenderableWidget(Button.builder(
                Component.literal(isActive ? "§l[" + TAB_NAMES[i] + "]§r" : TAB_NAMES[i]),
                btn -> {
                    saveInputNow();
                    activeTab = fi;
                    rebuildWidgets();
                }
            ).bounds(tabX, cy + 7, Math.max(tw, 50), 16).build());
            tabX += Math.max(tw, 50) + 4;
        }

        if (activeTab == 0) {
            buildTabBasic();
        } else {
            buildTabAdvanced();
        }

        // ── 底部按钮 ──
        addRenderableWidget(Button.builder(
            Component.literal("返回主菜单"),
            btn -> {
                saveInputNow();
                Minecraft.getInstance().setScreen(new ClickGuiScreen());
            }
        ).bounds(cx + 40, cy(ROW_BUTTON), 100, 20).build());

        addRenderableWidget(Button.builder(
            Component.literal("重置默认"),
            btn -> {
                cfg.mode = "OMNIROTATIONAL";
                cfg.ignoreBlindness = false;
                cfg.ignoreHunger = false;
                cfg.ignoreCollision = false;
                cfg.stopOnGround = false;
                cfg.stopOnAir = false;
                cfg.elytraRotation = true;
                cfg.smoothRotation = false;
                cfg.rotationSpeed = 90;
                for (int i = 0; i < modeValues.length; i++) {
                    if (modeValues[i].name().equals(cfg.mode)) {
                        modeIndex = i;
                        break;
                    }
                }
                SprintConfig.save();
                rebuildWidgets();
            }
        ).bounds(cx + 160, cy(ROW_BUTTON), 100, 20).build());
    }

    // ════════════════════════════════════════════════════════════
    // ★ 基础设置 Tab
    // ════════════════════════════════════════════════════════════

    private void buildTabBasic() {
        int cx = cx();

        // ── 模式选择 ──
        addRenderableWidget(Button.builder(
            Component.literal(modeValues[modeIndex].getChineseLabel()),
            btn -> {
                modeIndex = (modeIndex + 1) % modeValues.length;
                cfg.mode = modeValues[modeIndex].name();
                SprintConfig.save();
                rebuildWidgets();
            }
        ).bounds(cx + COL_WIDGET, cy(ROW_MODE), 85, 18).build());

        // ── 忽略条件 ──
        addToggle(cy(ROW_IGNORE_BLINDNESS), cfg.ignoreBlindness, v -> cfg.ignoreBlindness = v);
        addToggle(cy(ROW_IGNORE_HUNGER), cfg.ignoreHunger, v -> cfg.ignoreHunger = v);
        addToggle(cy(ROW_IGNORE_COLLISION), cfg.ignoreCollision, v -> cfg.ignoreCollision = v);
    }

    // ════════════════════════════════════════════════════════════
    // ★ 高级设置 Tab
    // ════════════════════════════════════════════════════════════

    private void buildTabAdvanced() {
        int cx = cx();

        // ── Legit 模式选项 ──
        addToggle(cy(ROW_STOP_GROUND), cfg.stopOnGround, v -> cfg.stopOnGround = v);
        addToggle(cy(ROW_STOP_AIR), cfg.stopOnAir, v -> cfg.stopOnAir = v);

        // ── 鞘翅旋转修正 ──
        addToggle(cy(ROW_ELYTRA), cfg.elytraRotation, v -> cfg.elytraRotation = v);

        // ── 平滑旋转开关 + 速度输入 ──
        addToggle(cy(ROW_SMOOTH_SWITCH), cfg.smoothRotation, v -> {
            cfg.smoothRotation = v;
            SprintConfig.save();
            rebuildWidgets();
        });

        // 速度输入框（仅平滑开启时）
        if (cfg.smoothRotation) {
            rotationSpeedInput = new EditBox(font, cx + COL_WIDGET + 90, cy(ROW_SMOOTH_SWITCH), 45, 16, Component.literal(""));
            rotationSpeedInput.setValue(String.valueOf(cfg.rotationSpeed));
            rotationSpeedInput.setMaxLength(3);
            rotationSpeedInput.setFilter(s -> s.matches("\\d*"));
            rotationSpeedInput.setResponder(s -> cachedSpeedText = s);
            addRenderableWidget(rotationSpeedInput);
        }
    }

    // ════════════════════════════════════════════════════════════
    // ★ 辅助方法
    // ════════════════════════════════════════════════════════════

    /** 添加开关按钮（即时保存，不触发 rebuildWidgets） */
    private void addToggle(int y, boolean currentValue, java.util.function.Consumer<Boolean> setter) {
        int cx = cx();
        addRenderableWidget(Button.builder(
            Component.literal(currentValue ? "开" : "关"),
            btn -> {
                boolean newVal = !currentValue;
                setter.accept(newVal);
                SprintConfig.save();
                btn.setMessage(Component.literal(newVal ? "开" : "关"));
            }
        ).bounds(cx + COL_WIDGET, y, 40, 18).build());
    }

    /** 保存输入框数值到配置 */
    private void saveInputNow() {
        if (rotationSpeedInput != null) {
            try {
                int val = Integer.parseInt(rotationSpeedInput.getValue());
                if (val > 0 && val <= 360) {
                    cfg.rotationSpeed = val;
                    SprintConfig.save();
                }
            } catch (NumberFormatException ignored) {}
        }
    }

    // ════════════════════════════════════════════════════════════
    // ★ 渲染
    // ════════════════════════════════════════════════════════════

    @Override
    public void render(@NotNull GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);

        int cx = cx();
        int cy = cy(0);

        GuiRenderHelper.drawPanelBackground(g, cx, cy, WIDTH, HEIGHT, false);

        // ── 标题 ──
        g.drawString(font, "强制疾跑配置 - " + TAB_NAMES[activeTab], cx + 10, cy + 2, 0xFFFFFF);

        if (activeTab == 0) {
            // ── 疾跑模式 ──
            g.drawString(font, "疾跑模式:", cx + COL_LABEL, cy(ROW_MODE), 0xAAAAAA);

            // ── 忽略条件 ──
            g.drawString(font, "§7- - - 忽略条件 - - -", cx + COL_LABEL, cy(ROW_IGNORE_TITLE), 0x666666);
            g.drawString(font, "忽略失明:", cx + COL_LABEL, cy(ROW_IGNORE_BLINDNESS), 0xAAAAAA);
            g.drawString(font, "忽略饥饿:", cx + COL_LABEL, cy(ROW_IGNORE_HUNGER), 0xAAAAAA);
            g.drawString(font, "忽略撞墙:", cx + COL_LABEL, cy(ROW_IGNORE_COLLISION), 0xAAAAAA);
        } else {
            // ── Legit 选项 ──
            g.drawString(font, "§7- - - Legit 模式 - - -", cx + COL_LABEL, cy(ROW_STOP_GROUND) - 16, 0x666666);
            g.drawString(font, "地面停止:", cx + COL_LABEL, cy(ROW_STOP_GROUND), 0xAAAAAA);
            g.drawString(font, "空中停止:", cx + COL_LABEL, cy(ROW_STOP_AIR), 0xAAAAAA);

            // ── 鞘翅旋转 ──
            g.drawString(font, "§7- - - 全向旋转 - - -", cx + COL_LABEL, cy(ROW_ELYTRA) - 16, 0x666666);
            g.drawString(font, "鞘翅旋转:", cx + COL_LABEL, cy(ROW_ELYTRA), 0xAAAAAA);

            // ── 平滑旋转 ──
            g.drawString(font, "平滑旋转:", cx + COL_LABEL, cy(ROW_SMOOTH_SWITCH), 0xAAAAAA);
            if (cfg.smoothRotation) {
                g.drawString(font, "§7°/帧", cx + COL_WIDGET + 137, cy(ROW_SMOOTH_SWITCH), 0x666666);
            }
        }

        super.render(g, mx, my, pt);
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void onClose() {
        saveInputNow();
        this.minecraft.setScreen(new ClickGuiScreen());
    }
}