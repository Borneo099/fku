package lexis.Hack.events;

import java.util.ArrayList;
import java.util.Iterator;

public interface IsNormalCubeListener extends Listener {
   void onIsNormalCube(IsNormalCubeEvent var1);

   public static class IsNormalCubeEvent extends CancellableEvent {
      public void fire(ArrayList listeners) {
         Iterator var2 = listeners.iterator();

         while(var2.hasNext()) {
            IsNormalCubeListener listener = (IsNormalCubeListener)var2.next();
            listener.onIsNormalCube(this);
            if (this.isCancelled()) {
               break;
            }
         }

      }

      public Class getListenerType() {
         return IsNormalCubeListener.class;
      }
   }
}
