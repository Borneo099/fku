package lexis.Hack.Hacks.Render;

import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;

public class NoHurtcamHack extends Hack {
   private HackConfig config = HackConfig.getInstance();
   private static final String CONFIG_KEY = "无受伤的晃动";

   public NoHurtcamHack() {
      super("无受伤的晃动", "禁止受伤时的视角晃动", Hack.Category.RENDER, true);
      this.loadConfig();
   }

   private void loadConfig() {
   }

   public void onEnable() {
   }

   public void onDisable() {
   }

   public void onUpdate() {
   }

   public void onClick() {
      this.toggle();
   }
}
