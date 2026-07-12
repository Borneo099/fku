package lexis.Hack.events;

import java.util.ArrayList;
import java.util.Iterator;

public class UpdateEvent extends Event {
   public static final UpdateEvent INSTANCE = new UpdateEvent();

   private UpdateEvent() {
   }

   public void fire(ArrayList listeners) {
      Iterator var2 = listeners.iterator();

      while(var2.hasNext()) {
         UpdateListener listener = (UpdateListener)var2.next();
         listener.onUpdate();
      }

   }

   public Class getListenerType() {
      return UpdateListener.class;
   }
}
