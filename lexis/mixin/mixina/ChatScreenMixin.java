package lexis.mixin.mixina;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Chat.ChatKeepOpenHack;
import lexis.Hack.Hackutil.HackManager;
import lexis.mixin.accessor.ChatScreenAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({ChatScreen.class})
public abstract class ChatScreenMixin {
   @Shadow
   protected EditBox f_95573_;

   @Redirect(
      method = {"keyPressed"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V",
   ordinal = 1
)
   )
   private void onSendChatMessageCloseChat(Minecraft mc, Screen screen) {
      ChatScreenAccessor accessor = (ChatScreenAccessor)this;
      Iterator var5 = HackManager.getInstance().getHacks().iterator();

      while(var5.hasNext()) {
         Hack h = (Hack)var5.next();
         if (h instanceof ChatKeepOpenHack hack && h.isEnabled()) {
            break;
         }
      }

      if (hack != null) {
         if (hack.shouldKeepText()) {
            mc.m_91152_((Screen)null);
            mc.m_91152_(new ChatScreen(accessor.getInput().m_94155_()));
         } else if (accessor.getInput().m_94155_().isEmpty()) {
            mc.m_91152_(screen);
         } else {
            accessor.getInput().m_94144_("");
         }
      } else {
         mc.m_91152_(screen);
      }

   }
}
