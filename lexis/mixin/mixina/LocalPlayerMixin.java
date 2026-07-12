package lexis.mixin.mixina;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Fun.FakeContainerOpenHack;
import lexis.Hack.Hackutil.HackManager;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({LocalPlayer.class})
public class LocalPlayerMixin {
   @Inject(
      method = {"closeContainer"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onCloseContainer(CallbackInfo ci) {
      Iterator var2 = HackManager.getInstance().getHacks().iterator();

      Hack hack;
      do {
         if (!var2.hasNext()) {
            return;
         }

         hack = (Hack)var2.next();
      } while(!(hack instanceof FakeContainerOpenHack) || !hack.isEnabled());

      ci.cancel();
   }

   @Inject(
      method = {"clientSideCloseContainer"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onClientSideCloseContainer(CallbackInfo ci) {
      Iterator var2 = HackManager.getInstance().getHacks().iterator();

      Hack hack;
      do {
         if (!var2.hasNext()) {
            return;
         }

         hack = (Hack)var2.next();
      } while(!(hack instanceof FakeContainerOpenHack) || !hack.isEnabled());

      ci.cancel();
   }
}
