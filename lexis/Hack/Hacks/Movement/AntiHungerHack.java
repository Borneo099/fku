package lexis.Hack.Hacks.Movement;

import lexis.Hack.Hack;

public class AntiHungerHack extends Hack {
   public static boolean enabled = false;

   public AntiHungerHack() {
      super("减少饥饿", new String[]{"减少掉饥饿度"}, Hack.Category.MOVEMENT, true);
   }

   public void onEnable() {
      enabled = true;
   }

   public void onDisable() {
      enabled = false;
   }

   public void onUpdate() {
   }

   public void onClick() {
      this.toggle();
   }
}
