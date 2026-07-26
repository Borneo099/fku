package fku.org.example.fku.features.attackindicator; /* water */

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * 攻击指示器渲染器 — 精简版：保留能量光束、脉冲波、光环、光柱标记，剑气重做为BattoSlash风格
 * 该渲染器由赛博教员实现
 */
@OnlyIn(Dist.CLIENT)
public class AttackIndicatorRenderer {
    private static final Minecraft mc = Minecraft.getInstance();

    /** 渲染玩家与目标之间的连接特效 */
    public static void renderConnectionEffects(PoseStack poseStack, Entity player, Entity target, AttackIndicatorConfig cfg) {
        if (player == null || target == null) return;
        double dist = player.distanceTo(target);
        if (dist > (cfg.particleLODDistance * 2.0f)) return;

        Vec3 playerPos = player.position().add(0.0, player.getBbHeight() * 0.3, 0.0);
        Vec3 targetPos = target.position().add(0.0, target.getBbHeight() * 0.5, 0.0);
        Vec3 cameraPos = mc.getEntityRenderDispatcher().camera.getPosition();
        Vec3 start = playerPos.subtract(cameraPos);
        Vec3 end = targetPos.subtract(cameraPos);

        if (cfg.enableBeam) renderBeam(poseStack, start, end, cfg);
        if (cfg.enablePulseWave) renderPulseWave(poseStack, start, end, cfg);
    }

    /** 渲染目标特效（光环、光束标记） */
    public static void renderTargetEffects(PoseStack poseStack, LivingEntity target, AttackIndicatorConfig cfg) {
        if (target == null) return;
        double dist = mc.player != null ? mc.player.distanceTo(target) : 0.0;
        if (dist > (cfg.particleLODDistance * 2.0f)) return;

        float partialTick = mc.getPartialTick();
        double renderX = target.xOld + (target.getX() - target.xOld) * partialTick;
        double renderY = target.yOld + (target.getY() - target.yOld) * partialTick;
        double renderZ = target.zOld + (target.getZ() - target.zOld) * partialTick;
        Vec3 cameraPos = mc.getEntityRenderDispatcher().camera.getPosition();

        poseStack.pushPose();
        poseStack.translate(renderX - cameraPos.x, renderY - cameraPos.y, renderZ - cameraPos.z);

        if (cfg.enableHalo) renderHalo(poseStack, target, cfg);
        if (cfg.enableBeamMarker) renderBeamMarker(poseStack, target, cfg);

        poseStack.popPose();
    }

    // ═══════ 能量光束 ═══════
    /** ★ 能量光束 — 从玩家到目标的持续光束 */
    private static void renderBeam(PoseStack poseStack, Vec3 start, Vec3 end, AttackIndicatorConfig cfg) {
        Vec3 diff = end.subtract(start);
        double length = diff.length();
        if (length < 0.1 || length > 64.0) return;
        Color color = parseColor(cfg.beamColor);
        float time = (System.currentTimeMillis() % 2000L) / 2000.0f;
        float flow = (float)(time * Math.PI * 4.0);
        float width = cfg.beamWidth * 0.05f;

        Vec3 dir = diff.normalize();
        Vec3 up = new Vec3(0.0, 1.0, 0.0);
        if (Math.abs(dir.dot(up)) > 0.99) up = new Vec3(1.0, 0.0, 0.0);
        Vec3 right = dir.cross(up).normalize();

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator t = Tesselator.getInstance();
        BufferBuilder buf = t.getBuilder();
        int r = color.getRed(), g = color.getGreen(), b = color.getBlue();
        int segments = 32;

        // 主要光束
        buf.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        for (int i = 0; i <= segments; i++) {
            double t1 = (double)i / segments;
            Vec3 p = start.add(diff.scale(t1));
            float pulse = 0.8f + 0.2f * (float)Math.sin(t1 * Math.PI * 8.0 + flow);
            float w = width * pulse;
            int alpha = (int)(180.0f * pulse);
            Vec3 rv = right.scale(w);
            buf.vertex(poseStack.last().pose(), (float)(p.x + rv.x), (float)(p.y + rv.y), (float)(p.z + rv.z)).color(r, g, b, alpha).endVertex();
            buf.vertex(poseStack.last().pose(), (float)(p.x - rv.x), (float)(p.y - rv.y), (float)(p.z - rv.z)).color(r, g, b, alpha).endVertex();
        }
        BufferUploader.drawWithShader(buf.end());

        // 核心白色高亮
        buf.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        for (int i = 0; i <= segments; i++) {
            double t1 = (double)i / segments;
            Vec3 p = start.add(diff.scale(t1));
            float pulse = 0.8f + 0.2f * (float)Math.sin(t1 * Math.PI * 8.0 + flow + 1.0);
            float w = width * 0.3f * pulse;
            int alpha = (int)(100.0f * pulse);
            Vec3 rv = right.scale(w);
            buf.vertex(poseStack.last().pose(), (float)(p.x + rv.x), (float)(p.y + rv.y), (float)(p.z + rv.z)).color(255, 255, 255, alpha).endVertex();
            buf.vertex(poseStack.last().pose(), (float)(p.x - rv.x), (float)(p.y - rv.y), (float)(p.z - rv.z)).color(255, 255, 255, alpha).endVertex();
        }
        BufferUploader.drawWithShader(buf.end());

        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
    }

    // ═══════ 脉冲波 ═══════
    /** ★ 脉冲波 — 沿连接线传播的脉冲能量球 */
    private static void renderPulseWave(PoseStack poseStack, Vec3 start, Vec3 end, AttackIndicatorConfig cfg) {
        Vec3 diff = end.subtract(start);
        double length = diff.length();
        if (length < 0.1 || length > 64.0) return;
        Color color = parseColor(cfg.waveColor);
        float time = (System.currentTimeMillis() % 2000L) / 2000.0f;
        float speed = cfg.waveSpeed;

        // 脉冲位置沿连接线移动
        float t = (time * speed) % 1.0f;
        Vec3 pos = start.add(diff.scale(t));
        float radius = (float)(length * 0.04f);

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buf = tesselator.getBuilder();
        int r = color.getRed(), g = color.getGreen(), b = color.getBlue();
        int alpha = (int)(200.0f * (1.0f - Math.abs(t - 0.5f) * 2.0f));

        // 能量球（圆环）
        buf.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        int segments = 16;
        for (int i = 0; i <= segments; i++) {
            float a = (float)(Math.PI * 2.0 * i / segments);
            float cos = (float)Math.cos(a), sin = (float)Math.sin(a);
            float inner = radius * 0.5f;
            float outer = radius;
            buf.vertex(poseStack.last().pose(), (float)(pos.x + cos * outer), (float)(pos.y + sin * outer), (float)(pos.z)).color(r, g, b, alpha).endVertex();
            buf.vertex(poseStack.last().pose(), (float)(pos.x + cos * inner), (float)(pos.y + sin * inner), (float)(pos.z)).color(r, g, b, (int)(alpha * 0.5f)).endVertex();
        }
        BufferUploader.drawWithShader(buf.end());

        // 中心闪光
        buf.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        int flashAlpha = (int)(150.0f * (1.0f - Math.abs(t - 0.5f) * 2.0f));
        for (int i = 0; i <= segments; i++) {
            float a = (float)(Math.PI * 2.0 * i / segments);
            float cos = (float)Math.cos(a), sin = (float)Math.sin(a);
            float inner = 0.0f;
            float outer = radius * 0.3f;
            buf.vertex(poseStack.last().pose(), (float)(pos.x + cos * outer), (float)(pos.y + sin * outer), (float)(pos.z)).color(255, 255, 255, flashAlpha).endVertex();
            buf.vertex(poseStack.last().pose(), (float)(pos.x + cos * inner), (float)(pos.y + sin * inner), (float)(pos.z)).color(255, 255, 255, (int)(flashAlpha * 0.3f)).endVertex();
        }
        BufferUploader.drawWithShader(buf.end());

        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
    }

    // ═══════ 目标特效 ═══════

    /** 光晕 — 目标脚下的发光圆环 */
    private static void renderHalo(PoseStack poseStack, LivingEntity target, AttackIndicatorConfig cfg) {
        float radius = target.getBbWidth() * cfg.haloRadius;
        float time = (float)((System.currentTimeMillis() % 10000L) / 10000.0f * (Math.PI * 2));
        Color color = parseColor(cfg.haloColor);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator t = Tesselator.getInstance();
        BufferBuilder buf = t.getBuilder();
        buf.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        int segments = 32;
        float angle = time * cfg.haloRotateSpeed;
        int r = color.getRed(), g = color.getGreen(), b = color.getBlue();
        for (int i = 0; i <= segments; i++) {
            float a = (float)(angle + (Math.PI * 2 * i / segments));
            float x = (float)Math.cos(a) * radius;
            float z = (float)Math.sin(a) * radius;
            float alpha = 0.3f + 0.7f * (0.5f + 0.5f * (float)Math.sin(a * 2.0f + time * 4.0f));
            buf.vertex(poseStack.last().pose(), x, 0.05f, z).color(r, g, b, (int)(alpha * 80.0f)).endVertex();
            buf.vertex(poseStack.last().pose(), x, 0.15f, z).color(r, g, b, (int)(alpha * 40.0f)).endVertex();
        }
        BufferUploader.drawWithShader(buf.end());
        RenderSystem.enableCull();
    }

    /** 光束标记 — 从目标头顶射出的光束 */
    private static void renderBeamMarker(PoseStack poseStack, LivingEntity target, AttackIndicatorConfig cfg) {
        float height = cfg.beamMarkerHeight;
        Color color = parseColor(cfg.beamMarkerColor);
        float time = (System.currentTimeMillis() % 2000L) / 2000.0f;

        Vec3 cameraPos = mc.getEntityRenderDispatcher().camera.getPosition();
        Vec3 targetPos = target.position().add(0.0, target.getBbHeight() * 0.5, 0.0);
        Vec3 toCamera = cameraPos.subtract(targetPos).normalize();
        Vec3 up = new Vec3(0.0, 1.0, 0.0);
        Vec3 right = toCamera.cross(up).normalize();
        if (right.length() < 0.001) right = new Vec3(1.0, 0.0, 0.0);
        Vec3 billboardUp = right.cross(toCamera).normalize();

        float frx = (float)right.x, fry = (float)right.y, frz = (float)right.z;
        float fux = (float)billboardUp.x, fuy = (float)billboardUp.y, fuz = (float)billboardUp.z;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator t = Tesselator.getInstance();
        BufferBuilder buf = t.getBuilder();
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        float alpha = 0.3f + 0.3f * (float)Math.sin(time * Math.PI * 2.0);
        int r = color.getRed(), g = color.getGreen(), b = color.getBlue();
        for (int layer = 0; layer < 3; layer++) {
            float width = 0.1f * (1.0f + layer * 0.5f);
            float aMul = 1.0f - layer * 0.3f;
            int a = (int)(alpha * 80.0f * aMul);
            int aTop = (int)(alpha * 25.0f * aMul);

            float bx1 = -width * frx, by1 = -width * fry, bz1 = -width * frz;
            float bx2 = width * frx, by2 = width * fry, bz2 = width * frz;
            float tx1 = -width * 0.2f * frx + height * fux;
            float ty1 = -width * 0.2f * fry + height * fuy;
            float tz1 = -width * 0.2f * frz + height * fuz;
            float tx2 = width * 0.2f * frx + height * fux;
            float ty2 = width * 0.2f * fry + height * fuy;
            float tz2 = width * 0.2f * frz + height * fuz;

            buf.vertex(poseStack.last().pose(), bx1, by1, bz1).color(r, g, b, a).endVertex();
            buf.vertex(poseStack.last().pose(), bx2, by2, bz2).color(r, g, b, a).endVertex();
            buf.vertex(poseStack.last().pose(), tx2, ty2, tz2).color(r, g, b, aTop).endVertex();
            buf.vertex(poseStack.last().pose(), tx1, ty1, tz1).color(r, g, b, aTop).endVertex();
        }
        BufferUploader.drawWithShader(buf.end());
        RenderSystem.enableCull();
    }

    /**
     * ★ 剑波 — 复刻test模组BattoSlash风格的水平月牙剑气
     * 设计思想：参考AkatZumaTool模组的BattoSlash效果，使用水平月牙形+多层辉光
     * - 水平月牙：在水平面（right-dir）内展开的半圆形月牙，剑波面朝目标
     * - 多层结构：外发光层 → 辉光层 → 主体层 → 核心高亮层
     * - 脉冲效果：沿路径先扩张后收缩，中心到两端渐变
     * - 颜色动态循环：Java层实时计算HSV偏移
     * 参考：BattoSlashQueue (c:/Users/18151/Desktop/fku/test)
     * 该方法由赛博教员实现
     */
    public static void renderSwordWave(PoseStack poseStack, Vec3 start, Vec3 end, float progress, AttackIndicatorConfig cfg) {
        Vec3 diff = end.subtract(start);
        double length = diff.length();
        if (length < 0.5 || length > 64.0) return;
        Vec3 dir = diff.normalize();

        // 构建水平平面（right, dir）— 月牙形在水平面内展开，深度偏移沿垂直方向（perp）
        Vec3 up = new Vec3(0.0, 1.0, 0.0);
        if (Math.abs(dir.dot(up)) > 0.99) up = new Vec3(1.0, 0.0, 0.0);
        Vec3 right = dir.cross(up).normalize();
        Vec3 perp = right.cross(dir).normalize(); // 垂直方向

        // 剑气沿路径移动，先扩张后收缩
        Vec3 wavePos = start.add(diff.scale(progress));
        float sizeScale = 0.2f + 0.8f * (float)Math.sin(progress * Math.PI);
        float baseRadius = (float)(length * 0.18f * cfg.swordWaveIntensity * sizeScale);

        // 颜色计算（动态HSV偏移）
        Color color = parseColor(cfg.swordWaveColor);
        float time = (System.currentTimeMillis() % 10000L) / 10000.0f;
        int r = color.getRed(), g = color.getGreen(), b = color.getBlue();

        // 渲染状态：叠加混合（additive blending）实现发光效果
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator t = Tesselator.getInstance();
        BufferBuilder buf = t.getBuilder();

        float frx = (float)right.x, fry = (float)right.y, frz = (float)right.z;
        float fpx = (float)perp.x, fpy = (float)perp.y, fpz = (float)perp.z;
        float fdx = (float)dir.x, fdy = (float)dir.y, fdz = (float)dir.z;

        int segments = 32;
        float arcSpan = 3.14159f; // 半圆
        float arcStart = -arcSpan * 0.5f;

        // ════════════════════════════════════════════
        // ★ 1. 外发光层（最外层，大且透明 — 模拟Bloom辉光）
        //    水平面内对称展开（sin*right + cos*dir），弧线从左→前→右
        // ════════════════════════════════════════════
        float glowOuter = baseRadius * 1.5f;
        buf.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        for (int i = 0; i <= segments; i++) {
            float angle = arcStart + arcSpan * i / segments;
            float cos = (float)Math.cos(angle);
            float sin = (float)Math.sin(angle);
            float depthFactor = (float)Math.abs(cos);
            float innerR = glowOuter * 0.3f;

            // ★ 对称弧线：sin*right + cos*dir → 左→前→右，面朝目标
            float ox = (sin * glowOuter * frx + cos * glowOuter * fdx);
            float oy = (sin * glowOuter * fry + cos * glowOuter * fdy);
            float oz = (sin * glowOuter * frz + cos * glowOuter * fdz);
            float ix = (sin * innerR * frx + cos * innerR * fdx);
            float iy = (sin * innerR * fry + cos * innerR * fdy);
            float iz = (sin * innerR * frz + cos * innerR * fdz);

            int alpha = (int)(25 * sizeScale * depthFactor);
            buf.vertex(poseStack.last().pose(), (float)(wavePos.x + ox), (float)(wavePos.y + oy), (float)(wavePos.z + oz)).color(r, g, b, alpha).endVertex();
            buf.vertex(poseStack.last().pose(), (float)(wavePos.x + ix), (float)(wavePos.y + iy), (float)(wavePos.z + iz)).color(r, g, b, alpha).endVertex();
        }
        BufferUploader.drawWithShader(buf.end());

        // ════════════════════════════════════════════
        // ★ 2. 辉光层（中等透明度，比主体略大）
        // ════════════════════════════════════════════
        float glowInner = baseRadius * 1.2f;
        buf.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        for (int i = 0; i <= segments; i++) {
            float angle = arcStart + arcSpan * i / segments;
            float cos = (float)Math.cos(angle);
            float sin = (float)Math.sin(angle);
            float depthFactor = (float)Math.abs(cos);
            float innerR = glowInner * 0.35f;

            float ox = (sin * glowInner * frx + cos * glowInner * fdx);
            float oy = (sin * glowInner * fry + cos * glowInner * fdy);
            float oz = (sin * glowInner * frz + cos * glowInner * fdz);
            float ix = (sin * innerR * frx + cos * innerR * fdx);
            float iy = (sin * innerR * fry + cos * innerR * fdy);
            float iz = (sin * innerR * frz + cos * innerR * fdz);

            int alpha = (int)(50 * sizeScale * depthFactor);
            buf.vertex(poseStack.last().pose(), (float)(wavePos.x + ox), (float)(wavePos.y + oy), (float)(wavePos.z + oz)).color(r, g, b, alpha).endVertex();
            buf.vertex(poseStack.last().pose(), (float)(wavePos.x + ix), (float)(wavePos.y + iy), (float)(wavePos.z + iz)).color(r, g, b, alpha).endVertex();
        }
        BufferUploader.drawWithShader(buf.end());

        // ════════════════════════════════════════════
        // ★ 3. 主体层（核心月牙 — 主要视觉效果）
        //    水平面内对称展开（sin*right + cos*dir），垂直偏移增强立体感
        // ════════════════════════════════════════════
        buf.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        for (int i = 0; i <= segments; i++) {
            float angle = arcStart + arcSpan * i / segments;
            float cos = (float)Math.cos(angle);
            float sin = (float)Math.sin(angle);
            float depthFactor = (float)Math.abs(cos);
            float innerR = baseRadius * (0.15f + 0.85f * (1.0f - depthFactor));
            // ★ 垂直深度偏移（沿perp方向）
            float depth = baseRadius * 0.04f * depthFactor;

            // ★ 对称弧线：sin*right + cos*dir → 左→前→右，面朝目标
            float ox = (sin * baseRadius * frx + cos * baseRadius * fdx);
            float oy = (sin * baseRadius * fry + cos * baseRadius * fdy);
            float oz = (sin * baseRadius * frz + cos * baseRadius * fdz);
            float ix = (sin * innerR * frx + cos * innerR * fdx);
            float iy = (sin * innerR * fry + cos * innerR * fdy);
            float iz = (sin * innerR * frz + cos * innerR * fdz);

            // 颜色动态偏移（模拟HSV循环）
            float hueShift = time * 0.3f + progress * 0.2f;
            int cr = (int)Math.min(255, r * (0.8f + 0.2f * (float)Math.sin(hueShift * Math.PI * 2)));
            int cg = (int)Math.min(255, g * (0.8f + 0.2f * (float)Math.sin(hueShift * Math.PI * 2 + 2.094f)));
            int cb = (int)Math.min(255, b * (0.8f + 0.2f * (float)Math.sin(hueShift * Math.PI * 2 + 4.188f)));

            int alpha = (int)(200 * sizeScale * depthFactor);

            // ★ 深度偏移沿垂直方向（perp），使月牙在水平面展开
            buf.vertex(poseStack.last().pose(),
                (float)(wavePos.x + ox + fpx * depth), (float)(wavePos.y + oy + fpy * depth), (float)(wavePos.z + oz + fpz * depth))
                .color(cr, cg, cb, alpha).endVertex();
            buf.vertex(poseStack.last().pose(),
                (float)(wavePos.x + ix - fpx * depth), (float)(wavePos.y + iy - fpy * depth), (float)(wavePos.z + iz - fpz * depth))
                .color(cr, cg, cb, (int)(alpha * 0.7f)).endVertex();
        }
        BufferUploader.drawWithShader(buf.end());

        // ════════════════════════════════════════════
        // ★ 4. 核心高亮层（白色发光核心）
        // ════════════════════════════════════════════
        float coreR = baseRadius * 0.45f;
        buf.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        for (int i = 0; i <= segments; i++) {
            float angle = arcStart + arcSpan * i / segments;
            float cos = (float)Math.cos(angle);
            float sin = (float)Math.sin(angle);
            float depthFactor = (float)Math.abs(cos);
            float innerR = coreR * 0.3f;

            float ox = (sin * coreR * frx + cos * coreR * fdx);
            float oy = (sin * coreR * fry + cos * coreR * fdy);
            float oz = (sin * coreR * frz + cos * coreR * fdz);
            float ix = (sin * innerR * frx + cos * innerR * fdx);
            float iy = (sin * innerR * fry + cos * innerR * fdy);
            float iz = (sin * innerR * frz + cos * innerR * fdz);

            int alpha = (int)(180 * sizeScale * depthFactor);
            buf.vertex(poseStack.last().pose(), (float)(wavePos.x + ox), (float)(wavePos.y + oy), (float)(wavePos.z + oz)).color(255, 255, 255, alpha).endVertex();
            buf.vertex(poseStack.last().pose(), (float)(wavePos.x + ix), (float)(wavePos.y + iy), (float)(wavePos.z + iz)).color(255, 255, 255, (int)(alpha * 0.4f)).endVertex();
        }
        BufferUploader.drawWithShader(buf.end());

        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
    }

    // ═══════ 颜色工具方法 ═══════

    /** 解析6位十六进制颜色字符串为AWT Color对象 */
    public static Color parseColor(String hex) {
        try {
            if (hex == null) return Color.RED;
            if (hex.startsWith("#")) hex = hex.substring(1);
            if (hex.length() == 6) {
                int r = Integer.parseInt(hex.substring(0, 2), 16);
                int g = Integer.parseInt(hex.substring(2, 4), 16);
                int b = Integer.parseInt(hex.substring(4, 6), 16);
                return new Color(r, g, b);
            }
            if (hex.length() == 8) {
                int a = Integer.parseInt(hex.substring(0, 2), 16);
                int r = Integer.parseInt(hex.substring(2, 4), 16);
                int g = Integer.parseInt(hex.substring(4, 6), 16);
                int b = Integer.parseInt(hex.substring(6, 8), 16);
                return new Color(r, g, b, a);
            }
            return Color.RED;
        } catch (Exception e) {
            return Color.RED;
        }
    }
}