package lexis.mixin.mixins;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Chat.ChatRetainHack;
import lexis.Hack.Hackutil.HackManager;
import lexis.Hack.Utils.Chat.ChatRetainHelper;
import lexis.mixin.accessor.ChatScreenAccessor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({ChatScreen.class})
public abstract class ChatScreenRetainMixin {
   @Shadow
   protected EditBox f_95573_;

   private boolean isHackEnabled() {
      Iterator var1 = HackManager.getInstance().getHacks().iterator();

      Hack hack;
      do {
         if (!var1.hasNext()) {
            return false;
         }

         hack = (Hack)var1.next();
      } while(!(hack instanceof ChatRetainHack) || !hack.isEnabled());

      return true;
   }

   @Inject(
      method = {"init"},
      at = {@At("TAIL")}
   )
   private void onInit(CallbackInfo ci) {
      if (this.isHackEnabled()) {
         ChatScreenAccessor a = (ChatScreenAccessor)this;
         if (!ChatRetainHelper.retainedMessage.isEmpty()) {
            a.getInput().m_94144_(ChatRetainHelper.retainedMessage);
            a.getInput().m_94196_(ChatRetainHelper.retainedMessage.length());
         }

      }
   }

   @Inject(
      method = {"removed"},
      at = {@At("HEAD")}
   )
   private void onRemoved(CallbackInfo ci) {
      if (this.isHackEnabled()) {
         String current = ((ChatScreenAccessor)this).getInput().m_94155_();
         if (!current.isEmpty()) {
            ChatRetainHelper.retainedMessage = current;
         }

      }
   }

   @Inject(
      method = {"keyPressed"},
      at = {@At("HEAD")}
   )
   private void onKeyPressed(int p_95591_, int p_95592_, int p_95593_, CallbackInfoReturnable cir) {
      if (this.isHackEnabled()) {
         if ((p_95591_ == 257 || p_95591_ == 335) && !((ChatScreenAccessor)this).getInput().m_94155_().isEmpty()) {
            ChatRetainHelper.retainedMessage = "";
         }

      }
   }
}
