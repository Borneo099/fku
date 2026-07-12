package lexis.Hack.Hacks.Movement;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;

public class NoMomentumHack extends Hack {
   private HackConfig config;
   private static final String CONFIG_KEY = "无惯性";
   public static boolean isEnabled = false;
   public static double deceleration = 1.0;
   public static boolean onlyOnGround = false;
   public static boolean onlyWhenNoInput = true;

   public NoMomentumHack() {
      super("无惯性", "取消移动惯性，松开按键立即停止", Hack.Category.MOVEMENT, true);
      this.addSetting(new Hack.Setting("仅地面", "仅在地面时生效", false));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      onlyOnGround = this.config.getBooleanSetting("无惯性", "仅地面", false);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         switch (setting.getName()) {
            case "仅地面":
               setting.setValue(onlyOnGround);
         }
      }

   }

   public void onEnable() {
      isEnabled = true;
   }

   public void onDisable() {
      isEnabled = false;
   }

   public void onUpdate() {
      boolean needSave = false;
      Iterator var2 = this.getSettings().iterator();

      while(var2.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var2.next();
         switch (setting.getName()) {
            case "仅地面":
               boolean newGround = setting.getBoolean();
               if (newGround != onlyOnGround) {
                  onlyOnGround = newGround;
                  needSave = true;
               }
         }
      }

      if (needSave) {
         this.config.saveHackSettings("无惯性", this.getSettings());
      }

   }

   public void onClick() {
      this.toggle();
   }
}
