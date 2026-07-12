package lexis.Hack.events;

import java.util.ArrayList;
import java.util.Iterator;

public interface IsPlayerInLavaListener extends Listener {
   void onIsPlayerInLava(IsPlayerInLavaEvent var1);

   public static class IsPlayerInLavaEvent extends Event {
      private boolean inLava;
      private final boolean normallyInLava;

      public IsPlayerInLavaEvent(boolean inLava) {
         this.inLava = inLava;
         this.normallyInLava = inLava;
      }

      public boolean isInLava() {
         return this.inLava;
      }

      public void setInLava(boolean inLava) {
         this.inLava = inLava;
      }

      public boolean isNormallyInLava() {
         return this.normallyInLava;
      }

      public void fire(ArrayList listeners) {
         Iterator var2 = listeners.iterator();

         while(var2.hasNext()) {
            IsPlayerInLavaListener listener = (IsPlayerInLavaListener)var2.next();
            listener.onIsPlayerInLava(this);
         }

      }

      public Class getListenerType() {
         return IsPlayerInLavaListener.class;
      }
   }
}
