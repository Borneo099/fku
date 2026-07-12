package lexis.Client.Goto;

import java.util.Iterator;
import java.util.List;

public class PostMotionEvent extends Event {
   public static final PostMotionEvent INSTANCE = new PostMotionEvent();

   public void fire(List listeners) {
      Iterator var2 = listeners.iterator();

      while(var2.hasNext()) {
         PostMotionListener listener = (PostMotionListener)var2.next();

         try {
            listener.onPostMotion();
         } catch (Exception var5) {
            var5.printStackTrace();
         }
      }

   }

   public Class getListenerType() {
      return PostMotionListener.class;
   }
}
