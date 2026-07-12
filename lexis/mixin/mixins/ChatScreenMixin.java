package lexis.mixin.mixins;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Chat.BetterChatHack;
import lexis.Hack.Hackutil.HackManager;
import lexis.Hack.Hackutil.NotificationManager;
import lexis.Hack.Utils.Chat.ChatMessageHelper;
import lexis.Hack.Utils.Chat.AI.AIUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({ChatScreen.class})
public class ChatScreenMixin {
   private final Minecraft mc = Minecraft.m_91087_();
   private final Map lastClickTime = new HashMap();
   private final Map clickCount = new HashMap();
   private static final long DOUBLE_CLICK_DELAY_MS = 300L;

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

   @Inject(
      method = {"mouseClicked"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable cir) {
      if (this.isBetterChatEnabled()) {
         if (this.mc.f_91074_ != null) {
            Style style = this.mc.f_91065_.m_93076_().m_93800_(mouseX, mouseY);
            if (style != null) {
               String insertion = style.m_131189_();
               if (insertion != null) {
                  boolean doubleClickMode = BetterChatHack.isDoubleClickMode();
                  String content;
                  if (insertion.startsWith("!copy:")) {
                     content = insertion.substring(6);
                     this.mc.f_91068_.m_90911_(content);
                     NotificationManager.info("复制", "已经复制成功！", 3);
                     cir.setReturnValue(true);
                  } else if (insertion.startsWith("!plus:")) {
                     content = insertion.substring(6);
                     if (doubleClickMode) {
                        if (this.isDoubleClick(insertion)) {
                           this.mc.f_91074_.f_108617_.m_246175_(ChatMessageHelper.stripColor(content));
                           this.resetClick(insertion);
                           cir.setReturnValue(true);
                        } else {
                           cir.setReturnValue(true);
                        }
                     } else {
                        this.mc.f_91074_.f_108617_.m_246175_(ChatMessageHelper.stripColor(content));
                        cir.setReturnValue(true);
                     }
                  } else {
                     String cleanFull;
                     if (insertion.startsWith("!full:")) {
                        content = insertion.substring(6);
                        cleanFull = ChatMessageHelper.stripColor(content);
                        if (doubleClickMode) {
                           if (this.isDoubleClick(insertion)) {
                              this.mc.f_91074_.f_108617_.m_246175_(cleanFull);
                              this.resetClick(insertion);
                              cir.setReturnValue(true);
                           } else {
                              cir.setReturnValue(true);
                           }
                        } else {
                           this.mc.f_91074_.f_108617_.m_246175_(cleanFull);
                           cir.setReturnValue(true);
                        }
                     } else if (insertion.startsWith("!translate:")) {
                        content = insertion.substring(11);
                        cleanFull = ChatMessageHelper.stripColor(content);
                        (new Thread(() -> {
                           try {
                              String translated = AIUtils.translate(cleanFull);
                              this.mc.execute(() -> {
                                 this.mc.f_91074_.m_5661_(Component.m_237113_("§7[§6Lexis§7] §b[翻译]：§f" + translated), false);
                              });
                           } catch (Exception var3) {
                              this.mc.execute(() -> {
                                 this.mc.f_91074_.m_5661_(Component.m_237113_("§7[§6Lexis§7] §b[翻译]：§c翻译失败: " + var3.getMessage()), false);
                              });
                           }

                        })).start();
                        cir.setReturnValue(true);
                     }
                  }

               }
            }
         }
      }
   }

   private boolean isDoubleClick(String key) {
      long now = System.currentTimeMillis();
      Long last = (Long)this.lastClickTime.get(key);
      int count = (Integer)this.clickCount.getOrDefault(key, 0);
      if (last != null && now - last <= 300L) {
         this.clickCount.put(key, count + 1);
         this.lastClickTime.put(key, now);
         return count + 1 >= 2;
      } else {
         this.clickCount.put(key, 1);
         this.lastClickTime.put(key, now);
         return false;
      }
   }

   private void resetClick(String key) {
      this.clickCount.remove(key);
      this.lastClickTime.remove(key);
   }
}
