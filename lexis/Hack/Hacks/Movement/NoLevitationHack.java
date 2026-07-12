package lexis.Hack.Hacks.Movement;

import lexis.Hack.Hack;

public class NoLevitationHack extends Hack {
   public NoLevitationHack() {
      super("无漂浮", "阻止漂浮效果", Hack.Category.MOVEMENT, true);
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
