package lexis.mixin.mixina;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Render.PortalEspHack;
import lexis.Hack.Hackutil.HackManager;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({LevelRenderer.class})
public class PortalEspRenderMixin {
   @Inject(
      method = {"renderLevel"},
      at = {@At("RETURN")}
   )
   private void onRenderLevel(PoseStack poseStack, float partialTick, long finishNano, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projectionMatrix, CallbackInfo ci) {
      Iterator var11 = HackManager.getInstance().getHacks().iterator();

      while(var11.hasNext()) {
         Hack hack = (Hack)var11.next();
         if (hack instanceof PortalEspHack && hack.isEnabled()) {
            ((PortalEspHack)hack).onRender(poseStack, partialTick);
            break;
         }
      }

   }
}
