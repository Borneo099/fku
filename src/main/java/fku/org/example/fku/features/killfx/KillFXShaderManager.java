package fku.org.example.fku.features.killfx; /* water */

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import fku.org.example.fku.Fku;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.Vec3;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * KillFX 位置着色器特效管理器
 *
 * 每个视觉组件独立 begin/end 渲染，避免 BufferBuilder 状态冲突。
 */
public class KillFXShaderManager {

    private static final Minecraft mc = Minecraft.getInstance();
    private static final LinkedList<ShaderEffect> effects = new LinkedList<>();

    public enum ShaderType { NONE, BLACKHOLE }

    public static void trigger(ShaderType type, Vec3 pos, float intensity, int durationTicks) {
        if (type == ShaderType.NONE) return;
        effects.add(new ShaderEffect(type, pos, intensity, durationTicks));
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

    public static void renderEffects(PoseStack poseStack, float partialTick) {
        if (effects.isEmpty() || mc.player == null || mc.level == null) return;

        Vec3 camPos = mc.gameRenderer.getMainCamera().getPosition();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        // 每个效果独立渲染
        for (ShaderEffect effect : effects) {
            float ex = (float)(effect.pos.x - camPos.x);
            float ey = (float)(effect.pos.y - camPos.y);
            float ez = (float)(effect.pos.z - camPos.z);
            float p = effect.progress;
            float sc = effect.intensity;
            float sz = 1.5f * sc * Math.min(p * 2.5f, (1f - p) * 2f + 0.3f);
            float alpha = 1f - p;

            renderDistortion(poseStack, ex, ey, ez, sz, p, sc);
            renderSphere(poseStack, ex, ey, ez, sz, p);
            renderDisk(poseStack, ex, ey, ez, sz, p, sc);
            renderLensing(poseStack, ex, ey, ez, sz, p, sc);
            renderParticles(poseStack, ex, ey, ez, sz, p, sc);
        }

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

    private static class ShaderEffect {
        final ShaderType type;
        final Vec3 pos;
        final float intensity;
        final int duration;
        int elapsed = 0;
        float progress = 0f;
        ShaderEffect(ShaderType type, Vec3 pos, float intensity, int duration) {
            this.type = type; this.pos = pos; this.intensity = intensity; this.duration = duration;
        }
    }
}
