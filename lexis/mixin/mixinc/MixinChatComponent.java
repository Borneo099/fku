package lexis.mixin.mixinc;

import java.util.List;
import lexis.Hack.Hacks.Baritone.XrayExposedAutoMineHack;
import lexis.Hack.Hacks.Chat.AntiSpamHack;
import lexis.mixin.accessor.AccessorChatComponent;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ChatComponent.class})
public abstract class MixinChatComponent {
   @Inject(
      method = {"addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void lexis$filterBaritone(Component component, MessageSignature signature, GuiMessageTag tag, CallbackInfo ci) {
      if (component != null) {
         String text = component.getString();
         if (text.startsWith("[Baritone]") && XrayExposedAutoMineHack.suppressBaritoneMessages() && (text.contains("Pos") || text.contains("Position") || text.contains("Fill") || text.contains("Selection"))) {
            ci.cancel();
         } else if (AntiSpamHack.enabled) {
            ChatComponent self = (ChatComponent)this;
            AccessorChatComponent acc = (AccessorChatComponent)self;
            String raw = component.getString();
            if (!raw.isEmpty()) {
               String clean = AntiSpamHack.stripCounter(raw);
               List all = acc.getAllMessages();
               int matchedIdx = -1;
               int prevCount = 1;
               int searchDepth = Math.min(all.size(), 64);

               int next;
               for(next = 0; next < searchDepth; ++next) {
                  String existing = ((GuiMessage)all.get(next)).f_240363_().getString();
                  if (AntiSpamHack.stripCounter(existing).equals(clean)) {
                     matchedIdx = next;
                     prevCount = AntiSpamHack.extractCount(existing);
                     break;
                  }
               }

               if (matchedIdx != -1) {
                  all.remove(matchedIdx);
                  acc.invokeRefreshTrimmedMessage();
                  next = prevCount + 1;
                  MutableComponent newMsg = component.m_6881_().m_7220_(Component.m_237113_(" [x" + next + "]").m_130938_((s) -> {
                     return s.m_178520_(AntiSpamHack.counterColorRGB());
                  }));
                  ci.cancel();
                  self.m_240964_(newMsg, signature, tag);
               }
            }
         }
      }
   }
}
