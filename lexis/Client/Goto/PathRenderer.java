package lexis.Client.Goto;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.awt.Color;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

public class PathRenderer {
   private static final Minecraft MC = Minecraft.m_91087_();

   public static void renderPath(PoseStack poseStack, List path, boolean depthTest, boolean isPlayerTarget) {
      if (path != null && !path.isEmpty() && MC.f_91074_ != null) {
         poseStack.m_85836_();
         Vec3 cameraPos = MC.f_91063_.m_109153_().m_90583_();
         poseStack.m_85837_(-cameraPos.f_82479_, -cameraPos.f_82480_, -cameraPos.f_82481_);
         float time = (float)(System.currentTimeMillis() % 10000L) / 10000.0F;
         boolean wasDepthTest = GL11.glGetBoolean(2929);

         try {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(GameRenderer::m_172811_);
            RenderSystem.disableDepthTest();
            GL11.glEnable(2848);
            GL11.glHint(3154, 4354);
            RenderSystem.lineWidth(4.0F);
            renderRainbowPath(poseStack, path, time);
            renderBreathingPoints(poseStack, path, time);
            renderStartAndGoal(poseStack, path, time, isPlayerTarget);
            renderFlowParticles(poseStack, path, time);
         } finally {
            if (wasDepthTest) {
               RenderSystem.enableDepthTest();
            }

            RenderSystem.disableBlend();
            GL11.glDisable(2848);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.lineWidth(1.0F);
            poseStack.m_85849_();
         }

      }
   }

   private static void renderRainbowPath(PoseStack poseStack, List path, float time) {
      BufferBuilder buffer = Tesselator.m_85913_().m_85915_();
      Matrix4f matrix = poseStack.m_85850_().m_252922_();
      buffer.m_166779_(Mode.DEBUG_LINES, DefaultVertexFormat.f_85815_);

      for(int i = 0; i < path.size() - 1; ++i) {
         BlockPos current = (BlockPos)path.get(i);
         BlockPos next = (BlockPos)path.get(i + 1);
         float progress = (float)i / (float)(path.size() - 1);
         float flowOffset = (time + progress) % 1.0F;
         float hue = (progress + time * 0.5F) % 1.0F;
         int rgb = Color.HSBtoRGB(hue, 0.9F, 1.0F);
         float r = (float)(rgb >> 16 & 255) / 255.0F;
         float g = (float)(rgb >> 8 & 255) / 255.0F;
         float b = (float)(rgb & 255) / 255.0F;
         float alpha = 0.7F + 0.3F * (float)Math.sin((double)time * Math.PI * 2.0 + (double)i);
         buffer.m_252986_(matrix, (float)current.m_123341_() + 0.5F, (float)current.m_123342_() + 0.5F, (float)current.m_123343_() + 0.5F).m_85950_(r, g, b, alpha).m_5752_();
         buffer.m_252986_(matrix, (float)next.m_123341_() + 0.5F, (float)next.m_123342_() + 0.5F, (float)next.m_123343_() + 0.5F).m_85950_(r, g, b, alpha).m_5752_();
         if (i % 2 == 0) {
            float glowAlpha = 0.3F + 0.2F * (float)Math.sin((double)time * Math.PI * 3.0 + (double)i);
            buffer.m_252986_(matrix, (float)current.m_123341_() + 0.5F, (float)current.m_123342_() + 0.5F + 0.2F, (float)current.m_123343_() + 0.5F).m_85950_(r, g, b, glowAlpha).m_5752_();
            buffer.m_252986_(matrix, (float)next.m_123341_() + 0.5F, (float)next.m_123342_() + 0.5F + 0.2F, (float)next.m_123343_() + 0.5F).m_85950_(r, g, b, glowAlpha).m_5752_();
         }
      }

      BufferUploader.m_231202_(buffer.m_231175_());
   }

   private static void renderBreathingPoints(PoseStack poseStack, List path, float time) {
      BufferBuilder buffer = Tesselator.m_85913_().m_85915_();
      Matrix4f matrix = poseStack.m_85850_().m_252922_();
      buffer.m_166779_(Mode.DEBUG_LINES, DefaultVertexFormat.f_85815_);
      float breath = 0.5F + 0.5F * (float)Math.sin((double)time * Math.PI * 2.0);
      float size = 0.2F + breath * 0.15F;

      for(int i = 0; i < path.size(); ++i) {
         BlockPos pos = (BlockPos)path.get(i);
         float progress = (float)i / (float)path.size();
         int rgb = Color.HSBtoRGB(progress, 0.9F, 1.0F);
         float r = (float)(rgb >> 16 & 255) / 255.0F;
         float g = (float)(rgb >> 8 & 255) / 255.0F;
         float b = (float)(rgb & 255) / 255.0F;
         float alpha = 0.6F + breath * 0.3F;
         buffer.m_252986_(matrix, (float)pos.m_123341_() + 0.5F - size, (float)pos.m_123342_() + 0.5F, (float)pos.m_123343_() + 0.5F).m_85950_(r, g, b, alpha).m_5752_();
         buffer.m_252986_(matrix, (float)pos.m_123341_() + 0.5F + size, (float)pos.m_123342_() + 0.5F, (float)pos.m_123343_() + 0.5F).m_85950_(r, g, b, alpha).m_5752_();
         buffer.m_252986_(matrix, (float)pos.m_123341_() + 0.5F, (float)pos.m_123342_() + 0.5F - size, (float)pos.m_123343_() + 0.5F).m_85950_(r, g, b, alpha).m_5752_();
         buffer.m_252986_(matrix, (float)pos.m_123341_() + 0.5F, (float)pos.m_123342_() + 0.5F + size, (float)pos.m_123343_() + 0.5F).m_85950_(r, g, b, alpha).m_5752_();
         buffer.m_252986_(matrix, (float)pos.m_123341_() + 0.5F, (float)pos.m_123342_() + 0.5F, (float)pos.m_123343_() + 0.5F - size).m_85950_(r, g, b, alpha).m_5752_();
         buffer.m_252986_(matrix, (float)pos.m_123341_() + 0.5F, (float)pos.m_123342_() + 0.5F, (float)pos.m_123343_() + 0.5F + size).m_85950_(r, g, b, alpha).m_5752_();
      }

      BufferUploader.m_231202_(buffer.m_231175_());
   }

   private static void renderFlowParticles(PoseStack poseStack, List path, float time) {
      if (path.size() >= 2) {
         BufferBuilder buffer = Tesselator.m_85913_().m_85915_();
         Matrix4f matrix = poseStack.m_85850_().m_252922_();
         buffer.m_166779_(Mode.DEBUG_LINES, DefaultVertexFormat.f_85815_);
         int numParticles = 5;
         float particleSpeed = 0.2F;

         for(int p = 0; p < numParticles; ++p) {
            float particleOffset = (time + (float)p / (float)numParticles) % 1.0F;

            for(int i = 0; i < path.size() - 1; ++i) {
               BlockPos current = (BlockPos)path.get(i);
               BlockPos next = (BlockPos)path.get(i + 1);
               float segmentProgress = particleOffset * (float)(path.size() - 1) - (float)i;
               if (segmentProgress >= 0.0F && segmentProgress <= 1.0F) {
                  float x = (float)current.m_123341_() + 0.5F + (float)(next.m_123341_() - current.m_123341_()) * segmentProgress;
                  float y = (float)current.m_123342_() + 0.5F + (float)(next.m_123342_() - current.m_123342_()) * segmentProgress;
                  float z = (float)current.m_123343_() + 0.5F + (float)(next.m_123343_() - current.m_123343_()) * segmentProgress;
                  float hue = ((float)i / (float)path.size() + time) % 1.0F;
                  int rgb = Color.HSBtoRGB(hue, 1.0F, 1.0F);
                  float r = (float)(rgb >> 16 & 255) / 255.0F;
                  float g = (float)(rgb >> 8 & 255) / 255.0F;
                  float b = (float)(rgb & 255) / 255.0F;
                  float size = 0.1F;
                  buffer.m_252986_(matrix, x - size, y, z).m_85950_(r, g, b, 1.0F).m_5752_();
                  buffer.m_252986_(matrix, x + size, y, z).m_85950_(r, g, b, 1.0F).m_5752_();
                  buffer.m_252986_(matrix, x, y - size, z).m_85950_(r, g, b, 1.0F).m_5752_();
                  buffer.m_252986_(matrix, x, y + size, z).m_85950_(r, g, b, 1.0F).m_5752_();
                  buffer.m_252986_(matrix, x, y, z - size).m_85950_(r, g, b, 1.0F).m_5752_();
                  buffer.m_252986_(matrix, x, y, z + size).m_85950_(r, g, b, 1.0F).m_5752_();
               }
            }
         }

         BufferUploader.m_231202_(buffer.m_231175_());
      }
   }

   private static void renderStartAndGoal(PoseStack poseStack, List path, float time, boolean isPlayerTarget) {
      BufferBuilder buffer = Tesselator.m_85913_().m_85915_();
      Matrix4f matrix = poseStack.m_85850_().m_252922_();
      float pulse = 0.5F + 0.5F * (float)Math.sin((double)time * Math.PI * 3.0);
      BlockPos start = (BlockPos)path.get(0);
      buffer.m_166779_(Mode.DEBUG_LINES, DefaultVertexFormat.f_85815_);
      drawGlowingBox(buffer, matrix, start, 0.2F, 0.5F, 1.0F, pulse);
      BufferUploader.m_231202_(buffer.m_231175_());
      BlockPos goal = (BlockPos)path.get(path.size() - 1);
      buffer.m_166779_(Mode.DEBUG_LINES, DefaultVertexFormat.f_85815_);
      float cx;
      float cz;
      float radius;
      float a1;
      float a2;
      float x1;
      float x2;
      float z2;
      if (isPlayerTarget) {
         drawGlowingBox(buffer, matrix, goal, 1.0F, 0.3F + pulse * 0.5F, 0.2F, 1.0F);
         cx = (float)goal.m_123341_() + 0.5F;
         float cy = (float)goal.m_123342_() + 1.2F;
         cz = (float)goal.m_123343_() + 0.5F;
         radius = 0.8F + pulse * 0.2F;

         for(int i = 0; i < 16; ++i) {
            a1 = (float)i * 3.1415927F * 2.0F / 16.0F;
            a2 = (float)(i + 1) * 3.1415927F * 2.0F / 16.0F;
            x1 = cx + (float)Math.cos((double)(a1 + time * 2.0F)) * radius;
            float z1 = cz + (float)Math.sin((double)(a1 + time * 2.0F)) * radius;
            x2 = cx + (float)Math.cos((double)(a2 + time * 2.0F)) * radius;
            z2 = cz + (float)Math.sin((double)(a2 + time * 2.0F)) * radius;
            buffer.m_252986_(matrix, x1, cy, z1).m_85950_(1.0F, 0.5F, 0.0F, 0.6F).m_5752_();
            buffer.m_252986_(matrix, x2, cy, z2).m_85950_(1.0F, 0.5F, 0.0F, 0.6F).m_5752_();
         }
      } else {
         cx = time % 1.0F;
         int rgb = Color.HSBtoRGB(cx, 1.0F, 1.0F);
         cz = (float)(rgb >> 16 & 255) / 255.0F;
         radius = (float)(rgb >> 8 & 255) / 255.0F;
         float b = (float)(rgb & 255) / 255.0F;
         drawGlowingBox(buffer, matrix, goal, cz, radius, b, pulse);
         a1 = (float)goal.m_123341_() + 0.5F;
         a2 = (float)goal.m_123342_() + 1.2F;
         x1 = (float)goal.m_123343_() + 0.5F;

         for(int i = 0; i < 3; ++i) {
            x2 = time * 3.1415927F * 2.0F + (float)(i * 2);
            z2 = a1 + (float)Math.cos((double)x2) * 1.0F;
            float z1 = x1 + (float)Math.sin((double)x2) * 1.0F;
            float x2 = a1 + (float)Math.cos((double)(x2 + 0.5F)) * 1.0F;
            float z2 = x1 + (float)Math.sin((double)(x2 + 0.5F)) * 1.0F;
            buffer.m_252986_(matrix, z2, a2, z1).m_85950_(cz, radius, b, 0.4F).m_5752_();
            buffer.m_252986_(matrix, x2, a2, z2).m_85950_(cz, radius, b, 0.4F).m_5752_();
         }
      }

      BufferUploader.m_231202_(buffer.m_231175_());
   }

   private static void drawGlowingBox(BufferBuilder buffer, Matrix4f matrix, BlockPos pos, float r, float g, float b, float pulse) {
      float x1 = (float)pos.m_123341_();
      float y1 = (float)pos.m_123342_();
      float z1 = (float)pos.m_123343_();
      float x2 = x1 + 1.0F;
      float y2 = y1 + 1.0F;
      float z2 = z1 + 1.0F;
      float alpha = 0.6F + pulse * 0.3F;
      buffer.m_252986_(matrix, x1, y1, z1).m_85950_(r, g, b, alpha).m_5752_();
      buffer.m_252986_(matrix, x2, y1, z1).m_85950_(r, g, b, alpha).m_5752_();
      buffer.m_252986_(matrix, x2, y1, z1).m_85950_(r, g, b, alpha).m_5752_();
      buffer.m_252986_(matrix, x2, y1, z2).m_85950_(r, g, b, alpha).m_5752_();
      buffer.m_252986_(matrix, x2, y1, z2).m_85950_(r, g, b, alpha).m_5752_();
      buffer.m_252986_(matrix, x1, y1, z2).m_85950_(r, g, b, alpha).m_5752_();
      buffer.m_252986_(matrix, x1, y1, z2).m_85950_(r, g, b, alpha).m_5752_();
      buffer.m_252986_(matrix, x1, y1, z1).m_85950_(r, g, b, alpha).m_5752_();
      buffer.m_252986_(matrix, x1, y2, z1).m_85950_(r, g, b, alpha).m_5752_();
      buffer.m_252986_(matrix, x2, y2, z1).m_85950_(r, g, b, alpha).m_5752_();
      buffer.m_252986_(matrix, x2, y2, z1).m_85950_(r, g, b, alpha).m_5752_();
      buffer.m_252986_(matrix, x2, y2, z2).m_85950_(r, g, b, alpha).m_5752_();
      buffer.m_252986_(matrix, x2, y2, z2).m_85950_(r, g, b, alpha).m_5752_();
      buffer.m_252986_(matrix, x1, y2, z2).m_85950_(r, g, b, alpha).m_5752_();
      buffer.m_252986_(matrix, x1, y2, z2).m_85950_(r, g, b, alpha).m_5752_();
      buffer.m_252986_(matrix, x1, y2, z1).m_85950_(r, g, b, alpha).m_5752_();
      buffer.m_252986_(matrix, x1, y1, z1).m_85950_(r, g, b, alpha).m_5752_();
      buffer.m_252986_(matrix, x1, y2, z1).m_85950_(r, g, b, alpha).m_5752_();
      buffer.m_252986_(matrix, x2, y1, z1).m_85950_(r, g, b, alpha).m_5752_();
      buffer.m_252986_(matrix, x2, y2, z1).m_85950_(r, g, b, alpha).m_5752_();
      buffer.m_252986_(matrix, x2, y1, z2).m_85950_(r, g, b, alpha).m_5752_();
      buffer.m_252986_(matrix, x2, y2, z2).m_85950_(r, g, b, alpha).m_5752_();
      buffer.m_252986_(matrix, x1, y1, z2).m_85950_(r, g, b, alpha).m_5752_();
      buffer.m_252986_(matrix, x1, y2, z2).m_85950_(r, g, b, alpha).m_5752_();
   }
}
