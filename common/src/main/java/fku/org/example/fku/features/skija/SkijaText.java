package fku.org.example.fku.features.skija;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Skija 文字渲染（阶段3）公开网关 —— 不含任何 io.github.humbleui.skija.* 引用，
 * 真正渲染在 SkijaTextBackend 里，仅当 Skija 可加载时才通过反射调用，杜绝无 Skija 时类校验崩溃。
 */
public class SkijaText {
    /** 仅含原版 MC 类型，Backend 可安全构造并返回 */
    static final class Entry {
        final DynamicTexture tex;
        final ResourceLocation rl;
        final int w, h;
        Entry(DynamicTexture tex, ResourceLocation rl, int w, int h) {
            this.tex = tex; this.rl = rl; this.w = w; this.h = h;
        }
    }

    private static final LinkedHashMap<String, Entry> CACHE = new LinkedHashMap<String, Entry>(64, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Entry> eldest) {
            if (size() > Math.max(8, SkijaConfig.getInstance().skijaCacheSize)) {
                Entry e = eldest.getValue();
                if (e != null) {
                    e.tex.close();
                    Minecraft.getInstance().getTextureManager().release(e.rl);
                }
                return true;
            }
            return false;
        }
    };

    private static Entry get(String text, float scale) {
        String key = text + "|" + (int) (scale * 1000f);
        Entry e = CACHE.get(key);
        if (e != null) return e;
        try {
            e = (Entry) SkijaReflect.call(
                    "fku.org.example.fku.features.skija.SkijaTextBackend", "build",
                    new Class[]{String.class, float.class}, text, scale);
        } catch (Throwable t) {
            SkijaRenderer.markUnavailable();
            return null;
        }
        if (e == null) return null;
        CACHE.put(key, e);
        return e;
    }

    public static void drawString(GuiGraphics g, String text, int x, int y, int color) {
        if (text == null || text.isEmpty()) return;
        if (!SkijaRenderer.isAvailable() || !SkijaConfig.getInstance().enableTextRendering
                || !SkijaReflect.isLoadable()) {
            g.drawString(Minecraft.getInstance().font, text, x, y, color);
            return;
        }
        Entry e = get(text, 1.0f);
        if (e == null) {
            g.drawString(Minecraft.getInstance().font, text, x, y, color);
            return;
        }
        tintBlit(g, e, x, y, color);
    }

    public static void drawStringScaled(GuiGraphics g, String text, float x, float y, float scale, int color) {
        if (text == null || text.isEmpty()) return;
        if (!SkijaRenderer.isAvailable() || !SkijaConfig.getInstance().enableTextRendering
                || !SkijaReflect.isLoadable()) {
            fallbackScaled(g, text, x, y, scale, color);
            return;
        }
        Entry e = get(text, scale);
        if (e == null) {
            fallbackScaled(g, text, x, y, scale, color);
            return;
        }
        tintBlit(g, e, (int) x, (int) y, color);
    }

    private static void fallbackScaled(GuiGraphics g, String text, float x, float y, float scale, int color) {
        var pose = g.pose();
        pose.pushPose();
        pose.translate(x, y, 0.0F);
        pose.scale(scale, scale, 1.0F);
        g.drawString(Minecraft.getInstance().font, text, 0, 0, color);
        pose.popPose();
    }

    private static void tintBlit(GuiGraphics g, Entry e, int x, int y, int color) {
        float r = ((color >> 16) & 0xFF) / 255f;
        float gg = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        float a = ((color >> 24) & 0xFF) / 255f;
        RenderSystem.setShaderColor(r, gg, b, a);
        g.blit(e.rl, x, y, 0, 0, e.w, e.h, e.w, e.h);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    public static int measureWidth(String text, float scale) {
        if (text == null || text.isEmpty()) return 0;
        Entry e = get(text, scale);
        return e == null ? 0 : e.w - 4;
    }

    public static void clearCache() {
        for (Entry e : CACHE.values()) if (e != null) {
            e.tex.close();
            Minecraft.getInstance().getTextureManager().release(e.rl);
        }
        CACHE.clear();
    }
}
