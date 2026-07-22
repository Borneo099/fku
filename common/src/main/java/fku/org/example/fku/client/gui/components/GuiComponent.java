package fku.org.example.fku.client.gui.components;

import com.mojang.blaze3d.platform.InputConstants;
import fku.org.example.fku.client.KeyBindings;
import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.config.GuiStyleConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class GuiComponent {
    protected int x;
    protected int y;
    protected int width;
    protected int height;
    protected String text;
    protected boolean visible = true;
    public boolean listeningForKey = false;
    protected boolean hovered = false;
    protected boolean pressed = false;
    protected float currentAlpha = 1.0f;

    public GuiComponent(int x, int y, int width, int height, String text) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.text = text;
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!this.visible || this.currentAlpha <= 0.01f) {
            return;
        }
        GuiStyleConfig config = GuiStyleConfig.getInstance();
        this.hovered = this.isHovered(mouseX, mouseY);
        int drawX = this.x;
        int drawY = this.y;
        int drawW = this.width;
        int drawH = this.height;
        if (this.hovered && this.currentAlpha > 0.5f) {
            int expand = 2;
            drawX -= expand;
            drawY -= expand;
            drawW += expand * 2;
            drawH += expand * 2;
        }
        int bgAlpha = (int)(180.0f * this.currentAlpha);
        int bgColor = this.hovered ? config.getPrimaryColorWithAlpha(bgAlpha) : config.getBackgroundColorWithAlpha(bgAlpha);
        GuiRenderHelper.drawRoundedRect(guiGraphics, drawX, drawY, drawW, drawH, bgColor, Math.max(2, config.cornerRadius / 2));
        if (this.hovered && this.currentAlpha > 0.5f) {
            int borderAlpha = (int)(200.0f * this.currentAlpha);
            GuiRenderHelper.drawRoundedOutline(guiGraphics, drawX, drawY, drawW, drawH, config.getPrimaryColorWithAlpha(borderAlpha), Math.max(2, config.cornerRadius / 2), 1);
        }
        int textAlpha = (int)(255.0f * this.currentAlpha);
        int textColor = textAlpha << 24 | config.getTextColor() & 0xFFFFFF;
        guiGraphics.drawString(Minecraft.getInstance().font, this.text, drawX + 5, drawY + (drawH - 8) / 2, textColor);
    }

    public void renderWithAlpha(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, float alpha) {
        this.currentAlpha = alpha;
        this.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isHovered(mouseX, mouseY) && button == 0) {
            this.pressed = true;
            this.listeningForKey = true;
            this.text = "\u8bf7\u6309\u4e0b\u6309\u952e.";
            return true;
        }
        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.listeningForKey) {
            InputConstants.Key newKey = InputConstants.getKey(keyCode, scanCode);
            KeyBindings.updateKeyBinding(newKey);
            String keyDisplay = newKey.getName();
            this.text = "\u7ed1\u5b9aGUI\u6309\u952e: " + keyDisplay;
            this.listeningForKey = false;
            this.pressed = false;
            return true;
        }
        return false;
    }

    protected boolean isHovered(double mouseX, double mouseY) {
        return mouseX >= this.x && mouseX <= (this.x + this.width) && mouseY >= this.y && mouseY <= (this.y + this.height);
    }

    public void updatePosition(int panelX, int panelY, int yOffset) {
        GuiStyleConfig config = GuiStyleConfig.getInstance();
        this.x = panelX + 5;
        this.y = panelY + yOffset;
        this.width = config.panelWidth - 10;
        this.height = config.componentHeight;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}

