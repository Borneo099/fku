package fku.org.example.fku.client.gui.components;

import fku.org.example.fku.config.GuiStyleConfig;
import fku.org.example.fku.client.gui.GuiRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/**
 * GUI组件基类
 * 支持圆角、美化效果
 */
public class GuiComponent {

    protected int x, y, width, height;
    protected String text;
    protected boolean visible = true;
    public boolean listeningForKey = false;

    public GuiComponent(int x, int y, int width, int height, String text) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.text = text;
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!visible) return;
        
        GuiStyleConfig config = GuiStyleConfig.getInstance();
        
        // 绘制圆角背景
        GuiRenderHelper.drawComponentBackground(guiGraphics, x, y, width, height, false);
        
        // 绘制文字
        guiGraphics.drawString(Minecraft.getInstance().font, text, x + 5, y + (height - 8) / 2, config.getTextColor());
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY) && button == 0) {
            listeningForKey = true;
            this.text = "请按下按键...";
            return true;
        }
        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (listeningForKey) {
            com.mojang.blaze3d.platform.InputConstants.Key newKey = 
                com.mojang.blaze3d.platform.InputConstants.getKey(keyCode, scanCode);
            fku.org.example.fku.client.KeyBindings.updateKeyBinding(newKey);
            String keyDisplay = newKey.getName();
            this.text = "绑定GUI按键: " + keyDisplay;
            listeningForKey = false;
            return true;
        }
        return false;
    }

    protected boolean isHovered(double mouseX, double mouseY) {
        return mouseX >= this.x && mouseX <= this.x + this.width &&
                mouseY >= this.y && mouseY <= this.y + this.height;
    }

    /**
     * 更新组件位置（新版本，支持yOffset参数）
     * @param panelX 面板X坐标
     * @param panelY 面板Y坐标
     * @param yOffset Y偏移量
     */
    public void updatePosition(int panelX, int panelY, int yOffset) {
        GuiStyleConfig config = GuiStyleConfig.getInstance();
        this.x = panelX + 5;
        this.y = panelY + yOffset;
        this.width = config.panelWidth - 10;
        this.height = config.componentHeight;
    }
    
    public boolean isVisible() {
        return visible;
    }
    
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}