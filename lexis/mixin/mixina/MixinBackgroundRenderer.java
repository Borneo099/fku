package lexis.mixin.mixina;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Render.NoRenderHack;
import lexis.Hack.Hackutil.HackManager;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({FogRenderer.class})
abstract class MixinBackgroundRenderer {
   private NoRenderHack getNoRender() {
      Iterator var1 = HackManager.getInstance().getHacks().iterator();

      Hack hack;
      do {
         if (!var1.hasNext()) {
            return null;
         }

         hack = (Hack)var1.next();
      } while(!(hack instanceof NoRenderHack) || !hack.isEnabled());

      return (NoRenderHack)hack;
   }

   @Inject(
      method = {"setupFog"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void onSetupFog(Camera camera, FogRenderer.FogMode fogMode, float viewDistance, boolean thickFog, float partialTick, CallbackInfo ci) {
      Iterator var6 = HackManager.getInstance().getHacks().iterator();

      while(var6.hasNext()) {
         Hack hack = (Hack)var6.next();
         if (hack instanceof NoRenderHack noRender && hack.isEnabled()) {
            if (noRender.noFog()) {
               ci.cancel();
            }
         }
      }

   }
}
