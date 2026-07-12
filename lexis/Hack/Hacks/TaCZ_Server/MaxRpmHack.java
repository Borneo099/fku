package lexis.Hack.Hacks.TaCZ_Server;

import lexis.Hack.Hack;
import lexis.Hack.events.EventManager;
import lexis.Hack.events.UpdateListener;

public class MaxRpmHack extends Hack implements UpdateListener {
   private static MaxRpmHack instance;
   public static boolean maxRpmActive = false;

   public MaxRpmHack() {
      super("满级射速", "消除武器射速冷却，所有武器超光速连发(仅房主/服务端自己有效，客户端不行！)", Hack.Category.TACZ_SERVER, true);
      instance = this;
   }

   public void onEnable() {
      maxRpmActive = true;
      EventManager.add(UpdateListener.class, this);
   }

   public void onDisable() {
      maxRpmActive = false;
      EventManager.remove(UpdateListener.class, this);
   }

   public void onUpdate() {
   }

   public void onClick() {
      this.toggle();
   }

   public static MaxRpmHack getInstance() {
      return instance;
   }
}
