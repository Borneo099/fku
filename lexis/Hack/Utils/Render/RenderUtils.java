package lexis.Hack.Utils.Render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.util.Iterator;
import java.util.List;
import lexis.Hack.Utils.Color;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class RenderUtils {
   private static final Minecraft mc = Minecraft.m_91087_();
   private static final Minecraft MC = Minecraft.m_91087_();

   private RenderUtils() {
   }

   public static void drawLine(PoseStack poseStack, double x1, double y1, double z1, double x2, double y2, double z2, Color color) {
      Vec3 camera = mc.f_91063_.m_109153_().m_90583_();
      x1 -= camera.f_82479_;
      y1 -= camera.f_82480_;
      z1 -= camera.f_82481_;
      x2 -= camera.f_82479_;
      y2 -= camera.f_82480_;
      z2 -= camera.f_82481_;
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableCull();
      RenderSystem.disableDepthTest();
      RenderSystem.setShader(GameRenderer::m_172811_);
      RenderSystem.lineWidth(2.0F);
      Matrix4f matrix = poseStack.m_85850_().m_252922_();
      BufferBuilder buffer = Tesselator.m_85913_().m_85915_();
      buffer.m_166779_(Mode.DEBUG_LINES, DefaultVertexFormat.f_85815_);
      buffer.m_252986_(matrix, (float)x1, (float)y1, (float)z1).m_85950_((float)color.r / 255.0F, (float)color.g / 255.0F, (float)color.b / 255.0F, (float)color.a / 255.0F).m_5752_();
      buffer.m_252986_(matrix, (float)x2, (float)y2, (float)z2).m_85950_((float)color.r / 255.0F, (float)color.g / 255.0F, (float)color.b / 255.0F, (float)color.a / 255.0F).m_5752_();
      BufferUploader.m_231202_(buffer.m_231175_());
      RenderSystem.enableDepthTest();
      RenderSystem.enableCull();
      RenderSystem.disableBlend();
      RenderSystem.lineWidth(1.0F);
   }

   public static void drawFilledBox(PoseStack poseStack, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, Color fillColor, Color lineColor, float lineWidth) {
      Vec3 camera = mc.f_91063_.m_109153_().m_90583_();
      minX -= camera.f_82479_;
      maxX -= camera.f_82479_;
      minY -= camera.f_82480_;
      maxY -= camera.f_82480_;
      minZ -= camera.f_82481_;
      maxZ -= camera.f_82481_;
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableCull();
      RenderSystem.disableDepthTest();
      RenderSystem.setShader(GameRenderer::m_172811_);
      RenderSystem.lineWidth(lineWidth);
      Matrix4f matrix = poseStack.m_85850_().m_252922_();
      Tesselator tesselator = Tesselator.m_85913_();
      BufferBuilder buffer = tesselator.m_85915_();
      buffer.m_166779_(Mode.QUADS, DefaultVertexFormat.f_85815_);
      renderBoxFaces(buffer, matrix, minX, minY, minZ, maxX, maxY, maxZ, (float)fillColor.r / 255.0F, (float)fillColor.g / 255.0F, (float)fillColor.b / 255.0F, (float)fillColor.a / 255.0F);
      BufferUploader.m_231202_(buffer.m_231175_());
      buffer.m_166779_(Mode.DEBUG_LINES, DefaultVertexFormat.f_85815_);
      renderBoxWireframe(buffer, matrix, minX, minY, minZ, maxX, maxY, maxZ, (float)lineColor.r / 255.0F, (float)lineColor.g / 255.0F, (float)lineColor.b / 255.0F, (float)lineColor.a / 255.0F);
      BufferUploader.m_231202_(buffer.m_231175_());
      RenderSystem.enableDepthTest();
      RenderSystem.enableCull();
      RenderSystem.disableBlend();
      RenderSystem.lineWidth(1.0F);
   }

   private static void renderBoxFaces(BufferBuilder buffer, Matrix4f matrix, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, float r, float g, float b, float a) {
      buffer.m_252986_(matrix, (float)minX, (float)minY, (float)minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)maxX, (float)minY, (float)minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)maxX, (float)minY, (float)maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)minX, (float)minY, (float)maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)minX, (float)maxY, (float)minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)minX, (float)maxY, (float)maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)maxX, (float)maxY, (float)maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)maxX, (float)maxY, (float)minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)minX, (float)minY, (float)minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)minX, (float)maxY, (float)minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)maxX, (float)maxY, (float)minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)maxX, (float)minY, (float)minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)minX, (float)minY, (float)maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)maxX, (float)minY, (float)maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)maxX, (float)maxY, (float)maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)minX, (float)maxY, (float)maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)minX, (float)minY, (float)minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)minX, (float)minY, (float)maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)minX, (float)maxY, (float)maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)minX, (float)maxY, (float)minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)maxX, (float)minY, (float)minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)maxX, (float)maxY, (float)minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)maxX, (float)maxY, (float)maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)maxX, (float)minY, (float)maxZ).m_85950_(r, g, b, a).m_5752_();
   }

   private static void renderBoxWireframe(BufferBuilder buffer, Matrix4f matrix, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, float r, float g, float b, float a) {
      buffer.m_252986_(matrix, (float)minX, (float)minY, (float)minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)maxX, (float)minY, (float)minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)maxX, (float)minY, (float)minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)maxX, (float)minY, (float)maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)maxX, (float)minY, (float)maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)minX, (float)minY, (float)maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)minX, (float)minY, (float)maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)minX, (float)minY, (float)minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)minX, (float)maxY, (float)minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)maxX, (float)maxY, (float)minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)maxX, (float)maxY, (float)minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)maxX, (float)maxY, (float)maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)maxX, (float)maxY, (float)maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)minX, (float)maxY, (float)maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)minX, (float)maxY, (float)maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)minX, (float)maxY, (float)minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)minX, (float)minY, (float)minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)minX, (float)maxY, (float)minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)maxX, (float)minY, (float)minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)maxX, (float)maxY, (float)minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)maxX, (float)minY, (float)maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)maxX, (float)maxY, (float)maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)minX, (float)minY, (float)maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)minX, (float)maxY, (float)maxZ).m_85950_(r, g, b, a).m_5752_();
   }

   public static Vec3 getCameraPos() {
      Camera camera = MC.f_91063_.m_109153_();
      return camera == null ? Vec3.f_82478_ : camera.m_90583_();
   }

   public static void drawSolidBoxes(PoseStack poseStack, List boxes, int color, boolean depthTest) {
      if (!boxes.isEmpty()) {
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         if (!depthTest) {
            RenderSystem.disableDepthTest();
         }

         RenderSystem.setShader(GameRenderer::m_172811_);
         Tesselator tesselator = Tesselator.m_85913_();
         BufferBuilder buffer = tesselator.m_85915_();
         Matrix4f matrix = poseStack.m_85850_().m_252922_();
         Vec3 cam = getCameraPos();
         buffer.m_166779_(Mode.QUADS, DefaultVertexFormat.f_85815_);
         Iterator var8 = boxes.iterator();

         while(var8.hasNext()) {
            AABB box = (AABB)var8.next();
            AABB translated = box.m_82386_(-cam.f_82479_, -cam.f_82480_, -cam.f_82481_);
            drawSolidBox(buffer, matrix, translated, color);
         }

         tesselator.m_85914_();
         if (!depthTest) {
            RenderSystem.enableDepthTest();
         }

         RenderSystem.disableBlend();
      }
   }

   private static void drawSolidBox(BufferBuilder buffer, Matrix4f matrix, AABB box, int color) {
      float x1 = (float)box.f_82288_;
      float y1 = (float)box.f_82289_;
      float z1 = (float)box.f_82290_;
      float x2 = (float)box.f_82291_;
      float y2 = (float)box.f_82292_;
      float z2 = (float)box.f_82293_;
      float r = (float)(color >> 16 & 255) / 255.0F;
      float g = (float)(color >> 8 & 255) / 255.0F;
      float b = (float)(color & 255) / 255.0F;
      float a = (float)(color >> 24 & 255) / 255.0F;
      buffer.m_252986_(matrix, x1, y1, z1).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, x2, y1, z1).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, x2, y1, z2).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, x1, y1, z2).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, x1, y2, z1).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, x1, y2, z2).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, x2, y2, z2).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, x2, y2, z1).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, x1, y1, z1).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, x1, y2, z1).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, x2, y2, z1).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, x2, y1, z1).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, x1, y1, z2).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, x2, y1, z2).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, x2, y2, z2).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, x1, y2, z2).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, x1, y1, z1).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, x1, y1, z2).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, x1, y2, z2).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, x1, y2, z1).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, x2, y1, z1).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, x2, y2, z1).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, x2, y2, z2).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, x2, y1, z2).m_85950_(r, g, b, a).m_5752_();
   }

   public static void drawOutlinedBoxes(PoseStack poseStack, List boxes, int color, boolean depthTest) {
      if (!boxes.isEmpty()) {
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         if (!depthTest) {
            RenderSystem.disableDepthTest();
         }

         RenderSystem.setShader(GameRenderer::m_172811_);
         RenderSystem.lineWidth(2.0F);
         Tesselator tesselator = Tesselator.m_85913_();
         BufferBuilder buffer = tesselator.m_85915_();
         Matrix4f matrix = poseStack.m_85850_().m_252922_();
         Vec3 cam = getCameraPos();
         buffer.m_166779_(Mode.DEBUG_LINES, DefaultVertexFormat.f_85815_);
         Iterator var8 = boxes.iterator();

         while(var8.hasNext()) {
            AABB box = (AABB)var8.next();
            AABB translated = box.m_82386_(-cam.f_82479_, -cam.f_82480_, -cam.f_82481_);
            drawOutlinedBox(buffer, matrix, translated, color);
         }

         tesselator.m_85914_();
         if (!depthTest) {
            RenderSystem.enableDepthTest();
         }

         RenderSystem.disableBlend();
         RenderSystem.lineWidth(1.0F);
      }
   }

   private static void drawOutlinedBox(BufferBuilder buffer, Matrix4f matrix, AABB box, int color) {
      float x1 = (float)box.f_82288_;
      float y1 = (float)box.f_82289_;
      float z1 = (float)box.f_82290_;
      float x2 = (float)box.f_82291_;
      float y2 = (float)box.f_82292_;
      float z2 = (float)box.f_82293_;
      float r = (float)(color >> 16 & 255) / 255.0F;
      float g = (float)(color >> 8 & 255) / 255.0F;
      float b = (float)(color & 255) / 255.0F;
      float a = (float)(color >> 24 & 255) / 255.0F;
      addLine(buffer, matrix, x1, y1, z1, x2, y1, z1, r, g, b, a);
      addLine(buffer, matrix, x2, y1, z1, x2, y1, z2, r, g, b, a);
      addLine(buffer, matrix, x2, y1, z2, x1, y1, z2, r, g, b, a);
      addLine(buffer, matrix, x1, y1, z2, x1, y1, z1, r, g, b, a);
      addLine(buffer, matrix, x1, y2, z1, x2, y2, z1, r, g, b, a);
      addLine(buffer, matrix, x2, y2, z1, x2, y2, z2, r, g, b, a);
      addLine(buffer, matrix, x2, y2, z2, x1, y2, z2, r, g, b, a);
      addLine(buffer, matrix, x1, y2, z2, x1, y2, z1, r, g, b, a);
      addLine(buffer, matrix, x1, y1, z1, x1, y2, z1, r, g, b, a);
      addLine(buffer, matrix, x2, y1, z1, x2, y2, z1, r, g, b, a);
      addLine(buffer, matrix, x2, y1, z2, x2, y2, z2, r, g, b, a);
      addLine(buffer, matrix, x1, y1, z2, x1, y2, z2, r, g, b, a);
   }

   private static void addLine(BufferBuilder buffer, Matrix4f matrix, float x1, float y1, float z1, float x2, float y2, float z2, float r, float g, float b, float a) {
      buffer.m_252986_(matrix, x1, y1, z1).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, x2, y2, z2).m_85950_(r, g, b, a).m_5752_();
   }

   public static void drawTracers(PoseStack poseStack, float partialTicks, List ends, int color, boolean depthTest) {
      if (!ends.isEmpty()) {
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         if (!depthTest) {
            RenderSystem.disableDepthTest();
         }

         RenderSystem.setShader(GameRenderer::m_172811_);
         RenderSystem.lineWidth(2.0F);
         Tesselator tesselator = Tesselator.m_85913_();
         BufferBuilder buffer = tesselator.m_85915_();
         Matrix4f matrix = poseStack.m_85850_().m_252922_();
         Vec3 start = getTracerOrigin(partialTicks);
         Vec3 cam = getCameraPos();
         float r = (float)(color >> 16 & 255) / 255.0F;
         float g = (float)(color >> 8 & 255) / 255.0F;
         float b = (float)(color & 255) / 255.0F;
         float a = (float)(color >> 24 & 255) / 255.0F;
         buffer.m_166779_(Mode.DEBUG_LINES, DefaultVertexFormat.f_85815_);
         Iterator var14 = ends.iterator();

         while(var14.hasNext()) {
            Vec3 end = (Vec3)var14.next();
            Vec3 e = end.m_82546_(cam);
            buffer.m_252986_(matrix, (float)start.f_82479_, (float)start.f_82480_, (float)start.f_82481_).m_85950_(r, g, b, a).m_5752_();
            buffer.m_252986_(matrix, (float)e.f_82479_, (float)e.f_82480_, (float)e.f_82481_).m_85950_(r, g, b, a).m_5752_();
         }

         tesselator.m_85914_();
         if (!depthTest) {
            RenderSystem.enableDepthTest();
         }

         RenderSystem.disableBlend();
         RenderSystem.lineWidth(1.0F);
      }
   }

   private static Vec3 getTracerOrigin(float partialTicks) {
      Vec3 look = MC.f_91074_.m_20154_().m_82490_(10.0);
      if (MC.f_91066_.m_92176_() == CameraType.THIRD_PERSON_FRONT) {
         look = look.m_82548_();
      }

      return look;
   }

   public static void drawLines(PoseStack poseStack, List points, int color, boolean depthTest) {
      if (points != null && points.size() >= 2) {
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         if (!depthTest) {
            RenderSystem.disableDepthTest();
         }

         RenderSystem.setShader(GameRenderer::m_172811_);
         RenderSystem.lineWidth(2.0F);
         Tesselator tesselator = Tesselator.m_85913_();
         BufferBuilder buffer = tesselator.m_85915_();
         Matrix4f matrix = poseStack.m_85850_().m_252922_();
         Vec3 cam = getCameraPos();
         buffer.m_166779_(Mode.DEBUG_LINES, DefaultVertexFormat.f_85815_);

         for(int i = 0; i < points.size() - 1; ++i) {
            Vec3 a = ((Vec3)points.get(i)).m_82546_(cam);
            Vec3 b = ((Vec3)points.get(i + 1)).m_82546_(cam);
            float x1 = (float)a.f_82479_;
            float y1 = (float)a.f_82480_;
            float z1 = (float)a.f_82481_;
            float x2 = (float)b.f_82479_;
            float y2 = (float)b.f_82480_;
            float z2 = (float)b.f_82481_;
            int r = color >> 16 & 255;
            int g = color >> 8 & 255;
            int bCol = color & 255;
            int aCol = color >> 24 & 255;
            buffer.m_252986_(matrix, x1, y1, z1).m_6122_(r, g, bCol, aCol).m_5752_();
            buffer.m_252986_(matrix, x2, y2, z2).m_6122_(r, g, bCol, aCol).m_5752_();
         }

         tesselator.m_85914_();
         if (!depthTest) {
            RenderSystem.enableDepthTest();
         }

         RenderSystem.disableBlend();
         RenderSystem.lineWidth(1.0F);
      }
   }
}
