package fku.org.example.fku.client.gui;

import fku.org.example.fku.config.GuiStyleConfig;
import fku.org.example.fku.client.gui.components.GuiPanel;
import fku.org.example.fku.client.gui.components.OtherPanel;
import fku.org.example.fku.client.gui.components.MovementPanel;
import fku.org.example.fku.client.gui.components.VisualPanel;
import fku.org.example.fku.client.gui.components.ToolPanel;
import fku.org.example.fku.client.gui.components.EntertainmentPanel;
import fku.org.example.fku.client.gui.components.CombatPanel;
import fku.org.example.fku.util.HotkeySystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 主GUI界面 — 顶部提示中键可绑定热键
 */
public class ClickGuiScreen extends Screen {
    private final List<GuiPanel> panels = new ArrayList<>();
    
    private float openAnimationProgress = 0f;
    private long openAnimationStartTime = 0;
    private boolean animationComplete = false;

    public ClickGuiScreen() {
        super(Component.literal("Fku ClickGUI"));
        panels.add(new OtherPanel());
        panels.add(new MovementPanel());
        panels.add(new VisualPanel());
        panels.add(new ToolPanel());
        panels.add(new EntertainmentPanel());
        panels.add(new CombatPanel());
        
        openAnimationStartTime = System.currentTimeMillis();
    }

    private void updateOpenAnimation() {
        GuiStyleConfig config = GuiStyleConfig.getInstance();
        if (!config.animationEnabled) {
            openAnimationProgress = 1f;
            animationComplete = true;
            return;
        }
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - openAnimationStartTime;
        openAnimationProgress = Math.min(1f, elapsed / (float) config.animationSpeed);
        if (openAnimationProgress >= 1f) animationComplete = true;
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mx, int my, float pt) {
        updateOpenAnimation();

        // 渲染面板
        for (GuiPanel panel : panels) {
            panel.render(g, mx, my, pt);
        }

        // ★ 顶部提示：中键绑定热键
        if (!HotkeySystem.isWaiting()) {
            String hint = "§7§o中键点击组件可绑定热键";
            int hw = font.width(hint.replace("§7§o", "").replace("§r", ""));
            g.drawString(font, hint, (width - hw) / 2, 8, 0x888888);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!animationComplete) return false;
        
        for (int i = panels.size() - 1; i >= 0; i--) {
            if (panels.get(i).mouseClicked(mouseX, mouseY, button)) {
                GuiPanel panel = panels.remove(i);
                panels.add(panel);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (!animationComplete) return false;
        for (GuiPanel panel : panels) panel.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (!animationComplete) return false;
        for (GuiPanel panel : panels) panel.mouseReleased(mouseX, mouseY, button);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!animationComplete) return false;
        // ★ ESC: 先取消热键绑定，再关闭 GUI
        if (keyCode == 256) {
            if (fku.org.example.fku.util.HotkeySystem.isWaiting()) {
                fku.org.example.fku.util.HotkeySystem.cancelBinding();
                return true; // 消耗事件，不关闭 GUI
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
