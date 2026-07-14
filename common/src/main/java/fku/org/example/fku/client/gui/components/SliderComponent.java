package fku.org.example.fku.client.gui.components;

import fku.org.example.fku.config.GuiStyleConfig;
import fku.org.example.fku.client.gui.GuiRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/**
 * 滑块组件
 * 用于调整数值参数
 */
public class SliderComponent {
    protected int x, y, width, height;
    protected int minValue, maxValue;
    protected int currentValue;
    protected String label;
    protected boolean dragging = false;
    protected OnValueChangedListener listener;
    
    public interface OnValueChangedListener {
        void onValueChanged(int value);
    }

    public SliderComponent(int x, int y, int width, int height, int minValue, int maxValue, int currentValue, String label, OnValueChangedListener listener) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.currentValue = currentValue;
        this.label = label;
        this.listener = listener;
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        GuiStyleConfig config = GuiStyleConfig.getInstance();
        
        // 绘制标签
        guiGraphics.drawString(Minecraft.getInstance().font, label + ": " + currentValue, x, y - 12, config.getTextColor());
        
        // 绘制背景
        GuiRenderHelper.drawRoundedRect(guiGraphics, x, y, width, height, config.getBackgroundColorWithAlpha(200), 4);
        
        // 计算滑块位置
        float percentage = (currentValue - minValue) / (float) (maxValue - minValue);
        int sliderWidth = (int) (width * percentage);
        
        // 绘制滑块填充
        GuiRenderHelper.drawRoundedRect(guiGraphics, x, y, sliderWidth, height, config.getPrimaryColorWithAlpha(200), 4);
        
        // 绘制滑块指示器
        int indicatorX = x + sliderWidth - 4;
        GuiRenderHelper.drawRoundedRect(guiGraphics, indicatorX, y - 2, 8, height + 4, 0xFFFFFFFF, 2);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height) {
            dragging = true;
            updateValue(mouseX);
            return true;
        }
        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (dragging && button == 0) {
            updateValue(mouseX);
            return true;
        }
        return false;
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (dragging) {
            dragging = false;
        }
    }

    private void updateValue(double mouseX) {
        float percentage = (float) ((mouseX - x) / width);
        percentage = Math.max(0, Math.min(1, percentage));
        currentValue = minValue + (int) (percentage * (maxValue - minValue));
        if (listener != null) {
            listener.onValueChanged(currentValue);
        }
    }

    public void setValue(int value) {
        this.currentValue = Math.max(minValue, Math.min(maxValue, value));
    }

    public int getValue() {
        return currentValue;
    }
}