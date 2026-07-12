package lexis.mixin.mixina;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Render.FreeCamHack;
import lexis.Hack.Hackutil.HackManager;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({GameRenderer.class})
public class GameRendererMixin {
   private FreeCamHack getFreeCam() {
      Iterator var1 = HackManager.getInstance().getHacks().iterator();

      Hack hack;
      do {
         if (!var1.hasNext()) {
            return null;
         }

         hack = (Hack)var1.next();
      } while(!(hack instanceof FreeCamHack) || !hack.isEnabled());

      return (FreeCamHack)hack;
   }

   @Inject(
      method = {"pick"},
      at = {@At("HEAD")}
   )
   private void onPickStart(float partialTicks, CallbackInfo ci) {
      FreeCamHack freeCam = this.getFreeCam();
      if (freeCam != null && freeCam.isActive()) {
         freeCam.onBeforeGameRendererPick();
      }

   }

   @Inject(
      method = {"pick"},
      at = {@At("TAIL")}
   )
   private void onPickEnd(float partialTicks, CallbackInfo ci) {
      FreeCamHack freeCam = this.getFreeCam();
      if (freeCam != null && freeCam.isActive()) {
         freeCam.onAfterGameRendererPick();
      }

   }
}
