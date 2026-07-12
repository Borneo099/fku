package lexis.mixin.mixina;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Render.FreeCamHack;
import lexis.Hack.Hackutil.HackManager;
import net.minecraft.client.CameraType;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({GameRenderer.class})
public class FreecamHandMixin {
   @Redirect(
      method = {"renderItemInHand"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/CameraType;isFirstPerson()Z"
)
   )
   private boolean forceHandRender(CameraType cameraType) {
      Iterator var2 = HackManager.getInstance().getHacks().iterator();

      while(var2.hasNext()) {
         Hack hack = (Hack)var2.next();
         if (hack instanceof FreeCamHack freeCam) {
            if (freeCam.isActive() && freeCam.shouldRenderHands()) {
               return true;
            }
         }
      }

      return cameraType.m_90612_();
   }
}
