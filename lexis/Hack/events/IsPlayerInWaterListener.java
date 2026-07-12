package lexis.Hack.events;

import java.util.ArrayList;
import java.util.Iterator;

public interface IsPlayerInWaterListener extends Listener {
   void onIsPlayerInWater(IsPlayerInWaterEvent var1);

   public static class IsPlayerInWaterEvent extends Event {
      private boolean inWater;
      private final boolean normallyInWater;

      public IsPlayerInWaterEvent(boolean inWater) {
         this.inWater = inWater;
         this.normallyInWater = inWater;
      }

      public boolean isInWater() {
         return this.inWater;
      }

      public void setInWater(boolean inWater) {
         this.inWater = inWater;
      }

      public boolean isNormallyInWater() {
         return this.normallyInWater;
      }

      public void fire(ArrayList listeners) {
         Iterator var2 = listeners.iterator();

         while(var2.hasNext()) {
            IsPlayerInWaterListener listener = (IsPlayerInWaterListener)var2.next();
            listener.onIsPlayerInWater(this);
         }

      }

      public Class getListenerType() {
         return IsPlayerInWaterListener.class;
      }
   }
}
