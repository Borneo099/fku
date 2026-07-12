package lexis.mixin.tacz;

import lexis.Hack.Hacks.TaCZ_Server.InfiniteAmmoHack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
   targets = {"com.tacz.guns.entity.shooter.LivingEntityAmmoCheck"},
   remap = false
)
public class TaczInfiniteAmmoMixin {
   @Shadow
   @Final
   private LivingEntity shooter;

   @Inject(
      method = {"consumesAmmoOrNot"},
      at = {@At("HEAD")},
      cancellable = true,
      remap = false
   )
   private void lexis$infiniteAmmo(CallbackInfoReturnable cir) {
      if (InfiniteAmmoHack.infiniteAmmoActive) {
         if (this.shooter != null) {
            try {
               Minecraft mc = Minecraft.m_91087_();
               if (mc.f_91074_ != null && this.shooter.m_20148_().equals(mc.f_91074_.m_20148_())) {
                  cir.setReturnValue(false);
               }
            } catch (Throwable var3) {
            }

         }
      }
   }
}
