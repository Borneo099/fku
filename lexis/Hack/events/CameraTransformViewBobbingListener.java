package lexis.Hack.events;

import java.util.ArrayList;
import java.util.Iterator;

public interface CameraTransformViewBobbingListener extends Listener {
   void onCameraTransformViewBobbing(CameraTransformViewBobbingEvent var1);

   public static class CameraTransformViewBobbingEvent extends CancellableEvent {
      public void fire(ArrayList listeners) {
         Iterator var2 = listeners.iterator();

         while(var2.hasNext()) {
            CameraTransformViewBobbingListener listener = (CameraTransformViewBobbingListener)var2.next();
            listener.onCameraTransformViewBobbing(this);
            if (this.isCancelled()) {
               break;
            }
         }

      }

      public Class getListenerType() {
         return CameraTransformViewBobbingListener.class;
      }
   }
}
