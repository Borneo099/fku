package lexis.mixin.mixinc;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Movement.SlippyHack;
import lexis.Hack.Hackutil.HackManager;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Block.class})
public class BlockMixin {
   @Inject(
      method = {"getFriction"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void onGetFriction(CallbackInfoReturnable cir) {
      Iterator var2 = HackManager.getInstance().getHacks().iterator();

      Hack hack;
      do {
         if (!var2.hasNext()) {
            return;
         }

         hack = (Hack)var2.next();
      } while(!(hack instanceof SlippyHack) || !hack.isEnabled());

      float newFriction = (float)((SlippyHack)hack).getFriction();
      cir.setReturnValue(newFriction);
   }
}
