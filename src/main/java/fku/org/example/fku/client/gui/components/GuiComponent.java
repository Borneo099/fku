package fku.org.example.fku.client.gui.components;

import fku.org.example.fku.client.KeyBindings;
import fku.org.example.fku.config.FkuConfig;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.awt.*;

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
        guiGraphics.fill(x, y, x + width, y + height, new Color(60, 60, 60, 200).getRGB());
        guiGraphics.renderOutline(x, y, width, height, new Color(150, 150, 150).getRGB());
        guiGraphics.drawString(Minecraft.getInstance().font, text, x + 5, y + (height - 8) / 2, 0xFFFFFF);
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
            InputConstants.Key newKey = InputConstants.getKey(keyCode, scanCode);
            KeyBindings.updateKeyBinding(newKey);
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

    public void updatePosition(int panelX, int panelY, int index) {
        this.x = panelX + 5;
        this.y = panelY + 25 + (index * (this.height + 5));
    }
}