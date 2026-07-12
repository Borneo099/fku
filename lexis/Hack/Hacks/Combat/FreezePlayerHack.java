package lexis.Hack.Hacks.Combat;

import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;

public class FreezePlayerHack extends Hack {
   private static boolean isFrozen = false;
   private HackConfig config = HackConfig.getInstance();
   private static final String CONFIG_KEY = "冻结玩家";

   public FreezePlayerHack() {
      super("冻结玩家", new String[]{"给上冻结玩家，可以打玩家", "别的玩家离你6米 也看到会受伤被打了"}, Hack.Category.COMBAT, true);
   }

   public void onEnable() {
      isFrozen = true;
   }

   public void onDisable() {
      isFrozen = false;
   }

   public void onUpdate() {
   }

   public void onClick() {
      this.toggle();
   }

   public static boolean isFrozen() {
      return isFrozen;
   }
}
