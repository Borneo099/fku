package lexis.Hack.events;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RightClickEvent {
   private static final List listeners = new ArrayList();
   private boolean cancelled = false;

   public static void register(RightClickListener listener) {
      listeners.add(listener);
   }

   public static void unregister(RightClickListener listener) {
      listeners.remove(listener);
   }

   public static void fire() {
      Iterator var0 = listeners.iterator();

      while(var0.hasNext()) {
         RightClickListener listener = (RightClickListener)var0.next();
         listener.onRightClick();
      }

   }

   public boolean isCancelled() {
      return this.cancelled;
   }

   public void cancel() {
      this.cancelled = true;
   }
}
