package lexis.Hack.Hacks.Render;

import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;

public class FullBrightHack extends Hack {
   public static boolean shouldReturnNightVisionEffect = false;
   private HackConfig config = HackConfig.getInstance();
   private static final String CONFIG_KEY = "全屏亮度";

   public FullBrightHack() {
      super("全屏亮度", "模拟夜视效果", Hack.Category.RENDER, true);
      this.loadConfig();
   }

   private void loadConfig() {
   }

   public void onEnable() {
      shouldReturnNightVisionEffect = true;
   }

   public void onDisable() {
      shouldReturnNightVisionEffect = false;
   }

   public void onUpdate() {
   }

   public void onClick() {
      this.toggle();
   }
}
