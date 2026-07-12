package lexis.Hack.events;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PacketCancelManager {
   private static final Map cancelCounts = new ConcurrentHashMap();

   public static void setCancelPacket(String className) {
      cancelCounts.put(className, 3);
   }

   public static boolean shouldCancel(String className) {
      Integer count = (Integer)cancelCounts.get(className);
      if (count != null && count > 0) {
         cancelCounts.put(className, count - 1);
         if (count - 1 == 0) {
            cancelCounts.remove(className);
         }

         return true;
      } else {
         return false;
      }
   }

   public static void clear() {
      cancelCounts.clear();
   }
}
