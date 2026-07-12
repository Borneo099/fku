package lexis.Client.Goto;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventBus {
   private static final Map listeners = new ConcurrentHashMap();

   public static void add(Class type, Listener listener) {
      ((List)listeners.computeIfAbsent(type, (k) -> {
         return new CopyOnWriteArrayList();
      })).add(listener);
   }

   public static void remove(Class type, Listener listener) {
      List list = (List)listeners.get(type);
      if (list != null) {
         list.remove(listener);
      }

   }

   public static void fire(Event event) {
      List list = (List)listeners.get(event.getListenerType());
      if (list != null) {
         List snapshot = new ArrayList(list);
         Iterator var3 = snapshot.iterator();

         while(var3.hasNext()) {
            Listener listener = (Listener)var3.next();

            try {
               event.fireSingle(listener);
            } catch (Exception var6) {
               var6.printStackTrace();
            }
         }
      }

   }

   public static void clear() {
      listeners.clear();
   }
}
