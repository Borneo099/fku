package fku.org.example.fku.features.skija;

import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Color;
import io.github.humbleui.skija.Font;
import io.github.humbleui.skija.FontMgr;
import io.github.humbleui.skija.FontMetrics;
import io.github.humbleui.skija.FontStyle;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.skija.Typeface;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * SkijaText 的真正渲染实现（含全部 io.github.humbleui.skija.* 引用）。
 * 仅由 SkijaText 在确认 Skija 可加载后通过反射调用。
 *
 * 文字以白色渲染到离屏纹理，上屏时按 ARGB 着色；任意字符默认字体无法覆盖时向系统字体管理器
 * 请求（含中文），整串都无法覆盖则 build() 返回 null，由网关回退原版字体（绝不豆腐块）。
 */
public class SkijaTextBackend {
    private static final float BASE_PX = 9.0f;
    private static final Typeface MISSING = null;
    private static final int SS = 4; // 超采样倍率：4x 渲染 + LINEAR 过滤，缩放后文字平滑
    private static int nameCounter = 0;
    private static boolean loggedNullOnce = false;
    private static final org.apache.logging.log4j.Logger LOGGER =
            org.apache.logging.log4j.LogManager.getLogger("FKU-Skija");

    private static final class Run {
        final String text;
        final Typeface typeface; // null = 默认字体
        Run(String t, Typeface tf) { this.text = t; this.typeface = tf; }
    }

    private static float px(float scale) { return BASE_PX * Math.max(0.1f, scale); }

    private static Typeface resolve(int codepoint) {
        if (new Font().getUTF32Glyph(codepoint) != 0) return null;
        FontMgr fm = FontMgr.getDefault();
        if (fm == null) return MISSING;
        Typeface tf = fm.matchFamilyStyleCharacter("", FontStyle.NORMAL, new String[]{"zh", "en"}, codepoint);
        if (tf != null && tf.getUTF32Glyph(codepoint) != 0) return tf;
        // 回退：显式尝试常见中文字族（Windows/macOS/Linux）
        for (String fam : new String[]{"Microsoft YaHei", "微软雅黑", "SimHei", "PingFang SC", "Noto Sans CJK SC"}) {
            Typeface tf2 = fm.matchFamilyStyle(fam, FontStyle.NORMAL);
            if (tf2 != null && tf2.getUTF32Glyph(codepoint) != 0) return tf2;
        }
        return MISSING;
    }

    private static boolean eq(Typeface a, Typeface b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a._nativeEquals(b);
    }

    private static List<Run> buildRuns(String text) {
        List<Run> runs = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        Typeface curTf = null;
        int i = 0;
        while (i < text.length()) {
            int cp = text.codePointAt(i);
            if (cp > 0xFFFF) i++;
            Typeface tf = resolve(cp);
            if (tf == MISSING) return null;
            if (cur.length() == 0 || eq(curTf, tf)) {
                cur.appendCodePoint(cp);
                curTf = tf;
            } else {
                runs.add(new Run(cur.toString(), curTf));
                cur.setLength(0);
                cur.appendCodePoint(cp);
                curTf = tf;
            }
            i++;
        }
        if (cur.length() > 0) runs.add(new Run(cur.toString(), curTf));
        return runs;
    }

    @SuppressWarnings("unchecked")
    public static SkijaText.Entry build(String text, float scale) {
        List<Run> runs = buildRuns(text);
        if (runs == null) {
            if (!loggedNullOnce) {
                loggedNullOnce = true;
                LOGGER.warn("[FKU-Skija] 文字构建返回 null（系统字体管理器无法覆盖字符），该串回退原版字体: " + text);
            }
            return null;
        }
        float size = px(scale);
        float totalW = 0f;
        for (Run r : runs) {
            Font f = r.typeface == null ? new Font() : new Font(r.typeface);
            f.setSize(size);
            totalW += f.measureTextWidth(r.text);
        }
        Font probe = new Font();
        probe.setSize(size);
        FontMetrics m = probe.getMetrics();
        float ascent = Math.abs(m.getAscent());
        float descent = Math.abs(m.getDescent());
        float lead = Math.abs(m.getLeading());
        if (ascent < 1f) ascent = size * 0.8f;
        if (descent < 1f) descent = size * 0.2f;
        int w = (int) Math.ceil(totalW) + 4;
        int h = (int) Math.ceil(ascent + descent + lead) + 2;
        int baseline = (int) Math.ceil(ascent) + 1;
        if (w <= 0 || h <= 0) return null;

        // 4x 超采样：纹理为逻辑尺寸的 SS 倍，上屏时按逻辑尺寸 blit（UV 0..1 覆盖整张纹理），
        // 配合 LINEAR 过滤，GUI 缩放下文字平滑无锯齿
        final float fsize = size * SS;
        final int fbase = baseline * SS;
        final float fx0 = 2.0f * SS;
        int tw = Math.max(1, w * SS);
        int th = Math.max(1, h * SS);
        DynamicTexture tex = SkijaRenderer.createDynamicTexture(tw, th, new SkijaRenderer.SkijaDraw() {
            @Override
            public void draw(Canvas cv) {
                Paint p = new Paint();
                p.setAntiAlias(true);
                p.setColor(Color.makeARGB(255, 255, 255, 255));
                float x = fx0;
                for (Run r : runs) {
                    Font f = r.typeface == null ? new Font() : new Font(r.typeface);
                    f.setSize(fsize);
                    cv.drawString(r.text, x, fbase, f, p);
                    x += f.measureTextWidth(r.text);
                }
            }
        });
        if (tex == null) return null;
        ResourceLocation rl = new ResourceLocation("fku", "skija/text/" + (nameCounter++));
        Minecraft.getInstance().getTextureManager().register(rl, tex);
        // Entry 的 w/h = 逻辑绘制尺寸（非纹理像素），网关直接按此 blit（UV 覆盖整张超采样纹理）
        return new SkijaText.Entry(tex, rl, w, h);
    }
}
