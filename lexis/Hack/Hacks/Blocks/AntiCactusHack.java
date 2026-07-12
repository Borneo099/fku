package lexis.Hack.Hacks.Blocks;

import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;

public class AntiCactusHack extends Hack {
   private HackConfig config = HackConfig.getInstance();
   private static final String CONFIG_KEY = "反仙人掌";

   public AntiCactusHack() {
      super("反仙人掌", "防止仙人掌伤害", Hack.Category.BLOCKS, true);
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
