package lexis.Hack.Hacks.Movement;

import lexis.Hack.Hack;

public class NoSlowdownHack extends Hack {
   public NoSlowdownHack() {
      super("无减速", "无视物品使用、灵魂沙+蜂蜜块 减速效果", Hack.Category.MOVEMENT, true);
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
