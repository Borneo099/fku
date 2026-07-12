package lexis.Hack.Hacks.Movement;

import lexis.Hack.Hack;

public class SnowShoeHack extends Hack {
   public SnowShoeHack() {
      super("雪上行走", "在细雪上行走，不会掉入", Hack.Category.MOVEMENT, true);
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
