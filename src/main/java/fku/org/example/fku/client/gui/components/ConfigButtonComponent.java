package fku.org.example.fku.client.gui.components;

import fku.org.example.fku.config.GuiStyleConfig;
import fku.org.example.fku.client.gui.GuiRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/**
 * 配置按钮组件
 * 用于打开配置界面
 */
public class ConfigButtonComponent extends GuiComponent {
    
    private final String label;
    private final Runnable onClick;

    public ConfigButtonComponent(int x, int y, int width, int height, String label, Runnable onClick) {
        super(x, y, width, height, label);
        this.label = label;
        this.onClick = onClick;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!visible) return;
        
        GuiStyleConfig config = GuiStyleConfig.getInstance();
        
        // 绘制圆角背景（使用主色调）
        int bgColor = config.getPrimaryColorWithAlpha(180);
        GuiRenderHelper.drawRoundedRect(guiGraphics, x, y, width, height, bgColor, Math.max(2, config.cornerRadius / 2));
        
        // 绘制边框
        int borderColor = config.getPrimaryColor() | (255 << 24);
        GuiRenderHelper.drawRoundedOutline(guiGraphics, x, y, width, height, borderColor, Math.max(2, config.cornerRadius / 2), 1);
        
        // 绘制文字
        guiGraphics.drawString(Minecraft.getInstance().font, label, x + 5, y + (height - 8) / 2, config.getTextColor());
        // ★ 右键配置提示
        guiGraphics.drawString(Minecraft.getInstance().font, ">>", x + width - 18, y + (height - 8) / 2, 0x888888);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY) && button == 0) {
            onClick.run();
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }
}