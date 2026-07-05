package fku.org.example.fku.client.gui.components;

import fku.org.example.fku.client.gui.GuiRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/**
 * 彩色圆盘选择器 — 完整的HSV彩色圆盘（饱和度和色相二维渐变）
 *
 * ★ 工作原理：
 *   圆盘中心 = 白色（饱和度S=0），圆盘边缘 = 纯色（饱和度S=1）
 *   色相H沿圆周变化（0°~360°）
 *   亮度V通过外部滑块控制
 *
 * ★ 绘制方式：
 *   逐像素遍历圆盘区域，根据像素到中心的距离计算饱和度，
 *   根据角度计算色相，HSV→RGB 得到最终颜色
 */
public class ColorWheelPicker {
    private static final int WHEEL_RADIUS = 60;
    private static final int WHEEL_DIAMETER = WHEEL_RADIUS * 2;

    private int centerX, centerY;
    private float hue = 0f;        // 0~1
    private float saturation = 1f; // 0~1
    private float value = 1f;      // 0~1
    private boolean isOpen = false;
    private OnColorChangedListener listener;
    private String hexColor = "88CCFF";

    public interface OnColorChangedListener {
        void onColorChanged(String hexColor);
    }

    public ColorWheelPicker(String initialHex, OnColorChangedListener listener) {
        this.hexColor = initialHex;
        this.listener = listener;
        float[] hsv = hexToHsv(initialHex);
        this.hue = hsv[0];
        this.saturation = hsv[1];
        this.value = hsv[2];
    }

    public void setColor(String hex) {
        this.hexColor = hex;
        float[] hsv = hexToHsv(hex);
        this.hue = hsv[0];
        this.saturation = hsv[1];
        this.value = hsv[2];
    }

    public void open(int x, int y) {
        this.centerX = x;
        this.centerY = y;
        this.isOpen = true;
    }

    public void close() { this.isOpen = false; }
    public boolean isOpen() { return isOpen; }
    public String getHexColor() { return hexColor; }

    public void render(GuiGraphics g, int mouseX, int mouseY) {
        if (!isOpen) return;

        int px = centerX - WHEEL_RADIUS - 8;
        int py = centerY - WHEEL_RADIUS - 8;
        int size = WHEEL_DIAMETER + 16;

        // 背景面板
        GuiRenderHelper.drawPanelBackground(g, px, py, size, size + 40, false);

        // 1. 绘制完整的彩色圆盘（逐像素填充）
        drawFullColorWheel(g);

        // 2. 绘制亮度条（下方）
        drawBrightnessBar(g);

        // 3. 绘制HEX值和当前颜色预览
        String hex = "#" + hexColor.toUpperCase();
        g.drawString(Minecraft.getInstance().font, hex, px + 5, py + size + 12, 0xFFFFFF);
        // 当前颜色小方块
        GuiRenderHelper.drawRoundedRect(g, px + size - 30, py + size + 8, 24, 16, hexToInt(hexColor), 3);
    }

    /** 绘制完整的HSV彩色圆盘 — 逐像素绘制 */
    private void drawFullColorWheel(GuiGraphics g) {
        int cx = centerX, cy = centerY;
        int r2 = WHEEL_RADIUS * WHEEL_RADIUS;

        // 遍历圆盘外接正方形区域
        for (int dx = -WHEEL_RADIUS; dx <= WHEEL_RADIUS; dx++) {
            for (int dy = -WHEEL_RADIUS; dy <= WHEEL_RADIUS; dy++) {
                int dist2 = dx * dx + dy * dy;
                if (dist2 > r2) continue; // 跳过圆盘外部

                float dist = (float) Math.sqrt(dist2) / WHEEL_RADIUS;
                float angle = (float) Math.atan2(dy, dx);
                if (angle < 0) angle += Math.PI * 2;

                float h = angle / (float)(Math.PI * 2);
                float s = dist;              // 中心饱和度0，边缘饱和度1
                float v = this.value;        // 使用当前亮度

                int rgb = hsvToInt(h, s, v);
                g.fill(cx + dx, cy + dy, cx + dx + 1, cy + dy + 1, rgb);
            }
        }
    }

    /** 亮度条（下方） */
    private void drawBrightnessBar(GuiGraphics g) {
        int barX = centerX - WHEEL_RADIUS;
        int barY = centerY + WHEEL_RADIUS + 10;
        int barW = WHEEL_DIAMETER;
        int barH = 12;

        // 绘制渐变亮度条：左黑右白
        for (int i = 0; i < barW; i++) {
            float t = (float) i / barW;
            int gray = (int)(t * 255);
            int color = (255 << 24) | (gray << 16) | (gray << 8) | gray;
            g.fill(barX + i, barY, barX + i + 1, barY + barH, color);
        }
        // 亮度指示器
        int indX = barX + (int)(this.value * barW);
        g.fill(indX - 2, barY - 1, indX + 3, barY + barH + 1, 0xFFFFFFFF);
    }

    /** 鼠标点击处理 */
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isOpen) return false;
        int px = centerX - WHEEL_RADIUS - 8;
        int py = centerY - WHEEL_RADIUS - 8;
        int size = WHEEL_DIAMETER + 16;

        // 点击关闭（点击面板外部）
        if (mouseX < px || mouseX > px + size || mouseY < py || mouseY > py + size + 40) {
            close();
            return true;
        }

        // 点击圆盘
        double dx = mouseX - centerX;
        double dy = mouseY - centerY;
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist <= WHEEL_RADIUS) {
            float angle = (float) Math.atan2(dy, dx);
            if (angle < 0) angle += Math.PI * 2;
            this.hue = angle / (float)(Math.PI * 2);
            this.saturation = (float) Math.min(dist / WHEEL_RADIUS, 1.0);
            updateHex();
            return true;
        }

        // 点击亮度条
        int barX = centerX - WHEEL_RADIUS;
        int barY = centerY + WHEEL_RADIUS + 10;
        int barW = WHEEL_DIAMETER;
        if (mouseY >= barY && mouseY < barY + 12 && mouseX >= barX && mouseX < barX + barW) {
            this.value = (float) ((mouseX - barX) / barW);
            updateHex();
            return true;
        }

        return false;
    }

    private void updateHex() {
        int rgb = hsvToInt(hue, saturation, value);
        this.hexColor = String.format("%02X%02X%02X", (rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF);
        if (listener != null) listener.onColorChanged(hexColor);
    }

    // ═══════ HSV ↔ RGB/HEX 工具方法 ═══════

    private static int hsvToInt(float h, float s, float v) {
        int i = (int)(h * 6);
        float f = h * 6 - i;
        float p = v * (1 - s);
        float q = v * (1 - f * s);
        float t = v * (1 - (1 - f) * s);
        float r, g, b2;
        switch (i % 6) {
            case 0: r=v; g=t; b2=p; break;
            case 1: r=q; g=v; b2=p; break;
            case 2: r=p; g=v; b2=t; break;
            case 3: r=p; g=q; b2=v; break;
            case 4: r=t; g=p; b2=v; break;
            default: r=v; g=p; b2=q; break;
        }
        return (255 << 24) | ((int)(r*255) << 16) | ((int)(g*255) << 8) | (int)(b2*255);
    }

    private static float[] hexToHsv(String hex) {
        if (hex == null || hex.length() < 6) return new float[]{0f, 0.8f, 0.8f};
        try {
            int r = Integer.parseInt(hex.substring(0, 2), 16);
            int g = Integer.parseInt(hex.substring(2, 4), 16);
            int b = Integer.parseInt(hex.substring(4, 6), 16);
            float rf = r/255f, gf = g/255f, bf = b/255f;
            float max = Math.max(rf, Math.max(gf, bf)), min = Math.min(rf, Math.min(gf, bf));
            float h, s, v = max;
            float d = max - min;
            s = max == 0 ? 0 : d / max;
            if (max == min) h = 0;
            else if (max == rf) h = ((gf-bf)/d + (gf<bf?6:0)) / 6f;
            else if (max == gf) h = ((bf-rf)/d + 2) / 6f;
            else h = ((rf-gf)/d + 4) / 6f;
            return new float[]{h, s, v};
        } catch (Exception e) { return new float[]{0f, 0.8f, 0.8f}; }
    }

    private static int hexToInt(String hex) {
        try {
            int r = Integer.parseInt(hex.substring(0, 2), 16);
            int g = Integer.parseInt(hex.substring(2, 4), 16);
            int b = Integer.parseInt(hex.substring(4, 6), 16);
            return (255 << 24) | (r << 16) | (g << 8) | b;
        } catch (Exception e) { return 0xFF88CCFF; }
    }
}
