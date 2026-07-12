package lexis.Hack.Hacks.Chat;

import lexis.Hack.Hack;

public class InfiniChatHack extends Hack {
   public InfiniChatHack() {
      super("无限聊天", new String[]{"取消聊天框长度256上限", "一般用于编辑NBT的指令", "注意：不推荐用于和别人聊天。大多数服务器一般,超过256字符就会无法显示后面的内容"}, Hack.Category.CHAT, true);
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
