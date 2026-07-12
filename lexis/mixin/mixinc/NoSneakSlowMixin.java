package lexis.mixin.mixinc;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Movement.NoSneakSlowHack;
import lexis.Hack.Hackutil.HackManager;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin({LocalPlayer.class})
public class NoSneakSlowMixin {
   @Shadow
   public Input f_108618_;

   @ModifyArg(
      method = {"aiStep"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/player/Input;tick(ZF)V"
),
      index = 1
   )
   private float modifySneakSpeed(float original) {
      Iterator var2 = HackManager.getInstance().getHacks().iterator();

      Hack hack;
      do {
         if (!var2.hasNext()) {
            return original;
         }

         hack = (Hack)var2.next();
      } while(!(hack instanceof NoSneakSlowHack) || !hack.isEnabled());

      return 1.0F;
   }
}
