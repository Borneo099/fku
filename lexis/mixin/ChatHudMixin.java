package lexis.mixin;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Baritone.StructureLocatorHack;
import lexis.Hack.Hacks.Chat.InfiniChatHack;
import lexis.Hack.Hacks.L_Enders_Cataclysm_C.CataclysmLocatorHack;
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
public class ChatHudMixin {
   @Inject(
      method = {"addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;ILnet/minecraft/client/GuiMessageTag;Z)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onAddMessage(Component message, MessageSignature signature, int ticks, GuiMessageTag tag, boolean refresh, CallbackInfo ci) {
      try {
         CataclysmLocatorHack.onChatMessage(message.getString());
      } catch (Throwable var10) {
      }

      try {
         StructureLocatorHack.onChatMessage(message.getString());
      } catch (Throwable var9) {
      }

      Iterator var7 = HackManager.getInstance().getHacks().iterator();

      while(var7.hasNext()) {
         Hack hack = (Hack)var7.next();
         if (hack instanceof InfiniChatHack && hack.isEnabled()) {
            break;
         }
      }

   }
}
