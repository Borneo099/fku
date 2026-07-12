package lexis.Hack.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class EventManager {
   private static final Map listeners = new HashMap();

   public static void add(Class type, Listener listener) {
      ArrayList list = (ArrayList)listeners.get(type);
      if (list == null) {
         list = new ArrayList();
         list.add(listener);
         listeners.put(type, list);
      } else {
         list.add(listener);
      }

   }

   public static void remove(Class type, Listener listener) {
      ArrayList list = (ArrayList)listeners.get(type);
      if (list != null) {
         list.remove(listener);
      }

   }

   public static void fire(Event event) {
      ArrayList list = (ArrayList)listeners.get(event.getListenerType());
      if (list != null && !list.isEmpty()) {
         ArrayList listeners2 = new ArrayList();
         Iterator var3 = list.iterator();

         while(var3.hasNext()) {
            Listener l = (Listener)var3.next();
            if (l != null) {
               listeners2.add(l);
            }
         }

         event.fire(listeners2);
      }

   }
}
