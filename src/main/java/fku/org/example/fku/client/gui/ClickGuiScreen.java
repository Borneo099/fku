package fku.org.example.fku.client.gui;

import fku.org.example.fku.config.GuiStyleConfig;
import fku.org.example.fku.client.gui.components.GuiPanel;
import fku.org.example.fku.client.gui.components.OtherPanel;
import fku.org.example.fku.client.gui.components.MovementPanel;
import fku.org.example.fku.client.gui.components.VisualPanel;
import fku.org.example.fku.client.gui.components.ToolPanel;
import fku.org.example.fku.client.gui.components.EntertainmentPanel;
import fku.org.example.fku.client.gui.components.CombatPanel;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 主GUI界面
 * 支持打开动画、毛玻璃背景
 */
public class ClickGuiScreen extends Screen {
    private final List<GuiPanel> panels = new ArrayList<>();
    
    // 打开动画相关
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

    /**
     * 更新打开动画
     */
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
        
        if (openAnimationProgress >= 1f) {
            animationComplete = true;
        }
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 更新动画
        updateOpenAnimation();
        
        // 根据动画进度调整面板透明度
        float alpha = animationComplete ? 1f : openAnimationProgress;
        
        // 渲染面板
        for (GuiPanel panel : panels) {
            panel.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!animationComplete) return false;
        
        // Reverse order for clicking (topmost panel first)
        for (int i = panels.size() - 1; i >= 0; i--) {
            if (panels.get(i).mouseClicked(mouseX, mouseY, button)) {
                // Move clicked panel to the end of the list (bring to front)
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
        
        for (GuiPanel panel : panels) {
            panel.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (!animationComplete) return false;
        
        for (GuiPanel panel : panels) {
            panel.mouseReleased(mouseX, mouseY, button);
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!animationComplete) return false;
        
        for (GuiPanel panel : panels) {
            if (panel.keyPressed(keyCode, scanCode, modifiers)) return true;
        }
        if (keyCode == 256 || fku.org.example.fku.client.KeyBindings.OPEN_GUI_KEY.matches(keyCode, scanCode)) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(null);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}