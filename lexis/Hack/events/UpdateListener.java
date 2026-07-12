package lexis.Hack.events;

import java.util.ArrayList;
import java.util.Iterator;

public interface UpdateListener extends Listener {
   void onUpdate();

   public static class UpdateEvent {
      public static final UpdateEvent INSTANCE = new UpdateEvent();
      private final ArrayList listeners = new ArrayList();

      private UpdateEvent() {
      }

      public void addListener(UpdateListener listener) {
         this.listeners.add(listener);
      }

      public void removeListener(UpdateListener listener) {
         this.listeners.remove(listener);
      }

      public void fire() {
         Iterator var1 = this.listeners.iterator();

         while(var1.hasNext()) {
            UpdateListener listener = (UpdateListener)var1.next();
            listener.onUpdate();
         }

      }
   }
}
