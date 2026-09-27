package fku.org.example.fku.client.gui.components;

import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.features.skija.SkijaColorWheel;
import fku.org.example.fku.features.skija.SkijaConfig;
import fku.org.example.fku.features.skija.SkijaRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;

import java.awt.Color;
import java.util.function.Consumer;

/**
 * 颜色选择器组件（HSV 色轮 + 亮度条）
 * ★ 阶段1：色轮圆盘改由 Skija(GPU) 渲染（见 SkijaColorWheel），消除原版每帧逐像素 g.fill() 卡顿。
 *   面板背景/亮度条/HEX/预览仍用原版（本就不卡）；Skija 不可用时整体回退原版。
 */
public class ColorWheelPicker {
    private int centerX, centerY;
    private final int WHEEL_RADIUS = 80;
    private final int WHEEL_DIAMETER = WHEEL_RADIUS * 2;
    public boolean isOpen = false;

    private float hue = 0.0f;
    private float saturation = 1.0f;
    private float value = 1.0f;

    private String hexColor = "00FF00";
    private Consumer<String> onColorChange = null;
    private final SkijaColorWheel skijaWheel = new SkijaColorWheel();

    public ColorWheelPicker() {
    }

    public ColorWheelPicker(String initialHex, Consumer<String> callback) {
        this.onColorChange = callback;
        setColor(initialHex);
    }

    private void notifyChange() {
        if (onColorChange != null) onColorChange.accept(hexColor);
    }

    public void open(int centerX, int centerY) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.isOpen = true;
    }

    public void close() {
        this.isOpen = false;
        this.skijaWheel.dispose();
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setHsv(float h, float s, float v) {
        this.hue = h;
        this.saturation = s;
        this.value = v;
    }

    public float[] getHsv() {
        return new float[]{hue, saturation, value};
    }

    public void setColor(String hex) {
        if (hex.startsWith("#")) hex = hex.substring(1);
        this.hexColor = hex;
        Color c = hexToInt(hex);
        float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
        this.hue = hsb[0];
        this.saturation = hsb[1];
        this.value = hsb[2];
    }

    public void setHex(String hex) {
        setColor(hex);
        notifyChange();
    }

    public String getHex() {
        return hexColor;
    }

    private boolean isInsideWheel(int mx, int my) {
        int dx = mx - centerX;
        int dy = my - centerY;
        return dx * dx + dy * dy <= WHEEL_RADIUS * WHEEL_RADIUS;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isOpen) return false;
        int px = centerX - WHEEL_RADIUS - 8;
        int py = centerY - WHEEL_RADIUS - 8;
        int size = WHEEL_DIAMETER + 16;
        if (mouseX >= px && mouseX <= px + size && mouseY >= py && mouseY <= py + size + 40) {
            int dx = (int) (mouseX - centerX);
            int dy = (int) (mouseY - centerY);
            double dist = Math.sqrt(dx * dx + dy * dy);
            if (dist <= WHEEL_RADIUS) {
                double angle = Math.atan2(dy, dx);
                if (angle < 0) angle += Math.PI * 2;
                this.hue = (float) (angle / (Math.PI * 2));
                this.saturation = (float) Math.min(dist / WHEEL_RADIUS, 1.0);
                updateHexFromHsv();
                return true;
            }
            int barX = centerX - WHEEL_RADIUS;
            int barY = centerY + WHEEL_RADIUS + 10;
            int barW = WHEEL_DIAMETER;
            if (mouseY >= barY && mouseY < barY + 12 && mouseX >= barX && mouseX < barX + barW) {
                this.value = (float) ((mouseX - barX) / barW);
                this.value = Math.max(0f, Math.min(1f, this.value));
                updateHexFromHsv();
                return true;
            }
            return false;
        } else {
            close();
            return true;
        }
    }

    public boolean mouseDragged(double mouseX, double mouseY) {
        if (!isOpen) return false;
        if (isInsideWheel((int) mouseX, (int) mouseY)) {
            updateFromWheel((int) mouseX, (int) mouseY);
            return true;
        }
        return false;
    }

    private void updateFromWheel(int mx, int my) {
        int dx = mx - centerX;
        int dy = my - centerY;
        double dist = Math.sqrt(dx * dx + dy * dy);
        this.saturation = (float) Math.min(1.0, dist / WHEEL_RADIUS);
        double ang = Math.atan2(dy, dx);
        if (ang < 0) ang += Math.PI * 2;
        this.hue = (float) (ang / (Math.PI * 2));
        updateHexFromHsv();
    }

    private void updateHexFromHsv() {
        Color c = Color.getHSBColor(hue, saturation, value);
        hexColor = String.format("%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
        notifyChange();
    }

    private static Color hexToInt(String hex) {
        if (hex.startsWith("#")) hex = hex.substring(1);
        if (hex.length() != 6) return Color.GREEN;
        try {
            return new Color(
                    Integer.parseInt(hex.substring(0, 2), 16),
                    Integer.parseInt(hex.substring(2, 4), 16),
                    Integer.parseInt(hex.substring(4, 6), 16)
            );
        } catch (Exception e) {
            return Color.GREEN;
        }
    }

    public void render(GuiGraphics g, int mouseX, int mouseY) {
        if (!isOpen) return;
        int px = centerX - WHEEL_RADIUS - 8;
        int py = centerY - WHEEL_RADIUS - 8;
        int size = WHEEL_DIAMETER + 16;

        boolean skija = SkijaRenderer.isAvailable() && SkijaConfig.getInstance().enableColorWheel;

        if (skija) {
            // 原版面板（不卡）+ Skija GPU 色轮圆盘 + 原版亮度条/指示器
            GuiRenderHelper.drawPanelBackground(g, px, py, size, size + 40, false);
            skijaWheel.render(g, centerX, centerY, WHEEL_RADIUS, hue, saturation, value);
            drawBrightnessBar(g);
            drawSelectionIndicator(g);
        } else {
            GuiRenderHelper.drawPanelBackground(g, px, py, size, size + 40, false);
            drawFullColorWheel(g);
            drawSelectionIndicator(g);
            drawBrightnessBar(g);
        }

        String hex = "#" + hexColor.toUpperCase();
        g.drawString(Minecraft.getInstance().font, hex, px + 5, py + size + 12, 0xFFFFFF);
        int previewColor = hexToInt(hexColor).getRGB();
        GuiRenderHelper.drawRoundedRect(g, px + size - 30, py + size + 8, 24, 16, previewColor, 3);
        GuiRenderHelper.drawRoundedOutline(g, px + size - 30, py + size + 8, 24, 16, 0xFF888888, 3, 1);
    }

    private void drawFullColorWheel(GuiGraphics g) {
        for (int y = -WHEEL_RADIUS; y <= WHEEL_RADIUS; y++) {
            for (int x = -WHEEL_RADIUS; x <= WHEEL_RADIUS; x++) {
                int dist = (int) Math.sqrt(x * x + y * y);
                if (dist > WHEEL_RADIUS) continue;
                float hue = (float) ((Math.atan2(y, x) + Math.PI) / (2 * Math.PI));
                float sat = (float) dist / WHEEL_RADIUS;
                Color c = Color.getHSBColor(hue, sat, value);
                g.fill(centerX + x, centerY + y, 1, 1, c.getRGB());
            }
        }
    }

    private void drawSelectionIndicator(GuiGraphics g) {
        double angle = hue * Math.PI * 2;
        int sx = (int) (centerX + Math.cos(angle) * saturation * WHEEL_RADIUS);
        int sy = (int) (centerY + Math.sin(angle) * saturation * WHEEL_RADIUS);
        g.fill(sx - 2, sy - 2, 4, 4, 0xFFFFFFFF);
        g.fill(sx - 3, sy - 1, 1, 2, 0xFF000000);
        g.fill(sx + 2, sy - 1, 1, 2, 0xFF000000);
        g.fill(sx - 1, sy - 3, 2, 1, 0xFF000000);
        g.fill(sx - 1, sy + 2, 2, 1, 0xFF000000);
    }

    private void drawBrightnessBar(GuiGraphics g) {
        int barX = centerX - WHEEL_RADIUS;
        int barY = centerY + WHEEL_RADIUS + 10;
        int barW = WHEEL_DIAMETER;
        int barH = 12;
        Color darkC = Color.getHSBColor(hue, saturation, 0f);
        Color fullC = Color.getHSBColor(hue, saturation, 1f);
        for (int i = 0; i < barW; i++) {
            float t = (float) i / barW;
            Color c = new Color(
                    (int) (darkC.getRed() + (fullC.getRed() - darkC.getRed()) * t),
                    (int) (darkC.getGreen() + (fullC.getGreen() - darkC.getGreen()) * t),
                    (int) (darkC.getBlue() + (fullC.getBlue() - darkC.getBlue()) * t)
            );
            g.fill(barX + i, barY, 1, barH, c.getRGB());
        }
        int indX = (int) (barX + value * barW);
        g.fill(indX - 1, barY - 1, 3, barH + 2, 0xFFFFFFFF);
    }
}
