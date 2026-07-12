package lexis.Hack.Hacks.Chat;

import lexis.Hack.Hack;
import lexis.Hack.Utils.Chat.ChatRetainHelper;

public class ChatRetainHack extends Hack {
   public ChatRetainHack() {
      super("聊天关闭不清文本", "关闭聊天框后保留输入文本，下次打开继续编辑", Hack.Category.CHAT, true);
   }

   public void onEnable() {
   }

   public void onDisable() {
      ChatRetainHelper.clear();
   }

   public void onUpdate() {
   }

   public void onClick() {
      this.toggle();
   }
}
