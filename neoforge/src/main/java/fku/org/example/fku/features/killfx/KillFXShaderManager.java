package fku.org.example.fku.features.killfx; /* water */

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import fku.org.example.fku.Fku;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * KillFX 位置着色器特效管理器（1.21.8 适配版）
 *
 * ★ 1.21.8 渲染管线变化（关键）：
 *   - ShaderInstance / RegisterShadersEvent / RenderSystem.setShader / BufferUploader.drawWithShader
 *     在 1.21.8 已被移除，替换为 RenderPipeline + RenderType + GpuBuffer 的统一 GPU 管线。
 *   - 自定义 GLSL（黑洞光线追踪、引力透镜）无法直接移植，改用 Tesselator 风格几何体
 *     （顶点色 + 叠加混合 LIGHTNING + 关闭深度测试）近似渲染，效果等价且稳定。
 *
 * ★ 渲染方式：
 *   - 建立一个自定义 RenderType（position_color 顶点格式 + 叠加混合 + 无深度测试 + 无背面剔除）。
 *   - 在 RenderLevelStageEvent.AfterEntities（模型视图矩阵=相机矩阵）中，
 *     用 Minecraft.renderBuffers().bufferSource() 获取 VertexConsumer，
 *     以【世界坐标】写入几何体，最后 endBatch 统一提交。
 *   - 几何体全部以 TRIANGLES 模式提交（render type 固定为 VertexFormat.Mode.TRIANGLES）。
 *
 * 支持特效：
 *   - 黑洞（BLACKHOLE）：暗色球体 + 发光吸积盘 + 外层光晕 + 落入粒子
 *   - 水晶（CRYSTAL）：菲涅尔球体（4 种风格）+ 外围光晕 + 闪烁粒子
 *   - 天光光束（SKY_BEAM）：自天而降的发光光柱
 *   - 天光环（SKY_RING）：旋转发光环
 *   - 超新星（HYPERNOVA）：膨胀辉光球 + 淡出
 *   - 光线爆发（RAY_BURST）：自死亡点向外辐射的光线
 */
public class KillFXShaderManager {

    private static final Minecraft mc = Minecraft.getInstance();
    private static final LinkedList<ShaderEffect> effects = new LinkedList<>();

    public enum ShaderType { NONE, BLACKHOLE, CRYSTAL, SKY_BEAM, SKY_RING, HYPERNOVA, RAY_BURST }

    // ─── 自定义渲染类型（懒加载，构建失败则降级停用） ───
    private static RenderType KILLFX_TYPE = null;
    private static boolean typeFailed = false;

    private static RenderType getKillFXType() {
        if (typeFailed) return null;
        if (KILLFX_TYPE == null) {
            try {
                RenderPipeline pipeline = RenderPipeline.builder()
                        .withLocation(ResourceLocation.fromNamespaceAndPath(Fku.MOD_ID, "killfx_glow"))
                        .withVertexShader(ResourceLocation.withDefaultNamespace("core/position_color"))
                        .withFragmentShader(ResourceLocation.withDefaultNamespace("core/position_color"))
                        .withBlend(BlendFunction.LIGHTNING)
                        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                        .withCull(false)
                        .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLES)
                        .build();
                KILLFX_TYPE = RenderType.create("killfx_glow", 1536,
                        pipeline, RenderType.CompositeState.builder().createCompositeState(false));
            } catch (Exception e) {
                typeFailed = true;
                Fku.LOGGER.error("[KillFX] 自定义渲染管线创建失败，已停用着色器特效", e);
            }
        }
        return KILLFX_TYPE;
    }

    /**
     * 触发特效 — 携带完整配置参数
     */
    public static void trigger(ShaderType type, Vec3 pos, float intensity, int durationTicks, String extraConfig) {
        if (type == ShaderType.NONE) return;
        effects.add(new ShaderEffect(type, pos, intensity, durationTicks, extraConfig));
    }

    /** 简化调用（无额外配置） */
    public static void trigger(ShaderType type, Vec3 pos, float intensity, int durationTicks) {
        trigger(type, pos, intensity, durationTicks, "");
    }

    public static void tick() {
        Iterator<ShaderEffect> it = effects.iterator();
        while (it.hasNext()) {
            ShaderEffect e = it.next();
            e.elapsed++;
            e.progress = Math.min(1f, (float) e.elapsed / e.duration);
            if (e.progress >= 1f) it.remove();
        }
    }

    /**
     * 渲染所有激活的特效。
     * @param poseStack 事件提供的 PoseStack（模型视图矩阵=相机矩阵，几何体用世界坐标写入）
     * @param partialTick 部分刻
     */
    public static void renderEffects(PoseStack poseStack, float partialTick) {
        RenderType type = getKillFXType();
        if (type == null || effects.isEmpty()) return;

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer vc = bufferSource.getBuffer(type);

        for (ShaderEffect effect : effects) {
            float p = effect.progress;
            float wx = (float) effect.pos.x;
            float wy = (float) effect.pos.y;
            float wz = (float) effect.pos.z;

            switch (effect.type) {
                case BLACKHOLE -> renderBlackhole(vc, wx, wy, wz, p, effect, partialTick);
                case CRYSTAL   -> renderCrystal(vc, wx, wy, wz, p, effect, partialTick);
                case SKY_BEAM  -> renderSkyBeam(vc, wx, wy, wz, p, effect, partialTick);
                case SKY_RING  -> renderSkyRing(vc, wx, wy, wz, p, effect, partialTick);
                case HYPERNOVA -> renderHypernova(vc, wx, wy, wz, p, effect);
                case RAY_BURST -> renderRayBurst(vc, wx, wy, wz, p, effect);
            }
        }

        bufferSource.endBatch(type);
    }

    // ════════════════════════════════════════════════════════════
    // ★ 顶点写入辅助（世界坐标 + 顶点色 0~1，TRIANGLES 模式）
    // ════════════════════════════════════════════════════════════

    private static void v(VertexConsumer vc, float x, float y, float z, float r, float g, float b, float a) {
        vc.addVertex(x, y, z).setColor(r, g, b, a);
    }

    /** 以两个三角形写入一个四边形（p0-p1-p2-p3 顺序） */
    private static void quad(VertexConsumer vc,
                             float x0, float y0, float z0,
                             float x1, float y1, float z1,
                             float x2, float y2, float z2,
                             float x3, float y3, float z3,
                             float r, float g, float b, float a) {
        v(vc, x0, y0, z0, r, g, b, a);
        v(vc, x1, y1, z1, r, g, b, a);
        v(vc, x2, y2, z2, r, g, b, a);
        v(vc, x0, y0, z0, r, g, b, a);
        v(vc, x2, y2, z2, r, g, b, a);
        v(vc, x3, y3, z3, r, g, b, a);
    }

    // ════════════════════════════════════════════════════════════
    // ★ 黑洞（几何体近似）
    // ════════════════════════════════════════════════════════════

    private static void renderBlackhole(VertexConsumer vc, float ex, float ey, float ez,
                                        float p, ShaderEffect effect, float partialTick) {
        float scaleMul = 1.0f;
        if (effect.extraConfig != null && !effect.extraConfig.isEmpty()) {
            try { scaleMul = Float.parseFloat(effect.extraConfig); } catch (Exception ignored) {}
        }
        float sc = effect.intensity * scaleMul;
        float sz = 1.5f * sc * Math.min(p * 2.5f, (1f - p) * 2f + 0.3f);
        if (sz <= 0.001f) return;

        // 暗色球体（事件视界）
        renderSphere(vc, ex, ey, ez, sz, p);
        // 吸积盘（带倾角）
        renderDisk(vc, ex, ey, ez, sz, p, sc);
        // 外层引力透镜光晕
        renderLensing(vc, ex, ey, ez, sz, p, sc);
        // 落入粒子
        renderParticles(vc, ex, ey, ez, sz, p, sc);
    }

    // ─── 黑洞球体（暗色） ───
    private static void renderSphere(VertexConsumer vc, float ex, float ey, float ez,
                                     float sz, float p) {
        float alpha = 1f - p * 0.5f;
        int stacks = 12, slices = 16;
        for (int i = 0; i < stacks; i++) {
            float ph1 = (float) (Math.PI * i / stacks), ph2 = (float) (Math.PI * (i + 1) / stacks);
            float r1 = sz * (float) Math.sin(ph1), r2 = sz * (float) Math.sin(ph2);
            float y1 = sz * (float) Math.cos(ph1), y2 = sz * (float) Math.cos(ph2);
            float br = i > stacks / 2 ? 0.02f : 0.04f;
            for (int j = 0; j < slices; j++) {
                float t1 = (float) (2 * Math.PI * j / slices), t2 = (float) (2 * Math.PI * (j + 1) / slices);
                quad(vc,
                        ex + r1 * (float) Math.cos(t1), ey + y1, ez + r1 * (float) Math.sin(t1),
                        ex + r2 * (float) Math.cos(t1), ey + y2, ez + r2 * (float) Math.sin(t1),
                        ex + r2 * (float) Math.cos(t2), ey + y2, ez + r2 * (float) Math.sin(t2),
                        ex + r1 * (float) Math.cos(t2), ey + y1, ez + r1 * (float) Math.sin(t2),
                        br, br, br, alpha);
            }
        }
    }

    // ─── 吸积盘（发光环带 + 赤道光环） ───
    private static void renderDisk(VertexConsumer vc, float ex, float ey, float ez,
                                   float sz, float p, float sc) {
        float alpha = 1f - p;
        float rot = p * 6f;
        for (int ring = 0; ring < 4; ring++) {
            float rr = sz * (0.6f + ring * 0.25f);
            float rh = sz * (0.04f + ring * 0.02f);
            float ra = alpha * (1f - ring * 0.15f);
            float r, g, bl;
            if (ring == 0) { r = 1f; g = 0.8f; bl = 0.6f; }
            else if (ring == 1) { r = 0.6f; g = 0.3f; bl = 1f; }
            else if (ring == 2) { r = 0.4f; g = 0.2f; bl = 0.8f; }
            else { r = 0.3f; g = 0.1f; bl = 0.5f; }
            for (int i = 0; i < 36; i++) {
                float a1 = (float) (2 * Math.PI * i / 36 + rot + ring * 0.5f);
                float a2 = (float) (2 * Math.PI * (i + 1) / 36 + rot + ring * 0.5f);
                float h1 = rh * (1f + (float) Math.sin(i * 1.5) * 0.5f);
                float h2 = rh * (1f + (float) Math.sin((i + 1) * 1.5) * 0.5f);
                float x1 = ex + rr * (float) Math.cos(a1), z1 = ez + rr * (float) Math.sin(a1);
                float x2 = ex + rr * (float) Math.cos(a2), z2 = ez + rr * (float) Math.sin(a2);
                float ix1 = ex + rr * 0.85f * (float) Math.cos(a1), iz1 = ez + rr * 0.85f * (float) Math.sin(a1);
                float ix2 = ex + rr * 0.85f * (float) Math.cos(a2), iz2 = ez + rr * 0.85f * (float) Math.sin(a2);
                quad(vc,
                        ix1, ey - h1, iz1, x1, ey - h2, z1, x2, ey - h2, z2, ix2, ey - h1, iz2,
                        r, g, bl, ra * 0.6f);
                quad(vc,
                        ix1, ey + h1, iz1, x1, ey + h2, z1, x2, ey + h2, z2, ix2, ey + h1, iz2,
                        r, g, bl, ra * 0.3f);
            }
        }
        // 赤道光环
        for (int i = 0; i < 48; i++) {
            float a1 = (float) (2 * Math.PI * i / 48 + rot), a2 = (float) (2 * Math.PI * (i + 1) / 48 + rot);
            float gx1 = ex + sz * 0.55f * (float) Math.cos(a1), gz1 = ez + sz * 0.55f * (float) Math.sin(a1);
            float gx2 = ex + sz * 0.55f * (float) Math.cos(a2), gz2 = ez + sz * 0.55f * (float) Math.sin(a2);
            float ga = alpha * (0.6f + 0.4f * (float) Math.sin(i * 3 + p * 8));
            quad(vc,
                    gx1, ey, gz1, gx2, ey, gz2, gx2, ey + sz * 0.02f, gz2, gx1, ey + sz * 0.02f, gz1,
                    1f, 0.9f, 0.7f, ga);
        }
    }

    // ─── 引力透镜光晕（近似，三角形扇） ───
    private static void renderLensing(VertexConsumer vc, float ex, float ey, float ez,
                                      float sz, float p, float sc) {
        float dist = 5f * sc * (1f - p * 0.5f);
        float a = (1f - p) * 0.03f;
        int seg = 16;
        for (int i = 0; i < seg; i++) {
            float a1 = (float) (2 * Math.PI * i / seg);
            float a2 = (float) (2 * Math.PI * (i + 1) / seg);
            v(vc, ex, ey, ez, 0.2f, 0.1f, 0.5f, 0f);
            v(vc, ex + dist * (float) Math.cos(a1), ey, ez + dist * (float) Math.sin(a1), 0.2f, 0.1f, 0.5f, a);
            v(vc, ex + dist * (float) Math.cos(a2), ey, ez + dist * (float) Math.sin(a2), 0.2f, 0.1f, 0.5f, a);
        }
    }

    // ─── 落入粒子 ───
    private static void renderParticles(VertexConsumer vc, float ex, float ey, float ez,
                                        float sz, float p, float sc) {
        float alpha = (1f - p) * 0.7f;
        for (int i = 0; i < 48; i++) {
            float ti = (float) i / 48;
            float ang = ti * (float) (2 * Math.PI) + p * 8f;
            float sr = sz * (0.3f + 0.7f * (1f - p * 0.8f)) * (1f - ti * 0.6f);
            float py = ey + (float) Math.sin(ti * 10f + p * 5f) * sz * 0.05f;
            float px = ex + sr * (float) Math.cos(ang), pz = ez + sr * (float) Math.sin(ang);
            float pr = 0.2f + 0.8f * (1f - ti), pg = 0.2f + 0.6f * (float) Math.sin(ti * 3f), pb = 0.4f + 0.6f * (1f - ti);
            float pa = alpha * (0.3f + 0.7f * (float) Math.sin(i * 0.7f + p * 10f));
            float psz = sz * 0.02f;
            quad(vc,
                    px - psz, py, pz - psz, px + psz, py, pz - psz, px + psz, py, pz + psz, px - psz, py, pz + psz,
                    pr, pg, pb, pa);
        }
    }

    // ════════════════════════════════════════════════════════════
    // ★ 超新星爆炸：辉光球体 + 膨胀 + 淡出
    // ════════════════════════════════════════════════════════════

    private static void renderHypernova(VertexConsumer vc, float ex, float ey, float ez,
                                         float p, ShaderEffect effect) {
        float sc = effect.intensity;
        float fadeIn = Math.min(p * 4f, 1f);
        float fadeOut = Math.max(1f - (p - 0.5f) / 0.5f, 0f);
        float alpha = fadeIn * fadeOut;
        if (alpha < 0.01f) return;

        float sz = 1.5f * sc * (0.3f + p * 1.2f);

        int stacks = 12, slices = 16;
        for (int i = 0; i < stacks; i++) {
            float ph1 = (float) (Math.PI * i / stacks), ph2 = (float) (Math.PI * (i + 1) / stacks);
            float r1 = sz * (float) Math.sin(ph1), r2 = sz * (float) Math.sin(ph2);
            float y1 = sz * (float) Math.cos(ph1), y2 = sz * (float) Math.cos(ph2);
            for (int j = 0; j < slices; j++) {
                float t1 = (float) (2 * Math.PI * j / slices), t2 = (float) (2 * Math.PI * (j + 1) / slices);
                float c1 = (float) Math.cos(t1), s1 = (float) Math.sin(t1);
                float c2 = (float) Math.cos(t2), s2 = (float) Math.sin(t2);
                float layer = (float) i / stacks;
                float br = 1f - layer * 0.3f;
                float ba = alpha * (1f - layer * 0.4f);
                quad(vc,
                        ex + r1 * c1, ey + y1, ez + r1 * s1,
                        ex + r2 * c1, ey + y2, ez + r2 * s1,
                        ex + r2 * c2, ey + y2, ez + r2 * s2,
                        ex + r1 * c2, ey + y1, ez + r1 * s2,
                        br, br * 0.8f, br * 0.3f, ba);
            }
        }

        // 外层光晕壳
        float outerSz = sz * 1.8f;
        int outerStacks = 8;
        for (int i = 0; i < outerStacks; i++) {
            float ph1 = (float) (Math.PI * i / outerStacks), ph2 = (float) (Math.PI * (i + 1) / outerStacks);
            float r1 = outerSz * (float) Math.sin(ph1), r2 = outerSz * (float) Math.sin(ph2);
            float y1 = outerSz * (float) Math.cos(ph1), y2 = outerSz * (float) Math.cos(ph2);
            for (int j = 0; j < slices; j++) {
                float t1 = (float) (2 * Math.PI * j / slices), t2 = (float) (2 * Math.PI * (j + 1) / slices);
                float c1 = (float) Math.cos(t1), s1 = (float) Math.sin(t1);
                float c2 = (float) Math.cos(t2), s2 = (float) Math.sin(t2);
                float f = 0.5f + 0.5f * (float) Math.sin(i * 1.3f + j * 0.7f + p * 8f);
                quad(vc,
                        ex + r1 * c1, ey + y1, ez + r1 * s1,
                        ex + r2 * c1, ey + y2, ez + r2 * s1,
                        ex + r2 * c2, ey + y2, ez + r2 * s2,
                        ex + r1 * c2, ey + y1, ez + r1 * s2,
                        1f, 0.5f, 0f, alpha * 0.2f * f);
            }
        }
    }

    // ════════════════════════════════════════════════════════════
    // ★ 光线爆发：从死亡点向外辐射的光线
    // ════════════════════════════════════════════════════════════

    private static void renderRayBurst(VertexConsumer vc, float ex, float ey, float ez,
                                        float p, ShaderEffect effect) {
        float sc = effect.intensity;
        float fadeIn = Math.min(p * 3f, 1f);
        float fadeOut = Math.max(1f - (p - 0.6f) / 0.4f, 0f);
        float alpha = fadeIn * fadeOut;
        if (alpha < 0.01f) return;

        float len = 4f * sc * (0.5f + p * 0.5f);
        float rot = p * 4f;
        int rays = 36;
        for (int i = 0; i < rays; i++) {
            float ang = (float) (2 * Math.PI * i / rays + rot);
            float dx = (float) Math.cos(ang), dz = (float) Math.sin(ang);
            float r = 0.2f + 0.8f * (float) Math.sin(i * 0.3f);
            float g = 0.2f + 0.6f * (float) Math.cos(i * 0.5f);
            float b = 0.4f + 0.6f * (float) Math.sin(i * 0.7f);
            float ra = alpha * (0.2f + 0.8f * (float) Math.sin(i * 1.7f + p * 6f));
            float w = 0.06f * sc;
            float hw = w * 0.5f;
            quad(vc,
                    ex - dx * hw, ey - w, ez - dz * hw,
                    ex - dx * hw, ey + w, ez - dz * hw,
                    ex + dx * len, ey + w * 0.3f, ez + dz * len,
                    ex + dx * len, ey - w * 0.3f, ez + dz * len,
                    r, g, b, ra);
        }
    }

    // ════════════════════════════════════════════════════════════
    // ★ 天光光束：自天而降的发光光柱
    // ════════════════════════════════════════════════════════════

    private static void renderSkyBeam(VertexConsumer vc, float ex, float ey, float ez,
                                      float p, ShaderEffect effect, float partialTick) {
        float sizeMul = 1.0f;
        if (effect.extraConfig != null && effect.extraConfig.startsWith("BEAM,")) {
            try { sizeMul = Float.parseFloat(effect.extraConfig.split(",")[1]); } catch (Exception ignored) {}
        }
        float sc = effect.intensity * sizeMul;
        float fadeIn = Math.min(p * 3f, 1f);
        float fadeOut = Math.max(1f - (p - 0.6f) / 0.4f, 0f);
        float alpha = fadeIn * fadeOut;
        if (sc <= 0.01f || alpha <= 0.01f) return;

        float height = 16.0f * sc;
        float radius = 1.2f * sc;

        int hSegs = 8, rSegs = 12;
        for (int h = 0; h < hSegs; h++) {
            float y0 = height * (float) h / hSegs, y1 = height * (float) (h + 1) / hSegs;
            for (int r = 0; r < rSegs; r++) {
                float a0 = (float) (2 * Math.PI * r / rSegs), a1 = (float) (2 * Math.PI * (r + 1) / rSegs);
                float c0 = (float) Math.cos(a0), s0 = (float) Math.sin(a0);
                float c1 = (float) Math.cos(a1), s1 = (float) Math.sin(a1);
                quad(vc,
                        ex + radius * c0, ey + y0, ez + radius * s0,
                        ex + radius * c1, ey + y0, ez + radius * s1,
                        ex + radius * c1, ey + y1, ez + radius * s1,
                        ex + radius * c0, ey + y1, ez + radius * s0,
                        1f, 1f, 1f, alpha);
            }
        }
    }

    // ════════════════════════════════════════════════════════════
    // ★ 天光环：旋转发光环
    // ════════════════════════════════════════════════════════════

    private static void renderSkyRing(VertexConsumer vc, float ex, float ey, float ez,
                                      float p, ShaderEffect effect, float partialTick) {
        float sizeMul = 1.0f;
        if (effect.extraConfig != null && effect.extraConfig.startsWith("RING,")) {
            try { sizeMul = Float.parseFloat(effect.extraConfig.split(",")[1]); } catch (Exception ignored) {}
        }
        float sc = effect.intensity * sizeMul;
        float fadeIn = Math.min(p * 3f, 1f);
        float fadeOut = Math.max(1f - (p - 0.6f) / 0.4f, 0f);
        float alpha = fadeIn * fadeOut;
        if (sc <= 0.01f || alpha <= 0.01f) return;

        float growProgress = Math.min(p / 0.4f, 1.0f);
        float growScale = 0.3f + 0.7f * growProgress;
        float baseSize = 2.0f * sc * growScale;
        float outerR = 2.0f * baseSize, innerR = 1.0f * baseSize;

        int ringSegs = 16, radialSegs = 4;
        float midR = (outerR + innerR) * 0.5f;
        float halfW = (outerR - innerR) * 0.5f;
        float tilt = (float) Math.toRadians(60.0);
        float cosT = (float) Math.cos(tilt), sinT = (float) Math.sin(tilt);
        // 环面倾斜：绕 X 轴旋转
        for (int i = 0; i < ringSegs; i++) {
            float a0 = (float) (2 * Math.PI * i / ringSegs), a1 = (float) (2 * Math.PI * (i + 1) / ringSegs);
            for (int j = 0; j < radialSegs; j++) {
                float t0 = -1f + 2f * j / radialSegs, t1 = -1f + 2f * (j + 1) / radialSegs;
                float r0 = midR + t0 * halfW, r1 = midR + t1 * halfW;
                float c0 = (float) Math.cos(a0), s0 = (float) Math.sin(a0);
                float c1 = (float) Math.cos(a1), s1 = (float) Math.sin(a1);
                // 局部环坐标（XZ 平面）→ 倾斜到 XY 平面附近
                float lx0 = r0 * c0, lz0 = r0 * s0;
                float lx1 = r1 * c0, lz1 = r1 * s0;
                float lx2 = r1 * c1, lz2 = r1 * s1;
                float lx3 = r0 * c1, lz3 = r0 * s1;
                // 绕 X 轴倾斜：y' = y*cos - z*sin ; z' = y*sin + z*cos
                quad(vc,
                        ex + lx0, ey + lz0 * cosT, ez + lz0 * sinT,
                        ex + lx1, ey + lz1 * cosT, ez + lz1 * sinT,
                        ex + lx2, ey + lz2 * cosT, ez + lz2 * sinT,
                        ex + lx3, ey + lz3 * cosT, ez + lz3 * sinT,
                        0.4f, 0.6f, 1.0f, alpha);
            }
        }
    }

    // ════════════════════════════════════════════════════════════
    // ★ 水晶特效渲染（菲涅尔球体 + 4 种风格）
    // ════════════════════════════════════════════════════════════

    private static void renderCrystal(VertexConsumer vc, float ex, float ey, float ez,
                                      float p, ShaderEffect effect, float partialTick) {
        String extra = effect.extraConfig;
        if (extra == null || extra.isEmpty()) extra = "CRYSTAL,88CCFF,1.0,0.8,1.5,true";
        String[] parts = extra.split(",");
        String style = parts.length > 0 ? parts[0] : "CRYSTAL";
        String hexColor = parts.length > 1 ? parts[1] : "88CCFF";
        float radiusScale = parts.length > 2 ? parseFloatSafe(parts[2], 1.0f) : 1.0f;
        float glowIntensity = parts.length > 3 ? parseFloatSafe(parts[3], 0.8f) : 0.8f;
        float rotSpeed = parts.length > 4 ? parseFloatSafe(parts[4], 1.5f) : 1.5f;
        boolean pulse = parts.length <= 5 || Boolean.parseBoolean(parts[5]);

        float sz = 2.0f * glowIntensity * radiusScale;
        float fadeIn = Math.min(p * 4f, 1f);
        float fadeOut = Math.max(1f - (p - 0.7f) / 0.3f, 0f);
        float alpha = fadeIn * fadeOut;
        float pulseScale = pulse ? 1f + 0.15f * (float) Math.sin(p * Math.PI * 6f) : 1f;
        sz *= pulseScale;

        int[] tint = parseHexColor(hexColor);
        float r = tint[0] / 255f, g = tint[1] / 255f, b = tint[2] / 255f;
        float rotation = p * rotSpeed * 8f;

        switch (style) {
            case "BLOOM"  -> renderCrystalBloom(vc, ex, ey, ez, sz, alpha, r, g, b, rotation, glowIntensity);
            case "GLASS"  -> renderCrystalGlass(vc, ex, ey, ez, sz, alpha, r, g, b, rotation);
            case "AURORA" -> renderCrystalAurora(vc, ex, ey, ez, sz, alpha, rotation, glowIntensity);
            default       -> renderCrystalDefault(vc, ex, ey, ez, sz, alpha, r, g, b, rotation, glowIntensity);
        }

        renderCrystalGlow(vc, ex, ey, ez, sz, alpha, r, g, b, glowIntensity, rotation);
        renderCrystalSparkles(vc, ex, ey, ez, sz, alpha, r, g, b, p, rotation);
    }

    private static void renderCrystalDefault(VertexConsumer vc, float ex, float ey, float ez,
                                              float sz, float alpha, float r, float g, float b,
                                              float rotation, float glowIntensity) {
        int stacks = 16, slices = 24;
        for (int i = 0; i < stacks; i++) {
            float ph1 = (float) (Math.PI * i / stacks), ph2 = (float) (Math.PI * (i + 1) / stacks);
            float r1 = sz * (float) Math.sin(ph1), r2 = sz * (float) Math.sin(ph2);
            float y1 = sz * (float) Math.cos(ph1), y2 = sz * (float) Math.cos(ph2);
            for (int j = 0; j < slices; j++) {
                float th1 = (float) (2 * Math.PI * j / slices + rotation);
                float th2 = (float) (2 * Math.PI * (j + 1) / slices + rotation);
                float c1 = (float) Math.cos(th1), s1 = (float) Math.sin(th1);
                float c2 = (float) Math.cos(th2), s2 = (float) Math.sin(th2);
                float fresnel1 = Math.abs((float) Math.cos(ph1));
                float fresnel2 = Math.abs((float) Math.cos(ph2));
                float a1 = alpha * (0.3f + 0.7f * fresnel1);
                float a2 = alpha * (0.3f + 0.7f * fresnel2);
                float lt1 = 0.6f + 0.4f * fresnel1;
                float lt2 = 0.6f + 0.4f * fresnel2;
                float lr1 = r * lt1, lg1 = g * lt1, lb1 = b * lt1;
                float lr2 = r * lt2, lg2 = g * lt2, lb2 = b * lt2;
                quad(vc,
                        ex + r1 * c1, ey + y1, ez + r1 * s1,
                        ex + r2 * c1, ey + y2, ez + r2 * s1,
                        ex + r2 * c2, ey + y2, ez + r2 * s2,
                        ex + r1 * c2, ey + y1, ez + r1 * s2,
                        lr1, lg1, lb1, a1);
            }
        }
    }

    private static void renderCrystalBloom(VertexConsumer vc, float ex, float ey, float ez,
                                            float sz, float alpha, float r, float g, float b,
                                            float rotation, float glowIntensity) {
        int stacks = 12, slices = 20;
        for (int i = 0; i < stacks; i++) {
            float ph1 = (float) (Math.PI * i / stacks), ph2 = (float) (Math.PI * (i + 1) / stacks);
            float r1 = sz * (float) Math.sin(ph1), r2 = sz * (float) Math.sin(ph2);
            float y1 = sz * (float) Math.cos(ph1), y2 = sz * (float) Math.cos(ph2);
            float bloomFactor = 1.5f + 0.5f * (float) Math.sin(rotation + i * 0.3f);
            float br = r * bloomFactor, bg = g * bloomFactor, bb = b * bloomFactor;
            float ba = alpha * 0.9f;
            for (int j = 0; j < slices; j++) {
                float th1 = (float) (2 * Math.PI * j / slices + rotation);
                float th2 = (float) (2 * Math.PI * (j + 1) / slices + rotation);
                float c1 = (float) Math.cos(th1), s1 = (float) Math.sin(th1);
                float c2 = (float) Math.cos(th2), s2 = (float) Math.sin(th2);
                float core = 0.7f + 0.3f * Math.abs((float) Math.cos(ph1));
                quad(vc,
                        ex + r1 * c1, ey + y1, ez + r1 * s1,
                        ex + r2 * c1, ey + y2, ez + r2 * s1,
                        ex + r2 * c2, ey + y2, ez + r2 * s2,
                        ex + r1 * c2, ey + y1, ez + r1 * s2,
                        br * core, bg * core, bb * core, ba);
            }
        }

        // BLOOM 光爆射线
        float rayAlpha = alpha * 0.3f;
        float brL = r * 1.2f, bgL = g * 1.2f, bbL = b * 1.2f;
        for (int i = 0; i < 12; i++) {
            float ang = (float) (2 * Math.PI * i / 12 + rotation * 0.5f);
            float ra = rayAlpha * (0.5f + 0.5f * (float) Math.sin(i * 2.5f + rotation));
            float len = sz * (1.5f + 0.3f * (float) Math.sin(i * 1.7f + rotation));
            float w = sz * 0.03f;
            float dx = (float) Math.cos(ang), dz = (float) Math.sin(ang);
            float px = ex + dx * w, pz = ez + dz * w;
            float px2 = ex + dx * len, pz2 = ez + dz * len;
            quad(vc,
                    px - dz * w, ey, pz + dx * w,
                    px + dz * w, ey, pz - dx * w,
                    px2 + dz * w * 0.5f, ey, pz2 - dx * w * 0.5f,
                    px2 - dz * w * 0.5f, ey, pz2 + dx * w * 0.5f,
                    brL, bgL, bbL, ra * 0.6f);
        }
    }

    private static void renderCrystalGlass(VertexConsumer vc, float ex, float ey, float ez,
                                            float sz, float alpha, float r, float g, float b,
                                            float rotation) {
        int stacks = 14, slices = 22;
        float glassAlpha = alpha * 0.35f;
        for (int i = 0; i < stacks; i++) {
            float ph1 = (float) (Math.PI * i / stacks), ph2 = (float) (Math.PI * (i + 1) / stacks);
            float r1 = sz * (float) Math.sin(ph1), r2 = sz * (float) Math.sin(ph2);
            float y1 = sz * (float) Math.cos(ph1), y2 = sz * (float) Math.cos(ph2);
            for (int j = 0; j < slices; j++) {
                float th1 = (float) (2 * Math.PI * j / slices + rotation);
                float th2 = (float) (2 * Math.PI * (j + 1) / slices + rotation);
                float c1 = (float) Math.cos(th1), s1 = (float) Math.sin(th1);
                float c2 = (float) Math.cos(th2), s2 = (float) Math.sin(th2);
                float edge1 = Math.abs((float) Math.sin(ph1));
                float edge2 = Math.abs((float) Math.sin(ph2));
                float highlight1 = edge1 * edge1;
                float highlight2 = edge2 * edge2;
                float a1 = glassAlpha * (0.5f + 0.5f * highlight1);
                float a2 = glassAlpha * (0.5f + 0.5f * highlight2);
                float white1 = highlight1 * 0.6f;
                float white2 = highlight2 * 0.6f;
                float lr1 = r * (1f - white1) + white1, lg1 = g * (1f - white1) + white1, lb1 = b * (1f - white1) + white1;
                float lr2 = r * (1f - white2) + white2, lg2 = g * (1f - white2) + white2, lb2 = b * (1f - white2) + white2;
                quad(vc,
                        ex + r1 * c1, ey + y1, ez + r1 * s1,
                        ex + r2 * c1, ey + y2, ez + r2 * s1,
                        ex + r2 * c2, ey + y2, ez + r2 * s2,
                        ex + r1 * c2, ey + y1, ez + r1 * s2,
                        lr1, lg1, lb1, a1);
            }
        }

        // 玻璃高光反射条纹
        float stripeAlpha = glassAlpha * 0.5f;
        for (int i = 0; i < 6; i++) {
            float ang = (float) (2 * Math.PI * i / 6 + rotation * 0.3f);
            float stripeW = sz * 0.04f;
            float stripeL = sz * 1.1f;
            float dx = (float) Math.cos(ang), dz = (float) Math.sin(ang);
            quad(vc,
                    ex + dx * stripeW, ey - sz * 0.1f, ez + dz * stripeW,
                    ex + dx * stripeW, ey + sz * 0.1f, ez + dz * stripeW,
                    ex + dx * stripeL, ey + sz * 0.1f, ez + dz * stripeL,
                    ex + dx * stripeL, ey - sz * 0.1f, ez + dz * stripeL,
                    1f, 1f, 1f, stripeAlpha);
        }
    }

    private static void renderCrystalAurora(VertexConsumer vc, float ex, float ey, float ez,
                                             float sz, float alpha, float rotation, float glowIntensity) {
        int stacks = 16, slices = 24;
        for (int i = 0; i < stacks; i++) {
            float ph1 = (float) (Math.PI * i / stacks), ph2 = (float) (Math.PI * (i + 1) / stacks);
            float r1 = sz * (float) Math.sin(ph1), r2 = sz * (float) Math.sin(ph2);
            float y1 = sz * (float) Math.cos(ph1), y2 = sz * (float) Math.cos(ph2);
            for (int j = 0; j < slices; j++) {
                float th1 = (float) (2 * Math.PI * j / slices + rotation);
                float th2 = (float) (2 * Math.PI * (j + 1) / slices + rotation);
                float c1 = (float) Math.cos(th1), s1 = (float) Math.sin(th1);
                float c2 = (float) Math.cos(th2), s2 = (float) Math.sin(th2);
                float hue1 = (float) (i * 0.06f + j * 0.04f + rotation * 0.1f);
                float hue2 = (float) ((i + 1) * 0.06f + j * 0.04f + rotation * 0.1f);
                float cx1 = (float) Math.sin(hue1 * Math.PI * 2);
                float cy1 = (float) Math.cos(hue1 * Math.PI * 3);
                float cx2 = (float) Math.sin(hue2 * Math.PI * 2);
                float cy2 = (float) Math.cos(hue2 * Math.PI * 3);
                float ar1 = 0.5f + 0.5f * cx1, ag1 = 0.3f + 0.7f * cy1, ab1 = 0.5f + 0.5f * (float) Math.sin(hue1 * Math.PI * 4);
                float ar2 = 0.5f + 0.5f * cx2, ag2 = 0.3f + 0.7f * cy2, ab2 = 0.5f + 0.5f * (float) Math.sin(hue2 * Math.PI * 4);
                quad(vc,
                        ex + r1 * c1, ey + y1, ez + r1 * s1,
                        ex + r2 * c1, ey + y2, ez + r2 * s1,
                        ex + r2 * c2, ey + y2, ez + r2 * s2,
                        ex + r1 * c2, ey + y1, ez + r1 * s2,
                        ar1, ag1, ab1, alpha * 0.7f);
            }
        }
    }

    private static void renderCrystalGlow(VertexConsumer vc, float ex, float ey, float ez,
                                           float sz, float alpha, float r, float g, float b,
                                           float glowIntensity, float rotation) {
        float glowAlpha = alpha * 0.2f * glowIntensity;
        float glowRad = sz * 1.8f;
        for (int i = 0; i < 36; i++) {
            float a1 = (float) (2 * Math.PI * i / 36 + rotation * 0.2f);
            float a2 = (float) (2 * Math.PI * (i + 1) / 36 + rotation * 0.2f);
            float x1 = ex + glowRad * (float) Math.cos(a1), z1 = ez + glowRad * (float) Math.sin(a1);
            float x2 = ex + glowRad * (float) Math.cos(a2), z2 = ez + glowRad * (float) Math.sin(a2);
            float ix1 = ex + sz * 0.6f * (float) Math.cos(a1), iz1 = ez + sz * 0.6f * (float) Math.sin(a1);
            float ix2 = ex + sz * 0.6f * (float) Math.cos(a2), iz2 = ez + sz * 0.6f * (float) Math.sin(a2);
            float flicker = 0.8f + 0.2f * (float) Math.sin(i * 1.5f + rotation);
            quad(vc,
                    ix1, ey, iz1, x1, ey, z1, x2, ey, z2, ix2, ey, iz2,
                    r, g, b, glowAlpha * flicker);
        }
    }

    private static void renderCrystalSparkles(VertexConsumer vc, float ex, float ey, float ez,
                                               float sz, float alpha, float r, float g, float b,
                                               float p, float rotation) {
        int sparkleCount = 30;
        float spAlpha = alpha * 0.9f;
        for (int i = 0; i < sparkleCount; i++) {
            float ti = (float) i / sparkleCount;
            float phi = (float) (Math.PI * ti);
            float theta = (float) (2 * Math.PI * (i * 0.618f) + rotation);
            float rs = sz * 1.02f;
            float sx = ex + rs * (float) Math.sin(phi) * (float) Math.cos(theta);
            float sy = ey + rs * (float) Math.cos(phi);
            float sz2 = ez + rs * (float) Math.sin(phi) * (float) Math.sin(theta);
            float sparkle = 0.5f + 0.5f * (float) Math.sin(i * 2.7f + p * 15f);
            float sa = spAlpha * sparkle * sparkle;
            float ss = sz * 0.025f;
            float sr2 = r * 1.3f, sg2 = g * 1.3f, sb2 = b * 1.3f;
            quad(vc,
                    sx - ss, sy - ss, sz2 - ss,
                    sx + ss, sy - ss, sz2 - ss,
                    sx + ss, sy + ss, sz2 + ss,
                    sx - ss, sy + ss, sz2 + ss,
                    sr2, sg2, sb2, sa);
        }
    }

    // ════════════════════════════════════════════════════════════
    // ★ 工具
    // ════════════════════════════════════════════════════════════

    public static int getActiveCount() { return effects.size(); }

    private static int[] parseHexColor(String hex) {
        int[] rgb = {0x88, 0xCC, 0xFF};
        if (hex == null || hex.length() < 6) return rgb;
        try {
            rgb[0] = Integer.parseInt(hex.substring(0, 2), 16);
            rgb[1] = Integer.parseInt(hex.substring(2, 4), 16);
            rgb[2] = Integer.parseInt(hex.substring(4, 6), 16);
        } catch (Exception ignored) {}
        return rgb;
    }

    private static float parseFloatSafe(String s, float def) {
        try { return Float.parseFloat(s); } catch (Exception e) { return def; }
    }

    private static class ShaderEffect {
        final ShaderType type;
        final Vec3 pos;
        final float intensity;
        final int duration;
        final String extraConfig;
        int elapsed = 0;
        float progress = 0f;
        ShaderEffect(ShaderType type, Vec3 pos, float intensity, int duration, String extraConfig) {
            this.type = type; this.pos = pos; this.intensity = intensity;
            this.duration = duration; this.extraConfig = extraConfig;
        }
        ShaderEffect(ShaderType type, Vec3 pos, float intensity, int duration) {
            this(type, pos, intensity, duration, "");
        }
    }
}
