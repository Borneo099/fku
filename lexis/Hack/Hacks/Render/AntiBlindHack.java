package lexis.Hack.Hacks.Render;

import lexis.Hack.Hack;

public class AntiBlindHack extends Hack {
   public AntiBlindHack() {
      super("防盲", "防止失明和黑暗效果", Hack.Category.RENDER, true);
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
