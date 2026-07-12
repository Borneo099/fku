package lexis.Hack.events;

import java.util.ArrayList;
import java.util.Iterator;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;

public class PlayerMoveEvent extends Event {
   private static final PlayerMoveEvent INSTANCE = new PlayerMoveEvent();
   public MoverType moverType;
   public Vec3 movement;

   public static PlayerMoveEvent get(MoverType moverType, Vec3 movement) {
      INSTANCE.moverType = moverType;
      INSTANCE.movement = movement;
      return INSTANCE;
   }

   public void fire(ArrayList listeners) {
      Iterator var2 = listeners.iterator();

      while(var2.hasNext()) {
         PlayerMoveListener listener = (PlayerMoveListener)var2.next();
         listener.onPlayerMove(this);
      }

   }

   public Class getListenerType() {
      return PlayerMoveListener.class;
   }
}
