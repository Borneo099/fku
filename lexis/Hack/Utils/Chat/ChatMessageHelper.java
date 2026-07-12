package lexis.Hack.Utils.Chat;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lexis.Hack.Hacks.Chat.BetterChatHack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.ClickEvent.Action;

public class ChatMessageHelper {
   private static final Pattern PLAYER_MSG_PATTERN = Pattern.compile("^<([^>]+)>\\s*(.*)");
   private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

   public static String stripColor(String text) {
      return text.replaceAll("§[0-9a-fk-or]", "");
   }

   public static MutableComponent addButtons(Component original) {
      String fullText = original.getString();
      String cleanFull = stripColor(fullText);
      String playerName = null;
      Matcher matcher = PLAYER_MSG_PATTERN.matcher(cleanFull);
      boolean isPlayerMessage = matcher.matches();
      String content;
      MutableComponent messageComponent;
      MutableComponent translateBtn;
      if (BetterChatHack.isBeautifyMessages() && isPlayerMessage) {
         playerName = matcher.group(1);
         String messageText = matcher.group(2);
         content = messageText;
         messageComponent = Component.m_237113_("");
         if (BetterChatHack.isShowTimestamp()) {
            String timeStr = LocalTime.now().format(TIME_FORMATTER);
            MutableComponent timeComp = Component.m_237113_(timeStr).m_130938_((style) -> {
               return style.m_131148_(TextColor.m_131266_(BetterChatHack.getTimestampColor()));
            });
            messageComponent.m_7220_(timeComp);
            messageComponent.m_7220_(Component.m_237113_(" "));
         }

         translateBtn = Component.m_237113_("┋").m_130938_((style) -> {
            return style.m_131148_(TextColor.m_131266_(BetterChatHack.getSeparatorColor()));
         });
         messageComponent.m_7220_(translateBtn).m_7220_(Component.m_237113_(" "));
         String myName = Minecraft.m_91087_().f_91074_ != null ? Minecraft.m_91087_().f_91074_.m_36316_().getName() : "";
         MutableComponent nameComp;
         if (BetterChatHack.isHighlightSelf() && playerName.equals(myName)) {
            nameComp = Component.m_237113_(playerName).m_130938_((style) -> {
               return style.m_131148_(TextColor.m_131266_(BetterChatHack.getHighlightSelfColor()));
            });
         } else {
            nameComp = Component.m_237113_(playerName).m_130940_(ChatFormatting.WHITE);
         }

         messageComponent.m_7220_(nameComp);
         messageComponent.m_7220_(Component.m_237113_(" ")).m_7220_(translateBtn).m_7220_(Component.m_237113_(" "));
         messageComponent.m_7220_(Component.m_237113_(messageText).m_130940_(ChatFormatting.WHITE));
      } else {
         messageComponent = original.m_6881_();
         if (isPlayerMessage) {
            content = matcher.group(2);
         } else {
            content = cleanFull;
         }
      }

      if (BetterChatHack.isShowCopy()) {
         translateBtn = Component.m_237113_(" [复制] ").m_130938_((style) -> {
            return style.m_131140_(ChatFormatting.GREEN).m_131142_(new ClickEvent(Action.RUN_COMMAND, "")).m_131138_("!copy:" + content).m_131144_(new HoverEvent(net.minecraft.network.chat.HoverEvent.Action.f_130831_, Component.m_237113_("复制消息内容")));
         });
         messageComponent.m_7220_(translateBtn);
      }

      if (BetterChatHack.isShowPlus()) {
         translateBtn = Component.m_237113_("[+1] ").m_130938_((style) -> {
            return style.m_131140_(ChatFormatting.GOLD).m_131142_(new ClickEvent(Action.RUN_COMMAND, "")).m_131138_("!plus:" + content).m_131144_(new HoverEvent(net.minecraft.network.chat.HoverEvent.Action.f_130831_, Component.m_237113_("重复发送消息")));
         });
         messageComponent.m_7220_(translateBtn);
      }

      if (BetterChatHack.isShowFull()) {
         translateBtn = Component.m_237113_("[全发]").m_130938_((style) -> {
            return style.m_131140_(ChatFormatting.LIGHT_PURPLE).m_131142_(new ClickEvent(Action.RUN_COMMAND, "")).m_131138_("!full:" + cleanFull).m_131144_(new HoverEvent(net.minecraft.network.chat.HoverEvent.Action.f_130831_, Component.m_237113_("发送完整格式消息")));
         });
         messageComponent.m_7220_(translateBtn);
      }

      if (BetterChatHack.isShowTranslate()) {
         translateBtn = Component.m_237113_(" [翻译]").m_130938_((style) -> {
            return style.m_131140_(ChatFormatting.AQUA).m_131142_(new ClickEvent(Action.RUN_COMMAND, "")).m_131138_("!translate:" + content).m_131144_(new HoverEvent(net.minecraft.network.chat.HoverEvent.Action.f_130831_, Component.m_237113_("翻译此消息(注意：这模型是 Gemini-3 等的很慢 2.不要点击多次 不然会拒绝ai回答了点后等就行)")));
         });
         messageComponent.m_7220_(translateBtn);
      }

      return messageComponent;
   }
}
