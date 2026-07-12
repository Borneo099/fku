package lexis.mixin.tacz;

import lexis.Hack.Hacks.TaCZ_Server.MaxRpmHack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
   targets = {"com.tacz.guns.entity.shooter.LivingEntityShoot"},
   remap = false
)
public class TaczMaxRpmMixin {
   @Shadow
   @Final
   private LivingEntity shooter;

   @Inject(
      method = {"getShootCoolDown(J)J"},
      at = {@At("HEAD")},
      cancellable = true,
      remap = false
   )
   private void lexis$forceNoCooldown(long gameTime, CallbackInfoReturnable cir) {
      if (MaxRpmHack.maxRpmActive) {
         if (this.shooter != null) {
            try {
               Minecraft mc = Minecraft.m_91087_();
               if (mc.f_91074_ != null && this.shooter.m_20148_().equals(mc.f_91074_.m_20148_())) {
                  cir.setReturnValue(0L);
               }
            } catch (Throwable var5) {
            }

         }
      }
   }
}
