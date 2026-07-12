package lexis.Hack.events;

import java.util.ArrayList;
import java.util.Iterator;

public interface LeftClickListener extends Listener {
   void onLeftClick(LeftClickEvent event);

   public static class LeftClickEvent {
      private boolean cancelled = false;
      private final ArrayList listeners = new ArrayList();

      public void addListener(LeftClickListener listener) {
         this.listeners.add(listener);
      }

      public void removeListener(LeftClickListener listener) {
         this.listeners.remove(listener);
      }

      public void fire() {
         Iterator var1 = this.listeners.iterator();

         while(var1.hasNext()) {
            LeftClickListener listener = (LeftClickListener)var1.next();
            listener.onLeftClick(this);
            if (this.cancelled) {
               break;
            }
         }

      }

      public boolean isCancelled() {
         return this.cancelled;
      }

      public void cancel() {
         this.cancelled = true;
      }
   }
}
