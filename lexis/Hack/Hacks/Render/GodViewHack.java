package lexis.Hack.Hacks.Render;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import net.minecraft.client.CameraType;

public class GodViewHack extends Hack {
   private static boolean enabledStatic = false;
   private static double height = 100.0;
   private CameraType previousCameraType;
   private HackConfig config;
   private static final String CONFIG_KEY = "上帝视角";

   public GodViewHack() {
      super("上帝视角", "相机定在玩家正上方天空", Hack.Category.RENDER, true);
      this.previousCameraType = CameraType.FIRST_PERSON;
      this.addSetting(new Hack.Setting("相机高度", "相机离玩家头的高度(格)", 100.0, 10.0, 500.0, Hack.ValueDisplay.DECIMAL));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      height = this.config.getDoubleSetting("上帝视角", "相机高度", 100.0);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting s = (Hack.Setting)var1.next();
         if (s.getName().equals("相机高度")) {
            s.setValue(height);
            break;
         }
      }

   }

   public void onEnable() {
      enabledStatic = true;
      this.previousCameraType = mc.f_91066_.m_92176_();
      mc.f_91066_.m_92157_(CameraType.THIRD_PERSON_BACK);
      this.loadConfig();
   }

   public void onDisable() {
      enabledStatic = false;
      mc.f_91066_.m_92157_(this.previousCameraType);
   }

   public void onUpdate() {
      boolean needSave = false;
      Iterator var2 = this.getSettings().iterator();

      while(var2.hasNext()) {
         Hack.Setting s = (Hack.Setting)var2.next();
         if (s.getName().equals("相机高度")) {
            double newHeight = s.getDouble();
            if (newHeight != height) {
               height = newHeight;
               needSave = true;
            }
            break;
         }
      }

      if (needSave) {
         this.config.saveHackSettings("上帝视角", this.getSettings());
      }

      if (enabledStatic && mc.f_91066_.m_92176_() != CameraType.THIRD_PERSON_BACK) {
         mc.f_91066_.m_92157_(CameraType.THIRD_PERSON_BACK);
      }

   }

   public void onClick() {
      this.toggle();
   }

   public static boolean isEnabledStatic() {
      return enabledStatic;
   }

   public static double getHeight() {
      return height;
   }
}
