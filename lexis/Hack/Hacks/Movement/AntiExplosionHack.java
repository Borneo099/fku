package lexis.Hack.Hacks.Movement;

import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;

public class AntiExplosionHack extends Hack {
   private HackConfig config = HackConfig.getInstance();
   private static final String CONFIG_KEY = "防爆炸击退";

   public AntiExplosionHack() {
      super("防爆炸击退", "不会爆炸飞你！", Hack.Category.MOVEMENT, true);
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

   public boolean shouldCancelExplosion() {
      return this.isEnabled();
   }
}
