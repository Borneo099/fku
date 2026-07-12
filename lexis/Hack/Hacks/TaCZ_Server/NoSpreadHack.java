package lexis.Hack.Hacks.TaCZ_Server;

import lexis.Hack.Hack;
import lexis.Hack.events.EventManager;
import lexis.Hack.events.UpdateListener;

public class NoSpreadHack extends Hack implements UpdateListener {
   private static NoSpreadHack instance;
   public static boolean noSpreadActive = false;

   public NoSpreadHack() {
      super("无散布", "消除所有武器子弹散布，弹道笔直（仅房主自己有效）", Hack.Category.TACZ_SERVER, true);
      instance = this;
   }

   public void onEnable() {
      noSpreadActive = true;
      EventManager.add(UpdateListener.class, this);
   }

   public void onDisable() {
      noSpreadActive = false;
      EventManager.remove(UpdateListener.class, this);
   }

   public void onUpdate() {
   }

   public void onClick() {
      this.toggle();
   }

   public static NoSpreadHack getInstance() {
      return instance;
   }
}
