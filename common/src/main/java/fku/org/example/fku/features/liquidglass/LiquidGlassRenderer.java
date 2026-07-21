package fku.org.example.fku.features.liquidglass;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import fku.org.example.fku.features.liquidglass.LiquidGlassConfig;
import fku.org.example.fku.features.liquidglass.LiquidGlassShaderRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

public class LiquidGlassRenderer {
    private static final Minecraft mc = Minecraft.getInstance();
    private static RenderTarget blurFramebuffer;

    public static void updateMipMapBlurTexture() {
        RenderTarget mainTarget;
        int width = mc.getWindow().getWidth();
        int height = mc.getWindow().getHeight();
        if (blurFramebuffer == null || LiquidGlassRenderer.blurFramebuffer.width != width || LiquidGlassRenderer.blurFramebuffer.height != height) {
            if (blurFramebuffer != null) {
                blurFramebuffer.destroyBuffers();
            }
            blurFramebuffer = new TextureTarget(width, height, false, Minecraft.ON_MS);
            blurFramebuffer.setFilterMode(9729);
            RenderSystem.bindTexture(blurFramebuffer.getColorTextureId());
            GL11.glTexParameteri(3553, 10241, 9987);
            GL11.glTexParameteri(3553, 10240, 9729);
            GL11.glTexParameteri(3553, 10242, 33071);
            GL11.glTexParameteri(3553, 10243, 33071);
            RenderSystem.bindTexture(0);
        }
        if ((mainTarget = mc.getMainRenderTarget()) != null) {
            RenderSystem.assertOnRenderThreadOrInit();
            GlStateManager._glBindFramebuffer(36008, mainTarget.frameBufferId);
            GlStateManager._glBindFramebuffer(36009, LiquidGlassRenderer.blurFramebuffer.frameBufferId);
            GlStateManager._glBlitFrameBuffer(0, 0, width, height, 0, 0, width, height, 16384, 9728);
            GlStateManager._glBindFramebuffer(36160, 0);
            RenderSystem.bindTexture(blurFramebuffer.getColorTextureId());
            GL30.glGenerateMipmap(3553);
            RenderSystem.bindTexture(0);
            mainTarget.unbindRead(true);
        }
    }

    public static void drawPanel(LiquidGlassConfig cfg) {
        if (blurFramebuffer == null) {
            return;
        }
        ShaderInstance shader = LiquidGlassShaderRegistry.getLiquidGlassShader();
        if (shader == null) {
            return;
        }
        int x = cfg.panelX;
        int y = cfg.panelY;
        float w = cfg.panelWidth;
        float h = cfg.panelHeight;
        shader.setSampler("DiffuseSampler", blurFramebuffer.getColorTextureId());
        if (shader.getUniform("ScreenSize") != null) {
            shader.getUniform("ScreenSize").set(mc.getWindow().getWidth(), mc.getWindow().getHeight());
        }
        if (shader.getUniform("QuadPos") != null) {
            shader.getUniform("QuadPos").set(x, y, w, h);
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
            shader.getUniform("TintMode").setInt(cfg.tintMode);
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
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
    }

    public static void cleanup() {
        if (blurFramebuffer != null) {
            blurFramebuffer.destroyBuffers();
            blurFramebuffer = null;
        }
    }
}

