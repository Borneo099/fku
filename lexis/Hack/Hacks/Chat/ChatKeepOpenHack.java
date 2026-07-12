package lexis.Hack.Hacks.Chat;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;

public class ChatKeepOpenHack extends Hack {
   private HackConfig config;
   private static final String CONFIG_KEY = "聊天发送后不关";
   private boolean keepText = false;

   public ChatKeepOpenHack() {
      super("聊天发送后不关", new String[]{"按下回车发送消息后不关闭聊天框，可以连续发送"}, Hack.Category.CHAT, true);
      this.addSetting(new Hack.Setting("发送后保留文本", "发送后不清除输入框中的文本", false));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      this.keepText = this.config.getBooleanSetting("聊天发送后不关", "发送后保留文本", false);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         if (setting.getName().equals("发送后保留文本")) {
            setting.setValue(this.keepText);
            break;
         }
      }

   }

   public void onEnable() {
   }

   public void onDisable() {
   }

   public void onUpdate() {
      boolean needSave = false;
      Iterator var2 = this.getSettings().iterator();

      while(var2.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var2.next();
         if (setting.getName().equals("发送后保留文本")) {
            if (setting.getBoolean() != this.keepText) {
               this.keepText = setting.getBoolean();
               needSave = true;
            }
            break;
         }
      }

      if (needSave) {
         this.config.saveHackSettings("聊天发送后不关", this.getSettings());
      }

   }

   public boolean shouldKeepText() {
      return this.isEnabled() && this.keepText;
   }

   public void onClick() {
      this.toggle();
   }
}
