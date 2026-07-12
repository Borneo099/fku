package lexis.Hack.Hacks.Movement;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;

public class BoatFrictionHack extends Hack {
   private HackConfig config;
   private static final String CONFIG_KEY = "船摩擦力";
   private double friction = 1.0;

   public BoatFrictionHack() {
      super("船摩擦力", "这样可以覆盖摩擦，让你的船能更快移动", Hack.Category.MOVEMENT, true);
      this.addSetting(new Hack.Setting("摩擦力系数", "正则块摩擦力 = 0.6。填充冰摩擦力=0.98", 1.0, 0.1, 0.99, Hack.ValueDisplay.DECIMAL));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      this.friction = this.config.getDoubleSetting("船摩擦力", "摩擦力系数", 1.0);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         if (setting.getName().equals("摩擦力系数")) {
            setting.setValue(this.friction);
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
         if (setting.getName().equals("摩擦力系数")) {
            double newFriction = setting.getDouble();
            if (newFriction != this.friction) {
               this.friction = newFriction;
               needSave = true;
            }
            break;
         }
      }

      if (needSave) {
         this.config.saveHackSettings("船摩擦力", this.getSettings());
      }

   }

   public double getFriction() {
      return this.friction;
   }

   public void onClick() {
      this.toggle();
   }
}
