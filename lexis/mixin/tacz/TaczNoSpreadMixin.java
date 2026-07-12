package lexis.mixin.tacz;

import com.tacz.guns.entity.shooter.ShooterDataHolder;
import java.lang.reflect.Method;
import lexis.Hack.Hacks.TaCZ_Server.NoSpreadHack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
   targets = {"com.tacz.guns.api.item.gun.AbstractGunItem"},
   remap = false
)
public class TaczNoSpreadMixin {
   private static Method shoot6Method;

   @Inject(
      method = {"doBulletSpread"},
      at = {@At("HEAD")},
      cancellable = true,
      remap = false
   )
   private void lexis$noSpread(ShooterDataHolder dataHolder, ItemStack stack, LivingEntity shooter, Projectile projectile, int seed, float velocity, float inaccuracy, float pitch, float yaw, CallbackInfo ci) {
      if (NoSpreadHack.noSpreadActive) {
         if (shooter != null && projectile != null) {
            try {
               Minecraft mc = Minecraft.m_91087_();
               if (mc.f_91074_ != null && shooter.m_20148_().equals(mc.f_91074_.m_20148_())) {
                  if (shoot6Method == null) {
                     shoot6Method = Projectile.class.getMethod("shoot", Entity.class, Float.TYPE, Float.TYPE, Float.TYPE, Float.TYPE, Float.TYPE);
                     shoot6Method.setAccessible(true);
                  }

                  shoot6Method.invoke(projectile, shooter, pitch, yaw, 0.0F, velocity, 0.0F);
                  ci.cancel();
               }
            } catch (Throwable var12) {
            }

         }
      }
   }
}
