package lexis.Hack.events;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class StopUsingItemEvent {
   private static final List listeners = new ArrayList();

   public static void register(StopUsingItemListener listener) {
      listeners.add(listener);
   }

   public static void unregister(StopUsingItemListener listener) {
      listeners.remove(listener);
   }

   public static void fire() {
      Iterator var0 = listeners.iterator();

      while(var0.hasNext()) {
         StopUsingItemListener listener = (StopUsingItemListener)var0.next();
         listener.onStopUsingItem();
      }

   }
}
