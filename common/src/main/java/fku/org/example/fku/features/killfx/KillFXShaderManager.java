package fku.org.example.fku.features.killfx; /* water */

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import fku.org.example.fku.Fku;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * KillFX 位置着色器特效管理器
 *
 * 支持：
 * - 黑洞（BLACKHOLE）：GLSL光线追踪黑洞（吸积盘+光子环+引力透镜）
 * - 水晶（CRYSTAL）：BufferBuilder菲涅尔球体（4种风格）
 * - 天光光束（SKY_BEAM）：从天而降的光柱（GLSL）
 * - 天光环（SKY_RING）：旋转光环（GLSL）
 *
 * ★ 参考：ShaderTest (https://github.com/seliaYYDS/ShaderTest)
 */
public class KillFXShaderManager {

    private static final Minecraft mc = Minecraft.getInstance();
    private static final LinkedList<ShaderEffect> effects = new LinkedList<>();

    public enum ShaderType { NONE, BLACKHOLE, CRYSTAL, SKY_BEAM, SKY_RING, HYPERNOVA, RAY_BURST }

    /**
     * 触发特效 — 携带完整配置参数
     * @param type         特效类型
     * @param pos          死亡位置
     * @param intensity    强度 (BLACKHOLE: 黑洞强度, CRYSTAL: 发光强度)
     * @param durationTicks 持续 tick
     * @param extraConfig  额外参数JSON字符串（水晶用：style,tintColor,radius,rotationSpeed,pulse）
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

    // ★ 黑洞渲染（GLSL着色器）：ShaderTest移植
    //   使用球体网格 + 自定义GLSL着色器，在屏幕空间做光线追踪
    private static void renderBlackhole(PoseStack ps, float ex, float ey, float ez, float p, ShaderEffect effect, float partialTick) {
        ShaderInstance shader = KillFXShaderRegistry.getBlackHoleShader();
        if (shader == null) return;

        // ★ 从 extraConfig 读取黑洞缩放（允许用户调整大小）
        float scaleMul = 1.0f;
        if (effect.extraConfig != null && !effect.extraConfig.isEmpty()) {
            try { scaleMul = Float.parseFloat(effect.extraConfig); } catch (Exception ignored) {}
        }
        float sc = effect.intensity * scaleMul;
        float sz = 1.5f * sc * Math.min(p * 2.5f, (1f - p) * 2f + 0.3f);
        if (sz <= 0.001f) return;

        Minecraft minecraft = Minecraft.getInstance();
        RenderTarget mainTarget = minecraft.getMainRenderTarget();

        // 1. 备份屏幕颜色和深度
        ensureTargets(mainTarget);
        copyColor(mainTarget, sceneCopyTarget);
        copyDepth(mainTarget, opaqueDepthTarget);
        mainTarget.bindWrite(false);

        // 2. 设置着色器采样器
        shader.setSampler("DiffuseSampler", sceneCopyTarget.getColorTextureId());
        shader.setSampler("DepthSampler", opaqueDepthTarget.getDepthTextureId());

        // 3. 设置uniforms
        if (shader.getUniform("ScreenSize") != null) {
            shader.getUniform("ScreenSize").set((float) mainTarget.width, (float) mainTarget.height);
        }
        float time = minecraft.level != null ? (minecraft.level.getGameTime() + partialTick) * 0.04f : 0f;
        if (shader.getUniform("Time") != null) {
            shader.getUniform("Time").set(time);
        }

        // 4. 构建模型矩阵
        PoseStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushPose();
        modelViewStack.setIdentity();
        modelViewStack.mulPoseMatrix(ps.last().pose());
        RenderSystem.applyModelViewMatrix();

        // 5. 设置球体变换
        modelViewStack.translate(ex, ey, ez);
        modelViewStack.scale(sz, sz, sz);

        // 适应吸积盘朝向
        Quaternionf diskRot = new Quaternionf()
                .rotateZ((float) Math.toRadians(10.0))
                .rotateX((float) Math.toRadians(-22.0));
        modelViewStack.mulPose(diskRot);
        RenderSystem.applyModelViewMatrix();

        // 6. 计算相机在物体空间的位置
        Vec3 camPos = mc.gameRenderer.getMainCamera().getPosition();
        Vector3f camObjSpace = new Vector3f(
                (float)(camPos.x - effect.pos.x) / sz,
                (float)(camPos.y - effect.pos.y) / sz,
                (float)(camPos.z - effect.pos.z) / sz
        );
        new Quaternionf(diskRot).conjugate().transform(camObjSpace);

        if (shader.getUniform("CameraPos") != null) {
            shader.getUniform("CameraPos").set(camObjSpace.x, camObjSpace.y, camObjSpace.z);
        }

        // 7. 渲染球体网格
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        Tesselator t = Tesselator.getInstance();
        BufferBuilder buf = t.getBuilder();
        buf.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

        int latSegs = 32, lonSegs = 48;
        for (int lat = 0; lat < latSegs; lat++) {
            float v0 = (float)lat / latSegs, v1 = (float)(lat+1) / latSegs;
            float th0 = (v0 - 0.5f) * (float)Math.PI, th1 = (v1 - 0.5f) * (float)Math.PI;
            for (int lon = 0; lon < lonSegs; lon++) {
                float u0 = (float)lon / lonSegs, u1 = (float)(lon+1) / lonSegs;
                float ph0 = u0 * (float)Math.PI * 2, ph1 = u1 * (float)Math.PI * 2;
                addSphereVertex(buf, th0, ph0);
                addSphereVertex(buf, th1, ph0);
                addSphereVertex(buf, th1, ph1);
                addSphereVertex(buf, th0, ph0);
                addSphereVertex(buf, th1, ph1);
                addSphereVertex(buf, th0, ph1);
            }
        }

        RenderSystem.setShader(() -> shader);
        BufferUploader.drawWithShader(buf.end());

        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        modelViewStack.popPose();
        RenderSystem.applyModelViewMatrix();
    }

    private static void addSphereVertex(BufferBuilder buf, float theta, float phi) {
        float ct = Mth.cos(theta);
        float x = ct * Mth.cos(phi);
        float y = Mth.sin(theta);
        float z = ct * Mth.sin(phi);
        buf.vertex(x, y, z).color(255, 255, 255, 255).endVertex();
    }

    // ─── 帧缓冲管理 ───
    private static TextureTarget sceneCopyTarget;
    private static TextureTarget opaqueDepthTarget;

    private static void ensureTargets(RenderTarget main) {
        if (sceneCopyTarget == null || sceneCopyTarget.width != main.width || sceneCopyTarget.height != main.height) {
            sceneCopyTarget = new TextureTarget(main.width, main.height, false, Minecraft.ON_OSX);
            sceneCopyTarget.setFilterMode(9729);
        }
        if (opaqueDepthTarget == null || opaqueDepthTarget.width != main.width || opaqueDepthTarget.height != main.height) {
            opaqueDepthTarget = new TextureTarget(main.width, main.height, true, Minecraft.ON_OSX);
            opaqueDepthTarget.setFilterMode(9728);
        }
    }

    private static void copyColor(RenderTarget src, RenderTarget dst) {
        RenderSystem.assertOnRenderThreadOrInit();
        GlStateManager._glBindFramebuffer(36008, src.frameBufferId);
        GlStateManager._glBindFramebuffer(36009, dst.frameBufferId);
        GlStateManager._glBlitFrameBuffer(0, 0, src.width, src.height, 0, 0, dst.width, dst.height, 16384, 9728);
        GlStateManager._glBindFramebuffer(36160, 0);
    }

    private static void copyDepth(RenderTarget src, RenderTarget dst) {
        RenderSystem.assertOnRenderThreadOrInit();
        GlStateManager._glBindFramebuffer(36008, src.frameBufferId);
        GlStateManager._glBindFramebuffer(36009, dst.frameBufferId);
        GlStateManager._glBlitFrameBuffer(0, 0, src.width, src.height, 0, 0, dst.width, dst.height, 256, 9728);
        GlStateManager._glBindFramebuffer(36160, 0);
    }

    public static void renderEffects(PoseStack poseStack, float partialTick) {
        if (effects.isEmpty() || mc.player == null || mc.level == null) return;

        // 每个效果独立渲染
        for (ShaderEffect effect : effects) {
            float p = effect.progress;

            switch (effect.type) {
                case CRYSTAL -> {
                    Vec3 camPos = mc.gameRenderer.getMainCamera().getPosition();
                    float ex = (float)(effect.pos.x - camPos.x);
                    float ey = (float)(effect.pos.y - camPos.y);
                    float ez = (float)(effect.pos.z - camPos.z);
                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                    RenderSystem.disableDepthTest();
                    RenderSystem.setShader(GameRenderer::getPositionColorShader);
                    renderCrystal(poseStack, ex, ey, ez, p, effect, partialTick);
                    RenderSystem.enableDepthTest();
                    RenderSystem.disableBlend();
                }
                case BLACKHOLE -> {
                    renderBlackhole(poseStack, 0, 0, 0, p, effect, partialTick);
                }
                case SKY_BEAM -> {
                    Vec3 camPos = mc.gameRenderer.getMainCamera().getPosition();
                    float ex = (float)(effect.pos.x - camPos.x);
                    float ey = (float)(effect.pos.y - camPos.y);
                    float ez = (float)(effect.pos.z - camPos.z);
                    renderSkyBeam(poseStack, ex, ey, ez, p, effect, partialTick);
                }
                case SKY_RING -> {
                    Vec3 camPos = mc.gameRenderer.getMainCamera().getPosition();
                    float ex = (float)(effect.pos.x - camPos.x);
                    float ey = (float)(effect.pos.y - camPos.y);
                    float ez = (float)(effect.pos.z - camPos.z);
                    renderSkyRing(poseStack, ex, ey, ez, p, effect, partialTick);
                }
                case HYPERNOVA -> {
                    Vec3 camPos = mc.gameRenderer.getMainCamera().getPosition();
                    float ex = (float)(effect.pos.x - camPos.x);
                    float ey = (float)(effect.pos.y - camPos.y);
                    float ez = (float)(effect.pos.z - camPos.z);
                    renderHypernova(poseStack, ex, ey, ez, p, effect);
                }
                case RAY_BURST -> {
                    Vec3 camPos = mc.gameRenderer.getMainCamera().getPosition();
                    float ex = (float)(effect.pos.x - camPos.x);
                    float ey = (float)(effect.pos.y - camPos.y);
                    float ez = (float)(effect.pos.z - camPos.z);
                    renderRayBurst(poseStack, ex, ey, ez, p, effect);
                }
            }
        }
    }

    // ════════════════════════════════════════════════════════════
    // ★ 超新星爆炸（Gemini 移植）：辉光球体 + 膨胀 + 淡出
    // ════════════════════════════════════════════════════════════
    private static void renderHypernova(PoseStack ps, float ex, float ey, float ez,
                                         float p, ShaderEffect effect) {
        float sc = effect.intensity;
        float fadeIn = Math.min(p * 4f, 1f);
        float fadeOut = Math.max(1f - (p - 0.5f) / 0.5f, 0f);
        float alpha = fadeIn * fadeOut;
        if (alpha < 0.01f) return;

        float sz = 1.5f * sc * (0.3f + p * 1.2f);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator t = Tesselator.getInstance();
        BufferBuilder buf = t.getBuilder();
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        int stacks = 12, slices = 16;
        for (int i = 0; i < stacks; i++) {
            float ph1 = (float)(Math.PI * i / stacks);
            float ph2 = (float)(Math.PI * (i + 1) / stacks);
            float r1 = sz * (float)Math.sin(ph1);
            float r2 = sz * (float)Math.sin(ph2);
            float y1 = sz * (float)Math.cos(ph1);
            float y2 = sz * (float)Math.cos(ph2);
            for (int j = 0; j < slices; j++) {
                float th1 = (float)(2 * Math.PI * j / slices);
                float th2 = (float)(2 * Math.PI * (j + 1) / slices);
                float c1 = (float)Math.cos(th1), s1 = (float)Math.sin(th1);
                float c2 = (float)Math.cos(th2), s2 = (float)Math.sin(th2);
                float layer = (float)i / stacks;
                float br = 1f - layer * 0.3f;
                float ba = alpha * (1f - layer * 0.4f);
                buf.vertex(ps.last().pose(), ex+r1*c1, ey+y1, ez+r1*s1).color(br,br*0.8f,br*0.3f,ba).endVertex();
                buf.vertex(ps.last().pose(), ex+r2*c1, ey+y2, ez+r2*s1).color(br,br*0.8f,br*0.3f,ba).endVertex();
                buf.vertex(ps.last().pose(), ex+r2*c2, ey+y2, ez+r2*s2).color(br,br*0.8f,br*0.3f,ba).endVertex();
                buf.vertex(ps.last().pose(), ex+r1*c2, ey+y1, ez+r1*s2).color(br,br*0.8f,br*0.3f,ba).endVertex();
            }
        }
        t.end();

        // 外层光晕壳
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        int outerStacks = 8;
        float outerSz = sz * 1.8f;
        for (int i = 0; i < outerStacks; i++) {
            float ph1 = (float)(Math.PI * i / outerStacks);
            float ph2 = (float)(Math.PI * (i + 1) / outerStacks);
            float r1 = outerSz * (float)Math.sin(ph1);
            float r2 = outerSz * (float)Math.sin(ph2);
            float y1 = outerSz * (float)Math.cos(ph1);
            float y2 = outerSz * (float)Math.cos(ph2);
            for (int j = 0; j < slices; j++) {
                float th1 = (float)(2 * Math.PI * j / slices);
                float th2 = (float)(2 * Math.PI * (j + 1) / slices);
                float c1=(float)Math.cos(th1),s1=(float)Math.sin(th1),c2=(float)Math.cos(th2),s2=(float)Math.sin(th2);
                float f = 0.5f+0.5f*(float)Math.sin(i*1.3f+j*0.7f+p*8f);
                buf.vertex(ps.last().pose(), ex+r1*c1, ey+y1, ez+r1*s1).color(1f,0.5f,0f,alpha*0.2f*f).endVertex();
                buf.vertex(ps.last().pose(), ex+r2*c1, ey+y2, ez+r2*s1).color(1f,0.5f,0f,alpha*0.2f*f).endVertex();
                buf.vertex(ps.last().pose(), ex+r2*c2, ey+y2, ez+r2*s2).color(1f,0.5f,0f,alpha*0.2f*f).endVertex();
                buf.vertex(ps.last().pose(), ex+r1*c2, ey+y1, ez+r1*s2).color(1f,0.5f,0f,alpha*0.2f*f).endVertex();
            }
        }
        t.end();

        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    // ════════════════════════════════════════════════════════════
    // ★ 光线爆发（Gemini 移植）：从死亡点向外辐射的光线
    // ════════════════════════════════════════════════════════════
    private static void renderRayBurst(PoseStack ps, float ex, float ey, float ez,
                                        float p, ShaderEffect effect) {
        float sc = effect.intensity;
        float fadeIn = Math.min(p * 3f, 1f);
        float fadeOut = Math.max(1f - (p - 0.6f) / 0.4f, 0f);
        float alpha = fadeIn * fadeOut;
        if (alpha < 0.01f) return;

        float len = 4f * sc * (0.5f + p * 0.5f);
        float rot = p * 4f;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator t = Tesselator.getInstance();
        BufferBuilder buf = t.getBuilder();
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        int rays = 36;
        for (int i = 0; i < rays; i++) {
            float ang = (float)(2*Math.PI*i/rays + rot);
            float dx = (float)Math.cos(ang), dz = (float)Math.sin(ang);
            float r = 0.2f+0.8f*(float)Math.sin(i*0.3f);
            float g = 0.2f+0.6f*(float)Math.cos(i*0.5f);
            float b = 0.4f+0.6f*(float)Math.sin(i*0.7f);
            float ra = alpha * (0.2f+0.8f*(float)Math.sin(i*1.7f+p*6f));
            float w = 0.06f * sc;
            float hw = w*0.5f;
            buf.vertex(ps.last().pose(), ex-dx*hw, ey-w, ez-dz*hw).color(r,g,b,ra).endVertex();
            buf.vertex(ps.last().pose(), ex-dx*hw, ey+w, ez-dz*hw).color(r,g,b,ra).endVertex();
            buf.vertex(ps.last().pose(), ex+dx*len, ey+w*0.3f, ez+dz*len).color(r,g,b,0f).endVertex();
            buf.vertex(ps.last().pose(), ex+dx*len, ey-w*0.3f, ez+dz*len).color(r,g,b,0f).endVertex();
        }
        t.end();

        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    // ─── 空间扭曲光晕 ───
    private static void renderDistortion(PoseStack ps, float ex, float ey, float ez, float sz, float p, float sc) {
        float dist = 5f * sc * (1f - p * 0.5f);
        float a = (1f - p) * 0.03f;
        Tesselator t = Tesselator.getInstance();
        BufferBuilder b = t.getBuilder();
        b.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        b.vertex(ps.last().pose(), ex, ey, ez).color(0.2f, 0.1f, 0.5f, 0f).endVertex();
        for (int i = 0; i <= 16; i++) {
            float ang = (float)(2 * Math.PI * i / 16);
            b.vertex(ps.last().pose(), ex + dist * (float)Math.cos(ang), ey, ez + dist * (float)Math.sin(ang))
                .color(0.2f, 0.1f, 0.5f, a).endVertex();
        }
        t.end();
    }

    // ─── 黑洞球体 ───
    private static void renderSphere(PoseStack ps, float ex, float ey, float ez, float sz, float p) {
        Tesselator t = Tesselator.getInstance();
        BufferBuilder b = t.getBuilder();
        b.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        float alpha = 1f - p * 0.5f;
        int stacks = 12, slices = 16;
        for (int i = 0; i < stacks; i++) {
            float ph1 = (float)(Math.PI * i / stacks), ph2 = (float)(Math.PI * (i + 1) / stacks);
            float r1 = sz * (float)Math.sin(ph1), r2 = sz * (float)Math.sin(ph2);
            float y1 = sz * (float)Math.cos(ph1), y2 = sz * (float)Math.cos(ph2);
            float br = i > stacks/2 ? 0.05f : 0.02f;
            for (int j = 0; j < slices; j++) {
                float t1 = (float)(2 * Math.PI * j / slices), t2 = (float)(2 * Math.PI * (j + 1) / slices);
                b.vertex(ps.last().pose(), ex + r1*(float)Math.cos(t1), ey+y1, ez + r1*(float)Math.sin(t1)).color(br,br,br,alpha).endVertex();
                b.vertex(ps.last().pose(), ex + r2*(float)Math.cos(t1), ey+y2, ez + r2*(float)Math.sin(t1)).color(br,br,br,alpha).endVertex();
                b.vertex(ps.last().pose(), ex + r2*(float)Math.cos(t2), ey+y2, ez + r2*(float)Math.sin(t2)).color(br,br,br,alpha).endVertex();
                b.vertex(ps.last().pose(), ex + r1*(float)Math.cos(t2), ey+y1, ez + r1*(float)Math.sin(t2)).color(br,br,br,alpha).endVertex();
            }
        }
        t.end();
    }

    // ─── 吸积盘 ───
    private static void renderDisk(PoseStack ps, float ex, float ey, float ez, float sz, float p, float sc) {
        Tesselator t = Tesselator.getInstance();
        BufferBuilder b = t.getBuilder();
        b.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        float alpha = 1f - p;
        float rot = p * 6f;
        for (int ring = 0; ring < 4; ring++) {
            float rr = sz * (0.6f + ring * 0.25f);
            float rh = sz * (0.04f + ring * 0.02f);
            float ra = alpha * (1f - ring * 0.15f);
            float r, g, bl;
            if (ring==0) { r=1f; g=0.8f; bl=0.6f; }
            else if (ring==1) { r=0.6f; g=0.3f; bl=1f; }
            else if (ring==2) { r=0.4f; g=0.2f; bl=0.8f; }
            else { r=0.3f; g=0.1f; bl=0.5f; }
            for (int i = 0; i < 36; i++) {
                float a1 = (float)(2*Math.PI*i/36 + rot + ring*0.5f);
                float a2 = (float)(2*Math.PI*(i+1)/36 + rot + ring*0.5f);
                float h1 = rh * (1f + (float)Math.sin(i*1.5)*0.5f);
                float h2 = rh * (1f + (float)Math.sin((i+1)*1.5)*0.5f);
                float x1 = ex + rr*(float)Math.cos(a1), z1 = ez + rr*(float)Math.sin(a1);
                float x2 = ex + rr*(float)Math.cos(a2), z2 = ez + rr*(float)Math.sin(a2);
                float ix1 = ex + rr*0.85f*(float)Math.cos(a1), iz1 = ez + rr*0.85f*(float)Math.sin(a1);
                float ix2 = ex + rr*0.85f*(float)Math.cos(a2), iz2 = ez + rr*0.85f*(float)Math.sin(a2);
                b.vertex(ps.last().pose(), ix1, ey-h1, iz1).color(r,g,bl,ra*0.6f).endVertex();
                b.vertex(ps.last().pose(), x1, ey-h2, z1).color(r,g,bl,ra*0.8f).endVertex();
                b.vertex(ps.last().pose(), x2, ey-h2, z2).color(r,g,bl,ra*0.8f).endVertex();
                b.vertex(ps.last().pose(), ix2, ey-h1, iz2).color(r,g,bl,ra*0.6f).endVertex();
                b.vertex(ps.last().pose(), ix1, ey+h1, iz1).color(r,g,bl,ra*0.3f).endVertex();
                b.vertex(ps.last().pose(), x1, ey+h2, z1).color(r,g,bl,ra*0.4f).endVertex();
                b.vertex(ps.last().pose(), x2, ey+h2, z2).color(r,g,bl,ra*0.4f).endVertex();
                b.vertex(ps.last().pose(), ix2, ey+h1, iz2).color(r,g,bl,ra*0.3f).endVertex();
            }
        }
        // 赤道光环
        for (int i = 0; i < 48; i++) {
            float a1 = (float)(2*Math.PI*i/48+rot), a2 = (float)(2*Math.PI*(i+1)/48+rot);
            float gx1 = ex + sz*0.55f*(float)Math.cos(a1), gz1 = ez + sz*0.55f*(float)Math.sin(a1);
            float gx2 = ex + sz*0.55f*(float)Math.cos(a2), gz2 = ez + sz*0.55f*(float)Math.sin(a2);
            float ga = alpha * (0.6f+0.4f*(float)Math.sin(i*3+p*8));
            b.vertex(ps.last().pose(), gx1, ey, gz1).color(1f,0.9f,0.7f,ga).endVertex();
            b.vertex(ps.last().pose(), gx2, ey, gz2).color(1f,0.9f,0.7f,ga).endVertex();
            b.vertex(ps.last().pose(), gx2, ey+sz*0.02f, gz2).color(1f,0.8f,0.5f,ga*0.5f).endVertex();
            b.vertex(ps.last().pose(), gx1, ey+sz*0.02f, gz1).color(1f,0.8f,0.5f,ga*0.5f).endVertex();
        }
        t.end();
    }

    // ─── 引力透镜 ───
    private static void renderLensing(PoseStack ps, float ex, float ey, float ez, float sz, float p, float sc) {
        Tesselator t = Tesselator.getInstance();
        BufferBuilder b = t.getBuilder();
        b.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        float alpha = (1f-p)*0.3f*sc;
        for (int layer = 0; layer < 3; layer++) {
            float lr = sz*2.5f*(0.8f+layer*0.3f);
            float la = alpha*(1f-layer*0.25f);
            float rot = p*3f+layer;
            for (int i = 0; i < 24; i++) {
                float a1 = (float)(2*Math.PI*i/24+rot), a2 = (float)(2*Math.PI*(i+1)/24+rot);
                float x1=ex+lr*(float)Math.cos(a1),z1=ez+lr*(float)Math.sin(a1);
                float x2=ex+lr*(float)Math.cos(a2),z2=ez+lr*(float)Math.sin(a2);
                float ix1=ex+lr*0.7f*(float)Math.cos(a1),iz1=ez+lr*0.7f*(float)Math.sin(a1);
                float ix2=ex+lr*0.7f*(float)Math.cos(a2),iz2=ez+lr*0.7f*(float)Math.sin(a2);
                float cr=0.3f+0.2f*(float)Math.cos(i*0.5f+layer);
                float cg=0.1f+0.1f*(float)Math.sin(i*0.7f);
                float cb=0.6f+0.3f*(float)Math.sin(i*0.5f+layer);
                b.vertex(ps.last().pose(),ix1,ey-sz*0.3f,iz1).color(cr,cg,cb,la*0.3f).endVertex();
                b.vertex(ps.last().pose(),x1,ey-sz*0.3f,z1).color(cr,cg,cb,la).endVertex();
                b.vertex(ps.last().pose(),x2,ey-sz*0.3f,z2).color(cr,cg,cb,la).endVertex();
                b.vertex(ps.last().pose(),ix2,ey-sz*0.3f,iz2).color(cr,cg,cb,la*0.3f).endVertex();
            }
        }
        t.end();
    }

    // ─── 落入粒子 ───
    private static void renderParticles(PoseStack ps, float ex, float ey, float ez, float sz, float p, float sc) {
        Tesselator t = Tesselator.getInstance();
        BufferBuilder b = t.getBuilder();
        b.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        float alpha = (1f-p)*0.7f;
        for (int i = 0; i < 48; i++) {
            float ti = (float)i/48;
            float ang = ti*(float)(2*Math.PI)+p*8f;
            float sr = sz*(0.3f+0.7f*(1f-p*0.8f))*(1f-ti*0.6f);
            float py = ey+(float)Math.sin(ti*10f+p*5f)*sz*0.05f;
            float px = ex+sr*(float)Math.cos(ang), pz = ez+sr*(float)Math.sin(ang);
            float pr=0.2f+0.8f*(1f-ti), pg=0.2f+0.6f*(float)Math.sin(ti*3f), pb=0.4f+0.6f*(1f-ti);
            float pa = alpha*(0.3f+0.7f*(float)Math.sin(i*0.7f+p*10f));
            float psz = sz*0.02f;
            b.vertex(ps.last().pose(), px-psz, py, pz-psz).color(pr,pg,pb,pa).endVertex();
            b.vertex(ps.last().pose(), px+psz, py, pz-psz).color(pr,pg,pb,pa).endVertex();
            b.vertex(ps.last().pose(), px+psz, py, pz+psz).color(pr,pg,pb,pa).endVertex();
            b.vertex(ps.last().pose(), px-psz, py, pz+psz).color(pr,pg,pb,pa).endVertex();
        }
        t.end();
    }

    public static int getActiveCount() { return effects.size(); }

    /**
     * 解析水晶RGB色调
     */
    private static int[] parseHexColor(String hex) {
        int[] rgb = {0x88, 0xCC, 0xFF}; // 默认蓝晶
        if (hex == null || hex.length() < 6) return rgb;
        try {
            rgb[0] = Integer.parseInt(hex.substring(0, 2), 16);
            rgb[1] = Integer.parseInt(hex.substring(2, 4), 16);
            rgb[2] = Integer.parseInt(hex.substring(4, 6), 16);
        } catch (Exception ignored) {}
        return rgb;
    }

    // ════════════════════════════════════════════════════════
    // ★ 天光光束渲染（GLSL着色器）ShaderTest移植
    //   圆柱体网格 + 高度渐变透明度
    // ════════════════════════════════════════════════════════

    private static void renderSkyBeam(PoseStack ps, float ex, float ey, float ez,
                                        float p, ShaderEffect effect, float partialTick) {
        ShaderInstance shader = KillFXShaderRegistry.getSkyBeamShader();
        if (shader == null) return;

        // ★ 从 extraConfig 读取大小（格式: BEAM,<size>）
        float sizeMul = 1.0f;
        if (effect.extraConfig != null && effect.extraConfig.startsWith("BEAM,")) {
            try { sizeMul = Float.parseFloat(effect.extraConfig.split(",")[1]); } catch (Exception ignored) {}
        }
        float sc = effect.intensity * sizeMul;
        float fadeIn = Math.min(p * 3f, 1f);
        float fadeOut = Math.max(1f - (p - 0.6f) / 0.4f, 0f);
        float alpha = fadeIn * fadeOut;
        if (sc <= 0.01f || alpha <= 0.01f) return;

        // ★ 修正定位：height 向上延伸（从天而降），光束底部在死亡位置上方
        float height = 16.0f * sc;
        float radius = 1.2f * sc;

        // 设置uniforms
        float gameTime = mc.level != null ? (mc.level.getGameTime() + partialTick) * 0.04f : 0f;
        if (shader.getUniform("Time") != null) shader.getUniform("Time").set(gameTime);
        if (shader.getUniform("BeamHeight") != null) shader.getUniform("BeamHeight").set(height);
        if (shader.getUniform("BeamRadius") != null) shader.getUniform("BeamRadius").set(radius);
        if (shader.getUniform("CoreRadius") != null) shader.getUniform("CoreRadius").set(radius * 0.2f);
        if (shader.getUniform("Intensity") != null) shader.getUniform("Intensity").set(sc);
        if (shader.getUniform("RevealFraction") != null) shader.getUniform("RevealFraction").set(fadeIn);

        // ★ 构建模型矩阵：光束底部在死亡位置（ey），向上延伸 height
        PoseStack mvStack = RenderSystem.getModelViewStack();
        mvStack.pushPose();
        mvStack.setIdentity();
        mvStack.mulPoseMatrix(ps.last().pose());
        mvStack.translate(ex, ey, ez);
        RenderSystem.applyModelViewMatrix();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(() -> shader);

        // 圆柱体网格（8段高度 × 12段环绕）
        Tesselator t = Tesselator.getInstance();
        BufferBuilder buf = t.getBuilder();
        buf.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

        int hSegs = 8, rSegs = 12;
        for (int h = 0; h < hSegs; h++) {
            float y0 = height * (float)h / hSegs, y1 = height * (float)(h+1) / hSegs;
            for (int r = 0; r < rSegs; r++) {
                float a0 = (float)(2 * Math.PI * r / rSegs), a1 = (float)(2 * Math.PI * (r+1) / rSegs);
                float c0 = (float)Math.cos(a0), s0 = (float)Math.sin(a0);
                float c1 = (float)Math.cos(a1), s1 = (float)Math.sin(a1);
                int a = (int)(alpha * 255);
                buf.vertex(radius*c0, y0, radius*s0).color(255, 255, 255, a).endVertex();
                buf.vertex(radius*c1, y0, radius*s1).color(255, 255, 255, a).endVertex();
                buf.vertex(radius*c1, y1, radius*s1).color(255, 255, 255, a).endVertex();
                buf.vertex(radius*c0, y0, radius*s0).color(255, 255, 255, a).endVertex();
                buf.vertex(radius*c1, y1, radius*s1).color(255, 255, 255, a).endVertex();
                buf.vertex(radius*c0, y1, radius*s0).color(255, 255, 255, a).endVertex();
            }
        }
        BufferUploader.drawWithShader(buf.end());

        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        mvStack.popPose();
        RenderSystem.applyModelViewMatrix();
    }

    // ════════════════════════════════════════════════════════
    // ★ 天光环渲染（GLSL着色器）ShaderTest移植
    //   环状网格 + 旋转辉光
    // ════════════════════════════════════════════════════════

    private static void renderSkyRing(PoseStack ps, float ex, float ey, float ez,
                                       float p, ShaderEffect effect, float partialTick) {
        ShaderInstance shader = KillFXShaderRegistry.getSkyRingShader();
        if (shader == null) return;

        // ★ 从 extraConfig 读取大小
        float sizeMul = 1.0f;
        if (effect.extraConfig != null && effect.extraConfig.startsWith("RING,")) {
            try { sizeMul = Float.parseFloat(effect.extraConfig.split(",")[1]); } catch (Exception ignored) {}
        }
        float sc = effect.intensity * sizeMul;
        float fadeIn = Math.min(p * 3f, 1f);
        float fadeOut = Math.max(1f - (p - 0.6f) / 0.4f, 0f);
        float alpha = fadeIn * fadeOut;
        if (sc <= 0.01f || alpha <= 0.01f) return;

        // ★ 渐变动画：从0.3倍逐渐扩大到1.0倍（前40%时间完成）
        float growProgress = Math.min(p / 0.4f, 1.0f);
        float growScale = 0.3f + 0.7f * growProgress;
        float baseSize = 2.0f * sc * growScale;
        float outerR = 2.0f * baseSize, innerR = 1.0f * baseSize;

        float gameTime = mc.level != null ? (mc.level.getGameTime() + partialTick) * 0.04f : 0f;
        if (shader.getUniform("Time") != null) shader.getUniform("Time").set(gameTime);
        if (shader.getUniform("OuterRadius") != null) shader.getUniform("OuterRadius").set(outerR);
        if (shader.getUniform("InnerRadius") != null) shader.getUniform("InnerRadius").set(innerR);
        if (shader.getUniform("Softness") != null) shader.getUniform("Softness").set(0.3f);
        if (shader.getUniform("Intensity") != null) shader.getUniform("Intensity").set(sc);
        if (shader.getUniform("RingPlane") != null) shader.getUniform("RingPlane").set(0.0f);

        PoseStack mvStack = RenderSystem.getModelViewStack();
        mvStack.pushPose();
        mvStack.setIdentity();
        mvStack.mulPoseMatrix(ps.last().pose());
        mvStack.translate(ex, ey + 0.5f * baseSize, ez);
        // 稍微倾斜环
        mvStack.mulPose(new org.joml.Quaternionf().rotateX((float)Math.toRadians(60.0)));
        RenderSystem.applyModelViewMatrix();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(() -> shader);

        // 环形网格（16段环绕 × 4段径向）
        Tesselator t = Tesselator.getInstance();
        BufferBuilder buf = t.getBuilder();
        buf.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

        int ringSegs = 16, radialSegs = 4;
        float midR = (outerR + innerR) * 0.5f;
        float halfW = (outerR - innerR) * 0.5f;
        for (int i = 0; i < ringSegs; i++) {
            float a0 = (float)(2 * Math.PI * i / ringSegs), a1 = (float)(2 * Math.PI * (i+1) / ringSegs);
            for (int j = 0; j < radialSegs; j++) {
                float t0 = -1f + 2f * j / radialSegs, t1 = -1f + 2f * (j+1) / radialSegs;
                float r0 = midR + t0 * halfW, r1 = midR + t1 * halfW;
                float c0 = (float)Math.cos(a0), s0 = (float)Math.sin(a0);
                float c1 = (float)Math.cos(a1), s1 = (float)Math.sin(a1);
                int a = (int)(alpha * 255);
                buf.vertex(r0*c0, 0, r0*s0).color(100, 150, 255, a).endVertex();
                buf.vertex(r1*c0, 0, r1*s0).color(120, 170, 255, a).endVertex();
                buf.vertex(r1*c1, 0, r1*s1).color(120, 170, 255, a).endVertex();
                buf.vertex(r0*c0, 0, r0*s0).color(100, 150, 255, a).endVertex();
                buf.vertex(r1*c1, 0, r1*s1).color(120, 170, 255, a).endVertex();
                buf.vertex(r0*c1, 0, r0*s0).color(100, 150, 255, a).endVertex();
            }
        }
        BufferUploader.drawWithShader(buf.end());

        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        mvStack.popPose();
        RenderSystem.applyModelViewMatrix();
    }

    // ════════════════════════════════════════════════════════
    // ★★★ 水晶特效渲染（参考 sphere-rendering 球体光线追踪）★★★
    // ════════════════════════════════════════════════════════

    /**
     * 水晶球体主渲染方法
     *
     * ★ 风格：
     *   CRYSTAL - 半透明晶体 + 菲涅尔反射 + 光泽
     *   BLOOM   - 发光晶体 + 光晕爆散
     *   GLASS   - 玻璃折射效果 + 边缘高亮
     *   AURORA  - 极光晶体 + 色彩渐变
     *
     * ★ 参考：sphere-rendering 的透明度后处理着色器
     *   (https://github.com/Hydrop002/sphere-rendering)
     *   借鉴其菲涅尔反射系数、色调混合、发光轮廓的核心算法
     */
    private static void renderCrystal(PoseStack ps, float ex, float ey, float ez,
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

        // 生命周期动画
        float sz = 2.0f * glowIntensity * radiusScale;
        float fadeIn = Math.min(p * 4f, 1f);                     // 前0.25快速出现
        float fadeOut = Math.max(1f - (p - 0.7f) / 0.3f, 0f);    // 后0.3淡出
        float alpha = fadeIn * fadeOut;
        float pulseScale = pulse ? 1f + 0.15f * (float)Math.sin(p * Math.PI * 6f) : 1f;
        sz *= pulseScale;

        int[] tint = parseHexColor(hexColor);
        float r = tint[0] / 255f, g = tint[1] / 255f, b = tint[2] / 255f;

        // 自转角度
        float rotation = p * rotSpeed * 8f;

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        switch (style) {
            case "BLOOM"  -> renderCrystalBloom(ps, ex, ey, ez, sz, alpha, r, g, b, rotation, glowIntensity);
            case "GLASS"  -> renderCrystalGlass(ps, ex, ey, ez, sz, alpha, r, g, b, rotation);
            case "AURORA" -> renderCrystalAurora(ps, ex, ey, ez, sz, alpha, rotation, glowIntensity);
            default       -> renderCrystalDefault(ps, ex, ey, ez, sz, alpha, r, g, b, rotation, glowIntensity);
        }

        // 通用：外围光晕 + 闪烁粒子
        renderCrystalGlow(ps, ex, ey, ez, sz, alpha, r, g, b, glowIntensity, rotation);
        renderCrystalSparkles(ps, ex, ey, ez, sz, alpha, r, g, b, p, rotation);
    }

    // ─── 默认晶体（CRYSTAL）───
    private static void renderCrystalDefault(PoseStack ps, float ex, float ey, float ez,
                                              float sz, float alpha, float r, float g, float b,
                                              float rotation, float glowIntensity) {
        Tesselator t = Tesselator.getInstance();
        BufferBuilder buf = t.getBuilder();
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        int stacks = 16, slices = 24;
        for (int i = 0; i < stacks; i++) {
            float ph1 = (float)(Math.PI * i / stacks);
            float ph2 = (float)(Math.PI * (i + 1) / stacks);
            float r1 = sz * (float)Math.sin(ph1);
            float r2 = sz * (float)Math.sin(ph2);
            float y1 = sz * (float)Math.cos(ph1);
            float y2 = sz * (float)Math.cos(ph2);
            for (int j = 0; j < slices; j++) {
                float th1 = (float)(2 * Math.PI * j / slices + rotation);
                float th2 = (float)(2 * Math.PI * (j + 1) / slices + rotation);
                float c1 = (float)Math.cos(th1), s1 = (float)Math.sin(th1);
                float c2 = (float)Math.cos(th2), s2 = (float)Math.sin(th2);

                // ★ 菲涅尔反射模拟：边缘更亮
                //   参考 sphere-rendering 的 fresnel = dot(-viewDir, normal)
                //   离视角越近的顶点越透明/亮，边缘处的顶点更不透明/暗
                //   这里用 phi 角度模拟：顶部 (ph≈0) 透明度高，边缘 (ph≈π/2) 透明度低
                float fresnel1 = Math.abs((float)Math.cos(ph1));
                float fresnel2 = Math.abs((float)Math.cos(ph2));
                float a1 = alpha * (0.3f + 0.7f * fresnel1);
                float a2 = alpha * (0.3f + 0.7f * fresnel2);

                // 光照模拟：顶部稍亮，底部稍暗
                float lt1 = 0.6f + 0.4f * fresnel1;
                float lt2 = 0.6f + 0.4f * fresnel2;
                float lr1 = r * lt1, lg1 = g * lt1, lb1 = b * lt1;
                float lr2 = r * lt2, lg2 = g * lt2, lb2 = b * lt2;

                // 四边体：上半球和下半球颜色略有差异
                buf.vertex(ps.last().pose(), ex + r1*c1, ey+y1, ez + r1*s1).color(lr1, lg1, lb1, a1).endVertex();
                buf.vertex(ps.last().pose(), ex + r2*c1, ey+y2, ez + r2*s1).color(lr2, lg2, lb2, a2).endVertex();
                buf.vertex(ps.last().pose(), ex + r2*c2, ey+y2, ez + r2*s2).color(lr2, lg2, lb2, a2).endVertex();
                buf.vertex(ps.last().pose(), ex + r1*c2, ey+y1, ez + r1*s2).color(lr1, lg1, lb1, a1).endVertex();
            }
        }
        t.end();
    }

    // ─── 发光晶体（BLOOM）───
    private static void renderCrystalBloom(PoseStack ps, float ex, float ey, float ez,
                                            float sz, float alpha, float r, float g, float b,
                                            float rotation, float glowIntensity) {
        Tesselator t = Tesselator.getInstance();
        BufferBuilder buf = t.getBuilder();
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        int stacks = 12, slices = 20;
        for (int i = 0; i < stacks; i++) {
            float ph1 = (float)(Math.PI * i / stacks);
            float ph2 = (float)(Math.PI * (i + 1) / stacks);
            float r1 = sz * (float)Math.sin(ph1);
            float r2 = sz * (float)Math.sin(ph2);
            float y1 = sz * (float)Math.cos(ph1);
            float y2 = sz * (float)Math.cos(ph2);
            // ★ BLOOM 风格：发光更强，边缘更亮，类似 sphere-rendering 的 STYLE 1
            float bloomFactor = 1.5f + 0.5f * (float)Math.sin(rotation + i * 0.3f);
            float br = r * bloomFactor, bg = g * bloomFactor, bb = b * bloomFactor;
            float ba = alpha * 0.9f;
            for (int j = 0; j < slices; j++) {
                float th1 = (float)(2 * Math.PI * j / slices + rotation);
                float th2 = (float)(2 * Math.PI * (j + 1) / slices + rotation);
                float c1 = (float)Math.cos(th1), s1 = (float)Math.sin(th1);
                float c2 = (float)Math.cos(th2), s2 = (float)Math.sin(th2);
                // 发光核心：越往中心越亮
                float core = 0.7f + 0.3f * Math.abs((float)Math.cos(ph1));
                buf.vertex(ps.last().pose(), ex + r1*c1, ey+y1, ez + r1*s1).color(br*core, bg*core, bb*core, ba).endVertex();
                buf.vertex(ps.last().pose(), ex + r2*c1, ey+y2, ez + r2*s1).color(br*core, bg*core, bb*core, ba).endVertex();
                buf.vertex(ps.last().pose(), ex + r2*c2, ey+y2, ez + r2*s2).color(br*core, bg*core, bb*core, ba).endVertex();
                buf.vertex(ps.last().pose(), ex + r1*c2, ey+y1, ez + r1*s2).color(br*core, bg*core, bb*core, ba).endVertex();
            }
        }
        t.end();

        // ★ BLOOM 光爆射线：从中心射出的径向线条
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        float rayAlpha = alpha * 0.3f;
        float brL = r * 1.2f, bgL = g * 1.2f, bbL = b * 1.2f;
        for (int i = 0; i < 12; i++) {
            float ang = (float)(2 * Math.PI * i / 12 + rotation * 0.5f);
            float ra = rayAlpha * (0.5f + 0.5f * (float)Math.sin(i * 2.5f + rotation));
            float len = sz * (1.5f + 0.3f * (float)Math.sin(i * 1.7f + rotation));
            float w = sz * 0.03f;
            float dx = (float)Math.cos(ang), dz = (float)Math.sin(ang);
            float px = ex + dx * w, pz = ez + dz * w;
            float px2 = ex + dx * len, pz2 = ez + dz * len;
            buf.vertex(ps.last().pose(), px - dz*w, ey, pz + dx*w).color(brL, bgL, bbL, ra*0.6f).endVertex();
            buf.vertex(ps.last().pose(), px + dz*w, ey, pz - dx*w).color(brL, bgL, bbL, ra*0.6f).endVertex();
            buf.vertex(ps.last().pose(), px2 + dz*w*0.5f, ey, pz2 - dx*w*0.5f).color(brL, bgL, bbL, 0f).endVertex();
            buf.vertex(ps.last().pose(), px2 - dz*w*0.5f, ey, pz2 + dx*w*0.5f).color(brL, bgL, bbL, 0f).endVertex();
        }
        t.end();
    }

    // ─── 玻璃折射（GLASS）───
    private static void renderCrystalGlass(PoseStack ps, float ex, float ey, float ez,
                                            float sz, float alpha, float r, float g, float b,
                                            float rotation) {
        Tesselator t = Tesselator.getInstance();
        BufferBuilder buf = t.getBuilder();
        // ★ GLASS 风格：高透明度、边缘强高亮、折射感
        //   参考 sphere-rendering STYLE 2 的 glass 模式
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        int stacks = 14, slices = 22;
        float glassAlpha = alpha * 0.35f; // 更透明
        for (int i = 0; i < stacks; i++) {
            float ph1 = (float)(Math.PI * i / stacks);
            float ph2 = (float)(Math.PI * (i + 1) / stacks);
            float r1 = sz * (float)Math.sin(ph1);
            float r2 = sz * (float)Math.sin(ph2);
            float y1 = sz * (float)Math.cos(ph1);
            float y2 = sz * (float)Math.cos(ph2);
            for (int j = 0; j < slices; j++) {
                float th1 = (float)(2 * Math.PI * j / slices + rotation);
                float th2 = (float)(2 * Math.PI * (j + 1) / slices + rotation);
                float c1 = (float)Math.cos(th1), s1 = (float)Math.sin(th1);
                float c2 = (float)Math.cos(th2), s2 = (float)Math.sin(th2);

                // ★ 边缘高亮（折射模拟）：phi 接近 π/2 时高亮
                float edge1 = Math.abs((float)Math.sin(ph1));
                float edge2 = Math.abs((float)Math.sin(ph2));
                float highlight1 = edge1 * edge1;
                float highlight2 = edge2 * edge2;

                float a1 = glassAlpha * (0.5f + 0.5f * highlight1);
                float a2 = glassAlpha * (0.5f + 0.5f * highlight2);
                // 边缘亮白色，中心偏原色
                float white1 = highlight1 * 0.6f;
                float white2 = highlight2 * 0.6f;
                float lr1 = r * (1f-white1) + white1, lg1 = g * (1f-white1) + white1, lb1 = b * (1f-white1) + white1;
                float lr2 = r * (1f-white2) + white2, lg2 = g * (1f-white2) + white2, lb2 = b * (1f-white2) + white2;

                buf.vertex(ps.last().pose(), ex + r1*c1, ey+y1, ez + r1*s1).color(lr1, lg1, lb1, a1).endVertex();
                buf.vertex(ps.last().pose(), ex + r2*c1, ey+y2, ez + r2*s1).color(lr2, lg2, lb2, a2).endVertex();
                buf.vertex(ps.last().pose(), ex + r2*c2, ey+y2, ez + r2*s2).color(lr2, lg2, lb2, a2).endVertex();
                buf.vertex(ps.last().pose(), ex + r1*c2, ey+y1, ez + r1*s2).color(lr1, lg1, lb1, a1).endVertex();
            }
        }
        t.end();

        // 玻璃高光反射条纹
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        float stripeAlpha = glassAlpha * 0.5f;
        for (int i = 0; i < 6; i++) {
            float ang = (float)(2 * Math.PI * i / 6 + rotation * 0.3f);
            float stripeW = sz * 0.04f;
            float stripeL = sz * 1.1f;
            float dx = (float)Math.cos(ang), dz = (float)Math.sin(ang);
            buf.vertex(ps.last().pose(), ex + dx*stripeW, ey-sz*0.1f, ez + dz*stripeW).color(1f,1f,1f,stripeAlpha).endVertex();
            buf.vertex(ps.last().pose(), ex + dx*stripeW, ey+sz*0.1f, ez + dz*stripeW).color(1f,1f,1f,stripeAlpha).endVertex();
            buf.vertex(ps.last().pose(), ex + dx*stripeL, ey+sz*0.1f, ez + dz*stripeL).color(1f,1f,1f,0f).endVertex();
            buf.vertex(ps.last().pose(), ex + dx*stripeL, ey-sz*0.1f, ez + dz*stripeL).color(1f,1f,1f,0f).endVertex();
        }
        t.end();
    }

    // ─── 极光晶体（AURORA）───
    private static void renderCrystalAurora(PoseStack ps, float ex, float ey, float ez,
                                             float sz, float alpha, float rotation, float glowIntensity) {
        Tesselator t = Tesselator.getInstance();
        BufferBuilder buf = t.getBuilder();
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        int stacks = 16, slices = 24;
        for (int i = 0; i < stacks; i++) {
            float ph1 = (float)(Math.PI * i / stacks);
            float ph2 = (float)(Math.PI * (i + 1) / stacks);
            float r1 = sz * (float)Math.sin(ph1);
            float r2 = sz * (float)Math.sin(ph2);
            float y1 = sz * (float)Math.cos(ph1);
            float y2 = sz * (float)Math.cos(ph2);
            for (int j = 0; j < slices; j++) {
                float th1 = (float)(2 * Math.PI * j / slices + rotation);
                float th2 = (float)(2 * Math.PI * (j + 1) / slices + rotation);
                float c1 = (float)Math.cos(th1), s1 = (float)Math.sin(th1);
                float c2 = (float)Math.cos(th2), s2 = (float)Math.sin(th2);

                // ★ AURORA 风格：色彩随位置和角度渐变
                float hue1 = (float)(i * 0.06f + j * 0.04f + rotation * 0.1f);
                float hue2 = (float)((i+1) * 0.06f + j * 0.04f + rotation * 0.1f);
                float cx1 = (float)Math.sin(hue1 * Math.PI * 2);
                float cy1 = (float)Math.cos(hue1 * Math.PI * 3);
                float cx2 = (float)Math.sin(hue2 * Math.PI * 2);
                float cy2 = (float)Math.cos(hue2 * Math.PI * 3);
                float ar1 = 0.5f + 0.5f * cx1, ag1 = 0.3f + 0.7f * cy1, ab1 = 0.5f + 0.5f * (float)Math.sin(hue1 * Math.PI * 4);
                float ar2 = 0.5f + 0.5f * cx2, ag2 = 0.3f + 0.7f * cy2, ab2 = 0.5f + 0.5f * (float)Math.sin(hue2 * Math.PI * 4);
                float a1 = alpha * 0.7f, a2 = alpha * 0.7f;

                buf.vertex(ps.last().pose(), ex + r1*c1, ey+y1, ez + r1*s1).color(ar1, ag1, ab1, a1).endVertex();
                buf.vertex(ps.last().pose(), ex + r2*c1, ey+y2, ez + r2*s1).color(ar2, ag2, ab2, a2).endVertex();
                buf.vertex(ps.last().pose(), ex + r2*c2, ey+y2, ez + r2*s2).color(ar2, ag2, ab2, a2).endVertex();
                buf.vertex(ps.last().pose(), ex + r1*c2, ey+y1, ez + r1*s2).color(ar1, ag1, ab1, a1).endVertex();
            }
        }
        t.end();
    }

    // ─── 通用：外围光晕 ───
    private static void renderCrystalGlow(PoseStack ps, float ex, float ey, float ez,
                                           float sz, float alpha, float r, float g, float b,
                                           float glowIntensity, float rotation) {
        Tesselator t = Tesselator.getInstance();
        BufferBuilder buf = t.getBuilder();
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        float glowAlpha = alpha * 0.2f * glowIntensity;
        float glowRad = sz * 1.8f;
        for (int i = 0; i < 36; i++) {
            float a1 = (float)(2 * Math.PI * i / 36 + rotation * 0.2f);
            float a2 = (float)(2 * Math.PI * (i + 1) / 36 + rotation * 0.2f);
            float x1 = ex + glowRad * (float)Math.cos(a1), z1 = ez + glowRad * (float)Math.sin(a1);
            float x2 = ex + glowRad * (float)Math.cos(a2), z2 = ez + glowRad * (float)Math.sin(a2);
            float ix1 = ex + sz * 0.6f * (float)Math.cos(a1), iz1 = ez + sz * 0.6f * (float)Math.sin(a1);
            float ix2 = ex + sz * 0.6f * (float)Math.cos(a2), iz2 = ez + sz * 0.6f * (float)Math.sin(a2);
            float flicker = 0.8f + 0.2f * (float)Math.sin(i * 1.5f + rotation);
            buf.vertex(ps.last().pose(), ix1, ey, iz1).color(r, g, b, glowAlpha * 0.3f).endVertex();
            buf.vertex(ps.last().pose(), x1, ey, z1).color(r, g, b, glowAlpha * flicker).endVertex();
            buf.vertex(ps.last().pose(), x2, ey, z2).color(r, g, b, glowAlpha * flicker).endVertex();
            buf.vertex(ps.last().pose(), ix2, ey, iz2).color(r, g, b, glowAlpha * 0.3f).endVertex();
        }
        t.end();
    }

    // ─── 通用：闪烁粒子 ───
    private static void renderCrystalSparkles(PoseStack ps, float ex, float ey, float ez,
                                               float sz, float alpha, float r, float g, float b,
                                               float p, float rotation) {
        Tesselator t = Tesselator.getInstance();
        BufferBuilder buf = t.getBuilder();
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        int sparkleCount = 30;
        float spAlpha = alpha * 0.9f;
        for (int i = 0; i < sparkleCount; i++) {
            float ti = (float)i / sparkleCount;
            float phi = (float)(Math.PI * ti);
            float theta = (float)(2 * Math.PI * (i * 0.618f) + rotation); // 黄金角分布
            float rs = sz * 1.02f; // 球体表面稍微外扩
            float sx = ex + rs * (float)Math.sin(phi) * (float)Math.cos(theta);
            float sy = ey + rs * (float)Math.cos(phi);
            float sz2 = ez + rs * (float)Math.sin(phi) * (float)Math.sin(theta);
            float sparkle = 0.5f + 0.5f * (float)Math.sin(i * 2.7f + p * 15f);
            float sa = spAlpha * sparkle * sparkle;
            float ss = sz * 0.025f;
            float sr2 = r * 1.3f, sg2 = g * 1.3f, sb2 = b * 1.3f;
            buf.vertex(ps.last().pose(), sx-ss, sy-ss, sz2-ss).color(sr2, sg2, sb2, sa).endVertex();
            buf.vertex(ps.last().pose(), sx+ss, sy-ss, sz2-ss).color(sr2, sg2, sb2, sa).endVertex();
            buf.vertex(ps.last().pose(), sx+ss, sy+ss, sz2+ss).color(sr2, sg2, sb2, 0f).endVertex();
            buf.vertex(ps.last().pose(), sx-ss, sy+ss, sz2+ss).color(sr2, sg2, sb2, 0f).endVertex();
        }
        t.end();
    }

    /**
     * 安全解析 float（用于额外配置参数）
     */
    private static float parseFloatSafe(String s, float def) {
        try { return Float.parseFloat(s); } catch (Exception e) { return def; }
    }

    private static class ShaderEffect {
        final ShaderType type;
        final Vec3 pos;
        final float intensity;
        final int duration;
        final String extraConfig; // 水晶专用额外参数
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
