package lexis.Hack.events;

import java.util.ArrayList;
import java.util.Iterator;

public class GameJoinedEvent extends Event {
   private static final GameJoinedEvent INSTANCE = new GameJoinedEvent();

   private GameJoinedEvent() {
   }

   public static GameJoinedEvent get() {
      return INSTANCE;
   }

   public void fire(ArrayList listeners) {
      Iterator var2 = listeners.iterator();

      while(var2.hasNext()) {
         GameJoinedListener listener = (GameJoinedListener)var2.next();
         listener.onGameJoined(this);
      }

   }

   public Class getListenerType() {
      return GameJoinedListener.class;
   }
}
