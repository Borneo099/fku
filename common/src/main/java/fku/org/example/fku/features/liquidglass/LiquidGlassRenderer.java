package fku.org.example.fku.features.liquidglass; /* water */

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import fku.org.example.fku.Fku;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL30;

/**
 * 液体玻璃渲染核心
 * 负责：
 * 1. 创建和管理 mipmap 模糊帧缓冲
 * 2. 使用自定义着色器渲染玻璃面板
 *
 * ★ 参考：LiquidGlassShader (https://github.com/Jacquesqwq/LiquidGlassShader)
 *   移植其 V3 单通道 mipmap blur + glass pass 方案，适配 Forge 1.20.1
 *
 * 该渲染器由赛博教员实现
 */
public class LiquidGlassRenderer {

    private static final Minecraft mc = Minecraft.getInstance();
    private static RenderTarget blurFramebuffer;

    /**
     * 更新 mipmap 模糊纹理
     * 将主渲染目标的内容复制到模糊帧缓冲，并生成 mipmap 链
     */
    public static void updateMipMapBlurTexture() {
        int width = mc.getWindow().getWidth();
        int height = mc.getWindow().getHeight();

        if (blurFramebuffer == null || blurFramebuffer.width != width || blurFramebuffer.height != height) {
            if (blurFramebuffer != null) {
                blurFramebuffer.destroyBuffers();
            }
            blurFramebuffer = new TextureTarget(width, height, false, Minecraft.ON_OSX);
            blurFramebuffer.setFilterMode(GL11.GL_LINEAR);

            // 设置 mipmap 参数
            RenderSystem.bindTexture(blurFramebuffer.getColorTextureId());
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
            RenderSystem.bindTexture(0);
        }

        // 复制主帧缓冲到模糊帧缓冲
        RenderTarget mainTarget = mc.getMainRenderTarget();
        if (mainTarget != null) {
            // 使用 glBlitFramebuffer 复制颜色缓冲
            RenderSystem.assertOnRenderThreadOrInit();
            GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, mainTarget.frameBufferId);
            GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, blurFramebuffer.frameBufferId);
            GlStateManager._glBlitFrameBuffer(0, 0, width, height, 0, 0, width, height, GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);
            GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);

            // 生成 mipmap
            RenderSystem.bindTexture(blurFramebuffer.getColorTextureId());
            GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
            RenderSystem.bindTexture(0);

            // 绑定回主帧缓冲
            mainTarget.bindWrite(true);
        }
    }

    /**
     * 绘制液体玻璃面板
     *
     * @param cfg 液体玻璃配置
     */
    public static void drawPanel(LiquidGlassConfig cfg) {
        if (blurFramebuffer == null) return;

        ShaderInstance shader = LiquidGlassShaderRegistry.getLiquidGlassShader();
        if (shader == null) return;

        int x = cfg.panelX;
        int y = cfg.panelY;
        float w = cfg.panelWidth;
        float h = cfg.panelHeight;

        // 设置着色器 uniforms
        shader.setSampler("DiffuseSampler", blurFramebuffer.getColorTextureId());

        if (shader.getUniform("ScreenSize") != null) {
            shader.getUniform("ScreenSize").set((float) mc.getWindow().getWidth(), (float) mc.getWindow().getHeight());
        }
        if (shader.getUniform("QuadPos") != null) {
            shader.getUniform("QuadPos").set((float) x, (float) y, w, h);
        }
        if (shader.getUniform("QuadRadius") != null) {
            shader.getUniform("QuadRadius").set(cfg.cornerRadius);
        }
        if (shader.getUniform("BlurRadius") != null) {
            shader.getUniform("BlurRadius").set(cfg.blurRadius);
        }
        if (shader.getUniform("RefractionPower") != null) {
            shader.getUniform("RefractionPower").set(cfg.refractionPower);
        }
        if (shader.getUniform("RefractionEdge") != null) {
            shader.getUniform("RefractionEdge").set(cfg.refractionEdge);
        }
        if (shader.getUniform("Dispersion") != null) {
            shader.getUniform("Dispersion").set(cfg.dispersion);
        }
        if (shader.getUniform("GlobalAlpha") != null) {
            shader.getUniform("GlobalAlpha").set(cfg.globalAlpha);
        }
        if (shader.getUniform("GlassTint") != null) {
            shader.getUniform("GlassTint").set(cfg.tintR, cfg.tintG, cfg.tintB, cfg.tintStrength);
        }
        if (shader.getUniform("TintMode") != null) {
            shader.getUniform("TintMode").set(cfg.tintMode);
        }
        if (shader.getUniform("Noise") != null) {
            shader.getUniform("Noise").set(cfg.noise);
        }
        if (shader.getUniform("GlowWeight") != null) {
            shader.getUniform("GlowWeight").set(cfg.glowWeight);
        }
        if (shader.getUniform("GlowBias") != null) {
            shader.getUniform("GlowBias").set(cfg.glowBias);
        }
        if (shader.getUniform("GlowEdge0") != null) {
            shader.getUniform("GlowEdge0").set(cfg.glowEdge0);
        }
        if (shader.getUniform("GlowEdge1") != null) {
            shader.getUniform("GlowEdge1").set(cfg.glowEdge1);
        }
        if (shader.getUniform("ChromaStrength") != null) {
            shader.getUniform("ChromaStrength").set(cfg.chromaStrength);
        }
        if (shader.getUniform("Darkness") != null) {
            shader.getUniform("Darkness").set(cfg.darkness);
        }

        // 渲染全屏四边形
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(() -> shader);

        Matrix4f identity = new Matrix4f();
        identity.identity();

        Tesselator t = Tesselator.getInstance();
        BufferBuilder buf = t.getBuilder();
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        buf.vertex(identity, -1.0f, -1.0f, 0.0f).color(255, 255, 255, 255).endVertex();
        buf.vertex(identity, -1.0f, 1.0f, 0.0f).color(255, 255, 255, 255).endVertex();
        buf.vertex(identity, 1.0f, 1.0f, 0.0f).color(255, 255, 255, 255).endVertex();
        buf.vertex(identity, 1.0f, -1.0f, 0.0f).color(255, 255, 255, 255).endVertex();
        BufferUploader.drawWithShader(buf.end());

        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        RenderSystem.setShader(net.minecraft.client.renderer.GameRenderer::getPositionColorShader);
    }

    /**
     * 清理资源
     */
    public static void cleanup() {
        if (blurFramebuffer != null) {
            blurFramebuffer.destroyBuffers();
            blurFramebuffer = null;
        }
    }
}