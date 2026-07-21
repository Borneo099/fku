package fku.org.example.fku.client.gui;

import fku.org.example.fku.config.GuiStyleConfig;
import net.minecraft.client.gui.GuiGraphics;

public class GuiRenderHelper {
    public static void drawRoundedRect(GuiGraphics guiGraphics, int x, int y, int width, int height, int color, int radius) {
        if (radius <= 0 || width <= 0 || height <= 0) {
            if (width > 0 && height > 0) {
                guiGraphics.m_280509_(x, y, x + width, y + height, color);
            }
            return;
        }
        if ((radius = Math.min(radius, Math.min(width / 2, height / 2))) <= 0) {
            guiGraphics.m_280509_(x, y, x + width, y + height, color);
            return;
        }
        guiGraphics.m_280509_(x + radius, y, x + width - radius, y + height, color);
        guiGraphics.m_280509_(x, y + radius, x + radius, y + height - radius, color);
        guiGraphics.m_280509_(x + width - radius, y + radius, x + width, y + height - radius, color);
        int step = Math.max(1, radius / 3);
        for (int i = 0; i <= radius; i += step) {
            guiGraphics.m_280509_(x + i, y + i, x + radius, y + radius, color);
            guiGraphics.m_280509_(x + width - radius, y + i, x + width - i, y + radius, color);
            guiGraphics.m_280509_(x + i, y + height - radius, x + radius, y + height - i, color);
            guiGraphics.m_280509_(x + width - radius, y + height - radius, x + width - i, y + height - i, color);
        }
    }

    public static void drawRoundedOutline(GuiGraphics guiGraphics, int x, int y, int width, int height, int color, int radius, int borderWidth) {
        int r;
        if (width <= 0 || height <= 0) {
            return;
        }
        if (radius <= 0) {
            guiGraphics.m_280637_(x, y, width, height, color);
            return;
        }
        if ((radius = Math.min(radius, Math.min(width / 2, height / 2))) <= 0) {
            guiGraphics.m_280637_(x, y, width, height, color);
            return;
        }
        guiGraphics.m_280509_(x + radius, y, x + width - radius, y + borderWidth, color);
        guiGraphics.m_280509_(x + radius, y + height - borderWidth, x + width - radius, y + height, color);
        guiGraphics.m_280509_(x, y + radius, x + borderWidth, y + height - radius, color);
        guiGraphics.m_280509_(x + width - borderWidth, y + radius, x + width, y + height - radius, color);
        for (int i = 0; i < borderWidth && (r = radius - i) > 0; ++i) {
            guiGraphics.m_280509_(x + i, y + i, x + borderWidth, y + borderWidth, color);
            guiGraphics.m_280509_(x + width - borderWidth, y + i, x + width - i, y + borderWidth, color);
            guiGraphics.m_280509_(x + i, y + height - borderWidth, x + borderWidth, y + height - i, color);
            guiGraphics.m_280509_(x + width - borderWidth, y + height - borderWidth, x + width - i, y + height - i, color);
        }
    }

    public static void drawSoftShadow(GuiGraphics guiGraphics, int x, int y, int width, int height, float alpha) {
        GuiStyleConfig config = GuiStyleConfig.getInstance();
        if (!config.shadowEnabled || alpha <= 0.0f) {
            return;
        }
        int shadowStrength = config.shadowStrength;
        int shadowSize = Math.max(4, shadowStrength / 8);
        int baseAlpha = (shadowStrength * alpha * 0.6f);
        for (int i = 0; i < shadowSize; ++i) {
            int layerAlpha = (baseAlpha * (1.0 - i / shadowSize) * 0.7f);
            if (layerAlpha <= 0) continue;
            int sc = layerAlpha << 24 | 0;
            int inset = i;
            guiGraphics.m_280509_(x + inset - 2, y + height + inset, x + width - inset + 2, y + height + inset + 2, sc);
            guiGraphics.m_280509_(x + width + inset, y + inset + 2, x + width + inset + 2, y + height - inset, sc);
        }
    }

    public static void drawGlowBorder(GuiGraphics guiGraphics, int x, int y, int width, int height, int radius, float alpha) {
        int baseAlpha = (180.0f * alpha);
        if (baseAlpha <= 0) {
            return;
        }
        int topColor = baseAlpha << 24 | 0xFFFFFF;
        guiGraphics.m_280509_(x + radius, y, x + width - radius, y + 1, topColor);
        int bottomColor = baseAlpha << 24 | 0;
        guiGraphics.m_280509_(x + radius, y + height - 1, x + width - radius, y + height, bottomColor);
    }

    public static void drawPanelBackground(GuiGraphics guiGraphics, int x, int y, int width, int height, boolean isTitleBar, float alpha) {
        GuiStyleConfig config = GuiStyleConfig.getInstance();
        GuiRenderHelper.drawSoftShadow(guiGraphics, x, y, width, height, alpha);
        int adjustedAlpha = (config.backgroundAlpha * alpha);
        adjustedAlpha = Math.max(0, Math.min(255, adjustedAlpha));
        int bgColor = isTitleBar ? config.getPrimaryColorWithAlpha(adjustedAlpha) : config.getBackgroundColorWithAlpha(adjustedAlpha);
        GuiRenderHelper.drawRoundedRect(guiGraphics, x, y, width, height, bgColor, config.cornerRadius);
        int borderColor = isTitleBar ? config.getPrimaryColorWithAlpha(Math.min(255, adjustedAlpha + 40)) : config.getBorderColorWithAlpha((200.0f * alpha));
        GuiRenderHelper.drawRoundedOutline(guiGraphics, x, y, width, height, borderColor, config.cornerRadius, 1);
        if (alpha > 0.5f) {
            int glowAlpha = (80.0f * alpha);
            int topGlow = glowAlpha << 24 | 0xFFFFFF;
            if (config.cornerRadius > 0) {
                guiGraphics.m_280509_(x + config.cornerRadius, y, x + width - config.cornerRadius, y + 1, topGlow);
            }
        }
    }

    public static void drawPanelBackground(GuiGraphics guiGraphics, int x, int y, int width, int height, boolean isTitleBar) {
        GuiRenderHelper.drawPanelBackground(guiGraphics, x, y, width, height, isTitleBar, 1.0f);
    }

    public static void drawComponentBackground(GuiGraphics guiGraphics, int x, int y, int width, int height, boolean enabled) {
        GuiRenderHelper.drawComponentBackground(guiGraphics, x, y, width, height, enabled, 1.0f);
    }

    public static void drawComponentBackground(GuiGraphics guiGraphics, int x, int y, int width, int height, boolean enabled, float alpha) {
        GuiStyleConfig config = GuiStyleConfig.getInstance();
        int adjAlpha = (180.0f * alpha);
        int bgColor = enabled ? config.getEnabledColor() | adjAlpha << 24 : config.getBackgroundColorWithAlpha(adjAlpha);
        int radius = Math.max(2, config.cornerRadius / 2);
        GuiRenderHelper.drawRoundedRect(guiGraphics, x, y, width, height, bgColor, radius);
        if (enabled) {
            int borderColor = config.getEnabledColor() | 0xFF000000;
            GuiRenderHelper.drawRoundedOutline(guiGraphics, x, y, width, height, borderColor, radius, 1);
        }
    }
}

