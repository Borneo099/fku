package lexis.mixin;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Render.BlinkHack;
import lexis.Hack.Hackutil.HackManager;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Connection.class})
public class ClientConnectionMixin {
   @Inject(
      method = {"send*"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onSend(Packet packet, CallbackInfo ci) {
      Iterator var3 = HackManager.getInstance().getHacks().iterator();

      Hack hack;
      do {
         if (!var3.hasNext()) {
            return;
         }

         hack = (Hack)var3.next();
      } while(!(hack instanceof BlinkHack) || !hack.isEnabled() || !((BlinkHack)hack).shouldCancelPacket(packet));

      ci.cancel();
   }
}
