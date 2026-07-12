package lexis.Hack.Hacks.L_Enders_Cataclysm_C;

import lexis.Hack.Hack;

public class NoScreenShakeHack extends Hack {
   private static boolean enabledStatic = false;

   public NoScreenShakeHack() {
      super("无屏幕震动", new String[]{"禁用灾变模组的屏幕震动效果"}, Hack.Category.CATACLYSM, true);
   }

   public void onEnable() {
      enabledStatic = true;
   }

   public void onDisable() {
      enabledStatic = false;
   }

   public void onUpdate() {
   }

   public void onClick() {
      this.toggle();
   }

   public static boolean isActive() {
      return enabledStatic;
   }
}
