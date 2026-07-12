package lexis.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.util.Iterator;
import java.util.List;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Blocks.AutoFixGroundHack;
import lexis.Hack.Hackutil.HackManager;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({LevelRenderer.class})
public class AutoFixGroundRenderMixin {
   private static final Minecraft mc = Minecraft.m_91087_();

   @Inject(
      method = {"renderLevel"},
      at = {@At("RETURN")}
   )
   private void onRenderLevel(PoseStack poseStack, float partialTick, long finishNano, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projectionMatrix, CallbackInfo ci) {
      AutoFixGroundHack hack = this.getAutoFixGroundHack();
      if (hack != null && hack.isEnabled()) {
         List blocks = hack.getBlocksToFix();
         int currentIndex = hack.getCurrentBlockIndex();
         if (!blocks.isEmpty()) {
            Vec3 cameraPos = mc.f_91063_.m_109153_().m_90583_();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableCull();
            RenderSystem.disableDepthTest();
            RenderSystem.setShader(GameRenderer::m_172811_);
            RenderSystem.depthMask(false);
            RenderSystem.lineWidth(3.0F);
            poseStack.m_85836_();
            poseStack.m_85837_(-cameraPos.f_82479_, -cameraPos.f_82480_, -cameraPos.f_82481_);
            Tesselator tesselator = Tesselator.m_85913_();
            BufferBuilder buffer = tesselator.m_85915_();
            Matrix4f matrix = poseStack.m_85850_().m_252922_();
            buffer.m_166779_(Mode.QUADS, DefaultVertexFormat.f_85815_);

            int i;
            BlockPos pos;
            float r;
            float g;
            float b;
            float alpha;
            for(i = 0; i < blocks.size(); ++i) {
               pos = (BlockPos)blocks.get(i);
               r = i == currentIndex ? 1.0F : 0.0F;
               g = i == currentIndex ? 0.0F : 1.0F;
               b = 0.0F;
               alpha = 0.15F;
               this.renderSides(buffer, matrix, new AABB(pos), r, g, b, alpha);
            }

            tesselator.m_85914_();
            buffer.m_166779_(Mode.DEBUG_LINES, DefaultVertexFormat.f_85815_);

            for(i = 0; i < blocks.size(); ++i) {
               pos = (BlockPos)blocks.get(i);
               r = i == currentIndex ? 1.0F : 0.0F;
               g = i == currentIndex ? 0.0F : 1.0F;
               b = 0.0F;
               alpha = 0.15F;
               this.renderWireframeBox(buffer, matrix, new AABB(pos), r, g, b, alpha);
            }

            tesselator.m_85914_();
            poseStack.m_85849_();
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            RenderSystem.lineWidth(1.0F);
         }
      }
   }

   private void renderSides(BufferBuilder buffer, Matrix4f matrix, AABB box, float r, float g, float b, float a) {
      float minX = (float)box.f_82288_;
      float minY = (float)box.f_82289_;
      float minZ = (float)box.f_82290_;
      float maxX = (float)box.f_82291_;
      float maxY = (float)box.f_82292_;
      float maxZ = (float)box.f_82293_;
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

   private void renderWireframeBox(BufferBuilder buffer, Matrix4f matrix, AABB box, float r, float g, float b, float a) {
      float minX = (float)box.f_82288_;
      float minY = (float)box.f_82289_;
      float minZ = (float)box.f_82290_;
      float maxX = (float)box.f_82291_;
      float maxY = (float)box.f_82292_;
      float maxZ = (float)box.f_82293_;
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

   private AutoFixGroundHack getAutoFixGroundHack() {
      Iterator var1 = HackManager.getInstance().getHacks().iterator();

      Hack hack;
      do {
         if (!var1.hasNext()) {
            return null;
         }

         hack = (Hack)var1.next();
      } while(!(hack instanceof AutoFixGroundHack));

      return (AutoFixGroundHack)hack;
   }
}
