package fku.org.example.fku.client.gui.components;

import fku.org.example.fku.config.GuiStyleConfig;
import fku.org.example.fku.client.gui.GuiRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/**
 * 颜色轮盘选择器组件
 */
public class ColorWheelPicker {
    private static final int WHEEL_RADIUS = 80;
    private static final int CENTER_RADIUS = 20;
    
    private int x, y;
    private int currentR, currentG, currentB;
    private boolean isOpen = false;
    private OnColorChangedListener listener;
    
    public interface OnColorChangedListener {
        void onColorChanged(int r, int g, int b);
    }

    public ColorWheelPicker(int currentR, int currentG, int currentB, OnColorChangedListener listener) {
        this.currentR = currentR;
        this.currentG = currentG;
        this.currentB = currentB;
        this.listener = listener;
    }

    public void open(int x, int y) {
        this.x = x;
        this.y = y;
        this.isOpen = true;
    }

    public void close() {
        this.isOpen = false;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (!isOpen) return;
        
        GuiStyleConfig config = GuiStyleConfig.getInstance();
        
        // 绘制背景面板
        GuiRenderHelper.drawPanelBackground(guiGraphics, x - WHEEL_RADIUS - 10, y - WHEEL_RADIUS - 10, WHEEL_RADIUS * 2 + 20, WHEEL_RADIUS * 2 + 20, false);
        
        // 绘制颜色轮盘
        drawColorWheel(guiGraphics);
        
        // 绘制中心颜色选择区域
        drawCenterPanel(guiGraphics);
        
        // 绘制当前颜色预览
        drawColorPreview(guiGraphics);
    }

    private void drawColorWheel(GuiGraphics guiGraphics) {
        int centerX = x;
        int centerY = y;
        
        // 绘制颜色轮盘（使用线段绘制）
        for (int angle = 0; angle < 360; angle += 2) {
            double rad = Math.toRadians(angle);
            int px = (int) (centerX + WHEEL_RADIUS * Math.cos(rad));
            int py = (int) (centerY + WHEEL_RADIUS * Math.sin(rad));
            
            // 获取HSV颜色（不使用AWT）
            float[] rgb = hsvToRgb(angle / 360f, 1f, 1f);
            int r = (int) (rgb[0] * 255);
            int g = (int) (rgb[1] * 255);
            int b = (int) (rgb[2] * 255);
            
            // 绘制像素点
            guiGraphics.fill(px - 2, py - 2, px + 2, py + 2, (255 << 24) | (r << 16) | (g << 8) | b);
        }
        
        // 绘制轮盘边框（使用矩形模拟）
        drawCircleOutline(guiGraphics, centerX, centerY, WHEEL_RADIUS, 0xFFFFFFFF);
    }

    private void drawCircleOutline(GuiGraphics guiGraphics, int centerX, int centerY, int radius, int color) {
        // 使用多个线段绘制圆形边框
        for (int angle = 0; angle < 360; angle += 4) {
            double rad = Math.toRadians(angle);
            int px = (int) (centerX + radius * Math.cos(rad));
            int py = (int) (centerY + radius * Math.sin(rad));
            guiGraphics.fill(px, py, px + 1, py + 1, color);
        }
    }

    private void drawCenterPanel(GuiGraphics guiGraphics) {
        int centerX = x;
        int centerY = y;
        
        // 绘制中心圆形区域（黑白渐变）
        for (int i = CENTER_RADIUS; i > 0; i--) {
            float brightness = i / (float) CENTER_RADIUS;
            int gray = (int) (brightness * 255);
            guiGraphics.fill(centerX - i, centerY - i, centerX + i, centerY + i, (255 << 24) | (gray << 16) | (gray << 8) | gray);
        }
        
        // 绘制中心边框
        drawCircleOutline(guiGraphics, centerX, centerY, CENTER_RADIUS, 0xFFFFFFFF);
    }

    private void drawColorPreview(GuiGraphics guiGraphics) {
        int previewX = x + WHEEL_RADIUS + 15;
        int previewY = y - 30;
        
        // 绘制预览框
        GuiRenderHelper.drawRoundedRect(guiGraphics, previewX, previewY, 60, 60, (255 << 24) | (currentR << 16) | (currentG << 8) | currentB, 4);
        GuiRenderHelper.drawRoundedOutline(guiGraphics, previewX, previewY, 60, 60, 0xFFFFFFFF, 4, 1);
        
        // 绘制RGB值
        String rgbStr = String.format("%d, %d, %d", currentR, currentG, currentB);
        guiGraphics.drawString(Minecraft.getInstance().font, rgbStr, previewX, previewY + 70, 0xFFFFFF);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isOpen) return false;
        
        int centerX = x;
        int centerY = y;
        
        // 检查是否点击在轮盘外部（关闭）
        double distToCenter = Math.sqrt(Math.pow(mouseX - centerX, 2) + Math.pow(mouseY - centerY, 2));
        if (distToCenter > WHEEL_RADIUS + 10) {
            close();
            return true;
        }
        
        if (button == 0) {
            // 检查是否点击在中心区域
            if (distToCenter <= CENTER_RADIUS) {
                // 在中心区域，调整亮度
                float brightness = (float) (distToCenter / CENTER_RADIUS);
                adjustBrightness(brightness);
            } else {
                // 在轮盘区域，选择颜色
                double angle = Math.atan2(mouseY - centerY, mouseX - centerX);
                if (angle < 0) angle += Math.PI * 2;
                float hue = (float) (angle / (Math.PI * 2));
                
                // 设置新颜色（不使用AWT）
                float[] rgb = hsvToRgb(hue, 1f, 1f);
                currentR = (int) (rgb[0] * 255);
                currentG = (int) (rgb[1] * 255);
                currentB = (int) (rgb[2] * 255);
                
                notifyListener();
            }
            return true;
        }
        
        return false;
    }

    private void adjustBrightness(float brightness) {
        // 将当前RGB转换为HSV（不使用AWT）
        float[] hsv = rgbToHsv(currentR, currentG, currentB);
        hsv[2] = brightness;
        
        float[] rgb = hsvToRgb(hsv[0], hsv[1], hsv[2]);
        currentR = (int) (rgb[0] * 255);
        currentG = (int) (rgb[1] * 255);
        currentB = (int) (rgb[2] * 255);
        
        notifyListener();
    }

    private void notifyListener() {
        if (listener != null) {
            listener.onColorChanged(currentR, currentG, currentB);
        }
    }

    public void setColor(int r, int g, int b) {
        this.currentR = r;
        this.currentG = g;
        this.currentB = b;
    }
    
    /**
     * HSV转RGB（不使用AWT）
     */
    private float[] hsvToRgb(float h, float s, float v) {
        float r, g, b;
        
        int i = (int) (h * 6);
        float f = h * 6 - i;
        float p = v * (1 - s);
        float q = v * (1 - f * s);
        float t = v * (1 - (1 - f) * s);
        
        switch (i % 6) {
            case 0: r = v; g = t; b = p; break;
            case 1: r = q; g = v; b = p; break;
            case 2: r = p; g = v; b = t; break;
            case 3: r = p; g = q; b = v; break;
            case 4: r = t; g = p; b = v; break;
            default: r = v; g = p; b = q; break;
        }
        
        return new float[]{r, g, b};
    }
    
    /**
     * RGB转HSV（不使用AWT）
     */
    private float[] rgbToHsv(int r, int g, int b) {
        float rf = r / 255f;
        float gf = g / 255f;
        float bf = b / 255f;
        
        float max = Math.max(rf, Math.max(gf, bf));
        float min = Math.min(rf, Math.min(gf, bf));
        float h, s, v = max;
        
        float d = max - min;
        s = max == 0 ? 0 : d / max;
        
        if (max == min) {
            h = 0; // achromatic
        } else {
            if (max == rf) h = ((gf - bf) / d + (gf < bf ? 6 : 0)) / 6;
            else if (max == gf) h = ((bf - rf) / d + 2) / 6;
            else h = ((rf - gf) / d + 4) / 6;
        }
        
        return new float[]{h, s, v};
    }
}