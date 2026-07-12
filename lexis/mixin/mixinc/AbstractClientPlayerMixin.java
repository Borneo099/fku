package lexis.mixin.mixinc;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Render.NoSprintFovHack;
import lexis.Hack.Hackutil.HackManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({AbstractClientPlayer.class})
public class AbstractClientPlayerMixin {
   @Inject(
      method = {"getFieldOfViewModifier"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void modifyFieldOfView(CallbackInfoReturnable cir) {
      if (Minecraft.m_91087_().f_91074_ == this) {
         Iterator var2 = HackManager.getInstance().getHacks().iterator();

         while(var2.hasNext()) {
            Hack hack = (Hack)var2.next();
            if (hack instanceof NoSprintFovHack && hack.isEnabled()) {
               cir.setReturnValue(1.0F);
               break;
            }
         }
      }

   }
}
