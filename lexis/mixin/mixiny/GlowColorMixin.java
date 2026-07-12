package lexis.mixin.mixiny;

import lexis.Hack.Utils.FakeGlowManager;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Entity.class})
public class GlowColorMixin {
   @Inject(
      method = {"getTeamColor"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onGetTeamColor(CallbackInfoReturnable cir) {
      Entity entity = (Entity)this;
      int color = FakeGlowManager.getGlowColor(entity);
      if (color != -1) {
         cir.setReturnValue(color);
         cir.cancel();
      }

   }
}
