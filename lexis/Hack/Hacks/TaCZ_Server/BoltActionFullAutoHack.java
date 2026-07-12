package lexis.Hack.Hacks.TaCZ_Server;

import lexis.Hack.Hack;
import lexis.Hack.events.EventManager;
import lexis.Hack.events.UpdateListener;

public class BoltActionFullAutoHack extends Hack implements UpdateListener {
   private static BoltActionFullAutoHack instance;
   public static boolean boltActionFullAutoActive = false;

   public BoltActionFullAutoHack() {
      super("所有狙击+霰弹允许连发", "消除拉栓/泵动弹延迟，所有狙击枪霰弹枪连发\n(配合满级射速+无限子弹实现超光速射击)", Hack.Category.TACZ_SERVER, true);
      instance = this;
   }

   public void onEnable() {
      boltActionFullAutoActive = true;
      EventManager.add(UpdateListener.class, this);
   }

   public void onDisable() {
      boltActionFullAutoActive = false;
      EventManager.remove(UpdateListener.class, this);
   }

   public void onUpdate() {
   }

   public void onClick() {
      this.toggle();
   }

   public static BoltActionFullAutoHack getInstance() {
      return instance;
   }
}
