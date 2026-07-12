package lexis.mixin.tacz;

import lexis.Hack.Hacks.TaCZ.NoSprintInterruptHack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
   targets = {"com.tacz.guns.client.gameplay.LocalPlayerSprint"},
   remap = false
)
public class TaczNoSprintMixin {
   @Inject(
      method = {"getProcessedSprintStatus"},
      at = {@At("HEAD")},
      cancellable = true,
      remap = false
   )
   private void onGetProcessedSprintStatus(boolean sprint, CallbackInfoReturnable cir) {
      if (NoSprintInterruptHack.noSprintInterruptActive) {
         cir.setReturnValue(sprint);
      }

   }
}
