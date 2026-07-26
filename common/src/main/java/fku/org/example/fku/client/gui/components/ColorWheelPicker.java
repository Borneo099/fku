package fku.org.example.fku.client.gui.components;

import fku.org.example.fku.client.gui.GuiRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import java.awt.Color;

/**
 * 彩色圆盘选择器 — 完整的HSV彩色圆盘（饱和度和色相二维渐变）
 *
 * ★ 工作原理：
 *   圆盘中心 = 白色（饱和度S=0），圆盘边缘 = 纯色（饱和度S=1）
 *   色相H沿圆周变化（0°~360°），0°=右=红，90°=下=黄绿，180°=左=青，270°=上=紫
 *   亮度V通过下方滑块控制
 *   使用java.awt.Color的HSB转换确保颜色准确性
 *
 * ★ 选中指示器（白色圆点标记当前颜色在圆盘上的位置）
 *   该颜色选择器由赛博教员实现
 */
public class ColorWheelPicker {
    private static final int WHEEL_RADIUS = 80;
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

        // ★ 2. 绘制选中指示器 — 白色圆点标记当前颜色在圆盘上的位置
        drawSelectionIndicator(g);

        // 3. 绘制亮度条（下方）
        drawBrightnessBar(g);

        // 4. 绘制HEX值和当前颜色预览
        String hex = "#" + hexColor.toUpperCase();
        g.drawString(Minecraft.getInstance().font, hex, px + 5, py + size + 12, 0xFFFFFF);
        // 当前颜色小方块
        int previewColor = hexToInt(hexColor);
        GuiRenderHelper.drawRoundedRect(g, px + size - 30, py + size + 8, 24, 16, previewColor, 3);
        GuiRenderHelper.drawRoundedOutline(g, px + size - 30, py + size + 8, 24, 16, 0xFF888888, 3, 1);
    }

    /** 绘制完整的HSV彩色圆盘 — 逐像素绘制 */
    private void drawFullColorWheel(GuiGraphics g) {
        int cx = centerX, cy = centerY;
        int r2 = WHEEL_RADIUS * WHEEL_RADIUS;

        // 遍历圆盘外接正方形区域
        for (int dx = -WHEEL_RADIUS; dx <= WHEEL_RADIUS; dx++) {
            for (int dy = -WHEEL_RADIUS; dy <= WHEEL_RADIUS; dy++) {
                int dist2 = dx * dx + dy * dy;
                if (dist2 > r2) continue;

                double dist = Math.sqrt(dist2);
                double s = dist / WHEEL_RADIUS;
                // 使用atan2: 角度=0在右侧(正x轴)，逆时针增加
                // 屏幕坐标y向下为正，所以atan2(dy,dx)在标准数学中：
                // 右(1,0)=0, 下(0,1)=π/2, 左(-1,0)=π, 上(0,-1)=3π/2
                double angle = Math.atan2(dy, dx);
                if (angle < 0) angle += Math.PI * 2;
                double h = angle / (Math.PI * 2);

                // 使用java.awt.Color的HSB转换确保准确性
                int rgb = Color.HSBtoRGB((float)h, (float)Math.min(s, 1.0), this.value);
                g.fill(cx + dx, cy + dy, cx + dx + 1, cy + dy + 1, rgb);
            }
        }
    }

    /** ★ 绘制选中指示器 — 白色圆点标记当前色相/饱和度位置 */
    private void drawSelectionIndicator(GuiGraphics g) {
        double angle = hue * Math.PI * 2;
        double dist = saturation * WHEEL_RADIUS;
        int sx = centerX + (int)(Math.cos(angle) * dist);
        int sy = centerY + (int)(Math.sin(angle) * dist);

        int ringRadius = Math.max(3, (int)(4 * (0.5 + 0.5 * saturation)));
        // 外圈白色圆环
        for (int dx = -ringRadius; dx <= ringRadius; dx++) {
            for (int dy = -ringRadius; dy <= ringRadius; dy++) {
                int d2 = dx * dx + dy * dy;
                int r2 = ringRadius * ringRadius;
                int innerR2 = (ringRadius - 2) * (ringRadius - 2);
                if (d2 <= r2 && d2 >= innerR2) {
                    g.fill(sx + dx, sy + dy, sx + dx + 1, sy + dy + 1, 0xFFFFFFFF);
                }
            }
        }
    }

    /** 亮度条（下方） */
    private void drawBrightnessBar(GuiGraphics g) {
        int barX = centerX - WHEEL_RADIUS;
        int barY = centerY + WHEEL_RADIUS + 10;
        int barW = WHEEL_DIAMETER;
        int barH = 12;

        for (int i = 0; i < barW; i++) {
            float t = (float) i / barW;
            int rgb = Color.HSBtoRGB(hue, saturation, t);
            g.fill(barX + i, barY, barX + i + 1, barY + barH, rgb);
        }
        // 亮度指示器
        int indX = barX + (int)(this.value * barW);
        g.fill(indX - 1, barY - 1, indX + 2, barY + barH + 1, 0xFFFFFFFF);
        g.fill(indX - 2, barY - 1, indX - 1, barY + barH + 1, 0xFF000000);
        g.fill(indX + 2, barY - 1, indX + 3, barY + barH + 1, 0xFF000000);
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
            double angle = Math.atan2(dy, dx);
            if (angle < 0) angle += Math.PI * 2;
            this.hue = (float)(angle / (Math.PI * 2));
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
            this.value = Math.max(0.0f, Math.min(1.0f, this.value));
            updateHex();
            return true;
        }

        return false;
    }

    private void updateHex() {
        // 使用java.awt.Color.HSBtoRGB确保颜色转换准确
        int rgb = Color.HSBtoRGB(hue, saturation, value);
        this.hexColor = String.format("%02X%02X%02X", (rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF);
        if (listener != null) listener.onColorChanged(hexColor);
    }

    // ═══════ HSV ↔ RGB/HEX 工具方法 ═══════

    /** 使用java.awt.Color的HSB转换 */
    private static float[] hexToHsv(String hex) {
        if (hex == null || hex.length() < 6) return new float[]{0f, 0.8f, 0.8f};
        try {
            int r = Integer.parseInt(hex.substring(0, 2), 16);
            int g = Integer.parseInt(hex.substring(2, 4), 16);
            int b = Integer.parseInt(hex.substring(4, 6), 16);
            return Color.RGBtoHSB(r, g, b, null);
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