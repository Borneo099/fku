package lexis.Hack.Hacks.Chat;

import lexis.Hack.Hack;

public class ChatNoTrimHack extends Hack {
   public ChatNoTrimHack() {
      super("聊天记录不清除", new String[]{"取消原版聊天100条上限，超过后不再删除最旧的聊天记录", "§c注意：长时间大量聊天会持续占用内存"}, Hack.Category.CHAT, true);
   }

   public void onEnable() {
   }

   public void onDisable() {
   }

   public void onUpdate() {
   }

   public void onClick() {
      this.toggle();
   }
}
