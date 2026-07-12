package lexis.mixin.mixina;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Render.NoFireOverlayHack;
import lexis.Hack.Hackutil.HackManager;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({ScreenEffectRenderer.class})
public class InGameOverlayRendererMixin {
   @Redirect(
      method = {"renderFire"},
      at = @At(
   value = "INVOKE",
   target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V",
   ordinal = 0
)
   )
   private static void redirectFireTranslate(PoseStack poseStack, float x, float y, float z) {
      float newYOffset = -0.3F;
      Iterator var5 = HackManager.getInstance().getHacks().iterator();

      while(var5.hasNext()) {
         Hack hack = (Hack)var5.next();
         if (hack instanceof NoFireOverlayHack noFire && hack.isEnabled()) {
            newYOffset = -0.3F - noFire.getOverlayOffset();
            break;
         }
      }

      poseStack.m_252880_(x, newYOffset, z);
   }
}
