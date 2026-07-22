package fku.org.example.fku.features.attackindicator;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import fku.org.example.fku.features.attackindicator.AttackIndicatorConfig;
import java.awt.Color;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class AttackIndicatorRenderer {
    private static final Minecraft mc = Minecraft.getInstance();
    private static final Random RNG = new Random();

    public static void renderConnectionEffects(PoseStack poseStack, Entity player, Entity target, AttackIndicatorConfig cfg) {
        if (player == null || target == null) {
            return;
        }
        double dist = player.distanceTo(target);
        if (dist > (cfg.particleLODDistance * 2.0f)) {
            return;
        }
        float partialTick = mc.getPartialTick();
        Vec3 playerPos = player.position().add(0.0, player.getBbHeight() * 0.3, 0.0);
        Vec3 targetPos = target.position().add(0.0, target.getBbHeight() * 0.5, 0.0);
        Vec3 cameraPos = AttackIndicatorRenderer.mc.getEntityRenderDispatcher().camera.getPosition();
        Vec3 start = playerPos.subtract(cameraPos);
        Vec3 end = targetPos.subtract(cameraPos);
        if (cfg.enableBeam) {
            AttackIndicatorRenderer.renderEnergyBeam(poseStack, start, end, cfg);
        }
        if (cfg.enableLightning) {
            AttackIndicatorRenderer.renderLightning(poseStack, start, end, cfg);
        }
        if (cfg.enablePulseWave) {
            AttackIndicatorRenderer.renderPulseWave(poseStack, start, end, cfg);
        }
        if (cfg.enableTether) {
            AttackIndicatorRenderer.renderTether(poseStack, start, end, cfg);
        }
    }

    private static void renderEnergyBeam(PoseStack poseStack, Vec3 start, Vec3 end, AttackIndicatorConfig cfg) {
        Vec3 up;
        Vec3 dir = end.subtract(start);
        double length = dir.length();
        if (length < 0.1) {
            return;
        }
        if (Math.abs((dir = dir.normalize()).dot(up = new Vec3(0.0, 1.0, 0.0))) > 0.99) {
            up = new Vec3(1.0, 0.0, 0.0);
        }
        Vec3 right = dir.cross(up).normalize();
        Color color = AttackIndicatorRenderer.parseColor(cfg.beamColor);
        float time = (System.currentTimeMillis() % 3000L) / 3000.0f;
        float beamWidth = cfg.beamWidth * 0.05f;
        AttackIndicatorRenderer.setupRender3D();
        Tesselator t = Tesselator.getInstance();
        BufferBuilder buf = t.getBuilder();
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        int segments = Math.max(8, (int)(length * 2.0));
        float segLen = (float)(length / segments);
        for (int i = 0; i < segments; ++i) {
            float t1 = i / segments;
            float t2 = (i + 1) / segments;
            float flow = (t1 + time * cfg.beamFlowSpeed) % 1.0f;
            float alpha = 0.3f + 0.7f * (float)(0.5f + 0.5f * Math.sin(flow * Math.PI * 8.0));
            float pulse = 0.5f + 0.5f * (float)Math.sin(flow * Math.PI * 2.0);
            float wobble = 0.02f * (float)Math.sin(t1 * 20.0f + time * 30.0f);
            Vec3 p1 = start.add(dir.scale(t1 * length)).add(right.scale(wobble));
            Vec3 p2 = start.add(dir.scale(t2 * length)).add(right.scale((wobble * 1.2f)));
            float w = beamWidth * (0.5f + 0.5f * pulse);
            int a = (int)(alpha * 150.0f);
            AttackIndicatorRenderer.addQuad(buf, poseStack, p1, p2, right, w, color.getRed(), color.getGreen(), color.getBlue(), a);
        }
        BufferUploader.drawWithShader(buf.end());
        AttackIndicatorRenderer.restoreRender3D();
    }

    private static void renderLightning(PoseStack poseStack, Vec3 start, Vec3 end, AttackIndicatorConfig cfg) {
        Vec3 up;
        Vec3 diff = end.subtract(start);
        double length = diff.length();
        if (length < 0.1) {
            return;
        }
        Color color = AttackIndicatorRenderer.parseColor(cfg.lightningColor);
        float time = (System.currentTimeMillis() % 2000L) / 2000.0f;
        Vec3 dir = diff.normalize();
        if (Math.abs(dir.dot(up = new Vec3(0.0, 1.0, 0.0))) > 0.99) {
            up = new Vec3(1.0, 0.0, 0.0);
        }
        Vec3 right = dir.cross(up).normalize();
        AttackIndicatorRenderer.setupRender3D();
        int segments = cfg.lightningSegments;
        float maxOffset = (float)(length * 0.15f);
        int alpha = (int)(180.0 * (0.5 + 0.5 * Math.sin(time * Math.PI * 2.0)));
        RNG.setSeed(System.currentTimeMillis() / 100L);
        float[] offsets = new float[segments + 1];
        for (int i = 1; i < segments; ++i) {
            float chaos = 0.5f + 0.5f * (float)Math.sin(i * 3.7 + (time * 50.0f));
            offsets[i] = (float)((RNG.nextDouble() - 0.5) * 2.0f * maxOffset * chaos);
            int n = i;
            offsets[n] = (offsets[n] * (float)(0.5 + 0.5 * Math.sin((time * 30.0f) + i * 1.3)));
        }
        offsets[segments] = 0.0f;
        offsets[0] = 0.0f;
        Tesselator t = Tesselator.getInstance();
        BufferBuilder buf = t.getBuilder();
        buf.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        for (int i = 0; i < segments; ++i) {
            double t1 = i / segments;
            double t2 = (i + 1) / segments;
            Vec3 p1 = start.add(diff.scale(t1)).add(right.scale((offsets[i] * 0.3f)));
            Vec3 p2 = start.add(diff.scale(t2)).add(right.scale((offsets[i + 1] * 0.3f)));
            buf.vertex(poseStack.last().pose(), (float)p1.x, (float)p1.y, (float)p1.z).color(color.getRed(), color.getGreen(), color.getBlue(), alpha).endVertex();
            buf.vertex(poseStack.last().pose(), (float)p2.x, (float)p2.y, (float)p2.z).color(color.getRed(), color.getGreen(), color.getBlue(), alpha).endVertex();
        }
        BufferUploader.drawWithShader(buf.end());
        AttackIndicatorRenderer.restoreRender3D();
    }

    private static void renderPulseWave(PoseStack poseStack, Vec3 start, Vec3 end, AttackIndicatorConfig cfg) {
        Vec3 up;
        Vec3 diff = end.subtract(start);
        double length = diff.length();
        if (length < 0.5) {
            return;
        }
        Color color = AttackIndicatorRenderer.parseColor(cfg.waveColor);
        float time = (System.currentTimeMillis() % 2000L) / 2000.0f;
        Vec3 dir = diff.normalize();
        if (Math.abs(dir.dot(up = new Vec3(0.0, 1.0, 0.0))) > 0.99) {
            up = new Vec3(1.0, 0.0, 0.0);
        }
        Vec3 right = dir.cross(up).normalize();
        Vec3 perp = right.cross(dir).normalize();
        float progress = time * cfg.waveSpeed % 1.0f;
        Vec3 center = start.add(diff.scale(progress));
        float waveRadius = (float)(length * 0.15f * (1.0f - Math.abs(progress - 0.5f) * 2.0f));
        if (waveRadius < 0.1f) {
            return;
        }
        AttackIndicatorRenderer.setupRender3D();
        Tesselator t = Tesselator.getInstance();
        BufferBuilder buf = t.getBuilder();
        buf.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        int rings = 24;
        float alpha = 0.6f * (1.0f - Math.abs(progress - 0.5f) * 2.0f);
        for (int i = 0; i <= rings; ++i) {
            float angle = (float)(Math.PI * 2 * i / rings);
            float x = (float)(Math.cos(angle) * waveRadius * right.x + Math.sin(angle) * waveRadius * perp.x);
            float y = (float)(Math.cos(angle) * waveRadius * right.y + Math.sin(angle) * waveRadius * perp.y);
            float z = (float)(Math.cos(angle) * waveRadius * right.z + Math.sin(angle) * waveRadius * perp.z);
            buf.vertex(poseStack.last().pose(), (float)(center.x + x), (float)(center.y + y), (float)(center.z + z)).color(color.getRed(), color.getGreen(), color.getBlue(), (int)(alpha * 255.0f)).endVertex();
            buf.vertex(poseStack.last().pose(), (float)(center.x + x * 0.8f), (float)(center.y + y * 0.8f), (float)(center.z + z * 0.8f)).color(color.getRed(), color.getGreen(), color.getBlue(), (int)(alpha * 180.0f)).endVertex();
        }
        BufferUploader.drawWithShader(buf.end());
        AttackIndicatorRenderer.restoreRender3D();
    }

    private static void renderTether(PoseStack poseStack, Vec3 start, Vec3 end, AttackIndicatorConfig cfg) {
        Vec3 up;
        Vec3 diff = end.subtract(start);
        double length = diff.length();
        if (length < 0.5) {
            return;
        }
        Color color = AttackIndicatorRenderer.parseColor(cfg.tetherColor);
        float time = (System.currentTimeMillis() % 3000L) / 3000.0f;
        Vec3 dir = diff.normalize();
        if (Math.abs(dir.dot(up = new Vec3(0.0, 1.0, 0.0))) > 0.99) {
            up = new Vec3(1.0, 0.0, 0.0);
        }
        Vec3 right = dir.cross(up).normalize();
        AttackIndicatorRenderer.setupRender3D();
        Tesselator t = Tesselator.getInstance();
        BufferBuilder buf = t.getBuilder();
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        int links = (int)(length * 2.0);
        float linkSpacing = (float)(length / links);
        float sway = cfg.tetherSway * 0.3f;
        for (int i = 0; i < links; ++i) {
            float t1 = i / links;
            float t2 = (i + 1) / links;
            float sway1 = sway * (float)Math.sin(t1 * 10.0f + time * 20.0f);
            float sway2 = sway * (float)Math.sin(t2 * 10.0f + time * 20.0f);
            float w1 = 0.04f * (0.5f + 0.5f * (float)Math.sin(t1 * 5.0f + time));
            float w2 = 0.04f * (0.5f + 0.5f * (float)Math.sin(t2 * 5.0f + time));
            Vec3 p1 = start.add(diff.scale(t1)).add(right.scale(sway1));
            Vec3 p2 = start.add(diff.scale(t2)).add(right.scale(sway2));
            int a = (int)(120.0f * (0.5f + 0.5f * (float)Math.sin(t1 * 3.0f + time * 2.0f)));
            AttackIndicatorRenderer.addQuad(buf, poseStack, p1, p2, right, w1, color.getRed(), color.getGreen(), color.getBlue(), a);
            AttackIndicatorRenderer.addQuad(buf, poseStack, p1, p2, up, w1 * 0.6f, color.getRed(), color.getGreen(), color.getBlue(), a / 2);
        }
        BufferUploader.drawWithShader(buf.end());
        AttackIndicatorRenderer.restoreRender3D();
    }

    public static void renderSwordWave(PoseStack poseStack, Vec3 start, Vec3 end, float progress, AttackIndicatorConfig cfg) {
        int alpha;
        float thickness;
        float iz;
        float iy;
        float ix;
        float oz;
        float oy;
        float ox;
        float sin;
        float cos;
        float angle;
        int i;
        Vec3 up;
        Vec3 diff = end.subtract(start);
        double length = diff.length();
        if (length < 0.1) {
            return;
        }
        Vec3 dir = diff.normalize();
        if (Math.abs(dir.dot(up = new Vec3(0.0, 1.0, 0.0))) > 0.99) {
            up = new Vec3(1.0, 0.0, 0.0);
        }
        Vec3 right = dir.cross(up).normalize();
        Vec3 perp = right.cross(dir).normalize();
        Vec3 wavePos = start.add(diff.scale(progress));
        float baseRadius = (float)(length * 0.12f * cfg.swordWaveIntensity);
        float innerRadius = baseRadius * 0.6f;
        float crescentOffset = baseRadius * 0.15f;
        float scale = 1.0f - 0.3f * progress;
        baseRadius *= scale;
        innerRadius *= scale;
        crescentOffset *= scale;
        float rotAngle = (float)(progress * Math.PI * 2.0f);
        Color color = AttackIndicatorRenderer.parseColor(cfg.swordWaveColor);
        float time = (System.currentTimeMillis() % 1000L) / 1000.0f;
        float pulseAlpha = 0.7f + 0.3f * (float)Math.sin(time * Math.PI * 2.0);
        AttackIndicatorRenderer.setupRender3D();
        Tesselator t = Tesselator.getInstance();
        BufferBuilder buf = t.getBuilder();
        buf.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        int segments = 20;
        float arcStart = -2.6179938f;
        float arcEnd = 2.6179938f;
        float arcRange = arcEnd - arcStart;
        float frx = (float)right.x;
        float fry = (float)right.y;
        float frz = (float)right.z;
        float fpx = (float)perp.x;
        float fpy = (float)perp.y;
        float fpz = (float)perp.z;
        float fdx = (float)dir.x;
        float fdy = (float)dir.y;
        float fdz = (float)dir.z;
        for (i = 0; i <= segments; ++i) {
            angle = arcStart + arcRange * i / segments + rotAngle;
            cos = (float)Math.cos(angle);
            sin = (float)Math.sin(angle);
            ox = cos * baseRadius * frx + sin * baseRadius * fpx;
            oy = cos * baseRadius * fry + sin * baseRadius * fpy;
            oz = cos * baseRadius * frz + sin * baseRadius * fpz;
            ix = cos * innerRadius * frx + sin * innerRadius * fpx + fpx * crescentOffset;
            iy = cos * innerRadius * fry + sin * innerRadius * fpy + fpy * crescentOffset;
            iz = cos * innerRadius * frz + sin * innerRadius * fpz + fpz * crescentOffset;
            thickness = 0.03f * scale;
            alpha = (int)(200.0f * pulseAlpha);
            buf.vertex(poseStack.last().pose(), (float)(wavePos.x + ox + fdx * thickness), (float)(wavePos.y + oy + fdy * thickness), (float)(wavePos.z + oz + fdz * thickness)).color(color.getRed(), color.getGreen(), color.getBlue(), alpha).endVertex();
            buf.vertex(poseStack.last().pose(), (float)(wavePos.x + ix + fdx * thickness), (float)(wavePos.y + iy + fdy * thickness), (float)(wavePos.z + iz + fdz * thickness)).color(color.getRed(), color.getGreen(), color.getBlue(), (int)(alpha * 0.6f)).endVertex();
        }
        BufferUploader.drawWithShader(buf.end());
        buf.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        for (i = 0; i <= segments; ++i) {
            angle = arcStart + arcRange * i / segments + rotAngle;
            cos = (float)Math.cos(angle);
            sin = (float)Math.sin(angle);
            ox = cos * baseRadius * frx + sin * baseRadius * fpx;
            oy = cos * baseRadius * fry + sin * baseRadius * fpy;
            oz = cos * baseRadius * frz + sin * baseRadius * fpz;
            ix = cos * innerRadius * frx + sin * innerRadius * fpx + fpx * crescentOffset;
            iy = cos * innerRadius * fry + sin * innerRadius * fpy + fpy * crescentOffset;
            iz = cos * innerRadius * frz + sin * innerRadius * fpz + fpz * crescentOffset;
            thickness = 0.03f * scale;
            alpha = (int)(200.0f * pulseAlpha);
            buf.vertex(poseStack.last().pose(), (float)(wavePos.x + ox - fdx * thickness), (float)(wavePos.y + oy - fdy * thickness), (float)(wavePos.z + oz - fdz * thickness)).color(color.getRed(), color.getGreen(), color.getBlue(), (int)(alpha * 0.8f)).endVertex();
            buf.vertex(poseStack.last().pose(), (float)(wavePos.x + ix - fdx * thickness), (float)(wavePos.y + iy - fdy * thickness), (float)(wavePos.z + iz - fdz * thickness)).color(color.getRed(), color.getGreen(), color.getBlue(), (int)(alpha * 0.5f)).endVertex();
        }
        BufferUploader.drawWithShader(buf.end());
        AttackIndicatorRenderer.restoreRender3D();
    }

    public static void renderTargetEffects(PoseStack poseStack, LivingEntity target, AttackIndicatorConfig cfg) {
        double dist;
        if (target == null) {
            return;
        }
        double d = dist = AttackIndicatorRenderer.mc.player != null ? AttackIndicatorRenderer.mc.player.distanceTo(target) : 0.0;
        if (dist > (cfg.particleLODDistance * 2.0f)) {
            return;
        }
        float partialTick = mc.getPartialTick();
        double renderX = target.xOld + (target.getX() - target.xOld) * partialTick;
        double renderY = target.yOld + (target.getY() - target.yOld) * partialTick;
        double renderZ = target.zOld + (target.getZ() - target.zOld) * partialTick;
        Vec3 cameraPos = AttackIndicatorRenderer.mc.getEntityRenderDispatcher().camera.getPosition();
        poseStack.pushPose();
        poseStack.translate(renderX - cameraPos.x, renderY - cameraPos.y, renderZ - cameraPos.z);
        if (cfg.enableLockBox) {
            AttackIndicatorRenderer.renderLockBox(poseStack, target, cfg);
        }
        if (cfg.enableHalo) {
            AttackIndicatorRenderer.renderHalo(poseStack, target, cfg);
        }
        if (cfg.enableBeamMarker) {
            AttackIndicatorRenderer.renderBeamMarker(poseStack, target, cfg);
        }
        if (cfg.enableGlow) {
            AttackIndicatorRenderer.renderGlow(poseStack, target, cfg);
        }
        poseStack.popPose();
    }

    private static void renderLockBox(PoseStack poseStack, LivingEntity target, AttackIndicatorConfig cfg) {
        float alpha;
        float z;
        float x;
        float angle;
        int i;
        float boxSize = target.getBbWidth() * 0.6f * cfg.boxSize;
        float height = target.getBbHeight() + 0.2f;
        float time = (float)((System.currentTimeMillis() % 10000L) / 10000.0f * (Math.PI * 2));
        Color color = AttackIndicatorRenderer.parseColor(cfg.boxColor);
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        AttackIndicatorRenderer.setupRender3D();
        Tesselator t = Tesselator.getInstance();
        BufferBuilder buf = t.getBuilder();
        int segments = 24;
        float angleStep = (float)(Math.PI * 2 / segments);
        float baseAngle = time * cfg.boxRotateSpeed;
        buf.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        for (i = 0; i <= segments; ++i) {
            angle = baseAngle + i * angleStep;
            x = (float)Math.cos(angle) * boxSize;
            z = (float)Math.sin(angle) * boxSize;
            alpha = 0.3f + 0.7f * (0.5f + 0.5f * (float)Math.sin(angle * 2.0f + time * 3.0f));
            buf.vertex(poseStack.last().pose(), x, 0.0f, z).color(r, g, b, (int)(alpha * 120.0f)).endVertex();
        }
        BufferUploader.drawWithShader(buf.end());
        buf.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        for (i = 0; i <= segments; ++i) {
            angle = baseAngle + i * angleStep;
            x = (float)Math.cos(angle) * boxSize;
            z = (float)Math.sin(angle) * boxSize;
            alpha = 0.3f + 0.7f * (0.5f + 0.5f * (float)Math.sin(angle * 2.0f + time * 3.0f));
            buf.vertex(poseStack.last().pose(), x, height, z).color(r, g, b, (int)(alpha * 120.0f)).endVertex();
        }
        BufferUploader.drawWithShader(buf.end());
        buf.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
        for (i = 0; i < segments; i += 6) {
            angle = baseAngle + i * angleStep;
            x = (float)Math.cos(angle) * boxSize;
            z = (float)Math.sin(angle) * boxSize;
            buf.vertex(poseStack.last().pose(), x, 0.0f, z).color(r, g, b, 100).endVertex();
            buf.vertex(poseStack.last().pose(), x, height, z).color(r, g, b, 100).endVertex();
        }
        BufferUploader.drawWithShader(buf.end());
        AttackIndicatorRenderer.restoreRender3D();
    }

    private static void renderHalo(PoseStack poseStack, LivingEntity target, AttackIndicatorConfig cfg) {
        float radius = target.getBbWidth() * cfg.haloRadius;
        float time = (float)((System.currentTimeMillis() % 10000L) / 10000.0f * (Math.PI * 2));
        Color color = AttackIndicatorRenderer.parseColor(cfg.haloColor);
        AttackIndicatorRenderer.setupRender3D();
        Tesselator t = Tesselator.getInstance();
        BufferBuilder buf = t.getBuilder();
        buf.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        int segments = 32;
        float angle = time * cfg.haloRotateSpeed;
        for (int i = 0; i <= segments; ++i) {
            float a = (float)(angle + (Math.PI * 2 * i / segments));
            float x = (float)Math.cos(a) * radius;
            float z = (float)Math.sin(a) * radius;
            float alpha = 0.3f + 0.7f * (0.5f + 0.5f * (float)Math.sin(a * 2.0f + time * 4.0f));
            buf.vertex(poseStack.last().pose(), x, 0.05f, z).color(color.getRed(), color.getGreen(), color.getBlue(), (int)(alpha * 80.0f)).endVertex();
            buf.vertex(poseStack.last().pose(), x, 0.15f, z).color(color.getRed(), color.getGreen(), color.getBlue(), (int)(alpha * 40.0f)).endVertex();
        }
        BufferUploader.drawWithShader(buf.end());
        AttackIndicatorRenderer.restoreRender3D();
    }

    private static void renderBeamMarker(PoseStack poseStack, LivingEntity target, AttackIndicatorConfig cfg) {
        float height = cfg.beamMarkerHeight;
        Color color = AttackIndicatorRenderer.parseColor(cfg.beamMarkerColor);
        float time = (System.currentTimeMillis() % 2000L) / 2000.0f;
        AttackIndicatorRenderer.setupRender3D();
        Tesselator t = Tesselator.getInstance();
        BufferBuilder buf = t.getBuilder();
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        float alpha = 0.3f + 0.3f * (float)Math.sin(time * Math.PI * 2.0);
        for (int i = 0; i < 3; ++i) {
            float width = 0.1f * (1.0f + i * 0.5f);
            float aMul = 1.0f - i * 0.3f;
            int a = (int)(alpha * 60.0f * aMul);
            buf.vertex(poseStack.last().pose(), -width, 0.1f, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), a).endVertex();
            buf.vertex(poseStack.last().pose(), width, 0.1f, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), a).endVertex();
            buf.vertex(poseStack.last().pose(), width, height, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), (int)(alpha * 20.0f * aMul)).endVertex();
            buf.vertex(poseStack.last().pose(), -width, height, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), (int)(alpha * 20.0f * aMul)).endVertex();
        }
        BufferUploader.drawWithShader(buf.end());
        AttackIndicatorRenderer.restoreRender3D();
    }

    private static void renderGlow(PoseStack poseStack, LivingEntity target, AttackIndicatorConfig cfg) {
        float z;
        float x;
        float angle;
        int i;
        Color color = AttackIndicatorRenderer.parseColor(cfg.glowColor);
        float time = (System.currentTimeMillis() % 2000L) / 2000.0f;
        float intensity = cfg.glowIntensity * (0.5f + 0.5f * (float)Math.sin(time * Math.PI * 2.0));
        float w = target.getBbWidth() * 0.5f;
        float h = target.getBbHeight();
        float expand = 0.15f * intensity;
        AttackIndicatorRenderer.setupRender3D();
        Tesselator t = Tesselator.getInstance();
        BufferBuilder buf = t.getBuilder();
        buf.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        for (i = 0; i <= 24; ++i) {
            angle = (float)(Math.PI * 2 * i / 24.0);
            x = (float)(Math.cos(angle) * (w + expand));
            z = (float)(Math.sin(angle) * (w + expand));
            buf.vertex(poseStack.last().pose(), x, -expand, z).color(color.getRed(), color.getGreen(), color.getBlue(), (int)(intensity * 80.0f)).endVertex();
        }
        BufferUploader.drawWithShader(buf.end());
        buf.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        for (i = 0; i <= 24; ++i) {
            angle = (float)(Math.PI * 2 * i / 24.0);
            x = (float)(Math.cos(angle) * (w + expand));
            z = (float)(Math.sin(angle) * (w + expand));
            buf.vertex(poseStack.last().pose(), x, h + expand, z).color(color.getRed(), color.getGreen(), color.getBlue(), (int)(intensity * 80.0f)).endVertex();
        }
        BufferUploader.drawWithShader(buf.end());
        buf.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
        for (i = 0; i < 4; ++i) {
            angle = (float)(Math.PI * 2 * i / 4.0);
            x = (float)(Math.cos(angle) * (w + expand));
            z = (float)(Math.sin(angle) * (w + expand));
            buf.vertex(poseStack.last().pose(), x, -expand, z).color(color.getRed(), color.getGreen(), color.getBlue(), (int)(intensity * 60.0f)).endVertex();
            buf.vertex(poseStack.last().pose(), x, h + expand, z).color(color.getRed(), color.getGreen(), color.getBlue(), (int)(intensity * 60.0f)).endVertex();
        }
        BufferUploader.drawWithShader(buf.end());
        AttackIndicatorRenderer.restoreRender3D();
    }

    public static void renderScreenOverlay(GuiGraphics guiGraphics, Entity target, AttackIndicatorConfig cfg) {
        int sw = guiGraphics.guiWidth();
        int sh = guiGraphics.guiHeight();
        if (cfg.enableDirectionArrow) {
            AttackIndicatorRenderer.renderDirectionArrow(guiGraphics, target, cfg, sw, sh);
        }
        if (cfg.enableEdgeFlash) {
            AttackIndicatorRenderer.renderEdgeFlash(guiGraphics, cfg, sw, sh);
        }
    }

    private static void renderDirectionArrow(GuiGraphics guiGraphics, Entity target, AttackIndicatorConfig cfg, int sw, int sh) {
        if (AttackIndicatorRenderer.mc.player == null) {
            return;
        }
        Vec3 lookVec = AttackIndicatorRenderer.mc.player.getLookAngle();
        Vec3 toTarget = target.position().subtract(AttackIndicatorRenderer.mc.player.position()).normalize();
        double angle = Math.atan2(toTarget.cross(lookVec).y, toTarget.dot(lookVec));
        if (Math.abs(Math.toDegrees(angle)) < 21.0) {
            return;
        }
        int cx = sw / 2;
        int cy = sh / 2;
        int radius = Math.min(sw, sh) / 2 - 20;
        double arrowAngle = angle - 1.5707963267948966;
        int ax = (int)(cx + Math.cos(arrowAngle) * radius * 0.5);
        int ay = (int)(cy + Math.sin(arrowAngle) * radius * 0.5);
        ax = Math.max(20, Math.min(sw - 20, ax));
        ay = Math.max(20, Math.min(sh - 20, ay));
        Color color = AttackIndicatorRenderer.parseColor(cfg.arrowColor);
        float sz = 8.0f * cfg.arrowSize;
        float a2 = (float)(angle + 1.5707963267948966);
        int x1 = (int)(ax + Math.cos(a2) * sz);
        int y1 = (int)(ay + Math.sin(a2) * sz);
        int x2 = (int)(ax + Math.cos(a2 + 2.5f) * sz);
        int y2 = (int)(ay + Math.sin(a2 + 2.5f) * sz);
        int x3 = (int)(ax + Math.cos(a2 - 2.5f) * sz);
        int y3 = (int)(ay + Math.sin(a2 - 2.5f) * sz);
        PoseStack pose = guiGraphics.pose();
        Matrix4f matrix = pose.last().pose();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator t = Tesselator.getInstance();
        BufferBuilder buf = t.getBuilder();
        buf.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        buf.vertex(matrix, x1, y1, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), 180).endVertex();
        buf.vertex(matrix, x2, y2, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), 180).endVertex();
        buf.vertex(matrix, x3, y3, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), 180).endVertex();
        BufferUploader.drawWithShader(buf.end());
        RenderSystem.disableBlend();
    }

    private static void renderEdgeFlash(GuiGraphics guiGraphics, AttackIndicatorConfig cfg, int sw, int sh) {
        Color color = AttackIndicatorRenderer.parseColor(cfg.flashColor);
        float time = (System.currentTimeMillis() % 1000L) / 1000.0f;
        float intensity = cfg.flashIntensity * (0.5f + 0.5f * (float)Math.sin(time * Math.PI * 2.0));
        int alpha = (int)(intensity * 255.0f);
        if (alpha < 5) {
            return;
        }
        PoseStack pose = guiGraphics.pose();
        Matrix4f matrix = pose.last().pose();
        int bw = 4;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator t = Tesselator.getInstance();
        BufferBuilder buf = t.getBuilder();
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        buf.vertex(matrix, 0.0f, 0.0f, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), alpha).endVertex();
        buf.vertex(matrix, sw, 0.0f, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), alpha).endVertex();
        buf.vertex(matrix, sw, bw, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), alpha).endVertex();
        buf.vertex(matrix, 0.0f, bw, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), alpha).endVertex();
        buf.vertex(matrix, 0.0f, (sh - bw), 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), alpha).endVertex();
        buf.vertex(matrix, sw, (sh - bw), 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), alpha).endVertex();
        buf.vertex(matrix, sw, sh, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), alpha).endVertex();
        buf.vertex(matrix, 0.0f, sh, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), alpha).endVertex();
        buf.vertex(matrix, 0.0f, 0.0f, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), alpha).endVertex();
        buf.vertex(matrix, bw, 0.0f, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), alpha).endVertex();
        buf.vertex(matrix, bw, sh, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), alpha).endVertex();
        buf.vertex(matrix, 0.0f, sh, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), alpha).endVertex();
        buf.vertex(matrix, (sw - bw), 0.0f, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), alpha).endVertex();
        buf.vertex(matrix, sw, 0.0f, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), alpha).endVertex();
        buf.vertex(matrix, sw, sh, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), alpha).endVertex();
        buf.vertex(matrix, (sw - bw), sh, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), alpha).endVertex();
        BufferUploader.drawWithShader(buf.end());
        RenderSystem.disableBlend();
    }

    public static Color parseColor(String hex) {
        try {
            if (hex.startsWith("#")) {
                hex = hex.substring(1);
            }
            if (hex.length() == 6) {
                return new Color(Integer.parseInt(hex, 16));
            }
            if (hex.length() == 8) {
                return new Color((int)Long.parseLong(hex, 16), true);
            }
            return Color.RED;
        }
        catch (Exception e) {
            return Color.RED;
        }
    }

    public static int hexToInt(String hex) {
        Color c = AttackIndicatorRenderer.parseColor(hex);
        return c.getRed() << 16 | c.getGreen() << 8 | c.getBlue();
    }

    private static void addQuad(BufferBuilder buf, PoseStack pose, Vec3 p1, Vec3 p2, Vec3 right, float w, int r, int g, int b, int a) {
        buf.vertex(pose.last().pose(), (float)(p1.x - right.x * w), (float)(p1.y - right.y * w), (float)(p1.z - right.z * w)).color(r, g, b, a).endVertex();
        buf.vertex(pose.last().pose(), (float)(p1.x + right.x * w), (float)(p1.y + right.y * w), (float)(p1.z + right.z * w)).color(r, g, b, a).endVertex();
        buf.vertex(pose.last().pose(), (float)(p2.x + right.x * w), (float)(p2.y + right.y * w), (float)(p2.z + right.z * w)).color(r, g, b, a).endVertex();
        buf.vertex(pose.last().pose(), (float)(p2.x - right.x * w), (float)(p2.y - right.y * w), (float)(p2.z - right.z * w)).color(r, g, b, a).endVertex();
    }

    private static void setupRender3D() {
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
    }

    private static void restoreRender3D() {
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }
}