package fku.org.example.fku.features.skija;

import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Color;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.types.RRect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * SkijaRoundRect 的真正渲染实现（含全部 io.github.humbleui.skija.* 引用）。
 * 仅由 SkijaRoundRect 在确认 Skija 可加载后通过反射调用。
 *
 * ★ RL 必须全局唯一（原子计数器）：不能用 CACHE.size() —— 面板入场动画每帧产生新尺寸纹理，
 *   缓存一旦淘汰，size 回退会让新纹理注册到「仍在使用」的 RL 上，导致面板 blit 到别人的纹理
 *   （症状：屏幕上出现大块错误色块）。
 * ★ 4x 超采样 + LINEAR 过滤：GUI 缩放下圆角平滑，不再有像素锯齿。
 * ★ blit 必须以「逻辑完整尺寸」（w+2*ox）作为四边形与 UV 基准，否则描边会被裁掉。
 */
public class SkijaRoundRectBackend {
    private static final int SS = 4; // 超采样倍率
    private static final AtomicLong RL_COUNTER = new AtomicLong();

    private static final class Entry {
        final DynamicTexture tex;
        final ResourceLocation rl;
        final int w, h;    // 逻辑尺寸
        final int ox, oy;  // 描边外扩（逻辑像素）
        Entry(DynamicTexture tex, ResourceLocation rl, int w, int h, int ox, int oy) {
            this.tex = tex; this.rl = rl; this.w = w; this.h = h; this.ox = ox; this.oy = oy;
        }
    }

    private static final LinkedHashMap<String, Entry> CACHE = new LinkedHashMap<String, Entry>(256, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Entry> eldest) {
            if (size() > 256) {
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

    private static int[] argb(int color) {
        return new int[]{(color >> 24) & 0xFF, (color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF};
    }

    private static Entry getOrCreate(String key, int w, int h, int ox, int oy, SkijaRenderer.SkijaDraw draw) {
        Entry e = CACHE.get(key);
        if (e != null) return e;
        DynamicTexture tex = SkijaRenderer.createDynamicTexture(
                Math.max(1, (w + ox * 2) * SS), Math.max(1, (h + oy * 2) * SS), draw);
        if (tex == null) return null;
        ResourceLocation rl = new ResourceLocation("fku", "skija/rr/" + RL_COUNTER.incrementAndGet());
        Minecraft.getInstance().getTextureManager().register(rl, tex);
        e = new Entry(tex, rl, w, h, ox, oy);
        CACHE.put(key, e);
        return e;
    }

    /** 按 Entry 逻辑完整尺寸 blit（UV 0..1 覆盖整张超采样纹理） */
    private static void blit(GuiGraphics g, Entry e, int x, int y) {
        int qw = e.w + e.ox * 2;
        int qh = e.h + e.oy * 2;
        g.blit(e.rl, x - e.ox, y - e.oy, 0, 0, qw, qh, qw, qh);
    }

    public static void drawRoundedRect(GuiGraphics g, int x, int y, int w, int h, int color, int radius) {
        if (w <= 0 || h <= 0) return;
        int r = Math.min(radius, Math.min(w / 2, h / 2));
        if (r <= 0) {
            g.fill(x, y, x + w, y + h, color);
            return;
        }
        String key = "f:" + w + ":" + h + ":" + r + ":" + color;
        int[] c = argb(color);
        Entry e = getOrCreate(key, w, h, 0, 0, new SkijaRenderer.SkijaDraw() {
            @Override
            public void draw(Canvas cv) {
                Paint p = new Paint();
                p.setAntiAlias(true);
                p.setColor(Color.makeARGB(c[0], c[1], c[2], c[3]));
                cv.drawRRect(RRect.makeXYWH(0, 0, w * SS, h * SS, r * SS), p);
            }
        });
        if (e == null) {
            g.fill(x, y, x + w, y + h, color);
            return;
        }
        blit(g, e, x, y);
    }

    public static void drawRoundedOutline(GuiGraphics g, int x, int y, int w, int h, int color, int radius, int thickness) {
        if (w <= 0 || h <= 0 || thickness <= 0) return;
        int r = Math.min(radius, Math.min(w / 2, h / 2));
        if (r <= 0) {
            g.fill(x, y, x + w, y + h, color);
            return;
        }
        String key = "o:" + w + ":" + h + ":" + r + ":" + thickness + ":" + color;
        int[] c = argb(color);
        Entry e = getOrCreate(key, w, h, thickness, thickness, new SkijaRenderer.SkijaDraw() {
            @Override
            public void draw(Canvas cv) {
                Paint p = new Paint();
                p.setAntiAlias(true);
                p.setColor(Color.makeARGB(c[0], c[1], c[2], c[3]));
                p.setStroke(true);
                p.setStrokeWidth(thickness * SS);
                cv.drawRRect(RRect.makeXYWH(
                        thickness * SS / 2f, thickness * SS / 2f,
                        (w - thickness) * SS, (h - thickness) * SS,
                        Math.max(0, r - thickness / 2f) * SS), p);
            }
        });
        if (e == null) {
            g.fill(x, y, x + w, y + h, color);
            return;
        }
        blit(g, e, x, y);
    }

    public static void clearCache() {
        for (Entry e : CACHE.values()) if (e != null) {
            e.tex.close();
            Minecraft.getInstance().getTextureManager().release(e.rl);
        }
        CACHE.clear();
    }
}
