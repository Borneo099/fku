package fku.org.example.fku.client.gui.components;

import fku.org.example.fku.config.GuiStyleConfig;
import fku.org.example.fku.client.gui.GuiRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/**
 * GUI组件基类 — 经 Apple Design 原则优化
 * - 悬停缩放反馈（Apple §1: pointer-down 即时反馈）
 * - alpha 支持（通过 currentAlpha 字段让子类 render 方法共享）
 * - 按压状态颜色
 */
public class GuiComponent {

    protected int x, y, width, height;
    protected String text;
    protected boolean visible = true;
    public boolean listeningForKey = false;
    
    // 交互状态
    protected boolean hovered = false;
    protected boolean pressed = false;
    /** ★ 当前渲染透明度（0~1），由 renderWithAlpha 设置，子类 render 中读取 */
    protected float currentAlpha = 1.0f;

    public GuiComponent(int x, int y, int width, int height, String text) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.text = text;
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!visible || currentAlpha <= 0.01f) return;
        
        GuiStyleConfig config = GuiStyleConfig.getInstance();
        hovered = isHovered(mouseX, mouseY);
        
        // 悬停缩放
        int drawX = x;
        int drawY = y;
        int drawW = width;
        int drawH = height;
        if (hovered && currentAlpha > 0.5f) {
            int expand = 2;
            drawX -= expand; drawY -= expand;
            drawW += expand * 2; drawH += expand * 2;
        }
        
        // 背景（带 currentAlpha）
        int bgAlpha = (int)(180 * currentAlpha);
        int bgColor = hovered ? 
            config.getPrimaryColorWithAlpha(bgAlpha) : 
            config.getBackgroundColorWithAlpha(bgAlpha);
        GuiRenderHelper.drawRoundedRect(guiGraphics, drawX, drawY, drawW, drawH, bgColor, Math.max(2, config.cornerRadius / 2));
        
        // 悬停边框
        if (hovered && currentAlpha > 0.5f) {
            int borderAlpha = (int)(200 * currentAlpha);
            GuiRenderHelper.drawRoundedOutline(guiGraphics, drawX, drawY, drawW, drawH, config.getPrimaryColorWithAlpha(borderAlpha), Math.max(2, config.cornerRadius / 2), 1);
        }
        
        // 文字
        int textAlpha = (int)(255 * currentAlpha);
        int textColor = (textAlpha << 24) | (config.getTextColor() & 0xFFFFFF);
        guiGraphics.drawString(Minecraft.getInstance().font, text, drawX + 5, drawY + (drawH - 8) / 2, textColor);
    }

    /**
     * ★ 设置本次渲染透明度后调用 render() —— 子类覆盖 render() 即可自动获得 alpha 支持
     */
    public void renderWithAlpha(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, float alpha) {
        this.currentAlpha = alpha;
        render(guiGraphics, mouseX, mouseY, partialTick);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY) && button == 0) {
            pressed = true;
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
            pressed = false;
            return true;
        }
        return false;
    }

    protected boolean isHovered(double mouseX, double mouseY) {
        return mouseX >= this.x && mouseX <= this.x + this.width &&
                mouseY >= this.y && mouseY <= this.y + this.height;
    }

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