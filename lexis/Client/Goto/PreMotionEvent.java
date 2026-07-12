package lexis.Client.Goto;

import java.util.Iterator;
import java.util.List;

public class PreMotionEvent extends Event {
   public static final PreMotionEvent INSTANCE = new PreMotionEvent();

   public void fire(List listeners) {
      Iterator var2 = listeners.iterator();

      while(var2.hasNext()) {
         PreMotionListener listener = (PreMotionListener)var2.next();

         try {
            listener.onPreMotion();
         } catch (Exception var5) {
            var5.printStackTrace();
         }
      }

   }

   public Class getListenerType() {
      return PreMotionListener.class;
   }
}
