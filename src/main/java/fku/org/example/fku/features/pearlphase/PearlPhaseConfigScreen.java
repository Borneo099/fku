package fku.org.example.fku.features.pearlphase; /* water */

import fku.org.example.fku.client.gui.ClickGuiScreen;
import fku.org.example.fku.client.gui.GuiRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

/**
 * 珍珠卡墙（PearlPhase）配置界面
 *
 * ★ 职责：
 *   右键「珍珠卡墙」组件后打开，提供所有参数的可视化配置。
 *   修改即时保存到 PearlPhaseConfig。
 *
 * ★ 设计思想：
 *   简单直观的开关 + 数值输入界面，所有操作即时生效。
 *   参考 AntiLagScreen / LootScreen 的布局风格。
 */
public class PearlPhaseConfigScreen extends Screen {

    private static final int WIDTH = 290;
    private static final int HEIGHT = 280;

    // ════════ 行偏移常量 ════════
    private static final int ROW_AUTO_THROW = 30;
    private static final int ROW_NO_CLIP = 53;
    private static final int ROW_SPEED = 76;
    private static final int ROW_BASE_SPEED = 99;
    private static final int ROW_AIM_TIME = 122;
    private static final int ROW_MAX_WAIT = 145;
    private static final int ROW_EDGE_OFFSET = 168;
    private static final int ROW_REMOVE_OVERLAY = 191;
    private static final int ROW_NO_FRONT = 214;
    private static final int ROW_CLOSE = 245;

    private final PearlPhaseConfig cfg = PearlPhaseConfig.getInstance();

    // 输入框
    private EditBox speedField;
    private EditBox baseSpeedField;
    private EditBox aimTimeField;
    private EditBox maxWaitField;
    private EditBox edgeOffsetField;

    // 开关按钮
    private Button autoThrowButton;
    private Button noClipButton;
    private Button removeOverlayButton;
    private Button noFrontButton;

    public PearlPhaseConfigScreen() {
        super(Component.literal("珍珠卡墙配置"));
    }

    @Override
    protected void init() {
        int cx = (this.width - WIDTH) / 2;
        int cy = (this.height - HEIGHT) / 2;

        // ★ 开关按钮（toggle 型，每次点击切换）
        autoThrowButton = buildToggleButton(cx + 160, cy + ROW_AUTO_THROW, cfg.autoThrow, "自动投掷", (btn) -> {
            cfg.setAutoThrow(!cfg.autoThrow);
            btn.setMessage(Component.literal((cfg.autoThrow ? "§a✔ 开启" : "§c✘ 关闭")));
        });
        addRenderableWidget(autoThrowButton);

        noClipButton = buildToggleButton(cx + 160, cy + ROW_NO_CLIP, cfg.noClipEnabled, "NoClip", (btn) -> {
            cfg.setNoClipEnabled(!cfg.noClipEnabled);
            btn.setMessage(Component.literal((cfg.noClipEnabled ? "§a✔ 开启" : "§c✘ 关闭")));
        });
        addRenderableWidget(noClipButton);

        // ★ 输入框（数值参数）
        speedField = new EditBox(font, cx + 150, cy + ROW_SPEED, 100, 16, Component.literal("移动倍率"));
        speedField.setValue(String.valueOf(cfg.speed));
        speedField.setResponder(s -> {
            try { cfg.setSpeed(Double.parseDouble(s)); } catch (NumberFormatException ignored) {}
        });
        addRenderableWidget(speedField);

        baseSpeedField = new EditBox(font, cx + 150, cy + ROW_BASE_SPEED, 100, 16, Component.literal("基础速度"));
        baseSpeedField.setValue(String.valueOf(cfg.baseSpeed));
        baseSpeedField.setResponder(s -> {
            try { cfg.setBaseSpeed(Double.parseDouble(s)); } catch (NumberFormatException ignored) {}
        });
        addRenderableWidget(baseSpeedField);

        aimTimeField = new EditBox(font, cx + 150, cy + ROW_AIM_TIME, 100, 16, Component.literal("瞄准时间"));
        aimTimeField.setValue(String.valueOf(cfg.aimTime));
        aimTimeField.setResponder(s -> {
            try { cfg.setAimTime(Integer.parseInt(s)); } catch (NumberFormatException ignored) {}
        });
        addRenderableWidget(aimTimeField);

        maxWaitField = new EditBox(font, cx + 150, cy + ROW_MAX_WAIT, 100, 16, Component.literal("等待Tick"));
        maxWaitField.setValue(String.valueOf(cfg.maxWaitTicks));
        maxWaitField.setResponder(s -> {
            try { cfg.setMaxWaitTicks(Integer.parseInt(s)); } catch (NumberFormatException ignored) {}
        });
        addRenderableWidget(maxWaitField);

        edgeOffsetField = new EditBox(font, cx + 150, cy + ROW_EDGE_OFFSET, 100, 16, Component.literal("边缘偏移"));
        edgeOffsetField.setValue(String.valueOf(cfg.edgeOffset));
        edgeOffsetField.setResponder(s -> {
            try { cfg.setEdgeOffset(Double.parseDouble(s)); } catch (NumberFormatException ignored) {}
        });
        addRenderableWidget(edgeOffsetField);

        // 开关按钮：移除窒息贴图
        removeOverlayButton = buildToggleButton(cx + 160, cy + ROW_REMOVE_OVERLAY, cfg.removeOverlay, "移除贴图", (btn) -> {
            cfg.setRemoveOverlay(!cfg.removeOverlay);
            btn.setMessage(Component.literal((cfg.removeOverlay ? "§a✔ 开启" : "§c✘ 关闭")));
        });
        addRenderableWidget(removeOverlayButton);

        // 开关按钮：禁用前方视角
        noFrontButton = buildToggleButton(cx + 160, cy + ROW_NO_FRONT, cfg.noFront, "禁用前视角", (btn) -> {
            cfg.setNoFront(!cfg.noFront);
            btn.setMessage(Component.literal((cfg.noFront ? "§a✔ 开启" : "§c✘ 关闭")));
        });
        addRenderableWidget(noFrontButton);

        // 关闭按钮
        addRenderableWidget(Button.builder(
                Component.literal("关闭"),
                (btn) -> onClose()
        ).bounds(cx + 100, cy + ROW_CLOSE, 80, 20).build());
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int cx = (this.width - WIDTH) / 2;
        int cy = (this.height - HEIGHT) / 2;

        // ★ 绘制标题
        guiGraphics.drawString(font, "§l珍珠卡墙配置", cx, cy + 10, 0xFFFFFF);

        // ★ 绘制标签
        drawLabel(guiGraphics, cx, cy + ROW_AUTO_THROW, "自动投掷：看向墙壁时自动投掷珍珠");
        drawLabel(guiGraphics, cx, cy + ROW_NO_CLIP, "NoClip：卡入方块后启用穿墙");
        drawLabel(guiGraphics, cx, cy + ROW_SPEED, "移动倍率（0~20）：方块内移动速度");
        drawLabel(guiGraphics, cx, cy + ROW_BASE_SPEED, "基础速度（0.00001~0.1）");
        drawLabel(guiGraphics, cx, cy + ROW_AIM_TIME, "瞄准时间(ms)（0~1000）");
        drawLabel(guiGraphics, cx, cy + ROW_MAX_WAIT, "投掷后等待Tick（20~600）");
        drawLabel(guiGraphics, cx, cy + ROW_EDGE_OFFSET, "边缘偏移（0.0001~0.1）");
        drawLabel(guiGraphics, cx, cy + ROW_REMOVE_OVERLAY, "移除窒息贴图");
        drawLabel(guiGraphics, cx, cy + ROW_NO_FRONT, "禁用前方第三人称");
    }

    private void drawLabel(GuiGraphics gui, int cx, int y, String text) {
        gui.drawString(font, text, cx + 10, y + 2, 0xCCCCCC);
    }

    /**
     * ★ 构建一个开关按钮
     * @param x X坐标
     * @param y Y坐标
     * @param initial 初始状态
     * @param label 按钮文字
     * @param onClick 点击回调
     */
    private Button buildToggleButton(int x, int y, boolean initial, String label, Button.OnPress onClick) {
        String text = initial ? "§a✔ 开启" : "§c✘ 关闭";
        return Button.builder(Component.literal(text), onClick)
                .bounds(x, y, 80, 16)
                .build();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_ENTER) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        if (Minecraft.getInstance().screen instanceof ClickGuiScreen) {
            Minecraft.getInstance().setScreen(Minecraft.getInstance().screen);
        } else {
            Minecraft.getInstance().setScreen(null);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}