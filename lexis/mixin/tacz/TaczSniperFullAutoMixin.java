package lexis.mixin.tacz;

import java.lang.reflect.Field;
import lexis.Hack.Hacks.TaCZ.SniperFullAutoHack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
   targets = {"com.tacz.guns.client.input.ShootKey"},
   remap = false
)
public class TaczSniperFullAutoMixin {
   private static Field lastTimeShootSuccessField;

   @Inject(
      method = {"autoShoot"},
      at = {@At("HEAD")},
      remap = false
   )
   private static void onAutoShootHead(CallbackInfo ci) {
      if (SniperFullAutoHack.sniperFullAutoActive && lastTimeShootSuccessField != null) {
         try {
            lastTimeShootSuccessField.setBoolean((Object)null, false);
         } catch (Exception var2) {
         }
      }

   }

   static {
      try {
         lastTimeShootSuccessField = Class.forName("com.tacz.guns.client.input.ShootKey").getDeclaredField("lastTimeShootSuccess");
         lastTimeShootSuccessField.setAccessible(true);
      } catch (Exception var1) {
      }

   }
}
