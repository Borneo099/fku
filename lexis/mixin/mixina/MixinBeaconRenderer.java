package lexis.mixin.mixina;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Render.NoRenderHack;
import lexis.Hack.Hackutil.HackManager;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({BeaconRenderer.class})
abstract class MixinBeaconRenderer {
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
      method = {"renderBeaconBeam*"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void onRenderBeaconBeam(PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, long gameTime, int yOffset, int height, float[] colors, CallbackInfo ci) {
      Iterator var9 = HackManager.getInstance().getHacks().iterator();

      while(var9.hasNext()) {
         Hack hack = (Hack)var9.next();
         if (hack instanceof NoRenderHack noRender && hack.isEnabled()) {
            if (noRender.noBeaconBeams()) {
               ci.cancel();
            }
         }
      }

   }
}
