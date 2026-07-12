package lexis.Hack.Hacks.Movement;

import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;

public class GUIMoveHack extends Hack {
   private HackConfig config = HackConfig.getInstance();
   private static final String CONFIG_KEY = "GUI移动";

   public GUIMoveHack() {
      super("GUI移动", "允许在打开GUI时移动", Hack.Category.MOVEMENT, true);
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
