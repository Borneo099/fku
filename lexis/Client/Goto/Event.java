package lexis.Client.Goto;

import java.util.List;

public abstract class Event {
   public abstract void fire(List listeners);

   public void fireSingle(Listener listener) {
      List single = List.of(listener);
      this.fire(single);
   }

   public abstract Class getListenerType();
}
