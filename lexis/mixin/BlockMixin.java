package lexis.mixin;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Movement.NoSlowdownHack;
import lexis.Hack.Hackutil.HackManager;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Block.class})
public class BlockMixin {
   @Inject(
      method = {"getSpeedFactor"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onGetSpeedFactor(CallbackInfoReturnable cir) {
      Iterator var2 = HackManager.getInstance().getHacks().iterator();

      Hack hack;
      do {
         if (!var2.hasNext()) {
            return;
         }

         hack = (Hack)var2.next();
      } while(!(hack instanceof NoSlowdownHack) || !hack.isEnabled());

      cir.setReturnValue(1.0F);
   }

   @Inject(
      method = {"getJumpFactor"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onGetJumpFactor(CallbackInfoReturnable cir) {
      Iterator var2 = HackManager.getInstance().getHacks().iterator();

      Hack hack;
      do {
         if (!var2.hasNext()) {
            return;
         }

         hack = (Hack)var2.next();
      } while(!(hack instanceof NoSlowdownHack) || !hack.isEnabled());

      cir.setReturnValue(1.0F);
   }
}
