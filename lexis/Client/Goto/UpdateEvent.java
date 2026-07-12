package lexis.Client.Goto;

import java.util.Iterator;
import java.util.List;

public class UpdateEvent extends Event {
   public static final UpdateEvent INSTANCE = new UpdateEvent();

   public void fire(List listeners) {
      Iterator var2 = listeners.iterator();

      while(var2.hasNext()) {
         UpdateListener listener = (UpdateListener)var2.next();

         try {
            listener.onUpdate();
         } catch (Exception var5) {
            var5.printStackTrace();
         }
      }

   }

   public Class getListenerType() {
      return UpdateListener.class;
   }
}
