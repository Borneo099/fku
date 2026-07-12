package lexis.Hack.Hacks.Render;

import lexis.Hack.Hack;

public class NoPumpkinHack extends Hack {
   public NoPumpkinHack() {
      super("无南瓜遮挡", "移除戴南瓜时的视野遮挡", Hack.Category.RENDER);
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
