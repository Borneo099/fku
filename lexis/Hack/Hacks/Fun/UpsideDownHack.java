package lexis.Hack.Hacks.Fun;

import lexis.Hack.Hack;

public class UpsideDownHack extends Hack {
   public UpsideDownHack() {
      super("倒立模型", new String[]{"自己的玩家模型倒立显示", "仅自己能看到，别人看不到"}, Hack.Category.FUN, true);
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
