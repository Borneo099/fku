package lexis.Hack.events;

import java.util.ArrayList;
import java.util.Iterator;

public interface SetOpaqueCubeListener extends Listener {
   void onSetOpaqueCube(SetOpaqueCubeEvent var1);

   public static class SetOpaqueCubeEvent extends CancellableEvent {
      public void fire(ArrayList listeners) {
         Iterator var2 = listeners.iterator();

         while(var2.hasNext()) {
            SetOpaqueCubeListener listener = (SetOpaqueCubeListener)var2.next();
            listener.onSetOpaqueCube(this);
            if (this.isCancelled()) {
               break;
            }
         }

      }

      public Class getListenerType() {
         return SetOpaqueCubeListener.class;
      }
   }
}
