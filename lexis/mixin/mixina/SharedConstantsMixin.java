package lexis.mixin.mixina;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Render.ColorCodeHack;
import lexis.Hack.Hackutil.HackManager;
import net.minecraft.SharedConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({SharedConstants.class})
public class SharedConstantsMixin {
   @Inject(
      method = {"isAllowedChatCharacter"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void isAllowedChatCharacter(char c, CallbackInfoReturnable cir) {
      Iterator var2 = HackManager.getInstance().getHacks().iterator();

      Hack hack;
      do {
         if (!var2.hasNext()) {
            return;
         }

         hack = (Hack)var2.next();
      } while(!(hack instanceof ColorCodeHack) || !hack.isEnabled());

      cir.setReturnValue(c >= ' ' && c != 127);
      cir.cancel();
   }
}
