package lexis.Hack.events;

import java.util.ArrayList;

public abstract class Event {
   public abstract void fire(ArrayList listeners);

   public abstract Class getListenerType();
}
