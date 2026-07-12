package lexis.mixin.mixins;

import com.mojang.blaze3d.platform.Window;
import lexis.Hack.Hacks.Protect.PreventGameCloseHack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Window.class})
public class WindowShouldCloseMixin {
   @Inject(
      method = {"shouldClose"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onShouldClose(CallbackInfoReturnable cir) {
      if (PreventGameCloseHack.isActive()) {
         cir.setReturnValue(false);
      }

   }
}
