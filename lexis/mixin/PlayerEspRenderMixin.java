package lexis.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Render.PlayerEspHack;
import lexis.Hack.Hackutil.HackManager;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({LevelRenderer.class})
public class PlayerEspRenderMixin {
   private static final Minecraft mc = Minecraft.m_91087_();

   @Inject(
      method = {"renderLevel"},
      at = {@At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/renderer/LevelRenderer;renderDebug(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/Camera;)V",
   shift = Shift.AFTER
)}
   )
   private void onRenderLevel(PoseStack poseStack, float partialTick, long finishNano, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projectionMatrix, CallbackInfo ci) {
      Iterator var11 = HackManager.getInstance().getHacks().iterator();

      while(var11.hasNext()) {
         Hack hack = (Hack)var11.next();
         if (hack instanceof PlayerEspHack && hack.isEnabled()) {
            ((PlayerEspHack)hack).onRender(poseStack, partialTick);
            break;
         }
      }

   }
}
