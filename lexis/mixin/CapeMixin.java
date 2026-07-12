package lexis.mixin;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Render.CapeHack;
import lexis.Hack.Hackutil.HackManager;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({AbstractClientPlayer.class})
public class CapeMixin {
   @Inject(
      method = {"getCloakTextureLocation"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onGetCapeTexture(CallbackInfoReturnable cir) {
      AbstractClientPlayer player = (AbstractClientPlayer)this;
      if (player.m_7578_()) {
         Iterator var3 = HackManager.getInstance().getHacks().iterator();

         while(var3.hasNext()) {
            Hack hack = (Hack)var3.next();
            if (hack instanceof CapeHack && hack.isEnabled()) {
               ResourceLocation cape = ((CapeHack)hack).getCurrentCape();
               if (cape != null) {
                  cir.setReturnValue(cape);
               }
               break;
            }
         }

      }
   }
}
