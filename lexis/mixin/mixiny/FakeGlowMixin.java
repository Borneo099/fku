package lexis.mixin.mixiny;

import lexis.Hack.Utils.FakeGlowManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({LivingEntity.class})
public class FakeGlowMixin {
   @Inject(
      method = {"isCurrentlyGlowing"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onIsCurrentlyGlowing(CallbackInfoReturnable cir) {
      Entity entity = (Entity)this;
      if (FakeGlowManager.getGlowColor(entity) != -1) {
         cir.setReturnValue(true);
      }

   }
}
