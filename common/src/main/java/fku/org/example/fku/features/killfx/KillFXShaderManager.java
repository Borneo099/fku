package fku.org.example.fku.features.killfx;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import fku.org.example.fku.features.killfx.KillFXShaderRegistry;
import java.util.Iterator;
import java.util.LinkedList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;

public class KillFXShaderManager {
    private static final Minecraft mc = Minecraft.getInstance();
    private static final LinkedList<ShaderEffect> effects = new LinkedList();
    private static TextureTarget sceneCopyTarget;
    private static TextureTarget opaqueDepthTarget;

    public static void trigger(ShaderType type, Vec3 pos, float intensity, int durationTicks, String extraConfig) {
        if (type == ShaderType.NONE) {
            return;
        }
        effects.add(new ShaderEffect(type, pos, intensity, durationTicks, extraConfig));
    }

    public static void trigger(ShaderType type, Vec3 pos, float intensity, int durationTicks) {
        KillFXShaderManager.trigger(type, pos, intensity, durationTicks, "");
    }

    public static void tick() {
        Iterator it = effects.iterator();
        while (it.hasNext()) {
            ShaderEffect e = (ShaderEffect)it.next();
            ++e.elapsed;
            e.progress = Math.min(1.0f, e.elapsed / e.duration);
            if (!(e.progress >= 1.0f)) continue;
            it.remove();
        }
    }

    private static void renderBlackhole(PoseStack ps, float ex, float ey, float ez, float p, ShaderEffect effect, float partialTick) {
        float time;
        float sc;
        float sz;
        ShaderInstance shader = KillFXShaderRegistry.getBlackHoleShader();
        if (shader == null) {
            return;
        }
        float scaleMul = 1.0f;
        if (effect.extraConfig != null && !effect.extraConfig.isEmpty()) {
            try {
                scaleMul = Float.parseFloat(effect.extraConfig);
            }
            catch (Exception exception) {
                // ignored
            }
        }
        if ((sz = 1.5f * (sc = effect.intensity * scaleMul) * Math.min(p * 2.5f, (1.0f - p) * 2.0f + 0.3f)) <= 0.001f) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        RenderTarget mainTarget = minecraft.getMainRenderTarget();
        KillFXShaderManager.ensureTargets(mainTarget);
        KillFXShaderManager.copyColor(mainTarget, (RenderTarget)sceneCopyTarget);
        KillFXShaderManager.copyDepth(mainTarget, (RenderTarget)opaqueDepthTarget);
        mainTarget.unbindRead(false);
        shader.setSampler("DiffuseSampler", sceneCopyTarget.getColorTextureId());
        shader.setSampler("DepthSampler", opaqueDepthTarget.getDepthTextureId());
        if (shader.getUniform("ScreenSize") != null) {
            shader.getUniform("ScreenSize").set(mainTarget.width, mainTarget.height);
        }
        float f = time = minecraft.level != null ? (minecraft.level.getGameTime() + partialTick) * 0.04f : 0.0f;
        if (shader.getUniform("Time") != null) {
            shader.getUniform("Time").set(time);
        }
        PoseStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushPose();
        modelViewStack.setIdentity();
        modelViewStack.mulPoseMatrix(ps.last().pose());
        RenderSystem.applyModelViewMatrix();
        modelViewStack.translate(ex, ey, ez);
        modelViewStack.scale(sz, sz, sz);
        Quaternionf diskRot = new Quaternionf().rotateZ((float)Math.toRadians(10.0)).rotateX((float)Math.toRadians(-22.0));
        modelViewStack.mulPose(diskRot);
        RenderSystem.applyModelViewMatrix();
        Vec3 camPos = KillFXShaderManager.mc.gameRenderer.getMainCamera().getPosition();
        Vector3f camObjSpace = new Vector3f((float)((camPos.x - effect.pos.x) / sz), (float)((camPos.y - effect.pos.y) / sz), (float)((camPos.z - effect.pos.z) / sz));
        new Quaternionf((Quaternionfc)diskRot).conjugate().transform(camObjSpace);
        if (shader.getUniform("CameraPos") != null) {
            shader.getUniform("CameraPos").set(camObjSpace.x, camObjSpace.y, camObjSpace.z);
        }
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        Tesselator t = Tesselator.getInstance();
        BufferBuilder buf = t.getBuilder();
        buf.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        int latSegs = 32;
        int lonSegs = 48;
        for (int lat = 0; lat < latSegs; ++lat) {
            float v0 = lat / latSegs;
            float v1 = (lat + 1) / latSegs;
            float th0 = (v0 - 0.5f) * (float)Math.PI;
            float th1 = (v1 - 0.5f) * (float)Math.PI;
            for (int lon = 0; lon < lonSegs; ++lon) {
                float u0 = lon / lonSegs;
                float u1 = (lon + 1) / lonSegs;
                float ph0 = u0 * (float)Math.PI * 2.0f;
                float ph1 = u1 * (float)Math.PI * 2.0f;
                KillFXShaderManager.addSphereVertex(buf, th0, ph0);
                KillFXShaderManager.addSphereVertex(buf, th1, ph0);
                KillFXShaderManager.addSphereVertex(buf, th1, ph1);
                KillFXShaderManager.addSphereVertex(buf, th0, ph0);
                KillFXShaderManager.addSphereVertex(buf, th1, ph1);
                KillFXShaderManager.addSphereVertex(buf, th0, ph1);
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

    private static void ensureTargets(RenderTarget main) {
        if (sceneCopyTarget == null || KillFXShaderManager.sceneCopyTarget.width != main.width || KillFXShaderManager.sceneCopyTarget.height != main.height) {
            sceneCopyTarget = new TextureTarget(main.width, main.height, false, Minecraft.ON_MS);
            sceneCopyTarget.setFilterMode(9729);
        }
        if (opaqueDepthTarget == null || KillFXShaderManager.opaqueDepthTarget.width != main.width || KillFXShaderManager.opaqueDepthTarget.height != main.height) {
            opaqueDepthTarget = new TextureTarget(main.width, main.height, true, Minecraft.ON_MS);
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
        if (effects.isEmpty() || KillFXShaderManager.mc.player == null || KillFXShaderManager.mc.level == null) {
            return;
        }
        for (ShaderEffect effect : effects) {
            float p = effect.progress;
            switch (effect.type) {
                case CRYSTAL: {
                    Vec3 camPos = KillFXShaderManager.mc.gameRenderer.getMainCamera().getPosition();
                    float ex = (float)(effect.pos.x - camPos.x);
                    float ey = (float)(effect.pos.y - camPos.y);
                    float ez = (float)(effect.pos.z - camPos.z);
                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                    RenderSystem.disableDepthTest();
                    RenderSystem.setShader(GameRenderer::getPositionColorShader);
                    KillFXShaderManager.renderCrystal(poseStack, ex, ey, ez, p, effect, partialTick);
                    RenderSystem.enableDepthTest();
                    RenderSystem.disableBlend();
                    break;
                }
                case BLACKHOLE: {
                    KillFXShaderManager.renderBlackhole(poseStack, 0.0f, 0.0f, 0.0f, p, effect, partialTick);
                    break;
                }
                case SKY_BEAM: {
                    Vec3 camPos = KillFXShaderManager.mc.gameRenderer.getMainCamera().getPosition();
                    float ex = (float)(effect.pos.x - camPos.x);
                    float ey = (float)(effect.pos.y - camPos.y);
                    float ez = (float)(effect.pos.z - camPos.z);
                    KillFXShaderManager.renderSkyBeam(poseStack, ex, ey, ez, p, effect, partialTick);
                    break;
                }
                case SKY_RING: {
                    Vec3 camPos = KillFXShaderManager.mc.gameRenderer.getMainCamera().getPosition();
                    float ex = (float)(effect.pos.x - camPos.x);
                    float ey = (float)(effect.pos.y - camPos.y);
                    float ez = (float)(effect.pos.z - camPos.z);
                    KillFXShaderManager.renderSkyRing(poseStack, ex, ey, ez, p, effect, partialTick);
                    break;
                }
                case HYPERNOVA: {
                    Vec3 camPos = KillFXShaderManager.mc.gameRenderer.getMainCamera().getPosition();
                    float ex = (float)(effect.pos.x - camPos.x);
                    float ey = (float)(effect.pos.y - camPos.y);
                    float ez = (float)(effect.pos.z - camPos.z);
                    KillFXShaderManager.renderHypernova(poseStack, ex, ey, ez, p, effect);
                    break;
                }
                case RAY_BURST: {
                    Vec3 camPos = KillFXShaderManager.mc.gameRenderer.getMainCamera().getPosition();
                    float ex = (float)(effect.pos.x - camPos.x);
                    float ey = (float)(effect.pos.y - camPos.y);
                    float ez = (float)(effect.pos.z - camPos.z);
                    KillFXShaderManager.renderRayBurst(poseStack, ex, ey, ez, p, effect);
                }
            }
        }
    }

    private static void renderHypernova(PoseStack ps, float ex, float ey, float ez, float p, ShaderEffect effect) {
        float fadeOut;
        float sc = effect.intensity;
        float fadeIn = Math.min(p * 4.0f, 1.0f);
        float alpha = fadeIn * (fadeOut = Math.max(1.0f - (p - 0.5f) / 0.5f, 0.0f));
        if (alpha < 0.01f) {
            return;
        }
        float sz = 1.5f * sc * (0.3f + p * 1.2f);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator t = Tesselator.getInstance();
        BufferBuilder buf = t.getBuilder();
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        int stacks = 12;
        int slices = 16;
        for (int i = 0; i < stacks; ++i) {
            float ph1 = ((float)Math.PI * i / stacks);
            float ph2 = ((float)Math.PI * (i + 1) / stacks);
            float r1 = sz * (float)Math.sin(ph1);
            float r2 = sz * (float)Math.sin(ph2);
            float y1 = sz * (float)Math.cos(ph1);
            float y2 = sz * (float)Math.cos(ph2);
            for (int j = 0; j < slices; ++j) {
                float th1 = ((float)Math.PI * 2 * j / slices);
                float th2 = ((float)Math.PI * 2 * (j + 1) / slices);
                float c1 = (float)Math.cos(th1);
                float s1 = (float)Math.sin(th1);
                float c2 = (float)Math.cos(th2);
                float s2 = (float)Math.sin(th2);
                float layer = i / stacks;
                float br = 1.0f - layer * 0.3f;
                float ba = alpha * (1.0f - layer * 0.4f);
                buf.vertex(ps.last().pose(), ex + r1 * c1, ey + y1, ez + r1 * s1).color(br, br * 0.8f, br * 0.3f, ba).endVertex();
                buf.vertex(ps.last().pose(), ex + r2 * c1, ey + y2, ez + r2 * s1).color(br, br * 0.8f, br * 0.3f, ba).endVertex();
                buf.vertex(ps.last().pose(), ex + r2 * c2, ey + y2, ez + r2 * s2).color(br, br * 0.8f, br * 0.3f, ba).endVertex();
                buf.vertex(ps.last().pose(), ex + r1 * c2, ey + y1, ez + r1 * s2).color(br, br * 0.8f, br * 0.3f, ba).endVertex();
            }
        }
        t.end();
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        int outerStacks = 8;
        float outerSz = sz * 1.8f;
        for (int i = 0; i < outerStacks; ++i) {
            float ph1 = ((float)Math.PI * i / outerStacks);
            float ph2 = ((float)Math.PI * (i + 1) / outerStacks);
            float r1 = outerSz * (float)Math.sin(ph1);
            float r2 = outerSz * (float)Math.sin(ph2);
            float y1 = outerSz * (float)Math.cos(ph1);
            float y2 = outerSz * (float)Math.cos(ph2);
            for (int j = 0; j < slices; ++j) {
                float th1 = ((float)Math.PI * 2 * j / slices);
                float th2 = ((float)Math.PI * 2 * (j + 1) / slices);
                float c1 = (float)Math.cos(th1);
                float s1 = (float)Math.sin(th1);
                float c2 = (float)Math.cos(th2);
                float s2 = (float)Math.sin(th2);
                float f = 0.5f + 0.5f * (float)Math.sin(i * 1.3f + j * 0.7f + p * 8.0f);
                buf.vertex(ps.last().pose(), ex + r1 * c1, ey + y1, ez + r1 * s1).color(1.0f, 0.5f, 0.0f, alpha * 0.2f * f).endVertex();
                buf.vertex(ps.last().pose(), ex + r2 * c1, ey + y2, ez + r2 * s1).color(1.0f, 0.5f, 0.0f, alpha * 0.2f * f).endVertex();
                buf.vertex(ps.last().pose(), ex + r2 * c2, ey + y2, ez + r2 * s2).color(1.0f, 0.5f, 0.0f, alpha * 0.2f * f).endVertex();
                buf.vertex(ps.last().pose(), ex + r1 * c2, ey + y1, ez + r1 * s2).color(1.0f, 0.5f, 0.0f, alpha * 0.2f * f).endVertex();
            }
        }
        t.end();
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private static void renderRayBurst(PoseStack ps, float ex, float ey, float ez, float p, ShaderEffect effect) {
        float fadeOut;
        float sc = effect.intensity;
        float fadeIn = Math.min(p * 3.0f, 1.0f);
        float alpha = fadeIn * (fadeOut = Math.max(1.0f - (p - 0.6f) / 0.4f, 0.0f));
        if (alpha < 0.01f) {
            return;
        }
        float len = 4.0f * sc * (0.5f + p * 0.5f);
        float rot = p * 4.0f;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator t = Tesselator.getInstance();
        BufferBuilder buf = t.getBuilder();
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        int rays = 36;
        for (int i = 0; i < rays; ++i) {
            float ang = ((float)Math.PI * 2 * i / rays + rot);
            float dx = (float)Math.cos(ang);
            float dz = (float)Math.sin(ang);
            float r = 0.2f + 0.8f * (float)Math.sin(i * 0.3f);
            float g = 0.2f + 0.6f * (float)Math.cos(i * 0.5f);
            float b = 0.4f + 0.6f * (float)Math.sin(i * 0.7f);
            float ra = alpha * (0.2f + 0.8f * (float)Math.sin(i * 1.7f + p * 6.0f));
            float w = 0.06f * sc;
            float hw = w * 0.5f;
            buf.vertex(ps.last().pose(), ex - dx * hw, ey - w, ez - dz * hw).color(r, g, b, ra).endVertex();
            buf.vertex(ps.last().pose(), ex - dx * hw, ey + w, ez - dz * hw).color(r, g, b, ra).endVertex();
            buf.vertex(ps.last().pose(), ex + dx * len, ey + w * 0.3f, ez + dz * len).color(r, g, b, 0.0f).endVertex();
            buf.vertex(ps.last().pose(), ex + dx * len, ey - w * 0.3f, ez + dz * len).color(r, g, b, 0.0f).endVertex();
        }
        t.end();
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private static void renderDistortion(PoseStack ps, float ex, float ey, float ez, float sz, float p, float sc) {
        float dist = 5.0f * sc * (1.0f - p * 0.5f);
        float a = (1.0f - p) * 0.03f;
        Tesselator t = Tesselator.getInstance();
        BufferBuilder b = t.getBuilder();
        b.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        b.vertex(ps.last().pose(), ex, ey, ez).color(0.2f, 0.1f, 0.5f, 0.0f).endVertex();
        for (int i = 0; i <= 16; ++i) {
            float ang = ((float)Math.PI * 2 * i / 16.0f);
            b.vertex(ps.last().pose(), ex + dist * (float)Math.cos(ang), ey, ez + dist * (float)Math.sin(ang)).color(0.2f, 0.1f, 0.5f, a).endVertex();
        }
        t.end();
    }

    private static void renderSphere(PoseStack ps, float ex, float ey, float ez, float sz, float p) {
        Tesselator t = Tesselator.getInstance();
        BufferBuilder b = t.getBuilder();
        b.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        float alpha = 1.0f - p * 0.5f;
        int stacks = 12;
        int slices = 16;
        for (int i = 0; i < stacks; ++i) {
            float ph1 = ((float)Math.PI * i / stacks);
            float ph2 = ((float)Math.PI * (i + 1) / stacks);
            float r1 = sz * (float)Math.sin(ph1);
            float r2 = sz * (float)Math.sin(ph2);
            float y1 = sz * (float)Math.cos(ph1);
            float y2 = sz * (float)Math.cos(ph2);
            float br = i > stacks / 2 ? 0.05f : 0.02f;
            for (int j = 0; j < slices; ++j) {
                float t1 = ((float)Math.PI * 2 * j / slices);
                float t2 = ((float)Math.PI * 2 * (j + 1) / slices);
                b.vertex(ps.last().pose(), ex + r1 * (float)Math.cos(t1), ey + y1, ez + r1 * (float)Math.sin(t1)).color(br, br, br, alpha).endVertex();
                b.vertex(ps.last().pose(), ex + r2 * (float)Math.cos(t1), ey + y2, ez + r2 * (float)Math.sin(t1)).color(br, br, br, alpha).endVertex();
                b.vertex(ps.last().pose(), ex + r2 * (float)Math.cos(t2), ey + y2, ez + r2 * (float)Math.sin(t2)).color(br, br, br, alpha).endVertex();
                b.vertex(ps.last().pose(), ex + r1 * (float)Math.cos(t2), ey + y1, ez + r1 * (float)Math.sin(t2)).color(br, br, br, alpha).endVertex();
            }
        }
        t.end();
    }

    private static void renderDisk(PoseStack ps, float ex, float ey, float ez, float sz, float p, float sc) {
        Tesselator t = Tesselator.getInstance();
        BufferBuilder b = t.getBuilder();
        b.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        float alpha = 1.0f - p;
        float rot = p * 6.0f;
        for (int ring = 0; ring < 4; ++ring) {
            float bl;
            float g;
            float r;
            float rr = sz * (0.6f + ring * 0.25f);
            float rh = sz * (0.04f + ring * 0.02f);
            float ra = alpha * (1.0f - ring * 0.15f);
            if (ring == 0) {
                r = 1.0f;
                g = 0.8f;
                bl = 0.6f;
            } else if (ring == 1) {
                r = 0.6f;
                g = 0.3f;
                bl = 1.0f;
            } else if (ring == 2) {
                r = 0.4f;
                g = 0.2f;
                bl = 0.8f;
            } else {
                r = 0.3f;
                g = 0.1f;
                bl = 0.5f;
            }
            for (int i = 0; i < 36; ++i) {
                float a1 = ((float)Math.PI * 2 * i / 36.0f + rot + (ring * 0.5f));
                float a2 = ((float)Math.PI * 2 * (i + 1) / 36.0f + rot + (ring * 0.5f));
                float h1 = rh * (1.0f + (float)Math.sin(i * 1.5) * 0.5f);
                float h2 = rh * (1.0f + (float)Math.sin((i + 1) * 1.5) * 0.5f);
                float x1 = ex + rr * (float)Math.cos(a1);
                float z1 = ez + rr * (float)Math.sin(a1);
                float x2 = ex + rr * (float)Math.cos(a2);
                float z2 = ez + rr * (float)Math.sin(a2);
                float ix1 = ex + rr * 0.85f * (float)Math.cos(a1);
                float iz1 = ez + rr * 0.85f * (float)Math.sin(a1);
                float ix2 = ex + rr * 0.85f * (float)Math.cos(a2);
                float iz2 = ez + rr * 0.85f * (float)Math.sin(a2);
                b.vertex(ps.last().pose(), ix1, ey - h1, iz1).color(r, g, bl, ra * 0.6f).endVertex();
                b.vertex(ps.last().pose(), x1, ey - h2, z1).color(r, g, bl, ra * 0.8f).endVertex();
                b.vertex(ps.last().pose(), x2, ey - h2, z2).color(r, g, bl, ra * 0.8f).endVertex();
                b.vertex(ps.last().pose(), ix2, ey - h1, iz2).color(r, g, bl, ra * 0.6f).endVertex();
                b.vertex(ps.last().pose(), ix1, ey + h1, iz1).color(r, g, bl, ra * 0.3f).endVertex();
                b.vertex(ps.last().pose(), x1, ey + h2, z1).color(r, g, bl, ra * 0.4f).endVertex();
                b.vertex(ps.last().pose(), x2, ey + h2, z2).color(r, g, bl, ra * 0.4f).endVertex();
                b.vertex(ps.last().pose(), ix2, ey + h1, iz2).color(r, g, bl, ra * 0.3f).endVertex();
            }
        }
        for (int i = 0; i < 48; ++i) {
            float a1 = ((float)Math.PI * 2 * i / 48.0f + rot);
            float a2 = ((float)Math.PI * 2 * (i + 1) / 48.0f + rot);
            float gx1 = ex + sz * 0.55f * (float)Math.cos(a1);
            float gz1 = ez + sz * 0.55f * (float)Math.sin(a1);
            float gx2 = ex + sz * 0.55f * (float)Math.cos(a2);
            float gz2 = ez + sz * 0.55f * (float)Math.sin(a2);
            float ga = alpha * (0.6f + 0.4f * (float)Math.sin((i * 3) + p * 8.0f));
            b.vertex(ps.last().pose(), gx1, ey, gz1).color(1.0f, 0.9f, 0.7f, ga).endVertex();
            b.vertex(ps.last().pose(), gx2, ey, gz2).color(1.0f, 0.9f, 0.7f, ga).endVertex();
            b.vertex(ps.last().pose(), gx2, ey + sz * 0.02f, gz2).color(1.0f, 0.8f, 0.5f, ga * 0.5f).endVertex();
            b.vertex(ps.last().pose(), gx1, ey + sz * 0.02f, gz1).color(1.0f, 0.8f, 0.5f, ga * 0.5f).endVertex();
        }
        t.end();
    }

    private static void renderLensing(PoseStack ps, float ex, float ey, float ez, float sz, float p, float sc) {
        Tesselator t = Tesselator.getInstance();
        BufferBuilder b = t.getBuilder();
        b.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        float alpha = (1.0f - p) * 0.3f * sc;
        for (int layer = 0; layer < 3; ++layer) {
            float lr = sz * 2.5f * (0.8f + layer * 0.3f);
            float la = alpha * (1.0f - layer * 0.25f);
            float rot = p * 3.0f + layer;
            for (int i = 0; i < 24; ++i) {
                float a1 = ((float)Math.PI * 2 * i / 24.0f + rot);
                float a2 = ((float)Math.PI * 2 * (i + 1) / 24.0f + rot);
                float x1 = ex + lr * (float)Math.cos(a1);
                float z1 = ez + lr * (float)Math.sin(a1);
                float x2 = ex + lr * (float)Math.cos(a2);
                float z2 = ez + lr * (float)Math.sin(a2);
                float ix1 = ex + lr * 0.7f * (float)Math.cos(a1);
                float iz1 = ez + lr * 0.7f * (float)Math.sin(a1);
                float ix2 = ex + lr * 0.7f * (float)Math.cos(a2);
                float iz2 = ez + lr * 0.7f * (float)Math.sin(a2);
                float cr = 0.3f + 0.2f * (float)Math.cos(i * 0.5f + layer);
                float cg = 0.1f + 0.1f * (float)Math.sin(i * 0.7f);
                float cb = 0.6f + 0.3f * (float)Math.sin(i * 0.5f + layer);
                b.vertex(ps.last().pose(), ix1, ey - sz * 0.3f, iz1).color(cr, cg, cb, la * 0.3f).endVertex();
                b.vertex(ps.last().pose(), x1, ey - sz * 0.3f, z1).color(cr, cg, cb, la).endVertex();
                b.vertex(ps.last().pose(), x2, ey - sz * 0.3f, z2).color(cr, cg, cb, la).endVertex();
                b.vertex(ps.last().pose(), ix2, ey - sz * 0.3f, iz2).color(cr, cg, cb, la * 0.3f).endVertex();
            }
        }
        t.end();
    }

    private static void renderParticles(PoseStack ps, float ex, float ey, float ez, float sz, float p, float sc) {
        Tesselator t = Tesselator.getInstance();
        BufferBuilder b = t.getBuilder();
        b.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        float alpha = (1.0f - p) * 0.7f;
        for (int i = 0; i < 48; ++i) {
            float ti = i / 48.0f;
            float ang = ti * ((float)Math.PI * 2) + p * 8.0f;
            float sr = sz * (0.3f + 0.7f * (1.0f - p * 0.8f)) * (1.0f - ti * 0.6f);
            float py = ey + (float)Math.sin(ti * 10.0f + p * 5.0f) * sz * 0.05f;
            float px = ex + sr * (float)Math.cos(ang);
            float pz = ez + sr * (float)Math.sin(ang);
            float pr = 0.2f + 0.8f * (1.0f - ti);
            float pg = 0.2f + 0.6f * (float)Math.sin(ti * 3.0f);
            float pb = 0.4f + 0.6f * (1.0f - ti);
            float pa = alpha * (0.3f + 0.7f * (float)Math.sin(i * 0.7f + p * 10.0f));
            float psz = sz * 0.02f;
            b.vertex(ps.last().pose(), px - psz, py, pz - psz).color(pr, pg, pb, pa).endVertex();
            b.vertex(ps.last().pose(), px + psz, py, pz - psz).color(pr, pg, pb, pa).endVertex();
            b.vertex(ps.last().pose(), px + psz, py, pz + psz).color(pr, pg, pb, pa).endVertex();
            b.vertex(ps.last().pose(), px - psz, py, pz + psz).color(pr, pg, pb, pa).endVertex();
        }
        t.end();
    }

    public static int getActiveCount() {
        return effects.size();
    }

    private static int[] parseHexColor(String hex) {
        int[] rgb = new int[]{136, 204, 255};
        if (hex == null || hex.length() < 6) {
            return rgb;
        }
        try {
            rgb[0] = Integer.parseInt(hex.substring(0, 2), 16);
            rgb[1] = Integer.parseInt(hex.substring(2, 4), 16);
            rgb[2] = Integer.parseInt(hex.substring(4, 6), 16);
        }
        catch (Exception exception) {
            // ignored
        }
        return rgb;
    }

    private static void renderSkyBeam(PoseStack ps, float ex, float ey, float ez, float p, ShaderEffect effect, float partialTick) {
        float gameTime;
        ShaderInstance shader = KillFXShaderRegistry.getSkyBeamShader();
        if (shader == null) {
            return;
        }
        float sizeMul = 1.0f;
        if (effect.extraConfig != null && effect.extraConfig.startsWith("BEAM,")) {
            try {
                sizeMul = Float.parseFloat(effect.extraConfig.split(",")[1]);
            }
            catch (Exception exception) {
                // ignored
            }
        }
        float sc = effect.intensity * sizeMul;
        float fadeIn = Math.min(p * 3.0f, 1.0f);
        float fadeOut = Math.max(1.0f - (p - 0.6f) / 0.4f, 0.0f);
        float alpha = fadeIn * fadeOut;
        if (sc <= 0.01f || alpha <= 0.01f) {
            return;
        }
        float height = 16.0f * sc;
        float radius = 1.2f * sc;
        float f = gameTime = KillFXShaderManager.mc.level != null ? (KillFXShaderManager.mc.level.getGameTime() + partialTick) * 0.04f : 0.0f;
        if (shader.getUniform("Time") != null) {
            shader.getUniform("Time").set(gameTime);
        }
        if (shader.getUniform("BeamHeight") != null) {
            shader.getUniform("BeamHeight").set(height);
        }
        if (shader.getUniform("BeamRadius") != null) {
            shader.getUniform("BeamRadius").set(radius);
        }
        if (shader.getUniform("CoreRadius") != null) {
            shader.getUniform("CoreRadius").set(radius * 0.2f);
        }
        if (shader.getUniform("Intensity") != null) {
            shader.getUniform("Intensity").set(sc);
        }
        if (shader.getUniform("RevealFraction") != null) {
            shader.getUniform("RevealFraction").set(fadeIn);
        }
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
        Tesselator t = Tesselator.getInstance();
        BufferBuilder buf = t.getBuilder();
        buf.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        int hSegs = 8;
        int rSegs = 12;
        for (int h = 0; h < hSegs; ++h) {
            float y0 = height * h / hSegs;
            float y1 = height * (h + 1) / hSegs;
            for (int r = 0; r < rSegs; ++r) {
                float a0 = ((float)Math.PI * 2 * r / rSegs);
                float a1 = ((float)Math.PI * 2 * (r + 1) / rSegs);
                float c0 = (float)Math.cos(a0);
                float s0 = (float)Math.sin(a0);
                float c1 = (float)Math.cos(a1);
                float s1 = (float)Math.sin(a1);
                int a = (int)(alpha * 255.0f);
                buf.vertex((radius * c0), y0, (radius * s0)).color(255, 255, 255, a).endVertex();
                buf.vertex((radius * c1), y0, (radius * s1)).color(255, 255, 255, a).endVertex();
                buf.vertex((radius * c1), y1, (radius * s1)).color(255, 255, 255, a).endVertex();
                buf.vertex((radius * c0), y0, (radius * s0)).color(255, 255, 255, a).endVertex();
                buf.vertex((radius * c1), y1, (radius * s1)).color(255, 255, 255, a).endVertex();
                buf.vertex((radius * c0), y1, (radius * s0)).color(255, 255, 255, a).endVertex();
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

    private static void renderSkyRing(PoseStack ps, float ex, float ey, float ez, float p, ShaderEffect effect, float partialTick) {
        float gameTime;
        ShaderInstance shader = KillFXShaderRegistry.getSkyRingShader();
        if (shader == null) {
            return;
        }
        float sizeMul = 1.0f;
        if (effect.extraConfig != null && effect.extraConfig.startsWith("RING,")) {
            try {
                sizeMul = Float.parseFloat(effect.extraConfig.split(",")[1]);
            }
            catch (Exception exception) {
                // ignored
            }
        }
        float sc = effect.intensity * sizeMul;
        float fadeIn = Math.min(p * 3.0f, 1.0f);
        float fadeOut = Math.max(1.0f - (p - 0.6f) / 0.4f, 0.0f);
        float alpha = fadeIn * fadeOut;
        if (sc <= 0.01f || alpha <= 0.01f) {
            return;
        }
        float growProgress = Math.min(p / 0.4f, 1.0f);
        float growScale = 0.3f + 0.7f * growProgress;
        float baseSize = 2.0f * sc * growScale;
        float outerR = 2.0f * baseSize;
        float innerR = 1.0f * baseSize;
        float f = gameTime = KillFXShaderManager.mc.level != null ? (KillFXShaderManager.mc.level.getGameTime() + partialTick) * 0.04f : 0.0f;
        if (shader.getUniform("Time") != null) {
            shader.getUniform("Time").set(gameTime);
        }
        if (shader.getUniform("OuterRadius") != null) {
            shader.getUniform("OuterRadius").set(outerR);
        }
        if (shader.getUniform("InnerRadius") != null) {
            shader.getUniform("InnerRadius").set(innerR);
        }
        if (shader.getUniform("Softness") != null) {
            shader.getUniform("Softness").set(0.3f);
        }
        if (shader.getUniform("Intensity") != null) {
            shader.getUniform("Intensity").set(sc);
        }
        if (shader.getUniform("RingPlane") != null) {
            shader.getUniform("RingPlane").set(0.0f);
        }
        PoseStack mvStack = RenderSystem.getModelViewStack();
        mvStack.pushPose();
        mvStack.setIdentity();
        mvStack.mulPoseMatrix(ps.last().pose());
        mvStack.translate(ex, ey + 0.5f * baseSize, ez);
        mvStack.mulPose(new Quaternionf().rotateX((float)Math.toRadians(60.0)));
        RenderSystem.applyModelViewMatrix();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(() -> shader);
        Tesselator t = Tesselator.getInstance();
        BufferBuilder buf = t.getBuilder();
        buf.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        int ringSegs = 16;
        int radialSegs = 4;
        float midR = (outerR + innerR) * 0.5f;
        float halfW = (outerR - innerR) * 0.5f;
        for (int i = 0; i < ringSegs; ++i) {
            float a0 = ((float)Math.PI * 2 * i / ringSegs);
            float a1 = ((float)Math.PI * 2 * (i + 1) / ringSegs);
            for (int j = 0; j < radialSegs; ++j) {
                float t0 = -1.0f + 2.0f * j / radialSegs;
                float t1 = -1.0f + 2.0f * (j + 1) / radialSegs;
                float r0 = midR + t0 * halfW;
                float r1 = midR + t1 * halfW;
                float c0 = (float)Math.cos(a0);
                float s0 = (float)Math.sin(a0);
                float c1 = (float)Math.cos(a1);
                float s1 = (float)Math.sin(a1);
                int a = (int)(alpha * 255.0f);
                buf.vertex((r0 * c0), 0.0, (r0 * s0)).color(100, 150, 255, a).endVertex();
                buf.vertex((r1 * c0), 0.0, (r1 * s0)).color(120, 170, 255, a).endVertex();
                buf.vertex((r1 * c1), 0.0, (r1 * s1)).color(120, 170, 255, a).endVertex();
                buf.vertex((r0 * c0), 0.0, (r0 * s0)).color(100, 150, 255, a).endVertex();
                buf.vertex((r1 * c1), 0.0, (r1 * s1)).color(120, 170, 255, a).endVertex();
                buf.vertex((r0 * c1), 0.0, (r0 * s0)).color(100, 150, 255, a).endVertex();
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

    private static void renderCrystal(PoseStack ps, float ex, float ey, float ez, float p, ShaderEffect effect, float partialTick) {
        String[] parts;
        String extra = effect.extraConfig;
        if (extra == null || extra.isEmpty()) {
            extra = "CRYSTAL,88CCFF,1.0,0.8,1.5,true";
        }
        String style = (parts = extra.split(",")).length > 0 ? parts[0] : "CRYSTAL";
        String hexColor = parts.length > 1 ? parts[1] : "88CCFF";
        float radiusScale = parts.length > 2 ? KillFXShaderManager.parseFloatSafe(parts[2], 1.0f) : 1.0f;
        float glowIntensity = parts.length > 3 ? KillFXShaderManager.parseFloatSafe(parts[3], 0.8f) : 0.8f;
        float rotSpeed = parts.length > 4 ? KillFXShaderManager.parseFloatSafe(parts[4], 1.5f) : 1.5f;
        boolean pulse = parts.length <= 5 || Boolean.parseBoolean(parts[5]);
        float sz = 2.0f * glowIntensity * radiusScale;
        float fadeIn = Math.min(p * 4.0f, 1.0f);
        float fadeOut = Math.max(1.0f - (p - 0.7f) / 0.3f, 0.0f);
        float alpha = fadeIn * fadeOut;
        float pulseScale = pulse ? 1.0f + 0.15f * (float)Math.sin(p * (float)Math.PI * 6.0) : 1.0f;
        sz *= pulseScale;
        int[] tint = KillFXShaderManager.parseHexColor(hexColor);
        float r = tint[0] / 255.0f;
        float g = tint[1] / 255.0f;
        float b = tint[2] / 255.0f;
        float rotation = p * rotSpeed * 8.0f;
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        switch (style) {
            case "BLOOM": {
                KillFXShaderManager.renderCrystalBloom(ps, ex, ey, ez, sz, alpha, r, g, b, rotation, glowIntensity);
                break;
            }
            case "GLASS": {
                KillFXShaderManager.renderCrystalGlass(ps, ex, ey, ez, sz, alpha, r, g, b, rotation);
                break;
            }
            case "AURORA": {
                KillFXShaderManager.renderCrystalAurora(ps, ex, ey, ez, sz, alpha, rotation, glowIntensity);
                break;
            }
            default: {
                KillFXShaderManager.renderCrystalDefault(ps, ex, ey, ez, sz, alpha, r, g, b, rotation, glowIntensity);
            }
        }
        KillFXShaderManager.renderCrystalGlow(ps, ex, ey, ez, sz, alpha, r, g, b, glowIntensity, rotation);
        KillFXShaderManager.renderCrystalSparkles(ps, ex, ey, ez, sz, alpha, r, g, b, p, rotation);
    }

    private static void renderCrystalDefault(PoseStack ps, float ex, float ey, float ez, float sz, float alpha, float r, float g, float b, float rotation, float glowIntensity) {
        Tesselator t = Tesselator.getInstance();
        BufferBuilder buf = t.getBuilder();
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        int stacks = 16;
        int slices = 24;
        for (int i = 0; i < stacks; ++i) {
            float ph1 = ((float)Math.PI * i / stacks);
            float ph2 = ((float)Math.PI * (i + 1) / stacks);
            float r1 = sz * (float)Math.sin(ph1);
            float r2 = sz * (float)Math.sin(ph2);
            float y1 = sz * (float)Math.cos(ph1);
            float y2 = sz * (float)Math.cos(ph2);
            for (int j = 0; j < slices; ++j) {
                float th1 = ((float)Math.PI * 2 * j / slices + rotation);
                float th2 = ((float)Math.PI * 2 * (j + 1) / slices + rotation);
                float c1 = (float)Math.cos(th1);
                float s1 = (float)Math.sin(th1);
                float c2 = (float)Math.cos(th2);
                float s2 = (float)Math.sin(th2);
                float fresnel1 = Math.abs((float)Math.cos(ph1));
                float fresnel2 = Math.abs((float)Math.cos(ph2));
                float a1 = alpha * (0.3f + 0.7f * fresnel1);
                float a2 = alpha * (0.3f + 0.7f * fresnel2);
                float lt1 = 0.6f + 0.4f * fresnel1;
                float lt2 = 0.6f + 0.4f * fresnel2;
                float lr1 = r * lt1;
                float lg1 = g * lt1;
                float lb1 = b * lt1;
                float lr2 = r * lt2;
                float lg2 = g * lt2;
                float lb2 = b * lt2;
                buf.vertex(ps.last().pose(), ex + r1 * c1, ey + y1, ez + r1 * s1).color(lr1, lg1, lb1, a1).endVertex();
                buf.vertex(ps.last().pose(), ex + r2 * c1, ey + y2, ez + r2 * s1).color(lr2, lg2, lb2, a2).endVertex();
                buf.vertex(ps.last().pose(), ex + r2 * c2, ey + y2, ez + r2 * s2).color(lr2, lg2, lb2, a2).endVertex();
                buf.vertex(ps.last().pose(), ex + r1 * c2, ey + y1, ez + r1 * s2).color(lr1, lg1, lb1, a1).endVertex();
            }
        }
        t.end();
    }

    private static void renderCrystalBloom(PoseStack ps, float ex, float ey, float ez, float sz, float alpha, float r, float g, float b, float rotation, float glowIntensity) {
        Tesselator t = Tesselator.getInstance();
        BufferBuilder buf = t.getBuilder();
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        int stacks = 12;
        int slices = 20;
        for (int i = 0; i < stacks; ++i) {
            float ph1 = ((float)Math.PI * i / stacks);
            float ph2 = ((float)Math.PI * (i + 1) / stacks);
            float r1 = sz * (float)Math.sin(ph1);
            float r2 = sz * (float)Math.sin(ph2);
            float y1 = sz * (float)Math.cos(ph1);
            float y2 = sz * (float)Math.cos(ph2);
            float bloomFactor = 1.5f + 0.5f * (float)Math.sin(rotation + i * 0.3f);
            float br = r * bloomFactor;
            float bg = g * bloomFactor;
            float bb = b * bloomFactor;
            float ba = alpha * 0.9f;
            for (int j = 0; j < slices; ++j) {
                float th1 = ((float)Math.PI * 2 * j / slices + rotation);
                float th2 = ((float)Math.PI * 2 * (j + 1) / slices + rotation);
                float c1 = (float)Math.cos(th1);
                float s1 = (float)Math.sin(th1);
                float c2 = (float)Math.cos(th2);
                float s2 = (float)Math.sin(th2);
                float core = 0.7f + 0.3f * Math.abs((float)Math.cos(ph1));
                buf.vertex(ps.last().pose(), ex + r1 * c1, ey + y1, ez + r1 * s1).color(br * core, bg * core, bb * core, ba).endVertex();
                buf.vertex(ps.last().pose(), ex + r2 * c1, ey + y2, ez + r2 * s1).color(br * core, bg * core, bb * core, ba).endVertex();
                buf.vertex(ps.last().pose(), ex + r2 * c2, ey + y2, ez + r2 * s2).color(br * core, bg * core, bb * core, ba).endVertex();
                buf.vertex(ps.last().pose(), ex + r1 * c2, ey + y1, ez + r1 * s2).color(br * core, bg * core, bb * core, ba).endVertex();
            }
        }
        t.end();
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        float rayAlpha = alpha * 0.3f;
        float brL = r * 1.2f;
        float bgL = g * 1.2f;
        float bbL = b * 1.2f;
        for (int i = 0; i < 12; ++i) {
            float ang = ((float)Math.PI * 2 * i / 12.0f + (rotation * 0.5f));
            float ra = rayAlpha * (0.5f + 0.5f * (float)Math.sin(i * 2.5f + rotation));
            float len = sz * (1.5f + 0.3f * (float)Math.sin(i * 1.7f + rotation));
            float w = sz * 0.03f;
            float dx = (float)Math.cos(ang);
            float dz = (float)Math.sin(ang);
            float px = ex + dx * w;
            float pz = ez + dz * w;
            float px2 = ex + dx * len;
            float pz2 = ez + dz * len;
            buf.vertex(ps.last().pose(), px - dz * w, ey, pz + dx * w).color(brL, bgL, bbL, ra * 0.6f).endVertex();
            buf.vertex(ps.last().pose(), px + dz * w, ey, pz - dx * w).color(brL, bgL, bbL, ra * 0.6f).endVertex();
            buf.vertex(ps.last().pose(), px2 + dz * w * 0.5f, ey, pz2 - dx * w * 0.5f).color(brL, bgL, bbL, 0.0f).endVertex();
            buf.vertex(ps.last().pose(), px2 - dz * w * 0.5f, ey, pz2 + dx * w * 0.5f).color(brL, bgL, bbL, 0.0f).endVertex();
        }
        t.end();
    }

    private static void renderCrystalGlass(PoseStack ps, float ex, float ey, float ez, float sz, float alpha, float r, float g, float b, float rotation) {
        Tesselator t = Tesselator.getInstance();
        BufferBuilder buf = t.getBuilder();
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        int stacks = 14;
        int slices = 22;
        float glassAlpha = alpha * 0.35f;
        for (int i = 0; i < stacks; ++i) {
            float ph1 = ((float)Math.PI * i / stacks);
            float ph2 = ((float)Math.PI * (i + 1) / stacks);
            float r1 = sz * (float)Math.sin(ph1);
            float r2 = sz * (float)Math.sin(ph2);
            float y1 = sz * (float)Math.cos(ph1);
            float y2 = sz * (float)Math.cos(ph2);
            for (int j = 0; j < slices; ++j) {
                float th1 = ((float)Math.PI * 2 * j / slices + rotation);
                float th2 = ((float)Math.PI * 2 * (j + 1) / slices + rotation);
                float c1 = (float)Math.cos(th1);
                float s1 = (float)Math.sin(th1);
                float c2 = (float)Math.cos(th2);
                float s2 = (float)Math.sin(th2);
                float edge1 = Math.abs((float)Math.sin(ph1));
                float edge2 = Math.abs((float)Math.sin(ph2));
                float highlight1 = edge1 * edge1;
                float highlight2 = edge2 * edge2;
                float a1 = glassAlpha * (0.5f + 0.5f * highlight1);
                float a2 = glassAlpha * (0.5f + 0.5f * highlight2);
                float white1 = highlight1 * 0.6f;
                float white2 = highlight2 * 0.6f;
                float lr1 = r * (1.0f - white1) + white1;
                float lg1 = g * (1.0f - white1) + white1;
                float lb1 = b * (1.0f - white1) + white1;
                float lr2 = r * (1.0f - white2) + white2;
                float lg2 = g * (1.0f - white2) + white2;
                float lb2 = b * (1.0f - white2) + white2;
                buf.vertex(ps.last().pose(), ex + r1 * c1, ey + y1, ez + r1 * s1).color(lr1, lg1, lb1, a1).endVertex();
                buf.vertex(ps.last().pose(), ex + r2 * c1, ey + y2, ez + r2 * s1).color(lr2, lg2, lb2, a2).endVertex();
                buf.vertex(ps.last().pose(), ex + r2 * c2, ey + y2, ez + r2 * s2).color(lr2, lg2, lb2, a2).endVertex();
                buf.vertex(ps.last().pose(), ex + r1 * c2, ey + y1, ez + r1 * s2).color(lr1, lg1, lb1, a1).endVertex();
            }
        }
        t.end();
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        float stripeAlpha = glassAlpha * 0.5f;
        for (int i = 0; i < 6; ++i) {
            float ang = ((float)Math.PI * 2 * i / 6.0f + (rotation * 0.3f));
            float stripeW = sz * 0.04f;
            float stripeL = sz * 1.1f;
            float dx = (float)Math.cos(ang);
            float dz = (float)Math.sin(ang);
            buf.vertex(ps.last().pose(), ex + dx * stripeW, ey - sz * 0.1f, ez + dz * stripeW).color(1.0f, 1.0f, 1.0f, stripeAlpha).endVertex();
            buf.vertex(ps.last().pose(), ex + dx * stripeW, ey + sz * 0.1f, ez + dz * stripeW).color(1.0f, 1.0f, 1.0f, stripeAlpha).endVertex();
            buf.vertex(ps.last().pose(), ex + dx * stripeL, ey + sz * 0.1f, ez + dz * stripeL).color(1.0f, 1.0f, 1.0f, 0.0f).endVertex();
            buf.vertex(ps.last().pose(), ex + dx * stripeL, ey - sz * 0.1f, ez + dz * stripeL).color(1.0f, 1.0f, 1.0f, 0.0f).endVertex();
        }
        t.end();
    }

    private static void renderCrystalAurora(PoseStack ps, float ex, float ey, float ez, float sz, float alpha, float rotation, float glowIntensity) {
        Tesselator t = Tesselator.getInstance();
        BufferBuilder buf = t.getBuilder();
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        int stacks = 16;
        int slices = 24;
        for (int i = 0; i < stacks; ++i) {
            float ph1 = ((float)Math.PI * i / stacks);
            float ph2 = ((float)Math.PI * (i + 1) / stacks);
            float r1 = sz * (float)Math.sin(ph1);
            float r2 = sz * (float)Math.sin(ph2);
            float y1 = sz * (float)Math.cos(ph1);
            float y2 = sz * (float)Math.cos(ph2);
            for (int j = 0; j < slices; ++j) {
                float th1 = ((float)Math.PI * 2 * j / slices + rotation);
                float th2 = ((float)Math.PI * 2 * (j + 1) / slices + rotation);
                float c1 = (float)Math.cos(th1);
                float s1 = (float)Math.sin(th1);
                float c2 = (float)Math.cos(th2);
                float s2 = (float)Math.sin(th2);
                float hue1 = i * 0.06f + j * 0.04f + rotation * 0.1f;
                float hue2 = (i + 1) * 0.06f + j * 0.04f + rotation * 0.1f;
                float cx1 = (float)Math.sin(hue1 * (float)Math.PI * 2.0);
                float cy1 = (float)Math.cos(hue1 * (float)Math.PI * 3.0);
                float cx2 = (float)Math.sin(hue2 * (float)Math.PI * 2.0);
                float cy2 = (float)Math.cos(hue2 * (float)Math.PI * 3.0);
                float ar1 = 0.5f + 0.5f * cx1;
                float ag1 = 0.3f + 0.7f * cy1;
                float ab1 = 0.5f + 0.5f * (float)Math.sin(hue1 * (float)Math.PI * 4.0);
                float ar2 = 0.5f + 0.5f * cx2;
                float ag2 = 0.3f + 0.7f * cy2;
                float ab2 = 0.5f + 0.5f * (float)Math.sin(hue2 * (float)Math.PI * 4.0);
                float a1 = alpha * 0.7f;
                float a2 = alpha * 0.7f;
                buf.vertex(ps.last().pose(), ex + r1 * c1, ey + y1, ez + r1 * s1).color(ar1, ag1, ab1, a1).endVertex();
                buf.vertex(ps.last().pose(), ex + r2 * c1, ey + y2, ez + r2 * s1).color(ar2, ag2, ab2, a2).endVertex();
                buf.vertex(ps.last().pose(), ex + r2 * c2, ey + y2, ez + r2 * s2).color(ar2, ag2, ab2, a2).endVertex();
                buf.vertex(ps.last().pose(), ex + r1 * c2, ey + y1, ez + r1 * s2).color(ar1, ag1, ab1, a1).endVertex();
            }
        }
        t.end();
    }

    private static void renderCrystalGlow(PoseStack ps, float ex, float ey, float ez, float sz, float alpha, float r, float g, float b, float glowIntensity, float rotation) {
        Tesselator t = Tesselator.getInstance();
        BufferBuilder buf = t.getBuilder();
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        float glowAlpha = alpha * 0.2f * glowIntensity;
        float glowRad = sz * 1.8f;
        for (int i = 0; i < 36; ++i) {
            float a1 = ((float)Math.PI * 2 * i / 36.0f + (rotation * 0.2f));
            float a2 = ((float)Math.PI * 2 * (i + 1) / 36.0f + (rotation * 0.2f));
            float x1 = ex + glowRad * (float)Math.cos(a1);
            float z1 = ez + glowRad * (float)Math.sin(a1);
            float x2 = ex + glowRad * (float)Math.cos(a2);
            float z2 = ez + glowRad * (float)Math.sin(a2);
            float ix1 = ex + sz * 0.6f * (float)Math.cos(a1);
            float iz1 = ez + sz * 0.6f * (float)Math.sin(a1);
            float ix2 = ex + sz * 0.6f * (float)Math.cos(a2);
            float iz2 = ez + sz * 0.6f * (float)Math.sin(a2);
            float flicker = 0.8f + 0.2f * (float)Math.sin(i * 1.5f + rotation);
            buf.vertex(ps.last().pose(), ix1, ey, iz1).color(r, g, b, glowAlpha * 0.3f).endVertex();
            buf.vertex(ps.last().pose(), x1, ey, z1).color(r, g, b, glowAlpha * flicker).endVertex();
            buf.vertex(ps.last().pose(), x2, ey, z2).color(r, g, b, glowAlpha * flicker).endVertex();
            buf.vertex(ps.last().pose(), ix2, ey, iz2).color(r, g, b, glowAlpha * 0.3f).endVertex();
        }
        t.end();
    }

    private static void renderCrystalSparkles(PoseStack ps, float ex, float ey, float ez, float sz, float alpha, float r, float g, float b, float p, float rotation) {
        Tesselator t = Tesselator.getInstance();
        BufferBuilder buf = t.getBuilder();
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        int sparkleCount = 30;
        float spAlpha = alpha * 0.9f;
        for (int i = 0; i < sparkleCount; ++i) {
            float ti = i / sparkleCount;
            float phi = ((float)Math.PI * ti);
            float theta = ((float)Math.PI * 2 * (i * 0.618f) + rotation);
            float rs = sz * 1.02f;
            float sx = ex + rs * (float)Math.sin(phi) * (float)Math.cos(theta);
            float sy = ey + rs * (float)Math.cos(phi);
            float sz2 = ez + rs * (float)Math.sin(phi) * (float)Math.sin(theta);
            float sparkle = 0.5f + 0.5f * (float)Math.sin(i * 2.7f + p * 15.0f);
            float sa = spAlpha * sparkle * sparkle;
            float ss = sz * 0.025f;
            float sr2 = r * 1.3f;
            float sg2 = g * 1.3f;
            float sb2 = b * 1.3f;
            buf.vertex(ps.last().pose(), sx - ss, sy - ss, sz2 - ss).color(sr2, sg2, sb2, sa).endVertex();
            buf.vertex(ps.last().pose(), sx + ss, sy - ss, sz2 - ss).color(sr2, sg2, sb2, sa).endVertex();
            buf.vertex(ps.last().pose(), sx + ss, sy + ss, sz2 + ss).color(sr2, sg2, sb2, 0.0f).endVertex();
            buf.vertex(ps.last().pose(), sx - ss, sy + ss, sz2 + ss).color(sr2, sg2, sb2, 0.0f).endVertex();
        }
        t.end();
    }

    private static float parseFloatSafe(String s, float def) {
        try {
            return Float.parseFloat(s);
        }
        catch (Exception e) {
            return def;
        }
    }

    public static enum ShaderType {
        NONE,
        BLACKHOLE,
        CRYSTAL,
        SKY_BEAM,
        SKY_RING,
        HYPERNOVA,
        RAY_BURST;

    }

    private static class ShaderEffect {
        final ShaderType type;
        final Vec3 pos;
        final float intensity;
        final int duration;
        final String extraConfig;
        int elapsed = 0;
        float progress = 0.0f;

        ShaderEffect(ShaderType type, Vec3 pos, float intensity, int duration, String extraConfig) {
            this.type = type;
            this.pos = pos;
            this.intensity = intensity;
            this.duration = duration;
            this.extraConfig = extraConfig;
        }

        ShaderEffect(ShaderType type, Vec3 pos, float intensity, int duration) {
            this(type, pos, intensity, duration, "");
        }
    }
}

