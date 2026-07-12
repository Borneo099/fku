package lexis.mixin;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Chat.InfiniChatHack;
import lexis.Hack.Hackutil.HackManager;
import lexis.mixin.accessor.ChatScreenAccessor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ChatScreen.class})
public abstract class ChatScreenMixin {
   @Shadow
   protected EditBox f_95573_;

   @Inject(
      method = {"init"},
      at = {@At("TAIL")}
   )
   private void onInit(CallbackInfo ci) {
      Iterator var2 = HackManager.getInstance().getHacks().iterator();

      while(var2.hasNext()) {
         Hack hack = (Hack)var2.next();
         if (hack instanceof InfiniChatHack && hack.isEnabled()) {
            ((ChatScreenAccessor)this).getInput().m_94199_(Integer.MAX_VALUE);
            break;
         }
      }

   }
}
