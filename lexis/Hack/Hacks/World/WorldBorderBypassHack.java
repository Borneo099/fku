package lexis.Hack.Hacks.World;

import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;

public class WorldBorderBypassHack extends Hack {
   private HackConfig config = HackConfig.getInstance();
   private static final String CONFIG_KEY = "世界边界绕过";

   public WorldBorderBypassHack() {
      super("世界边界绕过", "允许穿过世界边界", Hack.Category.WORLD, true);
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
