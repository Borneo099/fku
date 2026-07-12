package lexis.mixin.mixina;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Render.StarSkyHack;
import lexis.Hack.Hackutil.HackManager;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({LevelRenderer.class})
public class StarSkyMixin {
   private static final Minecraft mc = Minecraft.m_91087_();
   private static final ResourceLocation TEXTURE = new ResourceLocation("lexis", "modxpy/syc.png");
   private static final float OVERLAP = 0.002F;

   @Inject(
      method = {"renderSky"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onRenderSky(PoseStack p_202424_, Matrix4f p_254034_, float p_202426_, Camera p_202427_, boolean p_202428_, Runnable p_202429_, CallbackInfo ci) {
      Iterator var9 = HackManager.getInstance().getHacks().iterator();

      while(var9.hasNext()) {
         Hack hack = (Hack)var9.next();
         if (hack instanceof StarSkyHack starSky && hack.isEnabled()) {
            break;
         }
      }

      if (starSky != null) {
         ci.cancel();
         this.renderStarSphere(p_202424_);
      }

   }

   private void renderStarSphere(PoseStack poseStack) {
      RenderSystem.setShader(GameRenderer::m_172817_);
      RenderSystem.setShaderTexture(0, TEXTURE);
      GL11.glTexParameteri(3553, 10241, 9729);
      GL11.glTexParameteri(3553, 10240, 9729);
      RenderSystem.disableCull();
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      poseStack.m_85836_();
      Tesselator tesselator = Tesselator.m_85913_();
      BufferBuilder buffer = tesselator.m_85915_();
      Matrix4f matrix = poseStack.m_85850_().m_252922_();
      float radius = 100.0F;
      int stacks = 120;
      int slices = 240;
      buffer.m_166779_(Mode.QUADS, DefaultVertexFormat.f_85817_);

      for(int i = 0; i < stacks; ++i) {
         double phi0 = Math.PI * (double)i / (double)stacks;
         double phi1 = Math.PI * (double)(i + 1) / (double)stacks;
         double y0 = Math.cos(phi0);
         double y1 = Math.cos(phi1);
         double r0 = Math.sin(phi0);
         double r1 = Math.sin(phi1);

         for(int j = 0; j < slices; ++j) {
            double theta0 = 6.283185307179586 * (double)j / (double)slices;
            double theta1 = 6.283185307179586 * (double)(j + 1) / (double)slices;
            double x0 = Math.cos(theta0) * r0;
            double z0 = Math.sin(theta0) * r0;
            double x1 = Math.cos(theta0) * r1;
            double z1 = Math.sin(theta0) * r1;
            double x2 = Math.cos(theta1) * r1;
            double z2 = Math.sin(theta1) * r1;
            double x3 = Math.cos(theta1) * r0;
            double z3 = Math.sin(theta1) * r0;
            float u0 = (float)j / (float)slices;
            float u1 = (float)(j + 1) / (float)slices;
            float v0 = (float)i / (float)stacks;
            float v1 = (float)(i + 1) / (float)stacks;
            u0 -= 0.002F;
            u1 += 0.002F;
            buffer.m_252986_(matrix, (float)(x0 * (double)radius), (float)(y0 * (double)radius), (float)(z0 * (double)radius)).m_7421_(u0, v0).m_5752_();
            buffer.m_252986_(matrix, (float)(x1 * (double)radius), (float)(y1 * (double)radius), (float)(z1 * (double)radius)).m_7421_(u0, v1).m_5752_();
            buffer.m_252986_(matrix, (float)(x2 * (double)radius), (float)(y1 * (double)radius), (float)(z2 * (double)radius)).m_7421_(u1, v1).m_5752_();
            buffer.m_252986_(matrix, (float)(x3 * (double)radius), (float)(y0 * (double)radius), (float)(z3 * (double)radius)).m_7421_(u1, v0).m_5752_();
         }
      }

      tesselator.m_85914_();
      poseStack.m_85849_();
      RenderSystem.enableCull();
      RenderSystem.disableBlend();
   }
}
