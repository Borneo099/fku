package lexis.Hack.Hacks.Movement;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;

public class SpringJumpHack extends Hack {
   private HackConfig config;
   private static final String CONFIG_KEY = "弹簧跳";
   private double jumpHeight = 3.0;
   private int cooldownTicks = 5;

   public SpringJumpHack() {
      super("弹簧跳", "落地后自动弹起", Hack.Category.MOVEMENT, true);
      this.addSetting(new Hack.Setting("弹跳高度", "弹起高度（格）", 3.0, 1.0, 10.0, Hack.ValueDisplay.DECIMAL));
      this.addSetting(new Hack.Setting("冷却刻数", "弹跳后冷却时间（tick）", 5, 1, 20, Hack.ValueDisplay.INTEGER));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      this.jumpHeight = this.config.getDoubleSetting("弹簧跳", "弹跳高度", 3.0);
      this.cooldownTicks = this.config.getIntSetting("弹簧跳", "冷却刻数", 5);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         switch (setting.getName()) {
            case "弹跳高度":
               setting.setValue(this.jumpHeight);
               break;
            case "冷却刻数":
               setting.setValue(this.cooldownTicks);
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
         switch (setting.getName()) {
            case "弹跳高度":
               double newHeight = setting.getDouble();
               if (newHeight != this.jumpHeight) {
                  this.jumpHeight = newHeight;
                  needSave = true;
               }
               break;
            case "冷却刻数":
               int newCooldown = setting.getInt();
               if (newCooldown != this.cooldownTicks) {
                  this.cooldownTicks = newCooldown;
                  needSave = true;
               }
         }
      }

      if (needSave) {
         this.config.saveHackSettings("弹簧跳", this.getSettings());
      }

   }

   public double getJumpHeight() {
      return this.jumpHeight;
   }

   public int getCooldownTicks() {
      return this.cooldownTicks;
   }

   public void onClick() {
      this.toggle();
   }
}
