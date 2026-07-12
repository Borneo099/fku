package lexis.Hack.Hacks.Render;

import lexis.Hack.Hack;

public class NoSprintFovHack extends Hack {
   public NoSprintFovHack() {
      super("禁止冲刺视野缩放", "冲刺时视野不会变小", Hack.Category.RENDER, true);
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
