package lexis.mixin.mixina;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Chat.PlayerNotifierHack;
import lexis.Hack.Hackutil.HackManager;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ChatComponent.class})
public class PlayerJoinLeaveMixin {
   @Inject(
      method = {"addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;ILnet/minecraft/client/GuiMessageTag;Z)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onAddMessage(Component message, MessageSignature signature, int ticks, GuiMessageTag tag, boolean refresh, CallbackInfo ci) {
      String text = message.getString();
      if (text.matches(".*(joined|left|加入了|退出了).*游戏.*") || text.matches(".*(加入了游戏|退出了游戏).*")) {
         Iterator var8 = HackManager.getInstance().getHacks().iterator();

         while(var8.hasNext()) {
            Hack hack = (Hack)var8.next();
            if (hack instanceof PlayerNotifierHack && hack.isEnabled()) {
               ci.cancel();
               break;
            }
         }
      }

   }
}
