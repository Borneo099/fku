package lexis.Hack.Hacks.TaCZ;

import lexis.Hack.Hack;
import lexis.Hack.events.EventManager;
import lexis.Hack.events.UpdateListener;

public class InstantAimHack extends Hack implements UpdateListener {
   private static InstantAimHack instance;
   public static boolean instantAimActive = false;

   public InstantAimHack() {
      super("瞬镜", "跳过 TaCZ 开镜动画，瞬间完成瞄准", Hack.Category.TACZ, true);
      instance = this;
   }

   public void onEnable() {
      instantAimActive = true;
      EventManager.add(UpdateListener.class, this);
   }

   public void onDisable() {
      instantAimActive = false;
      EventManager.remove(UpdateListener.class, this);
   }

   public void onUpdate() {
   }

   public void onClick() {
      this.toggle();
   }

   public static InstantAimHack getInstance() {
      return instance;
   }
}
