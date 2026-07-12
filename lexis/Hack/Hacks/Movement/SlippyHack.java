package lexis.Hack.Hacks.Movement;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;

public class SlippyHack extends Hack {
   private HackConfig config;
   private static final String CONFIG_KEY = "滑溜";
   private double friction = 1.0;

   public SlippyHack() {
      super("滑溜", "改变所有方块的摩擦力", Hack.Category.MOVEMENT, true);
      this.addSetting(new Hack.Setting("摩擦力", "基础摩擦力", 1.0, 0.01, 1.1, Hack.ValueDisplay.DECIMAL));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      this.friction = this.config.getDoubleSetting("滑溜", "摩擦力", 1.0);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         if (setting.getName().equals("摩擦力")) {
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
         if (setting.getName().equals("摩擦力")) {
            double newVal = setting.getDouble();
            if (Math.abs(newVal - this.friction) > 1.0E-6) {
               this.friction = newVal;
               needSave = true;
            }
            break;
         }
      }

      if (needSave) {
         this.config.saveHackSettings("滑溜", this.getSettings());
      }

   }

   public double getFriction() {
      return this.friction;
   }

   public void onClick() {
      this.toggle();
   }
}
