package fku.org.example.fku.features.trail; /* water */

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.awt.Color;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;

/**
 * 拖尾特效管理器 — 统一管理路径点缓存、粒子发射、残影渲染和光轨渲染
 * 该管理器由赛博教员实现
 *
 * ★ 设计思想（实践论）：
 *   1. 路径点使用环形缓冲区（ArrayDeque<PathPoint>），固定大小避免内存膨胀
 *   2. 残影使用世界坐标轴Billboard（基于forward+worldUp），确保任何方向可见
 *   3. 流光轨迹使用连续线段，每个线段宽度方向基于segmentDir×forward，确保始终面向相机
 *   4. 各粒子模式使用不同渲染手法，确保视觉可区分
 */
@OnlyIn(Dist.CLIENT)
public class TrailManager {
    private static final Minecraft mc = Minecraft.getInstance();
    private static final Random RNG = new Random();

    /** 路径点环形缓冲区 */
    private final ArrayDeque<PathPoint> pathPoints = new ArrayDeque<>();
    /** 残影记录列表 */
    private final List<GhostRecord> ghostRecords = new ArrayList<>();
    /** 上一个玩家位置 */
    private Vec3 lastPlayerPos = null;
    /** 残影生成Tick计数 */
    private int ghostTickCounter = 0;
    /** 淡出计时器 */
    private int fadeOutTimer = 0;
    /** 是否正在触发 */
    private boolean isTriggering = false;
    /** 自定义视觉粒子 */
    private final List<VisualParticle> visualParticles = new ArrayList<>();

    /** 路径点数据类 */
    public static class PathPoint {
        public final Vec3 pos;
        public final long timestamp;
        public final float yRot;
        public final float xRot;

        public PathPoint(Vec3 pos, float yRot, float xRot) {
            this.pos = pos;
            this.timestamp = System.currentTimeMillis();
            this.yRot = yRot;
            this.xRot = xRot;
        }
    }

    /** 残影记录类 */
    public static class GhostRecord {
        public final Vec3 pos;
        public final float yRot;
        public final float xRot;
        public final long timestamp;
        public float alpha = 1.0f;

        public GhostRecord(Vec3 pos, float yRot, float xRot) {
            this.pos = pos;
            this.yRot = yRot;
            this.xRot = xRot;
            this.timestamp = System.currentTimeMillis();
        }
    }

    /** 自定义视觉粒子 */
    private static class VisualParticle {
        public Vec3 pos;
        public Vec3 velocity;
        public final long spawnTime;
        public final int lifetime;
        public final float size;
        public final int color;
        public final float phase;

        VisualParticle(Vec3 pos, Vec3 velocity, int lifetime, float size, int color) {
            this.pos = pos;
            this.velocity = velocity;
            this.spawnTime = System.currentTimeMillis();
            this.lifetime = lifetime;
            this.size = size;
            this.color = color;
            this.phase = RNG.nextFloat() * (float)Math.PI * 2;
        }

        boolean isExpired() {
            return System.currentTimeMillis() - spawnTime > lifetime * 50L;
        }

        float getAge() {
            return (System.currentTimeMillis() - spawnTime) / (float)(lifetime * 50L);
        }

        float getAlpha() {
            return Math.max(0, 1.0f - getAge());
        }
    }

    /**
     * 每Tick更新
     */
    public void tick(TrailConfig cfg) {
        if (mc.player == null || mc.level == null) return;

        if (cfg.disableInLowFps) {
            double fps = Minecraft.getInstance().getFps();
            if (fps < 30 && fps > 0) return;
        }

        boolean shouldTrigger = checkTrigger(cfg, mc.player);
        if (shouldTrigger) {
            isTriggering = true;
            fadeOutTimer = cfg.fadeOutTicks;
        } else {
            if (fadeOutTimer > 0) {
                fadeOutTimer--;
                isTriggering = true;
            } else {
                isTriggering = false;
            }
        }

        Vec3 currentPos = mc.player.position();

        // 静止时只更新粒子
        if (lastPlayerPos != null && currentPos.distanceToSqr(lastPlayerPos) < 0.0001) {
            updateVisualParticles(cfg);
            return;
        }

        // 记录路径点（用于流光模式）
        pathPoints.addLast(new PathPoint(currentPos, mc.player.getYRot(), mc.player.getXRot()));
        while (pathPoints.size() > cfg.streakMaxPoints) {
            pathPoints.removeFirst();
        }

        // 残影模式
        if ("GHOST".equals(cfg.trailMode)) {
            ghostTickCounter++;
            if (ghostTickCounter >= cfg.ghostInterval) {
                ghostTickCounter = 0;
                ghostRecords.add(new GhostRecord(currentPos, mc.player.getYRot(), mc.player.getXRot()));
                while (ghostRecords.size() > cfg.maxGhosts) {
                    ghostRecords.remove(0);
                }
            }
        }

        // 粒子模式
        switch (cfg.trailMode) {
            case "PARTICLE" -> spawnParticles(cfg, currentPos);
            case "ELEMENTAL_FOOTPRINT" -> spawnElementalFootprint(cfg, currentPos);
        }

        updateVisualParticles(cfg);
        lastPlayerPos = currentPos;
    }

    private void updateVisualParticles(TrailConfig cfg) {
        Iterator<VisualParticle> it = visualParticles.iterator();
        while (it.hasNext()) {
            VisualParticle p = it.next();
            p.pos = p.pos.add(p.velocity);
            p.velocity = p.velocity.scale(0.98);
            if (p.isExpired()) it.remove();
        }
        while (visualParticles.size() > cfg.maxParticles) {
            visualParticles.remove(0);
        }
    }

    private boolean checkTrigger(TrailConfig cfg, AbstractClientPlayer player) {
        return switch (cfg.triggerMode) {
            case "ALWAYS" -> true;
            case "SPRINTING" -> player.isSprinting();
            case "FLYING" -> player.getAbilities().flying || player.isFallFlying();
            case "JUMPING" -> !player.onGround();
            case "COMBAT" -> player.getMainHandItem().getItem().getDefaultInstance().isDamageableItem()
                || player.getLastHurtMobTimestamp() > 0;
            default -> false;
        };
    }

    /**
     * 粒子流 — 向后发射原版粒子
     */
    private void spawnParticles(TrailConfig cfg, Vec3 pos) {
        ClientLevel level = mc.level;
        if (level == null) return;

        ParticleOptions particle = resolveParticleType(cfg.particleType);
        if (particle == null) return;

        float yaw = mc.player != null ? mc.player.getYRot() : 0;
        double backX = -Math.sin(Math.toRadians(yaw));
        double backZ = Math.cos(Math.toRadians(yaw));

        for (int i = 0; i < cfg.particlesPerTick; i++) {
            double spread = cfg.particleSpread;
            double dx = (Math.random() - 0.5) * spread;
            double dy = (Math.random() - 0.5) * spread * 0.5;
            double dz = (Math.random() - 0.5) * spread;
            double speed = cfg.particleSpeed;

            level.addParticle(particle,
                pos.x + dx * 0.5, pos.y + 0.5 + dy * 0.5, pos.z + dz * 0.5,
                backX * speed + dx, Math.random() * speed * 0.5, backZ * speed + dz);
        }
    }

    /**
     * 元素足迹 — 地面脚印 + 向上元素火花
     */
    private void spawnElementalFootprint(TrailConfig cfg, Vec3 pos) {
        ClientLevel level = mc.level;
        if (level == null) return;

        ParticleOptions particle = resolveParticleType(cfg.particleType);
        if (particle == null) return;

        Color color = parseColor(cfg.mainColor);
        int packedColor = (color.getRed() << 16) | (color.getGreen() << 8) | color.getBlue();

        float yaw = mc.player != null ? mc.player.getYRot() : 0;
        float rYaw = (float)Math.toRadians(yaw);
        float cosYaw = Mth.cos(rYaw);
        float sinYaw = Mth.sin(rYaw);

        // 地面粒子（双椭圆足迹）
        for (int i = 0; i < cfg.particlesPerTick + 3; i++) {
            float side = (i % 2 == 0) ? -0.2f : 0.2f;
            float fwd = (i / 2) * 0.1f - 0.15f;
            float fx = side * cosYaw - fwd * sinYaw;
            float fz = side * sinYaw + fwd * cosYaw;
            level.addParticle(particle,
                pos.x + fx + (Math.random() - 0.5) * 0.15,
                pos.y + 0.05,
                pos.z + fz + (Math.random() - 0.5) * 0.15,
                0, 0.03, 0);
        }

        // 元素火花
        for (int i = 0; i < 3 && visualParticles.size() < 100; i++) {
            visualParticles.add(new VisualParticle(
                pos.add((Math.random() - 0.5) * 0.5, 0.1, (Math.random() - 0.5) * 0.5),
                new Vec3((Math.random() - 0.5) * 0.02, 0.04 + Math.random() * 0.03, (Math.random() - 0.5) * 0.02),
                20, 0.03f, packedColor));
        }
    }

    private ParticleOptions resolveParticleType(String type) {
        return switch (type) {
            case "FLAME" -> ParticleTypes.FLAME;
            case "DRAGON_BREATH" -> ParticleTypes.DRAGON_BREATH;
            case "END_ROD" -> ParticleTypes.END_ROD;
            case "FIREWORK" -> ParticleTypes.FIREWORK;
            case "PORTAL" -> ParticleTypes.PORTAL;
            case "SOUL" -> ParticleTypes.SOUL;
            default -> ParticleTypes.END_ROD;
        };
    }

    // ════════════════════════════════════════════════════════════════
    //  世界渲染入口
    // ════════════════════════════════════════════════════════════════

    public void render(PoseStack poseStack, TrailConfig cfg, Vec3 cameraPos) {
        if (!isTriggering && ghostRecords.isEmpty() && pathPoints.isEmpty()
            && visualParticles.isEmpty()) return;

        if (mc.player != null) {
            double dist = mc.player.position().distanceTo(cameraPos.add(0, 0, 0));
            if (dist > cfg.lodDistance) return;
        }

        if ("GHOST".equals(cfg.trailMode)) {
            renderGhosts(poseStack, cfg, cameraPos);
        }
        if ("LIGHT_STREAK".equals(cfg.trailMode)) {
            renderLightStreak(poseStack, cfg, cameraPos);
        }
        if ("ELEMENTAL_FOOTPRINT".equals(cfg.trailMode)) {
            renderVisualParticles(poseStack, cfg, cameraPos);
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  残影渲染 — Billboard四边形，始终面向相机
    // ════════════════════════════════════════════════════════════════

    private void renderGhosts(PoseStack poseStack, TrailConfig cfg, Vec3 cameraPos) {
        long now = System.currentTimeMillis();
        Iterator<GhostRecord> it = ghostRecords.iterator();
        while (it.hasNext()) {
            GhostRecord ghost = it.next();
            float age = (now - ghost.timestamp) / 1000.0f;
            float alphaRange = (float)(cfg.ghostAlphaStart - cfg.ghostAlphaEnd);
            ghost.alpha = Math.max(0, (float)cfg.ghostAlphaStart - age * Math.max(alphaRange * 2, 0.5f));
            if (ghost.alpha <= 0.01f) it.remove();
        }
        if (ghostRecords.isEmpty()) return;

        Color color = parseColor(cfg.mainColor);
        int r = color.getRed(), g = color.getGreen(), b = color.getBlue();

        // Billboard基向量
        Vector3f lookVec = mc.getEntityRenderDispatcher().camera.getLookVector();
        Vec3 forward = new Vec3(lookVec.x(), lookVec.y(), lookVec.z());
        Vec3 worldUp = new Vec3(0, 1, 0);
        if (Math.abs(forward.y) > 0.99f) worldUp = new Vec3(0, 0, 1);
        Vec3 right = forward.cross(worldUp).normalize();
        Vec3 up = right.cross(forward).normalize();

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(com.mojang.blaze3d.platform.GlStateManager.SourceFactor.SRC_ALPHA,
            com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator t = Tesselator.getInstance();
        BufferBuilder buf = t.getBuilder();

        for (GhostRecord ghost : ghostRecords) {
            Vec3 renderPos = ghost.pos.subtract(cameraPos);
            float alpha = ghost.alpha;
            if (alpha <= 0.01f) continue;

            float hw = 0.3f, hh = 0.9f;
            int ia = (int)(alpha * 200);

            Vec3 v1 = renderPos.add(right.scale(-hw)).add(up.scale(-hh));
            Vec3 v2 = renderPos.add(right.scale(hw)).add(up.scale(-hh));
            Vec3 v3 = renderPos.add(right.scale(hw)).add(up.scale(hh));
            Vec3 v4 = renderPos.add(right.scale(-hw)).add(up.scale(hh));

            buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            buf.vertex(poseStack.last().pose(), (float)v1.x, (float)v1.y, (float)v1.z).color(r, g, b, ia).endVertex();
            buf.vertex(poseStack.last().pose(), (float)v2.x, (float)v2.y, (float)v2.z).color(r, g, b, ia).endVertex();
            buf.vertex(poseStack.last().pose(), (float)v3.x, (float)v3.y, (float)v3.z).color(r, g, b, ia).endVertex();
            buf.vertex(poseStack.last().pose(), (float)v4.x, (float)v4.y, (float)v4.z).color(r, g, b, ia).endVertex();
            BufferUploader.drawWithShader(buf.end());
        }

        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }

    // ════════════════════════════════════════════════════════════════
    //  流光轨迹渲染 — 连续线段，Billboard宽度方向，始终面向相机
    // ════════════════════════════════════════════════════════════════

    private void renderLightStreak(PoseStack poseStack, TrailConfig cfg, Vec3 cameraPos) {
        if (pathPoints.size() < 2) return;

        Color startColor = parseColor(cfg.streakColorStart);
        Color endColor = parseColor(cfg.streakColorEnd);
        int r1 = startColor.getRed(), g1 = startColor.getGreen(), b1 = startColor.getBlue();
        int r2 = endColor.getRed(), g2 = endColor.getGreen(), b2 = endColor.getBlue();

        // 相机前方向
        Vector3f lookVec = mc.getEntityRenderDispatcher().camera.getLookVector();
        Vec3 camForward = new Vec3(lookVec.x(), lookVec.y(), lookVec.z());

        PathPoint[] points = pathPoints.toArray(new PathPoint[0]);
        int pointCount = points.length;

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(com.mojang.blaze3d.platform.GlStateManager.SourceFactor.SRC_ALPHA,
            com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator t = Tesselator.getInstance();
        BufferBuilder buf = t.getBuilder();

        float baseWidth = (float)cfg.streakWidth * 3.0f;
        float glowWidth = baseWidth * 2.5f;

        // 连续线段：每个线段使用segmentDir×camForward计算宽度方向，确保始终面向相机
        for (int i = 0; i < pointCount - 1; i++) {
            PathPoint p0 = points[i];
            PathPoint p1 = points[i + 1];
            Vec3 pos0 = p0.pos.subtract(cameraPos);
            Vec3 pos1 = p1.pos.subtract(cameraPos);

            float ageRatio = (float)i / (float)(pointCount - 1);
            int alpha = (int)((1.0f - ageRatio) * 220);
            if (alpha < 3) continue;

            int cr = (int)Mth.lerp(ageRatio, r1, r2);
            int cg = (int)Mth.lerp(ageRatio, g1, g2);
            int cb = (int)Mth.lerp(ageRatio, b1, b2);

            // 线段方向
            Vec3 segDir = pos1.subtract(pos0);
            double segLen = segDir.length();
            if (segLen < 0.001) continue;
            segDir = segDir.normalize();

            // ★ Billboard宽度方向：segDir × camForward，确保始终面向相机
            Vec3 wDir = segDir.cross(camForward).normalize();
            // 如果wDir长度≈0（线段平行于视线），用fallback
            if (wDir.length() < 0.1) {
                Vec3 fallback = new Vec3(0, 1, 0);
                wDir = segDir.cross(fallback).normalize();
                if (wDir.length() < 0.1) {
                    wDir = new Vec3(1, 0, 0);
                }
            }

            // 外发光层
            int glowAlpha = alpha / 4;
            Vec3 gl0 = pos0.add(wDir.scale(-glowWidth));
            Vec3 gr0 = pos0.add(wDir.scale(glowWidth));
            Vec3 gl1 = pos1.add(wDir.scale(-glowWidth));
            Vec3 gr1 = pos1.add(wDir.scale(glowWidth));

            buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            buf.vertex(poseStack.last().pose(), (float)gl0.x, (float)gl0.y, (float)gl0.z).color(cr, cg, cb, glowAlpha).endVertex();
            buf.vertex(poseStack.last().pose(), (float)gr0.x, (float)gr0.y, (float)gr0.z).color(cr, cg, cb, glowAlpha).endVertex();
            buf.vertex(poseStack.last().pose(), (float)gr1.x, (float)gr1.y, (float)gr1.z).color(cr, cg, cb, glowAlpha).endVertex();
            buf.vertex(poseStack.last().pose(), (float)gl1.x, (float)gl1.y, (float)gl1.z).color(cr, cg, cb, glowAlpha).endVertex();
            BufferUploader.drawWithShader(buf.end());

            // 核心层
            float coreW = baseWidth * 0.4f;
            Vec3 cl0 = pos0.add(wDir.scale(-coreW));
            Vec3 cr0 = pos0.add(wDir.scale(coreW));
            Vec3 cl1 = pos1.add(wDir.scale(-coreW));
            Vec3 cr1 = pos1.add(wDir.scale(coreW));

            buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            buf.vertex(poseStack.last().pose(), (float)cl0.x, (float)cl0.y, (float)cl0.z).color(cr, cg, cb, alpha).endVertex();
            buf.vertex(poseStack.last().pose(), (float)cr0.x, (float)cr0.y, (float)cr0.z).color(cr, cg, cb, alpha).endVertex();
            buf.vertex(poseStack.last().pose(), (float)cr1.x, (float)cr1.y, (float)cr1.z).color(cr, cg, cb, alpha).endVertex();
            buf.vertex(poseStack.last().pose(), (float)cl1.x, (float)cl1.y, (float)cl1.z).color(cr, cg, cb, alpha).endVertex();
            BufferUploader.drawWithShader(buf.end());
        }

        // 光晕点
        int glowStep = Math.max(1, pointCount / 20);
        for (int i = 0; i < pointCount; i += glowStep) {
            PathPoint pp = points[i];
            Vec3 renderPos = pp.pos.subtract(cameraPos);
            float ageRatio = (float)i / (float)(pointCount - 1);
            int alpha = (int)((1.0f - ageRatio) * 100);
            if (alpha < 5) continue;

            // 光晕点使用Billboard（世界Y轴向上）
            Vec3 billboardUp = new Vec3(0, 1, 0);
            if (Math.abs(camForward.y) > 0.99f) billboardUp = new Vec3(0, 0, 1);
            Vec3 bbRight = camForward.cross(billboardUp).normalize();
            Vec3 bbUp = bbRight.cross(camForward).normalize();

            int cr = (int)Mth.lerp(ageRatio, r1, r2);
            int cg = (int)Mth.lerp(ageRatio, g1, g2);
            int cb = (int)Mth.lerp(ageRatio, b1, b2);

            float dotSize = baseWidth * 0.8f;
            Vec3 d1 = renderPos.add(bbRight.scale(-dotSize)).add(bbUp.scale(-dotSize));
            Vec3 d2 = renderPos.add(bbRight.scale(dotSize)).add(bbUp.scale(-dotSize));
            Vec3 d3 = renderPos.add(bbRight.scale(dotSize)).add(bbUp.scale(dotSize));
            Vec3 d4 = renderPos.add(bbRight.scale(-dotSize)).add(bbUp.scale(dotSize));

            buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            buf.vertex(poseStack.last().pose(), (float)d1.x, (float)d1.y, (float)d1.z).color(cr, cg, cb, alpha).endVertex();
            buf.vertex(poseStack.last().pose(), (float)d2.x, (float)d2.y, (float)d2.z).color(cr, cg, cb, alpha).endVertex();
            buf.vertex(poseStack.last().pose(), (float)d3.x, (float)d3.y, (float)d3.z).color(cr, cg, cb, alpha).endVertex();
            buf.vertex(poseStack.last().pose(), (float)d4.x, (float)d4.y, (float)d4.z).color(cr, cg, cb, alpha).endVertex();
            BufferUploader.drawWithShader(buf.end());
        }

        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.blendFunc(com.mojang.blaze3d.platform.GlStateManager.SourceFactor.SRC_ALPHA,
            com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.disableBlend();
    }

    // ════════════════════════════════════════════════════════════════
    //  自定义视觉粒子渲染 — Billboard四边形
    // ════════════════════════════════════════════════════════════════

    private void renderVisualParticles(PoseStack poseStack, TrailConfig cfg, Vec3 cameraPos) {
        if (visualParticles.isEmpty()) return;

        Vector3f lookVec = mc.getEntityRenderDispatcher().camera.getLookVector();
        Vec3 forward = new Vec3(lookVec.x(), lookVec.y(), lookVec.z());
        Vec3 worldUp = new Vec3(0, 1, 0);
        if (Math.abs(forward.y) > 0.99f) worldUp = new Vec3(0, 0, 1);
        Vec3 right = forward.cross(worldUp).normalize();
        Vec3 up = right.cross(forward).normalize();

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(com.mojang.blaze3d.platform.GlStateManager.SourceFactor.SRC_ALPHA,
            com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator t = Tesselator.getInstance();
        BufferBuilder buf = t.getBuilder();

        for (VisualParticle vp : visualParticles) {
            Vec3 renderPos = vp.pos.subtract(cameraPos);
            float alpha = vp.getAlpha();
            if (alpha <= 0.01f) continue;

            int r = (vp.color >> 16) & 0xFF;
            int g = (vp.color >> 8) & 0xFF;
            int b = vp.color & 0xFF;
            int ia = (int)(alpha * 200);
            float s = vp.size;

            Vec3 v1 = renderPos.add(right.scale(-s)).add(up.scale(-s));
            Vec3 v2 = renderPos.add(right.scale(s)).add(up.scale(-s));
            Vec3 v3 = renderPos.add(right.scale(s)).add(up.scale(s));
            Vec3 v4 = renderPos.add(right.scale(-s)).add(up.scale(s));

            buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            buf.vertex(poseStack.last().pose(), (float)v1.x, (float)v1.y, (float)v1.z).color(r, g, b, ia).endVertex();
            buf.vertex(poseStack.last().pose(), (float)v2.x, (float)v2.y, (float)v2.z).color(r, g, b, ia).endVertex();
            buf.vertex(poseStack.last().pose(), (float)v3.x, (float)v3.y, (float)v3.z).color(r, g, b, ia).endVertex();
            buf.vertex(poseStack.last().pose(), (float)v4.x, (float)v4.y, (float)v4.z).color(r, g, b, ia).endVertex();
            BufferUploader.drawWithShader(buf.end());
        }

        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.blendFunc(com.mojang.blaze3d.platform.GlStateManager.SourceFactor.SRC_ALPHA,
            com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.disableBlend();
    }

    private static Color parseColor(String hex) {
        try {
            return new Color(Integer.parseInt(hex, 16));
        } catch (Exception e) {
            return new Color(0x00FFAA);
        }
    }

    public int getPathPointCount() { return pathPoints.size(); }
    public int getGhostCount() { return ghostRecords.size(); }
}