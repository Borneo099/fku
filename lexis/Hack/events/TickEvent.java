package lexis.Hack.events;

import java.util.ArrayList;
import java.util.Iterator;

public class TickEvent {
   public static class Post extends Event {
      private static final Post INSTANCE = new Post();

      public static Post get() {
         return INSTANCE;
      }

      public void fire(ArrayList listeners) {
         Iterator var2 = listeners.iterator();

         while(var2.hasNext()) {
            TickListener listener = (TickListener)var2.next();
            listener.onTick();
         }

      }

      public Class getListenerType() {
         return TickListener.class;
      }
   }

   public static class Pre extends Event {
      private static final Pre INSTANCE = new Pre();

      public static Pre get() {
         return INSTANCE;
      }

      public void fire(ArrayList listeners) {
         Iterator var2 = listeners.iterator();

         while(var2.hasNext()) {
            TickListener listener = (TickListener)var2.next();
            listener.onTick();
         }

      }

      public Class getListenerType() {
         return TickListener.class;
      }
   }
}
