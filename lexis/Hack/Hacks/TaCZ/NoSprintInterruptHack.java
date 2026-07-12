package lexis.Hack.Hacks.TaCZ;

import lexis.Hack.Hack;
import lexis.Hack.events.EventManager;
import lexis.Hack.events.UpdateListener;

public class NoSprintInterruptHack extends Hack implements UpdateListener {
   private static NoSprintInterruptHack instance;
   public static boolean noSprintInterruptActive = false;

   public NoSprintInterruptHack() {
      super("疾跑不断", "开枪/换弹/开镜时不打断疾跑", Hack.Category.TACZ, true);
      instance = this;
   }

   public void onEnable() {
      noSprintInterruptActive = true;
      EventManager.add(UpdateListener.class, this);
   }

   public void onDisable() {
      noSprintInterruptActive = false;
      EventManager.remove(UpdateListener.class, this);
   }

   public void onUpdate() {
   }

   public void onClick() {
      this.toggle();
   }

   public static NoSprintInterruptHack getInstance() {
      return instance;
   }
}
