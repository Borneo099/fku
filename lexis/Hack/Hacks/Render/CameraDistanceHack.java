package lexis.Hack.Hacks.Render;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;

public class CameraDistanceHack extends Hack {
   private HackConfig config;
   private static final String CONFIG_KEY = "相机距离";
   private float targetDistance = 12.0F;
   private float animationSpeed = 0.5F;
   private boolean scrollControl = false;

   public CameraDistanceHack() {
      super("相机距离", "调整第三人称相机距离", Hack.Category.RENDER, true);
      this.addSetting(new Hack.Setting("目标距离", "相机距离", 12.0, -2.0, 512.0, Hack.ValueDisplay.DECIMAL));
      this.addSetting(new Hack.Setting("动画速度", "平滑动画速度", 0.5, 0.1, 2.0, Hack.ValueDisplay.DECIMAL));
      this.addSetting(new Hack.Setting("操作滚动", "按住 Alt + 鼠标滚轮调整距离", false));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      this.targetDistance = (float)this.config.getDoubleSetting("相机距离", "目标距离", 12.0);
      this.animationSpeed = (float)this.config.getDoubleSetting("相机距离", "动画速度", 0.5);
      this.scrollControl = this.config.getBooleanSetting("相机距离", "操作滚动", false);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         switch (setting.getName()) {
            case "目标距离":
               setting.setValue((double)this.targetDistance);
               break;
            case "动画速度":
               setting.setValue((double)this.animationSpeed);
               break;
            case "操作滚动":
               setting.setValue(this.scrollControl);
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
            case "目标距离":
               float newTarget = (float)setting.getDouble();
               if (newTarget != this.targetDistance) {
                  this.targetDistance = newTarget;
                  needSave = true;
               }
               break;
            case "动画速度":
               float newSpeed = (float)setting.getDouble();
               if (newSpeed != this.animationSpeed) {
                  this.animationSpeed = newSpeed;
                  needSave = true;
               }
               break;
            case "操作滚动":
               boolean newScroll = setting.getBoolean();
               if (newScroll != this.scrollControl) {
                  this.scrollControl = newScroll;
                  needSave = true;
               }
         }
      }

      if (needSave) {
         this.config.saveHackSettings("相机距离", this.getSettings());
      }

   }

   public float getTargetDistance() {
      return this.targetDistance;
   }

   public float getAnimationSpeed() {
      return this.animationSpeed;
   }

   public boolean isScrollControlEnabled() {
      return this.scrollControl;
   }

   public void addDistance(float delta) {
      this.targetDistance = Math.max(-2.0F, Math.min(512.0F, this.targetDistance + delta));
      Iterator var2 = this.getSettings().iterator();

      while(var2.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var2.next();
         if (setting.getName().equals("目标距离")) {
            setting.setValue((double)this.targetDistance);
            break;
         }
      }

   }

   public void onClick() {
      this.toggle();
   }
}
