package lexis.mixin.mixins;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Chat.BetterChatHack;
import lexis.Hack.Hackutil.HackManager;
import lexis.Hack.Utils.Chat.ChatMessageHelper;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin({ChatComponent.class})
public class ChatComponentMixin {
   private boolean isBetterChatEnabled() {
      Iterator var1 = HackManager.getInstance().getHacks().iterator();

      Hack hack;
      do {
         if (!var1.hasNext()) {
            return false;
         }

         hack = (Hack)var1.next();
      } while(!(hack instanceof BetterChatHack) || !hack.isEnabled());

      return true;
   }

   @ModifyVariable(
      method = {"addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;ILnet/minecraft/client/GuiMessageTag;Z)V"},
      at = @At("HEAD"),
      argsOnly = true,
      index = 1
   )
   private Component modifyMessage(Component original, Component message, MessageSignature signature, int ticks, GuiMessageTag tag, boolean refresh) {
      return (Component)(this.isBetterChatEnabled() ? ChatMessageHelper.addButtons(original) : original);
   }
}
