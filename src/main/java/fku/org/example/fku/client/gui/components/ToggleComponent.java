package fku.org.example.fku.client.gui.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import java.awt.*;

public abstract class ToggleComponent extends GuiComponent {

    protected String label;

    public ToggleComponent(int x, int y, int width, int height, String label) {
        super(x, y, width, height, label);
        this.label = label;
    }

    // 子类实现：返回当前开关状态
    protected abstract boolean isEnabled();

    // 子类实现：切换开关状态
    protected abstract void toggle();

    // 子类实现：保存配置
    protected abstract void saveConfig();

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!visible) return;
        boolean enabled = isEnabled();

        // Background color based on state
        int bgColor = enabled ? new Color(0, 150, 0, 200).getRGB() : new Color(150, 0, 0, 200).getRGB();
        guiGraphics.fill(x, y, x + width, y + height, bgColor);
        guiGraphics.renderOutline(x, y, width, height, new Color(200, 200, 200).getRGB());

        // Text
        String displayStr = label + ": " + (enabled ? "ON" : "OFF");
        guiGraphics.drawString(Minecraft.getInstance().font, displayStr, x + 5, y + (height - 8) / 2, 0xFFFFFF);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY) && button == 0) {
            toggle();
            saveConfig();
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false; // Not used for toggle components
    }
}