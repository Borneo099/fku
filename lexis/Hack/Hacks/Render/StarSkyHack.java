package lexis.Hack.Hacks.Render;

import lexis.Hack.Hack;

public class StarSkyHack extends Hack {
   public StarSkyHack() {
      super("星空天空", "更好看的。 ", Hack.Category.RENDER, true);
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
