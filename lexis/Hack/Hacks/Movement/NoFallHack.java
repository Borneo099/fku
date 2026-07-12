package lexis.Hack.Hacks.Movement;

import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;

public class NoFallHack extends Hack {
   private HackConfig config = HackConfig.getInstance();
   private static final String CONFIG_KEY = "无摔伤2";

   public NoFallHack() {
      super("无摔伤2", new String[]{"无摔伤2的是下落速度快不会受伤"}, Hack.Category.MOVEMENT, true);
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
