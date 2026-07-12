package lexis.Hack.Hacks.TaCZ_Server;

import lexis.Hack.Hack;
import lexis.Hack.events.EventManager;
import lexis.Hack.events.UpdateListener;

public class InfiniteAmmoHack extends Hack implements UpdateListener {
   private static InfiniteAmmoHack instance;
   public static boolean infiniteAmmoActive = false;

   public InfiniteAmmoHack() {
      super("无限子弹", "开枪不消耗弹药（仅房主自己有效）", Hack.Category.TACZ_SERVER, true);
      instance = this;
   }

   public void onEnable() {
      infiniteAmmoActive = true;
      EventManager.add(UpdateListener.class, this);
   }

   public void onDisable() {
      infiniteAmmoActive = false;
      EventManager.remove(UpdateListener.class, this);
   }

   public void onUpdate() {
   }

   public void onClick() {
      this.toggle();
   }

   public static InfiniteAmmoHack getInstance() {
      return instance;
   }
}
