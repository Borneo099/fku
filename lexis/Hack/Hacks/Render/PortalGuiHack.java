package lexis.Hack.Hacks.Render;

import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;

public class PortalGuiHack extends Hack {
   private HackConfig config = HackConfig.getInstance();
   private static final String CONFIG_KEY = "传送门GUI";

   public PortalGuiHack() {
      super("传送门GUI", "允许在传送门内打开GUI", Hack.Category.RENDER, true);
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
