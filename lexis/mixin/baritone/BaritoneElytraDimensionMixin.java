package lexis.mixin.baritone;

import lexis.Hack.Hacks.Baritone.ElytraAnywhereHack;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Level.class})
public abstract class BaritoneElytraDimensionMixin {
   @Inject(
      method = {"dimension"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onDimension(CallbackInfoReturnable cir) {
      if (ElytraAnywhereHack.enabled && !ElytraAnywhereHack.rendering && this instanceof ClientLevel) {
         cir.setReturnValue(Level.f_46429_);
      }

   }
}
