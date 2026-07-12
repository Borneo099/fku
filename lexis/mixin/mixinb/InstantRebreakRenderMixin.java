package lexis.mixin.mixinb;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Blocks.InstantRebreakHack;
import lexis.Hack.Hackutil.HackManager;
import lexis.Hack.Utils.Colors.SettingColor;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({LevelRenderer.class})
public class InstantRebreakRenderMixin {
   private static final Minecraft mc = Minecraft.m_91087_();

   @Inject(
      method = {"renderLevel"},
      at = {@At("RETURN")}
   )
   private void onRenderLevel(PoseStack poseStack, float partialTick, long finishNano, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projectionMatrix, CallbackInfo ci) {
      Iterator var11 = HackManager.getInstance().getHacks().iterator();

      while(var11.hasNext()) {
         Hack hack = (Hack)var11.next();
         if (hack instanceof InstantRebreakHack ir && ((InstantRebreakHack)hack).shouldRender()) {
            BlockPos pos = ir.getCurrentBlockPos();
            if (pos != null) {
               Vec3 cameraPos = mc.f_91063_.m_109153_().m_90583_();
               RenderSystem.enableBlend();
               RenderSystem.defaultBlendFunc();
               RenderSystem.disableCull();
               RenderSystem.disableDepthTest();
               RenderSystem.setShader(GameRenderer::m_172811_);
               RenderSystem.depthMask(false);
               RenderSystem.lineWidth(3.0F);
               poseStack.m_85836_();
               poseStack.m_85837_((double)pos.m_123341_() - cameraPos.f_82479_, (double)pos.m_123342_() - cameraPos.f_82480_, (double)pos.m_123343_() - cameraPos.f_82481_);
               Tesselator tesselator = Tesselator.m_85913_();
               BufferBuilder buffer = tesselator.m_85915_();
               Matrix4f matrix = poseStack.m_85850_().m_252922_();
               SettingColor sideColor = ir.getSideColor();
               SettingColor lineColor = ir.getLineColor();
               float[] side = new float[]{(float)sideColor.r / 255.0F, (float)sideColor.g / 255.0F, (float)sideColor.b / 255.0F, (float)sideColor.a / 255.0F};
               float[] line = new float[]{(float)lineColor.r / 255.0F, (float)lineColor.g / 255.0F, (float)lineColor.b / 255.0F, (float)lineColor.a / 255.0F};
               buffer.m_166779_(Mode.QUADS, DefaultVertexFormat.f_85815_);
               renderBoxFaces(buffer, matrix, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F, side[0], side[1], side[2], side[3]);
               tesselator.m_85914_();
               buffer.m_166779_(Mode.DEBUG_LINES, DefaultVertexFormat.f_85815_);
               renderBoxWireframe2(buffer, matrix, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F, line[0], line[1], line[2], line[3]);
               tesselator.m_85914_();
               poseStack.m_85849_();
               RenderSystem.depthMask(true);
               RenderSystem.enableDepthTest();
               RenderSystem.enableCull();
               RenderSystem.disableBlend();
               RenderSystem.lineWidth(1.0F);
               break;
            }
         }
      }

   }

   private static void renderBoxFaces(BufferBuilder buffer, Matrix4f matrix, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, float r, float g, float b, float a) {
      buffer.m_252986_(matrix, minX, minY, minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, maxX, minY, minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, maxX, minY, maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, minX, minY, maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, minX, maxY, minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, minX, maxY, maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, maxX, maxY, maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, maxX, maxY, minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, minX, minY, minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, minX, maxY, minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, maxX, maxY, minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, maxX, minY, minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, minX, minY, maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, maxX, minY, maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, maxX, maxY, maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, minX, maxY, maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, minX, minY, minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, minX, minY, maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, minX, maxY, maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, minX, maxY, minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, maxX, minY, minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, maxX, maxY, minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, maxX, maxY, maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, maxX, minY, maxZ).m_85950_(r, g, b, a).m_5752_();
   }

   private static void renderBoxWireframe2(BufferBuilder buffer, Matrix4f matrix, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, float r, float g, float b, float a) {
      buffer.m_252986_(matrix, minX, minY, minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, maxX, minY, minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, maxX, minY, minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, maxX, minY, maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, maxX, minY, maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, minX, minY, maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, minX, minY, maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, minX, minY, minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, minX, maxY, minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, maxX, maxY, minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, maxX, maxY, minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, maxX, maxY, maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, maxX, maxY, maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, minX, maxY, maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, minX, maxY, maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, minX, maxY, minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, minX, minY, minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, minX, maxY, minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, maxX, minY, minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, maxX, maxY, minZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, maxX, minY, maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, maxX, maxY, maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, minX, minY, maxZ).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, minX, maxY, maxZ).m_85950_(r, g, b, a).m_5752_();
   }
}
