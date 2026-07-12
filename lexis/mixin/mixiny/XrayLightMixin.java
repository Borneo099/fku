package lexis.mixin.mixiny;

import lexis.Hack.Hacks.Render.XrayHack;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({LevelRenderer.class})
public class XrayLightMixin {
   @Inject(
      method = {"getLightColor(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)I"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void onGetLightColor(CallbackInfoReturnable cir) {
      if (XrayHack.enabled) {
         cir.setReturnValue(15728880);
      }

   }
}
