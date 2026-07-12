package lexis.Hack.Hacks.Render;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;

public class NoFireOverlayHack extends Hack {
   private HackConfig config;
   private static final String CONFIG_KEY = "去火焰贴图";
   private double offset = 0.6;
   private int precisionMode = 3;

   public NoFireOverlayHack() {
      super("去火焰贴图", "移除屏幕边缘的火焰效果", Hack.Category.RENDER, true);
      this.addSetting(new Hack.Setting("偏移量", "降低火焰覆盖的量", 0.6, 0.01, 0.6, Hack.ValueDisplay.DECIMAL));
      this.config = HackConfig.getInstance();
      this.loadConfig();
      this.detectPrecisionMode();
   }

   private void loadConfig() {
      this.offset = this.config.getDoubleSetting("去火焰贴图", "偏移量", 0.6);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         if (setting.getName().equals("偏移量")) {
            setting.setValue(this.offset);
            break;
         }
      }

   }

   private void detectPrecisionMode() {
      double min = 0.01;
      double max = 0.6;
      if (min >= 1.0) {
         this.precisionMode = 1;
      } else if (min >= 0.1) {
         this.precisionMode = 2;
      } else {
         this.precisionMode = 3;
      }

   }

   private String formatValue(double value) {
      switch (this.precisionMode) {
         case 1:
            return String.valueOf((int)value);
         case 2:
            return String.format("%.1f", value);
         case 3:
            return String.format("%.2f", value);
         default:
            return String.valueOf(value);
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
         if (setting.getName().equals("偏移量")) {
            double newOffset = setting.getDouble();
            if (newOffset != this.offset) {
               this.offset = newOffset;
               needSave = true;
            }
            break;
         }
      }

      if (needSave) {
         this.config.saveHackSettings("去火焰贴图", this.getSettings());
      }

   }

   public float getOverlayOffset() {
      return this.isEnabled() ? (float)this.offset : 0.0F;
   }

   public void onClick() {
      this.toggle();
   }
}
