package lexis.mixin.tacz;

import lexis.Hack.Hacks.TaCZ_Server.MaxRpmHack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
   targets = {"com.tacz.guns.client.gameplay.LocalPlayerShoot"},
   remap = false
)
public class TaczClientShootMixin {
   @Inject(
      method = {"getCoolDown"},
      at = {@At("HEAD")},
      cancellable = true,
      remap = false
   )
   private void lexis$forceClientNoCooldown(CallbackInfoReturnable cir) {
      if (MaxRpmHack.maxRpmActive) {
         cir.setReturnValue(0L);
      }

   }
}
