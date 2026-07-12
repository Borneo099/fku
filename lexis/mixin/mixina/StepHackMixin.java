package lexis.mixin.mixina;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Movement.StepHack;
import lexis.Hack.Hackutil.HackManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Entity.class})
public class StepHackMixin {
   @Shadow
   private float f_19793_;

   @Inject(
      method = {"maxUpStep"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void onMaxUpStep(CallbackInfoReturnable cir) {
      Entity entity = (Entity)this;
      if (entity instanceof Player) {
         Iterator var3 = HackManager.getInstance().getHacks().iterator();

         Hack hack;
         do {
            if (!var3.hasNext()) {
               return;
            }

            hack = (Hack)var3.next();
         } while(!(hack instanceof StepHack) || !hack.isEnabled());

         cir.setReturnValue(((StepHack)hack).getHeight());
      }
   }
}
