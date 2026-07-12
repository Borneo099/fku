package lexis.Hack.Hacks.TaCZ;

import lexis.Hack.Hack;
import lexis.Hack.events.EventManager;
import lexis.Hack.events.UpdateListener;

public class SniperFullAutoHack extends Hack implements UpdateListener {
   private static SniperFullAutoHack instance;
   public static boolean sniperFullAutoActive = false;

   public SniperFullAutoHack() {
      super("全狙击自动", "所有狙击枪允许使用长按左键连续射击！", Hack.Category.TACZ, true);
      instance = this;
   }

   public void onEnable() {
      sniperFullAutoActive = true;
      EventManager.add(UpdateListener.class, this);
   }

   public void onDisable() {
      sniperFullAutoActive = false;
      EventManager.remove(UpdateListener.class, this);
   }

   public void onUpdate() {
   }

   public void onClick() {
      this.toggle();
   }

   public static SniperFullAutoHack getInstance() {
      return instance;
   }
}
