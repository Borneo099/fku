package fku.org.example.fku.features.skija;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Skija 圆角渲染（阶段2）公开网关 —— 不含任何 io.github.humbleui.skija.* 引用，
 * 真正渲染在 SkijaRoundRectBackend 里，仅当 Skija 可加载时才通过反射调用。
 */
public class SkijaRoundRect {
    public static void drawRoundedRect(GuiGraphics g, int x, int y, int w, int h, int color, int radius) {
        if (!SkijaRenderer.isAvailable() || !SkijaConfig.getInstance().enableRoundedCorners || !SkijaReflect.isLoadable()) {
            g.fill(x, y, x + w, y + h, color);
            return;
        }
        try {
            SkijaReflect.call("fku.org.example.fku.features.skija.SkijaRoundRectBackend", "drawRoundedRect",
                    new Class[]{GuiGraphics.class, int.class, int.class, int.class, int.class, int.class, int.class},
                    g, x, y, w, h, color, radius);
        } catch (Throwable t) {
            SkijaRenderer.markUnavailable();
            g.fill(x, y, x + w, y + h, color);
        }
    }

    public static void drawRoundedOutline(GuiGraphics g, int x, int y, int w, int h, int color, int radius, int thickness) {
        if (!SkijaRenderer.isAvailable() || !SkijaConfig.getInstance().enableRoundedCorners || !SkijaReflect.isLoadable()) {
            g.fill(x, y, x + w, y + h, color);
            return;
        }
        try {
            SkijaReflect.call("fku.org.example.fku.features.skija.SkijaRoundRectBackend", "drawRoundedOutline",
                    new Class[]{GuiGraphics.class, int.class, int.class, int.class, int.class, int.class, int.class, int.class},
                    g, x, y, w, h, color, radius, thickness);
        } catch (Throwable t) {
            SkijaRenderer.markUnavailable();
            g.fill(x, y, x + w, y + h, color);
        }
    }

    public static void clearCache() {
        try {
            SkijaReflect.call("fku.org.example.fku.features.skija.SkijaRoundRectBackend", "clearCache", new Class[0]);
        } catch (Throwable ignore) { /* 忽略 */ }
    }
}
