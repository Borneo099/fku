package lexis.Hack.Hacks.Movement;

import lexis.Hack.Hack;

public class NoSneakSlowHack extends Hack {
   public NoSneakSlowHack() {
      super("蹲下不减速", "潜行时保持正常移动速度", Hack.Category.MOVEMENT, true);
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
