package fku.org.example.fku.client.gui.components;

import fku.org.example.fku.config.GuiStyleConfig;
import fku.org.example.fku.client.gui.GuiRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/**
 * 滑块组件 — 经 Apple Design 原则优化
 * - 弹簧手柄（Apple §4: spring physics）
 * - 圆润指示器
 * - 拖拽速度感知
 */
public class SliderComponent {
    protected int x, y, width, height;
    protected int minValue, maxValue;
    protected int currentValue;
    protected String label;
    protected boolean dragging = false;
    protected OnValueChangedListener listener;
    
    // 弹簧手柄位置（独立于数值变化）
    protected float handlePos;       // 弹簧位置 (0~1)
    protected float handleVel = 0f;
    protected float handleTarget = 0f;
    
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
        this.handleTarget = (currentValue - minValue) / (float) (maxValue - minValue);
        this.handlePos = this.handleTarget;
    }

    /**
     * 更新弹簧手柄
     */
    protected void updateHandle() {
        handleTarget = (currentValue - minValue) / (float) (maxValue - minValue);
        float dt = Math.min(1f / 20f, 0.05f); // ~50ms max
        
        // 临界阻尼弹簧
        float stiffness = 12f;
        float damping = 2f * (float) Math.sqrt(stiffness);
        float disp = handlePos - handleTarget;
        float force = -stiffness * disp;
        float dampForce = -damping * handleVel;
        float accel = force + dampForce;
        handleVel += accel * dt;
        handlePos += handleVel * dt;
        
        if (Math.abs(disp) < 0.005f && Math.abs(handleVel) < 0.01f) {
            handlePos = handleTarget;
            handleVel = 0;
        }
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        GuiStyleConfig config = GuiStyleConfig.getInstance();
        updateHandle();
        
        // 绘制标签
        guiGraphics.drawString(Minecraft.getInstance().font, label + ": " + currentValue, x, y - 12, config.getTextColor());
        
        // 绘制轨道背景
        GuiRenderHelper.drawRoundedRect(guiGraphics, x, y, width, 4, config.getBackgroundColorWithAlpha(200), 2);
        
        // 填充轨（弹簧位置）
        int fillWidth = (int) (width * handlePos);
        if (fillWidth > 0) {
            GuiRenderHelper.drawRoundedRect(guiGraphics, x, y, fillWidth, 4, config.getPrimaryColorWithAlpha(200), 2);
        }
        
        // 弹簧手柄 — 圆形指示器
        int handleX = x + fillWidth - 3;
        if (handleX < x) handleX = x;
        int handleY = y - 3;
        int handleSize = 10;
        GuiRenderHelper.drawRoundedRect(guiGraphics, handleX, handleY, handleSize, handleSize, 0xFFFFFFFF, handleSize / 2);
        
        // 手柄外发光（Apple §12: light-catching）
        GuiRenderHelper.drawRoundedOutline(guiGraphics, handleX - 1, handleY - 1, handleSize + 2, handleSize + 2, config.getPrimaryColorWithAlpha(100), handleSize / 2 + 1, 1);
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