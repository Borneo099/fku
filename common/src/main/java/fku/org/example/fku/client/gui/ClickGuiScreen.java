package fku.org.example.fku.client.gui;

import fku.org.example.fku.client.gui.components.GuiComponent;
import fku.org.example.fku.client.gui.components.GuiPanel;
import fku.org.example.fku.client.gui.components.OtherPanel;
import fku.org.example.fku.client.gui.components.MovementPanel;
import fku.org.example.fku.client.gui.components.VisualPanel;
import fku.org.example.fku.client.gui.components.ToolPanel;
import fku.org.example.fku.client.gui.components.EntertainmentPanel;
import fku.org.example.fku.client.gui.components.CombatPanel;
import fku.org.example.fku.client.gui.components.WorldPanel;
import fku.org.example.fku.config.GuiStyleConfig;
import fku.org.example.fku.util.HotkeySystem;
import fku.org.example.fku.features.dynamicisland.DynamicIslandHud;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 主GUI界面 — 经 Apple Design 原则优化
 * - 即时反馈：不阻塞输入（§1 Response）
 * - 面板错峰弹簧进入（§3 Interruptibility + §8 空间一致性）
 * - 组件逐个淡入，跟随面板展开节奏
 * - 顶部提示中键可绑定热键
 */
public class ClickGuiScreen extends Screen {
    private final List<GuiPanel> panels = new ArrayList<>();
    private final GuiBackground guiBackground = new GuiBackground();
    /** UI 整体缩放（随窗口比例，基准 1920x1080） */
    private float uiScale = 1f;
    
    public ClickGuiScreen() {
        super(Component.literal("Fku ClickGUI"));
        panels.add(new OtherPanel());
        panels.add(new MovementPanel());
        panels.add(new VisualPanel());
        panels.add(new ToolPanel());
        panels.add(new EntertainmentPanel());
        panels.add(new CombatPanel());
        panels.add(new WorldPanel());
        
        // ★ 错峰：每个面板延迟启动，形成依次弹出的效果
        for (int i = 0; i < panels.size(); i++) {
            panels.get(i).setPanelIndex(i);
        }
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mx, int my, float pt) {
        // 不阻塞输入 — Apple §1: 即时反馈
        // 面板自身管理弹簧动画

        // UI 整体缩放：以真实窗口像素为基准（1920x1080），等比
        // 下限 1.0 保证不缩小（字清晰），上限 4.0 允许大屏放大
        var win = Minecraft.getInstance().getWindow();
        float uiScaleReal = Math.min((float) win.getWidth() / 1920f, (float) win.getHeight() / 1080f);
        this.uiScale = Math.max(1.0f, Math.min(4.0f, uiScaleReal));
        GuiPanel.setUiScale(this.uiScale);

        // 记录当前鼠标坐标，供组件 hover 判定复用（白边选中效果）
        GuiComponent.hoveredMouseX = mx;
        GuiComponent.hoveredMouseY = my;

        // 动态背景层（开启时绘制在面板之下，不拦截交互；背景铺满窗口不缩放）
        GuiStyleConfig cfg = GuiStyleConfig.getInstance();
        if (cfg.backgroundEnabled) {
            guiBackground.setEnabled(true);
            guiBackground.setStyle(cfg.backgroundStyle);
            guiBackground.resize(width, height);
            guiBackground.update();
            guiBackground.render(g);
        }

        // 整体缩放坐标系：面板/组件按窗口比例等比缩放，鼠标坐标反除 uiScale
        float sx = mx / this.uiScale;
        float sy = my / this.uiScale;
        g.pose().pushPose();
        g.pose().scale(this.uiScale, this.uiScale, this.uiScale);

        // 按添加顺序渲染（后面板在上面，但鼠标点击反向遍历）
        for (GuiPanel panel : panels) {
            panel.render(g, (int) sx, (int) sy, pt);
        }

        // ★ 顶部提示：中键绑定热键（置于缩放坐标系内，随 GUI 一起缩放并居中）
        if (!HotkeySystem.isWaiting()) {
            String hint = "§7§o中键点击组件可绑定热键";
            String plain = hint.replace("§7§o", "").replace("§r", "");
            int hw = font.width(plain);
            int padX = 8;
            int designW = (int) ((float) width / this.uiScale);
            GuiRenderHelper.drawRoundedRect(g, (designW - hw) / 2 - padX, 4, hw + padX * 2, 16, (200 << 24) | 0x000000, 6);
            g.drawString(font, hint, (designW - hw) / 2, 8, 0x888888);
        }

        g.pose().popPose();

        // ★ 灵动岛 HUD（始终置顶绘制，不随 GUI 缩放）
        DynamicIslandHud.renderNow(g);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // 不检查 animationComplete — Apple §1: 即时响应
        // 鼠标坐标反除 uiScale，映射到缩放坐标系内的面板逻辑
        double cx = mouseX / this.uiScale;
        double cy = mouseY / this.uiScale;
        for (int i = panels.size() - 1; i >= 0; i--) {
            if (panels.get(i).mouseClicked(cx, cy, button)) {
                GuiPanel panel = panels.remove(i);
                panels.add(panel);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        double cx = mouseX / this.uiScale;
        double cy = mouseY / this.uiScale;
        for (GuiPanel panel : panels) panel.mouseDragged(cx, cy, button, dragX / this.uiScale, dragY / this.uiScale);
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        double cx = mouseX / this.uiScale;
        double cy = mouseY / this.uiScale;
        for (GuiPanel panel : panels) panel.mouseReleased(cx, cy, button);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // ESC: 先取消热键绑定，再关闭 GUI
        if (keyCode == 256) {
            if (fku.org.example.fku.util.HotkeySystem.isWaiting()) {
                fku.org.example.fku.util.HotkeySystem.cancelBinding();
                return true;
            }
            this.onClose();
            return true;
        }
        for (GuiPanel panel : panels) {
            if (panel.keyPressed(keyCode, scanCode, modifiers)) return true;
        }
        if (fku.org.example.fku.client.KeyBindings.OPEN_GUI_KEY.matches(keyCode, scanCode)) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() { this.minecraft.setScreen(null); }
    @Override
    public boolean isPauseScreen() { return false; }
}
