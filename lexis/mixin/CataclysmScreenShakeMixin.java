package lexis.mixin;

import lexis.Hack.Hacks.L_Enders_Cataclysm_C.NoScreenShakeHack;
import net.minecraftforge.client.event.ViewportEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
   targets = {"com.github.L_Ender.cataclysm.client.event.ClientEvent"},
   remap = false
)
public class CataclysmScreenShakeMixin {
   @Inject(
      method = {"onCameraSetup"},
      at = {@At("HEAD")},
      cancellable = true,
      remap = false
   )
   private void onCameraSetup(ViewportEvent.ComputeCameraAngles event, CallbackInfo ci) {
      if (NoScreenShakeHack.isActive()) {
         ci.cancel();
      }

   }
}
