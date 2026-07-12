package lexis.mixin.mixina;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Render.WallHack;
import lexis.Hack.Hackutil.HackManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({LivingEntityRenderer.class})
public class WallHackMixin {
   private static final Minecraft mc = Minecraft.m_91087_();

   @Inject(
      method = {"render*"},
      at = {@At("HEAD")}
   )
   private void onRenderHead(LivingEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
      Iterator var9 = HackManager.getInstance().getHacks().iterator();

      while(var9.hasNext()) {
         Hack hack = (Hack)var9.next();
         if (hack instanceof WallHack wallHack && hack.isEnabled()) {
            break;
         }
      }

      if (wallHack != null) {
         if (wallHack.shouldPerspective(entity)) {
            RenderSystem.depthMask(false);
            GL11.glDepthFunc(519);
         }

      }
   }

   @Inject(
      method = {"render*"},
      at = {@At("RETURN")}
   )
   private void onRenderReturn(LivingEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
      Iterator var9 = HackManager.getInstance().getHacks().iterator();

      while(var9.hasNext()) {
         Hack hack = (Hack)var9.next();
         if (hack instanceof WallHack wallHack && hack.isEnabled()) {
            break;
         }
      }

      if (wallHack != null) {
         if (wallHack.shouldPerspective(entity)) {
            GL11.glDepthFunc(515);
            RenderSystem.depthMask(true);
         }

      }
   }
}
