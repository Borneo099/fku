package lexis.mixin.mixina;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Chat.ChatHistoryHack;
import lexis.Hack.Hackutil.HackManager;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ChatComponent.class})
public class ChatComponentMixin {
   @Inject(
      method = {"clearMessages"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onClearMessages(CallbackInfo ci) {
      Iterator var2 = HackManager.getInstance().getHacks().iterator();

      Hack hack;
      do {
         if (!var2.hasNext()) {
            return;
         }

         hack = (Hack)var2.next();
      } while(!(hack instanceof ChatHistoryHack) || !hack.isEnabled());

      ci.cancel();
   }
}
