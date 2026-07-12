package lexis.Hack.Hacks.Lexis;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;

public class NotificationHack extends Hack {
   private NotificationMode mode;
   private HackConfig config;

   public NotificationHack() {
      super("通知系统", "右下显示功能开关等通知", Hack.Category.LEXIS, true);
      this.mode = NotificationHack.NotificationMode.TOAST;
      this.addSetting(new Hack.Setting("通知模式", "选择通知显示方式", "右下通知", new String[]{"右下通知", "聊天通知"}));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      String modeStr = this.config.getStringSetting("通知系统", "通知模式", "右下通知");
      NotificationMode[] var2 = NotificationHack.NotificationMode.values();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         NotificationMode m = var2[var4];
         if (m.toString().equals(modeStr)) {
            this.mode = m;
            break;
         }
      }

      Iterator var6 = this.getSettings().iterator();

      while(var6.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var6.next();
         if (setting.getName().equals("通知模式")) {
            setting.setValue(modeStr);
            break;
         }
      }

   }

   public void onEnable() {
   }

   public void onDisable() {
   }

   public String getDisplayName() {
      return String.format("%s [%s]", this.getName(), this.mode.toString());
   }

   public void onUpdate() {
      boolean needSave = false;
      Iterator var2 = this.getSettings().iterator();

      while(var2.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var2.next();
         if (setting.getName().equals("通知模式")) {
            String newMode = setting.getString();
            NotificationMode newEnum = null;
            NotificationMode[] var6 = NotificationHack.NotificationMode.values();
            int var7 = var6.length;

            for(int var8 = 0; var8 < var7; ++var8) {
               NotificationMode m = var6[var8];
               if (m.toString().equals(newMode)) {
                  newEnum = m;
                  break;
               }
            }

            if (newEnum != null && newEnum != this.mode) {
               this.mode = newEnum;
               needSave = true;
            }
            break;
         }
      }

      if (needSave) {
         this.config.saveHackSettings("通知系统", this.getSettings());
      }

   }

   public NotificationMode getMode() {
      return this.mode;
   }

   public void onClick() {
      this.toggle();
   }

   public static enum NotificationMode {
      TOAST("右下通知"),
      CHAT("聊天通知");

      private final String displayName;

      private NotificationMode(String name) {
         this.displayName = name;
      }

      public String toString() {
         return this.displayName;
      }

      // $FF: synthetic method
      private static NotificationMode[] $values() {
         return new NotificationMode[]{TOAST, CHAT};
      }
   }
}
