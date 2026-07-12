package lexis.mixin.baritone;

import lexis.Hack.Hacks.Baritone.ElytraAnywhereHack;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({GameRenderer.class})
public class BaritoneElytraRenderMixin {
   @Inject(
      method = {"render"},
      at = {@At("HEAD")}
   )
   private void onRenderStart(float partialTicks, long nanoTime, boolean renderLevel, CallbackInfo ci) {
      if (ElytraAnywhereHack.enabled) {
         ElytraAnywhereHack.rendering = true;
      }

   }

   @Inject(
      method = {"render"},
      at = {@At("RETURN")}
   )
   private void onRenderEnd(float partialTicks, long nanoTime, boolean renderLevel, CallbackInfo ci) {
      ElytraAnywhereHack.rendering = false;
   }
}
