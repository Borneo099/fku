package fku.org.example.fku.client.gui.components;

import fku.org.example.fku.config.GuiStyleConfig;
import fku.org.example.fku.client.gui.GuiRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/**
 * 开关组件基类
 * 支持圆角、美化效果
 */
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
        
        GuiStyleConfig config = GuiStyleConfig.getInstance();
        boolean enabled = isEnabled();

        // 绘制圆角背景
        GuiRenderHelper.drawComponentBackground(guiGraphics, x, y, width, height, enabled);

        // 绘制文字
        String displayStr = label + ": " + (enabled ? "ON" : "OFF");
        int textColor = enabled ? config.getTextColor() : (0xAAAAAA);
        guiGraphics.drawString(Minecraft.getInstance().font, displayStr, x + 5, y + (height - 8) / 2, textColor);
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
        return false; // 开关组件不处理按键
    }
}