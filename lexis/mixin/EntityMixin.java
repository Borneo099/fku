package lexis.mixin;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Movement.AntiPushHack;
import lexis.Hack.Hackutil.HackManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Entity.class})
public class EntityMixin {
   @Inject(
      method = {"push*"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onPush2(Entity entity, CallbackInfo ci) {
      Entity self = (Entity)this;
      if (self instanceof Player) {
         Iterator var4 = HackManager.getInstance().getHacks().iterator();

         Hack hack;
         do {
            if (!var4.hasNext()) {
               return;
            }

            hack = (Hack)var4.next();
         } while(!(hack instanceof AntiPushHack) || !hack.isEnabled() || !((AntiPushHack)hack).shouldCancelPush());

         ci.cancel();
      }
   }
}
