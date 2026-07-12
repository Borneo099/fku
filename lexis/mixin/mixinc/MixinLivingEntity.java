package lexis.mixin.mixinc;

import lexis.Hack.Hacks.Render.FullBrightHack;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({LivingEntity.class})
public class MixinLivingEntity {
   @Inject(
      method = {"hasEffect"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onHasEffect(MobEffect effect, CallbackInfoReturnable cir) {
      if (FullBrightHack.shouldReturnNightVisionEffect && effect == MobEffects.f_19611_) {
         cir.setReturnValue(true);
      }

   }

   @Inject(
      method = {"getEffect"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onGetEffect(MobEffect effect, CallbackInfoReturnable cir) {
      if (FullBrightHack.shouldReturnNightVisionEffect && effect == MobEffects.f_19611_) {
         cir.setReturnValue(new MobEffectInstance(MobEffects.f_19611_, 1000));
      }

   }
}
