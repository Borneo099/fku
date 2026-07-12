package lexis.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.World.NukerHack;
import lexis.Hack.Hackutil.HackManager;
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
public class NukerRenderMixin {
   private static final Minecraft mc = Minecraft.m_91087_();

   @Inject(
      method = {"renderLevel"},
      at = {@At("RETURN")}
   )
   private void onRenderLevel(PoseStack poseStack, float partialTick, long finishNano, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projectionMatrix, CallbackInfo ci) {
      Iterator var11 = HackManager.getInstance().getHacks().iterator();

      while(var11.hasNext()) {
         Hack hack = (Hack)var11.next();
         if (hack instanceof NukerHack && hack.isEnabled()) {
            BlockPos target = NukerHack.getCurrentTarget();
            if (target != null) {
               this.renderBlockHighlight(poseStack, target);
            }
         }
      }

   }

   private void renderBlockHighlight(PoseStack poseStack, BlockPos pos) {
      Vec3 cameraPos = mc.f_91063_.m_109153_().m_90583_();
      RenderSystem.disableDepthTest();
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.setShader(GameRenderer::m_172811_);
      RenderSystem.lineWidth(3.0F);
      poseStack.m_85836_();
      poseStack.m_85837_((double)pos.m_123341_() - cameraPos.f_82479_, (double)pos.m_123342_() - cameraPos.f_82480_, (double)pos.m_123343_() - cameraPos.f_82481_);
      Tesselator tesselator = Tesselator.m_85913_();
      BufferBuilder buffer = tesselator.m_85915_();
      Matrix4f matrix = poseStack.m_85850_().m_252922_();
      buffer.m_166779_(Mode.TRIANGLES, DefaultVertexFormat.f_85815_);
      float r = 1.0F;
      float g = 0.0F;
      float b = 0.0F;
      float a = 0.35F;
      buffer.m_252986_(matrix, 0.0F, 0.0F, 0.0F).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, 1.0F, 0.0F, 1.0F).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, 0.0F, 0.0F, 1.0F).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, 0.0F, 0.0F, 0.0F).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, 1.0F, 0.0F, 0.0F).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, 1.0F, 0.0F, 1.0F).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, 0.0F, 1.0F, 0.0F).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, 0.0F, 1.0F, 1.0F).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, 1.0F, 1.0F, 1.0F).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, 0.0F, 1.0F, 0.0F).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, 1.0F, 1.0F, 1.0F).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, 1.0F, 1.0F, 0.0F).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, 0.0F, 0.0F, 0.0F).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, 0.0F, 1.0F, 0.0F).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, 1.0F, 1.0F, 0.0F).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, 0.0F, 0.0F, 0.0F).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, 1.0F, 1.0F, 0.0F).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, 1.0F, 0.0F, 0.0F).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, 0.0F, 0.0F, 1.0F).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, 1.0F, 1.0F, 1.0F).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, 0.0F, 1.0F, 1.0F).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, 0.0F, 0.0F, 1.0F).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, 1.0F, 0.0F, 1.0F).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, 1.0F, 1.0F, 1.0F).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, 0.0F, 0.0F, 0.0F).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, 0.0F, 1.0F, 1.0F).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, 0.0F, 1.0F, 0.0F).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, 0.0F, 0.0F, 0.0F).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, 0.0F, 0.0F, 1.0F).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, 0.0F, 1.0F, 1.0F).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, 1.0F, 0.0F, 0.0F).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, 1.0F, 1.0F, 0.0F).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, 1.0F, 1.0F, 1.0F).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, 1.0F, 0.0F, 0.0F).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, 1.0F, 1.0F, 1.0F).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, 1.0F, 0.0F, 1.0F).m_85950_(r, g, b, a).m_5752_();
      tesselator.m_85914_();
      RenderSystem.lineWidth(3.0F);
      buffer.m_166779_(Mode.DEBUG_LINES, DefaultVertexFormat.f_85815_);
      a = 1.0F;
      buffer.m_252986_(matrix, 0.0F, 0.0F, 0.0F).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, 1.0F, 0.0F, 0.0F).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, 1.0F, 0.0F, 0.0F).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, 1.0F, 0.0F, 1.0F).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, 1.0F, 0.0F, 1.0F).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, 0.0F, 0.0F, 1.0F).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, 0.0F, 0.0F, 1.0F).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, 0.0F, 0.0F, 0.0F).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, 0.0F, 1.0F, 0.0F).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, 1.0F, 1.0F, 0.0F).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, 1.0F, 1.0F, 0.0F).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, 1.0F, 1.0F, 1.0F).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, 1.0F, 1.0F, 1.0F).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, 0.0F, 1.0F, 1.0F).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, 0.0F, 1.0F, 1.0F).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, 0.0F, 1.0F, 0.0F).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, 0.0F, 0.0F, 0.0F).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, 0.0F, 1.0F, 0.0F).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, 1.0F, 0.0F, 0.0F).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, 1.0F, 1.0F, 0.0F).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, 1.0F, 0.0F, 1.0F).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, 1.0F, 1.0F, 1.0F).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, 0.0F, 0.0F, 1.0F).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, 0.0F, 1.0F, 1.0F).m_85950_(r, g, b, a).m_5752_();
      tesselator.m_85914_();
      poseStack.m_85849_();
      RenderSystem.enableDepthTest();
      RenderSystem.disableBlend();
      RenderSystem.lineWidth(1.0F);
   }
}
