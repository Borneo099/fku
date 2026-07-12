package lexis.mixin.mixinb;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Render.NoHurtcamHack;
import lexis.Hack.Hackutil.HackManager;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({GameRenderer.class})
public class GameRendererMixin {
   @Inject(
      method = {"bobHurt"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onBobHurt(PoseStack poseStack, float partialTick, CallbackInfo ci) {
      Iterator var4 = HackManager.getInstance().getHacks().iterator();

      Hack hack;
      do {
         if (!var4.hasNext()) {
            return;
         }

         hack = (Hack)var4.next();
      } while(!(hack instanceof NoHurtcamHack) || !hack.isEnabled());

      ci.cancel();
   }
}
