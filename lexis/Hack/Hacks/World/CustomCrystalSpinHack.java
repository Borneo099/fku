package lexis.Hack.Hacks.World;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;

public class CustomCrystalSpinHack extends Hack {
   private static boolean featureEnabled = false;
   private static float verticalSpeed = 1.0F;
   private static float verticalAmplitude = 0.2F;
   private static float rotationSpeed = 30.0F;
   private static boolean syncWithWorldTime = false;
   private HackConfig config = HackConfig.getInstance();
   private static final String CONFIG_KEY = "自定义水晶旋转";

   public CustomCrystalSpinHack() {
      super("自定义水晶旋转", "自定义旋转动画", Hack.Category.WORLD, true);
      this.addSetting(new Hack.Setting("垂直速度", "上下浮动的速度", 1.0, 0.0, 35.0, Hack.ValueDisplay.DECIMAL));
      this.addSetting(new Hack.Setting("垂直幅度", "上下浮动的幅度(格)", 0.2, 0.0, 30.0, Hack.ValueDisplay.DECIMAL));
      this.addSetting(new Hack.Setting("旋转速度", "每秒旋转角度", 30.0, 0.0, 1580.0, Hack.ValueDisplay.DECIMAL));
      this.addSetting(new Hack.Setting("同步世界时间", "动画与游戏时间同步(可避免闪烁)", false));
      this.loadConfig();
   }

   private void loadConfig() {
      verticalSpeed = (float)this.config.getDoubleSetting("自定义水晶旋转", "垂直速度", 1.0);
      verticalAmplitude = (float)this.config.getDoubleSetting("自定义水晶旋转", "垂直幅度", 0.2);
      rotationSpeed = (float)this.config.getDoubleSetting("自定义水晶旋转", "旋转速度", 30.0);
      syncWithWorldTime = this.config.getBooleanSetting("自定义水晶旋转", "同步世界时间", false);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         switch (setting.getName()) {
            case "垂直速度":
               setting.setValue((double)verticalSpeed);
               break;
            case "垂直幅度":
               setting.setValue((double)verticalAmplitude);
               break;
            case "旋转速度":
               setting.setValue((double)rotationSpeed);
               break;
            case "同步世界时间":
               setting.setValue(syncWithWorldTime);
         }
      }

   }

   public void onEnable() {
      featureEnabled = true;
   }

   public void onDisable() {
      featureEnabled = false;
   }

   public void onUpdate() {
      boolean needSave = false;
      Iterator var2 = this.getSettings().iterator();

      while(var2.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var2.next();
         switch (setting.getName()) {
            case "垂直速度":
               float v = (float)setting.getDouble();
               if ((double)Math.abs(v - verticalSpeed) > 0.001) {
                  verticalSpeed = v;
                  needSave = true;
               }
               break;
            case "垂直幅度":
               float a = (float)setting.getDouble();
               if ((double)Math.abs(a - verticalAmplitude) > 0.001) {
                  verticalAmplitude = a;
                  needSave = true;
               }
               break;
            case "旋转速度":
               float r = (float)setting.getDouble();
               if ((double)Math.abs(r - rotationSpeed) > 0.001) {
                  rotationSpeed = r;
                  needSave = true;
               }
               break;
            case "同步世界时间":
               boolean s = setting.getBoolean();
               if (s != syncWithWorldTime) {
                  syncWithWorldTime = s;
                  needSave = true;
               }
         }
      }

      if (needSave) {
         this.config.saveHackSettings("自定义水晶旋转", this.getSettings());
      }

   }

   public static boolean isFeatureEnabled() {
      return featureEnabled;
   }

   public static float getVerticalSpeed() {
      return verticalSpeed;
   }

   public static float getVerticalAmplitude() {
      return verticalAmplitude;
   }

   public static float getRotationSpeed() {
      return rotationSpeed;
   }

   public static boolean isSyncWithWorldTime() {
      return syncWithWorldTime;
   }

   public void onClick() {
      this.toggle();
   }
}
