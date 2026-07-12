package lexis.mixin.mixins;

import java.util.Iterator;
import java.util.List;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Chat.ChatNoTrimHack;
import lexis.Hack.Hackutil.HackManager;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({ChatComponent.class})
public class ChatNoTrimMixin {
   private boolean lexis$isNoTrimEnabled() {
      Iterator var1 = HackManager.getInstance().getHacks().iterator();

      Hack hack;
      do {
         if (!var1.hasNext()) {
            return false;
         }

         hack = (Hack)var1.next();
      } while(!(hack instanceof ChatNoTrimHack) || !hack.isEnabled());

      return true;
   }

   @Redirect(
      method = {"addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;ILnet/minecraft/client/GuiMessageTag;Z)V"},
      at = @At(
   value = "INVOKE",
   target = "Ljava/util/List;remove(I)Ljava/lang/Object;"
),
      require = 0
   )
   private Object lexis$cancelTrimRemove(List list, int index) {
      return this.lexis$isNoTrimEnabled() ? null : list.remove(index);
   }
}
