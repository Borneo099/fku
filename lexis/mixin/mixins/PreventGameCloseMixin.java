package lexis.mixin.mixins;

import lexis.Hack.Hacks.Protect.PreventGameCloseHack;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Minecraft.class})
public class PreventGameCloseMixin {
   @Inject(
      method = {"stop"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onStop(CallbackInfo ci) {
      if (PreventGameCloseHack.isActive()) {
         ci.cancel();
      }

   }
}
