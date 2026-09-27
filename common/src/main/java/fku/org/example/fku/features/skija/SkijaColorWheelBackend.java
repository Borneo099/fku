package fku.org.example.fku.features.skija;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * 颜色轮盘（阶段1）渲染实现 —— 纯 Java 逐像素计算 HSV 圆盘。
 *
 * ★ 不再使用 skija 的渐变 Shader：0.143.x 上 Shader.makeSweepGradient 行为不可靠，
 *   失败时 Paint 以默认纯白绘制，整个圆盘变成一片白色。逐像素数学计算结果确定且完全平滑。
 * ★ 2x 超采样 + LINEAR 过滤：GUI 缩放下圆盘边缘与指示器依然圆润无锯齿。
 * ★ 指示器（黑环+白点）与圆盘一并烘焙进纹理，随 hue/sat 移动。
 */
public class SkijaColorWheelBackend {
    private static final int PAD = 6;
    private static final int SS = 2; // 超采样倍率
    private static final ResourceLocation TEX_KEY = new ResourceLocation("fku", "skija/wheel");

    private DynamicTexture tex = null;
    private int cachedR = -1;
    private float cachedHue = -1;
    private float cachedSat = -1;
    private float cachedVal = -1;

    public void render(GuiGraphics g, int centerX, int centerY, int radius, float hue, float sat, float value) {
        if (radius <= 0) return;
        if (tex == null || cachedR != radius
                || Math.abs(cachedHue - hue) > 0.0005f
                || Math.abs(cachedSat - sat) > 0.0005f
                || Math.abs(cachedVal - value) > 0.0005f) {
            DynamicTexture nt = build(radius, hue, sat, value);
            if (nt == null) return;
            if (tex != null) tex.close();
            tex = nt;
            Minecraft.getInstance().getTextureManager().register(TEX_KEY, tex);
            cachedR = radius;
            cachedHue = hue;
            cachedSat = sat;
            cachedVal = value;
        }
        int logical = 2 * radius + 2 * PAD;
        g.blit(TEX_KEY, centerX - radius - PAD, centerY - radius - PAD, 0, 0, logical, logical, logical, logical);
    }

    private DynamicTexture build(int R, float hue, float sat, float val) {
        int logical = 2 * R + 2 * PAD;
        int size = logical * SS;
        float c = (R + PAD) * SS;
        float rR = R * SS;

        // 指示器位置（与圆盘同一角度约定：hue=0 朝 +x，逆时针）
        double ang = hue * Math.PI * 2;
        float mx = c + (float) (Math.cos(ang) * sat * rR);
        float my = c + (float) (Math.sin(ang) * sat * rR);
        float mDark = 6f * SS;   // 黑环半径（纹理像素）
        float mWhite = 4f * SS;  // 白点半径
        int mb = (int) mDark + 2;

        // useCalloc=true：未覆盖像素保持 0（透明），无需逐像素清零
        NativeImage ni = new NativeImage(size, size, true);
        try {
            for (int y = 0; y < size; y++) {
                float dy = y + 0.5f - c;
                // 指示器行范围快速跳过
                boolean nearMarkerRow = Math.abs(y + 0.5f - my) <= mb;
                for (int x = 0; x < size; x++) {
                    float dx = x + 0.5f - c;
                    float dist = (float) Math.sqrt(dx * dx + dy * dy);
                    float cov = clamp(rR - dist + 0.5f, 0f, 1f); // 圆盘边缘 AA
                    if (cov <= 0f) continue;

                    float h = (float) Math.atan2(dy, dx) / (2f * (float) Math.PI);
                    if (h < 0f) h += 1f;
                    float s = Math.min(dist / rR, 1f);

                    int rgb = hsvToRgb(h, s, val);
                    int r = (rgb >> 16) & 0xFF;
                    int gg = (rgb >> 8) & 0xFF;
                    int b = rgb & 0xFF;
                    int a = (int) (255 * cov);

                    // 指示器混合（黑环 → 白点）
                    if (nearMarkerRow) {
                        float mdx = x + 0.5f - mx;
                        float mdy = y + 0.5f - my;
                        if (Math.abs(mdx) <= mb) {
                            float md = (float) Math.sqrt(mdx * mdx + mdy * mdy);
                            float cd = clamp(mDark - md + 0.5f, 0f, 1f);
                            if (cd > 0f) {
                                r = mix(r, 0, cd);
                                gg = mix(gg, 0, cd);
                                b = mix(b, 0, cd);
                                a = Math.max(a, (int) (255 * cd));
                            }
                            float cw = clamp(mWhite - md + 0.5f, 0f, 1f);
                            if (cw > 0f) {
                                r = mix(r, 255, cw);
                                gg = mix(gg, 255, cw);
                                b = mix(b, 255, cw);
                                a = Math.max(a, (int) (255 * cw));
                            }
                        }
                    }

                    // NativeImage.setPixelRGBA 为小端 ABGR 打包
                    ni.setPixelRGBA(x, y, (a << 24) | (b << 16) | (gg << 8) | r);
                }
            }
            DynamicTexture t = new DynamicTexture(ni);
            t.upload();
            t.setFilter(true, false); // 线性过滤：超采样缩小平滑
            return t;
        } catch (Throwable t2) {
            ni.close();
            return null;
        }
    }

    private static float clamp(float v, float lo, float hi) {
        return v < lo ? lo : (v > hi ? hi : v);
    }

    private static int mix(int base, int target, float cov) {
        return (int) (base * (1f - cov) + target * cov + 0.5f);
    }

    /** HSV → RGB（h∈[0,1)），返回 0xRRGGBB */
    private static int hsvToRgb(float h, float s, float v) {
        float f = h * 6f;
        int i = Math.floorMod((int) f, 6);
        float fr = f - (int) f;
        float p = v * (1f - s);
        float q = v * (1f - s * fr);
        float t = v * (1f - s * (1f - fr));
        float r, g, b;
        switch (i) {
            case 0:  r = v; g = t; b = p; break;
            case 1:  r = q; g = v; b = p; break;
            case 2:  r = p; g = v; b = t; break;
            case 3:  r = p; g = q; b = v; break;
            case 4:  r = t; g = p; b = v; break;
            default: r = v; g = p; b = q; break;
        }
        return (((int) (r * 255f + 0.5f)) << 16) | (((int) (g * 255f + 0.5f)) << 8) | (int) (b * 255f + 0.5f);
    }

    public void dispose() {
        if (tex != null) {
            tex.close();
            tex = null;
        }
    }
}
