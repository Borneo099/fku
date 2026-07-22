package fku.org.example.fku.client.gui.components;

import fku.org.example.fku.client.gui.GuiRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class ColorWheelPicker {
    private static final int WHEEL_RADIUS = 60;
    private static final int WHEEL_DIAMETER = 120;
    private int centerX;
    private int centerY;
    private float hue = 0.0f;
    private float saturation = 1.0f;
    private float value = 1.0f;
    private boolean isOpen = false;
    private OnColorChangedListener listener;
    private String hexColor = "88CCFF";

    public ColorWheelPicker(String initialHex, OnColorChangedListener listener) {
        this.hexColor = initialHex;
        this.listener = listener;
        float[] hsv = ColorWheelPicker.hexToHsv(initialHex);
        this.hue = hsv[0];
        this.saturation = hsv[1];
        this.value = hsv[2];
    }

    public void setColor(String hex) {
        this.hexColor = hex;
        float[] hsv = ColorWheelPicker.hexToHsv(hex);
        this.hue = hsv[0];
        this.saturation = hsv[1];
        this.value = hsv[2];
    }

    public void open(int x, int y) {
        this.centerX = x;
        this.centerY = y;
        this.isOpen = true;
    }

    public void close() {
        this.isOpen = false;
    }

    public boolean isOpen() {
        return this.isOpen;
    }

    public String getHexColor() {
        return this.hexColor;
    }

    public void render(GuiGraphics g, int mouseX, int mouseY) {
        if (!this.isOpen) {
            return;
        }
        int px = this.centerX - 60 - 8;
        int py = this.centerY - 60 - 8;
        int size = 136;
        GuiRenderHelper.drawPanelBackground(g, px, py, size, size + 40, false);
        this.drawFullColorWheel(g);
        this.drawBrightnessBar(g);
        String hex = "#" + this.hexColor.toUpperCase();
        g.drawString(Minecraft.getInstance().font, hex, px + 5, py + size + 12, 0xFFFFFF);
        GuiRenderHelper.drawRoundedRect(g, px + size - 30, py + size + 8, 24, 16, ColorWheelPicker.hexToInt(this.hexColor), 3);
    }

    private void drawFullColorWheel(GuiGraphics g) {
        int cx = this.centerX;
        int cy = this.centerY;
        int r2 = 3600;
        for (int dx = -60; dx <= 60; ++dx) {
            for (int dy = -60; dy <= 60; ++dy) {
                int dist2 = dx * dx + dy * dy;
                if (dist2 > r2) continue;
                float dist = (float)(Math.sqrt(dist2) / 60.0f);
                float angle = (float)Math.atan2(dy, dx);
                if (angle < 0.0f) {
                    angle = (float)(angle + Math.PI * 2);
                }
                float h = (float)(angle / (Math.PI * 2));
                float s = dist;
                float v = this.value;
                int rgb = ColorWheelPicker.hsvToInt(h, s, v);
                g.fill(cx + dx, cy + dy, cx + dx + 1, cy + dy + 1, rgb);
            }
        }
    }

    private void drawBrightnessBar(GuiGraphics g) {
        int barX = this.centerX - 60;
        int barY = this.centerY + 60 + 10;
        int barW = 120;
        int barH = 12;
        for (int i = 0; i < barW; ++i) {
            float t = (float)i / barW;
            int gray = (int)(t * 255.0f);
            int color = 0xFF000000 | gray << 16 | gray << 8 | gray;
            g.fill(barX + i, barY, barX + i + 1, barY + barH, color);
        }
        int indX = barX + (int)(this.value * barW);
        g.fill(indX - 2, barY - 1, indX + 3, barY + barH + 1, -1);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.isOpen) {
            return false;
        }
        int px = this.centerX - 60 - 8;
        int py = this.centerY - 60 - 8;
        int size = 136;
        if (mouseX < px || mouseX > (px + size) || mouseY < py || mouseY > (py + size + 40)) {
            this.close();
            return true;
        }
        double dx = mouseX - this.centerX;
        double dy = mouseY - this.centerY;
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist <= 60.0) {
            float angle = (float)Math.atan2(dy, dx);
            if (angle < 0.0f) {
                angle = (float)(angle + Math.PI * 2);
            }
            this.hue = (float)(angle / (Math.PI * 2));
            this.saturation = (float)Math.min(dist / 60.0, 1.0);
            this.updateHex();
            return true;
        }
        int barX = this.centerX - 60;
        int barY = this.centerY + 60 + 10;
        int barW = 120;
        if (mouseY >= barY && mouseY < (barY + 12) && mouseX >= barX && mouseX < (barX + barW)) {
            this.value = (float)((mouseX - barX) / barW);
            this.updateHex();
            return true;
        }
        return false;
    }

    private void updateHex() {
        int rgb = ColorWheelPicker.hsvToInt(this.hue, this.saturation, this.value);
        this.hexColor = String.format("%02X%02X%02X", rgb >> 16 & 0xFF, rgb >> 8 & 0xFF, rgb & 0xFF);
        if (this.listener != null) {
            this.listener.onColorChanged(this.hexColor);
        }
    }

    private static int hsvToInt(float h, float s, float v) {
        float g;
        float r;
        int i = (int)(h * 6.0f);
        float f = h * 6.0f - i;
        float p = v * (1.0f - s);
        float q = v * (1.0f - f * s);
        float t = v * (1.0f - (1.0f - f) * s);
        return 0xFF000000 | (int)(r * 255.0f) << 16 | (int)(g * 255.0f) << 8 | (int)((switch (i % 6) {
            case 0 -> {
                r = v;
                g = t;
                yield p;
            }
            case 1 -> {
                r = q;
                g = v;
                yield p;
            }
            case 2 -> {
                r = p;
                g = v;
                yield t;
            }
            case 3 -> {
                r = p;
                g = q;
                yield v;
            }
            case 4 -> {
                r = t;
                g = p;
                yield v;
            }
            default -> {
                r = v;
                g = p;
                yield q;
            }
        }) * 255.0f);
    }

    private static float[] hexToHsv(String hex) {
        if (hex == null || hex.length() < 6) {
            return new float[]{0.0f, 0.8f, 0.8f};
        }
        try {
            float s;
            int r = Integer.parseInt(hex.substring(0, 2), 16);
            int g = Integer.parseInt(hex.substring(2, 4), 16);
            int b = Integer.parseInt(hex.substring(4, 6), 16);
            float rf = r / 255.0f;
            float gf = g / 255.0f;
            float bf = b / 255.0f;
            float max = Math.max(rf, Math.max(gf, bf));
            float min = Math.min(rf, Math.min(gf, bf));
            float v = max;
            float d = max - min;
            float f = s = max == 0.0f ? 0.0f : d / max;
            float h = max == min ? 0.0f : (max == rf ? ((gf - bf) / d + (gf < bf ? 6 : 0)) / 6.0f : (max == gf ? ((bf - rf) / d + 2.0f) / 6.0f : ((rf - gf) / d + 4.0f) / 6.0f));
            return new float[]{h, s, v};
        }
        catch (Exception e) {
            return new float[]{0.0f, 0.8f, 0.8f};
        }
    }

    private static int hexToInt(String hex) {
        try {
            int r = Integer.parseInt(hex.substring(0, 2), 16);
            int g = Integer.parseInt(hex.substring(2, 4), 16);
            int b = Integer.parseInt(hex.substring(4, 6), 16);
            return 0xFF000000 | r << 16 | g << 8 | b;
        }
        catch (Exception e) {
            return -7811841;
        }
    }

    public static interface OnColorChangedListener {
        public void onColorChanged(String var1);
    }
}

