package lexis.Hack.Hacks.Movement;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;

public class BouncyHack extends Hack {
   private HackConfig config;
   private static final String CONFIG_KEY = "弹跳";
   private double jumpHeight = 3.0;

   public BouncyHack() {
      super("弹跳", new String[]{"在史莱姆块上弹得更高", "这用来是小游戏服务器作弊能力！"}, Hack.Category.MOVEMENT, true);
      this.addSetting(new Hack.Setting("弹跳高度", "弹起高度(格)", 3.0, 1.0, 512.0, Hack.ValueDisplay.DECIMAL));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      this.jumpHeight = this.config.getDoubleSetting("弹跳", "弹跳高度", 3.0);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         if (setting.getName().equals("弹跳高度")) {
            setting.setValue(this.jumpHeight);
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
         if (setting.getName().equals("弹跳高度")) {
            double newHeight = setting.getDouble();
            if (newHeight != this.jumpHeight) {
               this.jumpHeight = newHeight;
               needSave = true;
            }
            break;
         }
      }

      if (needSave) {
         this.config.saveHackSettings("弹跳", this.getSettings());
      }

   }

   public double getJumpHeight() {
      return this.jumpHeight;
   }

   public void onClick() {
      this.toggle();
   }
}
