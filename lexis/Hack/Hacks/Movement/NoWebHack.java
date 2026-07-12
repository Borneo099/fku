package lexis.Hack.Hacks.Movement;

import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;

public class NoWebHack extends Hack {
   private HackConfig config = HackConfig.getInstance();

   public NoWebHack() {
      super("无视蜘蛛网", "无视蜘蛛网减速效果", Hack.Category.MOVEMENT, true);
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
