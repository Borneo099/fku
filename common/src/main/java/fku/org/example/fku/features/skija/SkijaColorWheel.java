package fku.org.example.fku.features.skija;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Skija 颜色轮盘（阶段1）公开网关 —— 不含任何 io.github.humbleui.skija.* 引用。
 * 真正渲染在 SkijaColorWheelBackend 实例里，仅当 Skija 可加载时才通过反射实例化并调用。
 */
public class SkijaColorWheel {
    private Object backend;
    private boolean tried;

    private Object backend() {
        if (!tried) {
            tried = true;
            if (SkijaRenderer.isAvailable() && SkijaConfig.getInstance().enableColorWheel && SkijaReflect.isLoadable()) {
                try {
                    backend = Class.forName("fku.org.example.fku.features.skija.SkijaColorWheelBackend")
                            .getDeclaredConstructor().newInstance();
                } catch (Throwable t) {
                    SkijaRenderer.markUnavailable();
                }
            }
        }
        return backend;
    }

    public void render(GuiGraphics g, int centerX, int centerY, int radius, float hue, float sat, float value) {
        Object b = backend();
        if (b == null) return; // 回退：不绘制色轮（由原版面板等兜底）
        try {
            b.getClass().getMethod("render", GuiGraphics.class, int.class, int.class, int.class, float.class, float.class, float.class)
                    .invoke(b, g, centerX, centerY, radius, hue, sat, value);
        } catch (Throwable t) {
            SkijaRenderer.markUnavailable();
        }
    }

    public void dispose() {
        if (backend != null) {
            try {
                backend.getClass().getMethod("dispose").invoke(backend);
            } catch (Throwable ignore) { /* 忽略 */ }
            backend = null;
        }
    }
}
