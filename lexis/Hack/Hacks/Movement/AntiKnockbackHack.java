package lexis.Hack.Hacks.Movement;

import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;

public class AntiKnockbackHack extends Hack {
   private HackConfig config = HackConfig.getInstance();
   private static final String CONFIG_KEY = "防击退";

   public AntiKnockbackHack() {
      super("防击退", "取消击退效果", Hack.Category.MOVEMENT, true);
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
