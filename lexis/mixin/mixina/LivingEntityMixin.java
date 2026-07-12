package lexis.mixin.mixina;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Movement.NoLevitationHack;
import lexis.Hack.Hackutil.HackManager;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({LivingEntity.class})
public class LivingEntityMixin {
   @Inject(
      method = {"hasEffect"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onHasEffect(MobEffect effect, CallbackInfoReturnable cir) {
      if (effect == MobEffects.f_19620_) {
         Iterator var3 = HackManager.getInstance().getHacks().iterator();

         while(var3.hasNext()) {
            Hack hack = (Hack)var3.next();
            if (hack instanceof NoLevitationHack && hack.isEnabled()) {
               cir.setReturnValue(false);
               return;
            }
         }
      }

   }
}
