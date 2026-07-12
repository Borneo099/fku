package lexis.Hack.Hacks.Render;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.Utils.CameraSmooth;

public class CameraSmoothHack extends Hack {
   private HackConfig config;
   private static final String CONFIG_KEY = "运动摄像机重制版";
   private double smoothSpeed = 0.2;

   public CameraSmoothHack() {
      super("运动摄像机重制版", "重制版优化多了 更丝滑！", Hack.Category.RENDER, true);
      this.addSetting(new Hack.Setting("平滑速度", "视角跟玩家的速度 (0.01 = 慢, 0.6 = 快)", 0.2, 0.01, 0.6, Hack.ValueDisplay.DECIMAL));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      this.smoothSpeed = this.config.getDoubleSetting("运动摄像机重制版", "平滑速度", 0.2);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         if (setting.getName().equals("平滑速度")) {
            setting.setValue(this.smoothSpeed);
            break;
         }
      }

   }

   public void onEnable() {
      CameraSmooth.setEnabled(true);
      CameraSmooth.setSmoothSpeed(this.smoothSpeed);
   }

   public void onDisable() {
      CameraSmooth.setEnabled(false);
   }

   public void onUpdate() {
      boolean needSave = false;
      Iterator var2 = this.getSettings().iterator();

      while(var2.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var2.next();
         if (setting.getName().equals("平滑速度")) {
            double newSpeed = setting.getDouble();
            if (newSpeed != this.smoothSpeed) {
               this.smoothSpeed = newSpeed;
               CameraSmooth.setSmoothSpeed(this.smoothSpeed);
               needSave = true;
            }
            break;
         }
      }

      if (needSave) {
         this.config.saveHackSettings("运动摄像机重制版", this.getSettings());
      }

   }

   public void onClick() {
      this.toggle();
   }
}
