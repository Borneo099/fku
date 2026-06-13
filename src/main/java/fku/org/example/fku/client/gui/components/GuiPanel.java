package fku.org.example.fku.client.gui.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public abstract class GuiPanel {
    protected String title;
    protected int x, y, width, height;
    protected boolean dragging = false;
    protected int dragOffsetX, dragOffsetY;
    protected boolean expanded = true;
    protected final List<GuiComponent> components = new ArrayList<>();
    protected final Minecraft mc = Minecraft.getInstance();

    public GuiPanel(String title, int x, int y, int width, int height) {
        this.title = title;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        init();
    }

    protected abstract void init();

    protected void addComponent(GuiComponent component) {
        this.components.add(component);
        updatePositions();
    }

    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int alpha = 200;
        guiGraphics.fill(x, y, x + width, y + (expanded ? height : 20), new Color(30, 30, 30, alpha).getRGB());
        guiGraphics.fill(x, y, x + width, y + 20, new Color(0, 102, 204, alpha).getRGB());
        guiGraphics.drawString(mc.font, title, x + 5, y + 6, 0xFFFFFF);
        
        if (expanded) {
            for (GuiComponent component : components) {
                component.render(guiGraphics, mouseX, mouseY, partialTick);
            }
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 20) {
            if (button == 0) {
                dragging = true;
                dragOffsetX = (int) mouseX - x;
                dragOffsetY = (int) mouseY - y;
                return true;
            } else if (button == 1) {
                expanded = !expanded;
                return true;
            }
        }
        if (expanded) {
            for (GuiComponent component : components) {
                if (component.mouseClicked(mouseX, mouseY, button)) return true;
            }
        }
        return false;
    }

    public void mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (dragging) {
            this.x = (int) mouseX - dragOffsetX;
            this.y = (int) mouseY - dragOffsetY;
            clampPosition();
            updatePositions();
            savePosition();
        }
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (dragging) {
            dragging = false;
        }
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (GuiComponent component : components) {
            if (component.keyPressed(keyCode, scanCode, modifiers)) return true;
        }
        return false;
    }

    protected void updatePositions() {
        for (int i = 0; i < components.size(); i++) {
            components.get(i).updatePosition(this.x, this.y, i);
        }
    }

    protected void clampPosition() {
        if (this.x < 0) this.x = 0;
        if (this.y < 0) this.y = 0;
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        if (this.x + this.width > screenWidth) this.x = screenWidth - this.width;
        if (this.y + (expanded ? height : 20) > screenHeight) this.y = screenHeight - (expanded ? height : 20);
    }

    protected abstract void savePosition();
}
