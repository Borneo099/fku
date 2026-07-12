package lexis.Hack.Hacks.Movement;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;

public class AntiPushHack extends Hack {
   private boolean antiPush = true;
   private HackConfig config = HackConfig.getInstance();

   public AntiPushHack() {
      super("反推动", new String[]{"防止所有实体天天推动你 烦人"}, Hack.Category.MOVEMENT, true);
      this.loadConfig();
   }

   private void loadConfig() {
      this.antiPush = this.config.getBooleanSetting("反推动", "免疫推动", true);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         if (setting.getName().equals("免疫推动")) {
            setting.setValue(this.antiPush);
            break;
         }
      }

   }

   public void onEnable() {
   }

   public void onDisable() {
   }

   public void onUpdate() {
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         if (setting.getName().equals("免疫推动")) {
            this.antiPush = setting.getBoolean();
            break;
         }
      }

   }

   public void onClick() {
      this.toggle();
   }

   public boolean shouldCancelPush() {
      return this.isEnabled() && this.antiPush;
   }
}
